package com.healthchain.backend.controller;

import com.healthchain.backend.model.CustomIdentity;
import com.healthchain.backend.model.util.ErrorMessage;
import com.healthchain.backend.model.util.NetworkProperties;
import com.healthchain.backend.service.AuthService;
import lombok.extern.log4j.Log4j;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hyperledger.fabric.gateway.Identity;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;


@RestController
@Log4j
@RequestMapping("/healthchain/api/auth/")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private NetworkProperties networkProperties;

    /**
     * Login method t..
     *
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody CustomIdentity id, @RequestParam String hospName) throws Exception {
        byte[] result;
        NetworkProperties.HospInfo hospInfo = networkProperties.getHospInfoByName().get(hospName);

        Path networkConfigPath = Paths.get(hospInfo.getNetworkConfigPath());

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
            e.printStackTrace();
            ErrorMessage error = ErrorMessage.builder().code(HttpStatus.UNAUTHORIZED.value()).timestamp(new Date()).message("Given identity is not fault").build();
            return ResponseEntity.status(error.getCode()).body(error);
        }
    }


    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestParam("username") String username,
                                      @RequestParam("hospName") String hospName,
                                      @RequestBody CustomIdentity adminId) {

        try {
            CustomIdentity identity = authService.registerUser(username, hospName, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(identity);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private ResponseEntity<ErrorMessage> buildErrorResponseEntity(HttpStatus code, Exception e) {
        ErrorMessage errorMessage = ErrorMessage.builder().code(code.value()).timestamp(new Date())
                .name("HEALTHCHAIN_EXCEPTION").message(e.getMessage()).build();
        return ResponseEntity.status(code).body(errorMessage);
    }


}
