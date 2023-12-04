package com.healthchain.backend.controller;

//import com.healthchain.backend.model.Identity;
import com.healthchain.backend.model.CustomIdentity;
import com.healthchain.backend.model.util.ErrorMessage;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAAffiliation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.hyperledger.fabric.gateway.Identity;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Timestamp;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/healthchain/api/auth/")
public class AuthController {

    /**
     * Login method t..
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody CustomIdentity id) throws Exception{
        byte[] result;

        // Load a file system based wallet for managing identities.
//        Path walletPath = Paths.get("wallet");
//        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("/vagrant/backend/connection-hosp1.yaml");
//        Path networkConfigPath = Paths.get("/vagrant", "network", "organizations", "peerOrganizations", "hosp1.healthchain.com", "connection-hosp1.yaml");

//        Path walletPath = Paths.get("wallet");
//        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP

//        System.out.println(id);

        // Create an X509Identity
        try {
            Identity identity = Identities.newX509Identity(id.getMspId(), Identities.readX509Certificate(id.getCredentials().getCertificate()), Identities.readPrivateKey(id.getCredentials().getPrivateKey()));
            Gateway.Builder builder = Gateway.createBuilder();
            builder.identity(identity)
                    .networkConfig(networkConfigPath)
                    .discovery(true);

            // create a gateway connection
            try {
                Gateway gateway = builder.connect();
                // get the network and contract
                Network network = gateway.getNetwork("hospitalchannel");
                Contract contract = network.getContract("healthchainCC");

                result = contract.evaluateTransaction("getUserRoles");

                //get user roles from attribute...



                // Access the user's identity
//                User user = gateway.ge
//                if (user != null) {
//                    // Access the attributes of the user's identity
//                    String[] attributeNames = user.getAttributeNames();
//                    for (String attributeName : attributeNames) {
//                        String attributeValue = user.getAttributeValue(attributeName);
//                        System.out.println("Attribute: " + attributeName + ", Value: " + attributeValue);
//
//                        // Perform checks based on attributes
//                        if ("Specialization".equals(attributeName) && "Cardiologist".equals(attributeValue)) {
//                            // Perform action for a Cardiologist
//                        }
//                    }
//                }

//                result = contract.evaluateTransaction("queryAllCars");
//            System.out.println(new String(result));

//            contract.submitTransaction("createCar", "CAR10", "VW", "Polo", "Grey", "Mary");
//
//            result = contract.evaluateTransaction("queryCar", "CAR10");
//            System.out.println(new String(result));
//
//            contract.submitTransaction("changeCarOwner", "CAR10", "Archie");
//
//            result = contract.evaluateTransaction("queryCar", "CAR10");
//            System.out.println(new String(result));

                return ResponseEntity.status(HttpStatus.OK).body(new String(result));
            } catch (Exception e) {
                e.printStackTrace();
                ErrorMessage error = ErrorMessage.builder().code(HttpStatus.FORBIDDEN.value()).timestamp(new Date()).name("The connection to the blockchain network failed due to insufficient permissions or some errors")
                        .message(e.getMessage()).build();
                return ResponseEntity.status(error.getCode()).body(error);
            }
        } catch (Exception e) {
            ErrorMessage error = ErrorMessage.builder().code(HttpStatus.UNAUTHORIZED.value()).timestamp(new Date()).message("Given identity is not fault").build();
            return ResponseEntity.status(error.getCode()).body(error);
        }
    }
}
