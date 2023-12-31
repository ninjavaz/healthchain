package com.healthchain.backend.service;

import com.google.gson.Gson;
import com.healthchain.backend.model.network.*;
import com.healthchain.backend.model.resource.PatientResource;
import com.healthchain.backend.model.resource.PractitionerResource;
import com.healthchain.backend.model.resource.Resource;
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
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Properties;

@Service
@Log4j
public class AuthService {

    @Autowired
    private NetworkProperties networkProps;

    /**
     * Method that registers new user and returning their Identities to download and store for next auth processes
     * @param role - role of the user
     * @param adminId - adminId needed to register and enroll the user
     * @param resource - resource
     * @return - customIdentity of created user
     */
    public CustomIdentity enrollUser(Role role, CustomIdentity adminId, Resource resource) throws Exception {

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
        // Set attributes for the user
        registrationRequest.setEnrollmentID(resource.getId());
        registrationRequest.addAttribute(new Attribute("role", role.getValue(), true));

        // Register and enroll the new user
        String enrollmentSecret = caClient.register(registrationRequest, buildAdminUser(hospName, adminIdentity));
        X509Enrollment enrollment = (X509Enrollment) caClient.enroll(resource.getId(), enrollmentSecret);

        X509Identity user = Identities.newX509Identity(hospInfo.getMspName(), enrollment);
        wallet.put(resource.getId(), user);
        log.info("Successfully enrolled user: " + resource.getId() + " and imported it into the wallet");

        String abc = this.createResource(adminIdentity, resource);

//        wallet.put(resource.getId(), (Identity) customIdentity);
        return CustomIdentity.builder().hospName(hospName).username(resource.getId() + " " + abc).role(role).mspId(user.getMspId()).version(1).type("X.509").credentials(
                        Credentials.builder()
                                .certificate(Identities.toPemString(user.getCertificate()))
                                .privateKey(Identities.toPemString(user.getPrivateKey())).build())
                .build();

    }

    public Resource getResource(CustomIdentity customId, String hospName) throws Exception {


//        String hospName = ho;
        HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);
        byte[] result;
        System.out.println(hospInfo);
        System.out.println(hospName);


        Identity identity = Identities.newX509Identity(customId.getMspId(), Identities.readX509Certificate(customId.getCredentials().getCertificate()), Identities.readPrivateKey(customId.getCredentials().getPrivateKey()));
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(identity)
                .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
                .discovery(true);

        Gateway gateway = builder.connect();
        // get the network and contract
        Network network = gateway.getNetwork(networkProps.getChannel());
        Contract contract = network.getContract(networkProps.getContract());

        // Gson instance
        Gson gson = new Gson();

        return gson.fromJson(new String(contract.evaluateTransaction("getResourceForUser")), Resource.class);

//        switch(customId.getRole()) {
//            case PATIENT:
//                return gson.fromJson(new String(contract.evaluateTransaction("getResourceForUser")), PatientResource.class);
////                return new String(contract.evaluateTransaction("getPatientResourceById"));
//            case PRACTITIONER:
//                return gson.fromJson(new String(contract.evaluateTransaction("getPractitionerResourceForUser")), PractitionerResource.class);
//            default:
//                return null;
//        }
    }

    /**
     * Method used only while starting an app to create current admin Identities and save them in the wallet.
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

//            EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
//            enrollmentRequest.addHost("localhost");
//            enrollmentRequest.setProfile("tls");
//            enrollmentRequest.addAttrReq("role");

//            Enrollment existingAdminEnrollment = caClient.enroll(hospInfo.getExistingAdminUsername(), hospInfo.getPassword());
//            String certPath = "/path/to/enrollmentCert.pem"; // Path to enrollment certificate
//            String keyPath = "/path/to/privateKey.pem"; // Path to private key
//
//            String certificate = new String(Files.readAllBytes(Paths.get(certPath)), StandardCharsets.UTF_8);
//            PrivateKey privateKey = Identities.readPrivateKey((Reader) Paths.get(keyPath));
//
//            Enrollment existingAdminEnrollment = new X509Enrollment(privateKey, certificate);
//
//            X509Identity existingAdminIdentity = Identities.newX509Identity(hospInfo.getMspName(), existingAdminEnrollment);

            // Create a registration request for a new admin
//            RegistrationRequest regRequest = new RegistrationRequest(hospInfo.getUsername());
//            regRequest.setSecret( hospInfo.getPassword());
//            regRequest.setType("admin");
//            regRequest.addAttribute(new Attribute("role", "ADMIN", true)); // Include role in the certificate
//
//            // Register and enroll the new admin
//            String enrollmentSecret = caClient.register(regRequest, buildAdminUser(hospName, existingAdminIdentity)); // Use the enrolled admin's identity for registration

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

    private String createResource(Identity adminId, Resource resource) throws Exception {
        String hospName = resource.getManagingOrganization().getIdentifier();
        HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);
        byte[] result;
//        try {
//            Identity identity = Identities.newX509Identity(adminId.getMspId(), Identities.readX509Certificate(adminId.getCredentials().getCertificate()), Identities.readPrivateKey(adminId.getCredentials().getPrivateKey()));
            Gateway.Builder builder = Gateway.createBuilder();
            builder.identity(adminId)
                    .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
                    .discovery(true);

            // create a gateway connection
//            try {
            Gateway gateway = builder.connect();
            // get the network and contract
            Network network = gateway.getNetwork(networkProps.getChannel());
            Contract contract = network.getContract(networkProps.getContract());

            // Gson instance
            Gson gson = new Gson();

            // Convert object to JSON string
            String resourceJson = gson.toJson(resource);

            switch(resource.getResourceType()) {
                case "Patient":
                    return new String(contract.submitTransaction("createPatientResource", resourceJson));
                case "Practitioner":
                    return new String(contract.submitTransaction("createPractitionerResource", resourceJson));
                default:
                    return null;
            }
//            result = contract.evaluateTransaction("createPatientResource", resourceJson);
//
//            return new String(result);

//                return new String(result);
//            } catch (Exception e) {
//                e.printStackTrace();
//                ErrorMessage error = ErrorMessage.builder().code(HttpStatus.FORBIDDEN.value()).timestamp(new Date()).name("The connection to the blockchain network failed due to insufficient permissions or some errors")
//                        .message(e.getMessage()).build();
//                return ResponseEntity.status(error.getCode()).body(error);
//            }
//        } catch (Exception e) {
//            log.debug("TUTAJJJJ");
//            e.printStackTrace();
//
//        }
//        return null;
    }




    /**
     * Compare identity and customIdentity in order to check wether data given by user are correct and related to already stored Identity.
     * @param identity
     * @param customIdentity
     * @return
     */
    private boolean isIdentitySameAsCustomIdentity(X509Identity identity, CustomIdentity customIdentity) {
        return Identities.toPemString(identity.getCertificate()).equals(customIdentity.getCredentials().getCertificate())
                && Identities.toPemString(identity.getPrivateKey()).equals(customIdentity.getCredentials().getPrivateKey());
    }

    /**
     *
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
