package com.healthchain.backend.service;

import com.healthchain.backend.model.Credentials;
import com.healthchain.backend.model.CustomIdentity;
import com.healthchain.backend.model.CustomUser;
import com.healthchain.backend.model.CustomEnrollment;
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
public class AuthService {

    @Autowired
    private NetworkProperties networkProps;

    /**
     * Method that registers new user and returning their Identities to download and store for next auth processes
     * @param username
     * @param hospName
     * @param adminId
     * @return
     * @throws Exception
     */
    public CustomIdentity registerUser(String username, String hospName, CustomIdentity adminId) throws Exception {
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
        if (wallet.get(username) != null) {
            throw new RuntimeException("An identity for the user: '" + username + "' already exists in the wallet");
        }

        X509Identity adminIdentity = (X509Identity) wallet.get(hospInfo.getUsername());

        if (!isIdentitySameAsCustomIdentity(adminIdentity, adminId)) {
//                System.out.println(hospInfo.getUsername() + " needs to be the same as value passed in request body");
            throw new RuntimeException(hospInfo.getUsername() + " identity in wallet does not match to given identity");
        }

        // Register the user, enroll the user, and import the new identity into the wallet
        RegistrationRequest registrationRequest = new RegistrationRequest(username);
        // Set attributes for the user
        registrationRequest.setEnrollmentID(username);
//        registrationRequest.setAffiliation(hospName);
        registrationRequest.addAttribute(new Attribute("role", "patient", true));

        // Register and enroll the new user
        String enrollmentSecret = caClient.register(registrationRequest, buildAdminUser(hospName, adminIdentity));
        X509Enrollment enrollment = (X509Enrollment) caClient.enroll(username, enrollmentSecret);

        X509Identity user = Identities.newX509Identity(hospInfo.getMspName(), enrollment);
        wallet.put(username, user);
        log.info("Successfully enrolled user" + username +" and imported it into the wallet");
        return CustomIdentity.builder().mspId(user.getMspId()).version(1).type("X.509").credentials(
                Credentials.builder()
                        .certificate(Identities.toPemString(user.getCertificate()))
                        .privateKey(Identities.toPemString(user.getPrivateKey())).build())
                .build();
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
                .name(hospInfo.getUsername())
                .enrollment(CustomEnrollment.builder()
                        .cert(Identities.toPemString(adminId.getCertificate()))
                        .key(adminId.getPrivateKey()).build())
                .build();
    }

    /**
     * Method used only while starting an app to create current admin Identities and save them in the wallet.
     * @param hospName
     */
    public void enrollAdmin(String hospName) {

        // Create a CA client for interacting with the CA.
        Properties props = new Properties();
        props.put("pemFile", networkProps.getHospInfoByName().get(hospName).getCertPath());
        props.put("allowAllHostNames", "true");
        try {
            HFCAClient caClient = HFCAClient.createNewInstance(networkProps.getHospInfoByName().get(hospName).getCaUrl(), props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);

            // Create a wallet for managing identities
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

            // Check to see if we've already enrolled the admin user.
            if (wallet.get(networkProps.getHospInfoByName().get(hospName).getUsername()) != null) {
                log.warn("An identity for the admin user " + networkProps.getHospInfoByName().get(hospName).getUsername() + " already exists in the wallet");
                return;
            }

            // Enroll the admin user, and import the new identity into the wallet.
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");
            X509Enrollment enrollment = (X509Enrollment) caClient.enroll(networkProps.getHospInfoByName().get(hospName).getUsername(),
                    networkProps.getHospInfoByName().get(hospName).getPassword(), enrollmentRequestTLS);
            Identity user = Identities.newX509Identity(networkProps.getHospInfoByName().get(hospName).getMspName(), enrollment);
            wallet.put(networkProps.getHospInfoByName().get(hospName).getUsername(), user);
            log.info("Successfully enrolled user " + networkProps.getHospInfoByName().get(hospName).getUsername() + " and imported it into the wallet");
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
