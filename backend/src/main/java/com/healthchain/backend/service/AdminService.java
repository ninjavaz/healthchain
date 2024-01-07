package com.healthchain.backend.service;

import com.healthchain.backend.model.Mapper;
import com.healthchain.backend.model.dto.ResourceDTO;
import com.healthchain.backend.model.network.*;
import com.healthchain.backend.model.entity.resource.Resource;
import com.healthchain.backend.model.util.NetworkProperties;
import com.healthchain.backend.model.util.NetworkProperties.HospInfo;
import lombok.extern.log4j.Log4j;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Properties;

@Service
@Log4j
public class AdminService {
    @Autowired
    private NetworkProperties networkProps;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private Mapper mapper;

    /**
     * Method used only while starting an app to create current admin Identities and save them in the wallet.
     *
     * @param hospName
     */
    public void enrollAdmin(String hospName) {
        // Create a CA client for interacting with the CA.
        HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);
        Properties props = new Properties();
        props.put("pemFile", hospInfo.getCertPath());
        props.put("allowAllHostNames", "true");
        try {
            HFCAClient caClient = HFCAClient.createNewInstance(hospInfo.getCaUrl(), props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);

            // Create a wallet for managing identities
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

            // Check to see if we've already enrolled the admin user.
            if (wallet.get(hospInfo.getAdminUsername()) != null) {
                log.warn("An identity for the admin user " + hospInfo.getAdminUsername() + " already exists in the wallet");
                return;
            }

            // Enroll the admin user, and import the new identity into the wallet.
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");

            X509Enrollment enrollment = (X509Enrollment) caClient.enroll(hospInfo.getAdminUsername(),
                    hospInfo.getAdminPassword(), enrollmentRequestTLS);
            Identity user = Identities.newX509Identity(hospInfo.getMspName(), enrollment);
            wallet.put(hospInfo.getAdminUsername(), user);
            log.info("Successfully enrolled user " + hospInfo.getAdminUsername() + " and imported it into the wallet");

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    /**
     * Method that registers new user and returning their Identities to download and store for next auth processes
     *
     * @param role     - role of the user
     * @param adminId  - adminId needed to register and enroll the user
     * @param resource - resource
     * @return - customIdentity of created user
     */
    public CustomIdentity enrollUser(Role role, CustomIdentity adminId, ResourceDTO resourceDTO) throws Exception {
        Resource resource = null;

        switch(role) {
            case PATIENT:
                resource = this.mapper.mapToPatientResource(resourceDTO);
                break;
            case PRACTITIONER:
                resource = this.mapper.mapToPractitionerResource(resourceDTO);
                break;
        }


        String hospName = resource.getManagingOrganization().getIdentifier();
        HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);
        // Create a CA client for interacting with the CA.
        Properties props = new Properties();
        props.put("pemFile", hospInfo.getCertPath());
        props.put("allowAllHostNames", "true");

        HFCAClient caClient = HFCAClient.createNewInstance(hospInfo.getCaUrl(), props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        // Create a wallet for managing identities
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        // Check to see if we've already enrolled the user.
        if (wallet.get(resource.getId()) != null) {
            throw new RuntimeException("An identity for the user: '" + resource.getId() + "' already exists in the wallet");
        }

        X509Identity adminIdentity = (X509Identity) wallet.get(hospInfo.getAdminUsername());
        if (!isIdentitySameAsCustomIdentity(adminIdentity, adminId)) {
            throw new RuntimeException(hospInfo.getAdminUsername() + " identity in wallet does not match given identity");
        }

        // Register the user, enroll the user, and import the new identity into the wallet
        RegistrationRequest registrationRequest = new RegistrationRequest(resource.getId());
        registrationRequest.setEnrollmentID(resource.getId());
        registrationRequest.addAttribute(new Attribute("role", role.getValue(), true));

        // Register and enroll the new user
        String enrollmentSecret = caClient.register(registrationRequest, buildAdminUser(hospName, adminIdentity));
        X509Enrollment enrollment = (X509Enrollment) caClient.enroll(resource.getId(), enrollmentSecret);


        CustomIdentity customIdentity = CustomIdentity.builder()
                .mspId(adminId.getMspId())
                .credentials(
                        Credentials.builder()
                                .certificate(Identities.toPemString(adminIdentity.getCertificate()))
                                .privateKey(Identities.toPemString(adminIdentity.getPrivateKey())).build())
                .build();

        this.createResource(customIdentity, resource);
        log.info("Successfully created resource for user on blockchain: " + resource.getId());

        X509Identity user = Identities.newX509Identity(hospInfo.getMspName(), enrollment);
        wallet.put(resource.getId(), user);
        log.info("Successfully enrolled user: " + resource.getId() + " and imported it into the wallet");

        return CustomIdentity.builder().hospName(hospName).username(resource.getId()).role(role).mspId(user.getMspId()).version(1).type("X.509").credentials(
                        Credentials.builder()
                                .certificate(Identities.toPemString(user.getCertificate()))
                                .privateKey(Identities.toPemString(user.getPrivateKey())).build())
                .build();
    }

    private void createResource(CustomIdentity adminIdentity, Resource resource) throws Exception {
        String hospName = resource.getManagingOrganization().getIdentifier();
        this.transactionService.submitTransaction(TransactionType.createResource, Resource.class, hospName, adminIdentity, resource);
//        HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);
//
//        Gateway.Builder builder = Gateway.createBuilder();
//        builder.identity(adminId)
//                .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
//                .discovery(true);
//
//        // create a gateway connection
//        Gateway gateway = builder.connect();
//        // get the network and contract
//        Network network = gateway.getNetwork(networkProps.getChannel());
//        Contract contract = network.getContract(networkProps.getContract());
//
//        // Gson instance
//        Gson gson = new Gson();
//
//        // Convert object to JSON string
//        String resourceJson = gson.toJson(resource);
//        contract.submitTransaction("createResource", resourceJson);
//        return new String(contract.submitTransaction("createResource", resourceJson));
    }

    /**
     * Compare identity and customIdentity in order to check wether data given by user are correct and related to already stored Identity.
     *
     * @param identity
     * @param customIdentity
     * @return
     */
    private boolean isIdentitySameAsCustomIdentity(X509Identity identity, CustomIdentity customIdentity) {
        return Identities.toPemString(identity.getCertificate()).equals(customIdentity.getCredentials().getCertificate())
                && Identities.toPemString(identity.getPrivateKey()).equals(customIdentity.getCredentials().getPrivateKey());
    }

    /**
     * @param hospName
     * @param adminId
     * @return
     */
    private CustomUser buildAdminUser(String hospName, X509Identity adminId) {
        HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);
        return CustomUser.builder()
                .name(hospInfo.getAdminUsername())
                .enrollment(CustomEnrollment.builder()
                        .cert(Identities.toPemString(adminId.getCertificate()))
                        .key(adminId.getPrivateKey()).build())
                .build();
    }
}
