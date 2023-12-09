package com.healthchain.backend.service;

import com.healthchain.backend.model.util.NetworkProperties;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Properties;

@Service
public class AuthService {

    @Autowired
    private NetworkProperties networkProps;

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
                System.out.println("An identity for the admin user " + networkProps.getHospInfoByName().get(hospName).getUsername() + " already exists in the wallet");
                return;
            }

            // Enroll the admin user, and import the new identity into the wallet.
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");
            Enrollment enrollment = caClient.enroll(networkProps.getHospInfoByName().get(hospName).getUsername(),
                    networkProps.getHospInfoByName().get(hospName).getPassword(), enrollmentRequestTLS);
            Identity user = Identities.newX509Identity(networkProps.getHospInfoByName().get(hospName).getMspName(), enrollment);
            wallet.put(networkProps.getHospInfoByName().get(hospName).getUsername(), user);
            System.out.println("Successfully enrolled user " + networkProps.getHospInfoByName().get(hospName).getUsername() + " and imported it into the wallet");
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
