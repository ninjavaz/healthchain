package com.healthchain.backend.controller;

import com.google.gson.Gson;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.Role;
import com.healthchain.backend.model.resource.PatientResource;
import com.healthchain.backend.model.resource.Resource;
import com.healthchain.backend.model.util.ErrorMessage;
import com.healthchain.backend.model.util.NetworkProperties;
import com.healthchain.backend.service.AuthService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;


@RestController
@Log4j
@RequestMapping("/healthchain/api/")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private NetworkProperties networkProperties;

//    @RequestMapping(value = "/check", method = RequestMethod.GET)
//    public Object check(HttpSession session) {
//        return
//                session.getAttribute("identity") +
//                        String.valueOf(session.getCreationTime()) +
//                        String.valueOf(session.getMaxInactiveInterval());
//    }

//    /**
//     * Login method t..
//     *
//     * @return
//     */
//    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    public void login(HttpSession session, @RequestBody CustomIdentity identity, @RequestParam String hospName) throws Exception {
//
////        session.setAttribute("identity", identity.toString());
////        session.setMaxInactiveInterval(60);
//        byte[] result;
//        NetworkProperties.HospInfo hospInfo = networkProperties.getHospInfoByName().get(hospName);
//
//        Path networkConfigPath = Paths.get(hospInfo.getNetworkConfigPath());

//        try {
//            Identity identity = Identities.newX509Identity(id.getMspId(), Identities.readX509Certificate(id.getCredentials().getCertificate()), Identities.readPrivateKey(id.getCredentials().getPrivateKey()));
//            Gateway.Builder builder = Gateway.createBuilder();
//            builder.identity(identity)
//                    .networkConfig(networkConfigPath)
//                    .discovery(true);
//
//            // create a gateway connection
//            try {
//                Gateway gateway = builder.connect();
//                // get the network and contract
//                Network network = gateway.getNetwork("hospitalchannel");
//                Contract contract = network.getContract("healthchainCC");
//
//                result = contract.evaluateTransaction("getUserRole");
//
//                //get user roles from attribute...
//
//
//                // Access the user's identity
////                User user = gateway.ge
////                if (user != null) {
////                    // Access the attributes of the user's identity
////                    String[] attributeNames = user.getAttributeNames();
////                    for (String attributeName : attributeNames) {
////                        String attributeValue = user.getAttributeValue(attributeName);
////                        System.out.println("Attribute: " + attributeName + ", Value: " + attributeValue);
////
////                        // Perform checks based on attributes
////                        if ("Specialization".equals(attributeName) && "Cardiologist".equals(attributeValue)) {
////                            // Perform action for a Cardiologist
////                        }
////                    }
////                }
//
////                result = contract.evaluateTransaction("queryAllCars");
////            System.out.println(new String(result));
//
////            contract.submitTransaction("createCar", "CAR10", "VW", "Polo", "Grey", "Mary");
////
////            result = contract.evaluateTransaction("queryCar", "CAR10");
////            System.out.println(new String(result));
////
////            contract.submitTransaction("changeCarOwner", "CAR10", "Archie");
////
////            result = contract.evaluateTransaction("queryCar", "CAR10");
////            System.out.println(new String(result));
//
//                return ResponseEntity.status(HttpStatus.OK).body(new String(result));
//            } catch (Exception e) {
//                e.printStackTrace();
//                ErrorMessage error = ErrorMessage.builder().code(HttpStatus.FORBIDDEN.value()).timestamp(new Date()).name("The connection to the blockchain network failed due to insufficient permissions or some errors")
//                        .message(e.getMessage()).build();
//                return ResponseEntity.status(error.getCode()).body(error);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            ErrorMessage error = ErrorMessage.builder().code(HttpStatus.UNAUTHORIZED.value()).timestamp(new Date()).message("Given identity is not fault").build();
//            return ResponseEntity.status(error.getCode()).body(error);
//        }
//    }



    @RequestMapping(value = "/Patient", method = RequestMethod.GET)
    public ResponseEntity<?> getPatientResource(@RequestHeader("Authorization") String identity, @RequestParam("hospName") String hospName) {
        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(identity, CustomIdentity.class);
            Resource patient = authService.getResource(obj, hospName);
            return ResponseEntity.status(HttpStatus.OK).body(patient);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }


    @RequestMapping(value = "/Patient", method = RequestMethod.POST)
    public ResponseEntity<?> savePatientResource(@RequestHeader("Authorization") String adminId,
                                      @RequestBody PatientResource patient) {

        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(adminId, CustomIdentity.class);
            CustomIdentity identity = authService.enrollUser(Role.PATIENT, obj, patient);
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
