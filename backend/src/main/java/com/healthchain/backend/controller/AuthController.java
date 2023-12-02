package com.healthchain.backend.controller;

//import com.healthchain.backend.model.Identity;
import com.healthchain.backend.model.CustomIdentity;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.NetworkConfig;
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
import java.util.List;

@RestController
@RequestMapping("/healthchain/api/auth/")
public class AuthController {

    /**
     * Login method t..
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody CustomIdentity id) throws Exception{
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
        Identity user = Identities.newX509Identity(id.getMspId(), Identities.readX509Certificate(id.getCredentials().getCertificate()), Identities.readPrivateKey(id.getCredentials().getPrivateKey()));

//        NetworkConfig networkConfig = NetworkConfig.fromYamlFile(networkConfigPath);


        Gateway.Builder builder = Gateway.createBuilder();

        builder.identity(user)
//        builder.identity(wallet, "user2")
                .networkConfig(networkConfigPath)
                .discovery(true);


        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("hospitalchannel");
            Contract contract = network.getContract("healthchainCC1");


            result = contract.evaluateTransaction("queryAllCars");
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
        }

        return new String(result);
    }
}
