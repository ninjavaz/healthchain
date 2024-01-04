package com.healthchain.backend.controller;

import com.google.gson.Gson;
import com.healthchain.backend.model.consent.Consent;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.Role;
import com.healthchain.backend.model.resource.PatientResource;
import com.healthchain.backend.model.resource.PractitionerResource;
import com.healthchain.backend.model.resource.Resource;
import com.healthchain.backend.model.util.ErrorMessage;
import com.healthchain.backend.model.util.NetworkProperties;
import com.healthchain.backend.service.AdminService;
import com.healthchain.backend.service.PatientService;
import com.healthchain.backend.service.PractitonerService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;


@RestController
@Log4j
@RequestMapping("/healthchain/api/practitioner/")
public class PractitionerController {
    @Autowired
    private PractitonerService practitonerService;
    @Autowired
    private NetworkProperties networkProperties;


    //PRACTITIONER PERMISSIONED
    @RequestMapping(value = "/Practitioner", method = RequestMethod.GET)
    public ResponseEntity<?> getPractitonerResource(@RequestHeader("Authorization") String identity) {
        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(identity, CustomIdentity.class);
            Resource practitioner = this.practitonerService.getResource(obj);
            return ResponseEntity.status(HttpStatus.OK).body(practitioner);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @RequestMapping(value = "/Patient", method = RequestMethod.GET)
    public ResponseEntity<?> getPatientResourcesForPractitioner(@RequestHeader("Authorization") String identity) {
        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(identity, CustomIdentity.class);
            List<PatientResource> patients = this.practitonerService.getPatientResourcesForPractitioner(obj);
            return ResponseEntity.status(HttpStatus.OK).body(patients);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    //UTIL
    private ResponseEntity<ErrorMessage> buildErrorResponseEntity(HttpStatus code, Exception e) {
        ErrorMessage errorMessage = ErrorMessage.builder().code(code.value()).timestamp(new Date())
                .name("HEALTHCHAIN_EXCEPTION").message(e.getMessage()).build();
        return ResponseEntity.status(code).body(errorMessage);
    }
}
