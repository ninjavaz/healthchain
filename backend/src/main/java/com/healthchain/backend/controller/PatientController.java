package com.healthchain.backend.controller;

import com.google.gson.Gson;
import com.healthchain.backend.model.consent.Consent;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.resource.Resource;
import com.healthchain.backend.model.util.ErrorMessage;
import com.healthchain.backend.model.util.NetworkProperties;
import com.healthchain.backend.service.PatientService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;


@RestController
@Log4j
@RequestMapping("/healthchain/api/patient/")
public class PatientController {
    @Autowired
    private PatientService patientService;
    @Autowired
    private NetworkProperties networkProperties;

    //PATIENT PERMISSIONED
    @RequestMapping(value = "/Patient", method = RequestMethod.GET)
    public ResponseEntity<?> getPatientResource(@RequestHeader("Authorization") String identity) {
        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(identity, CustomIdentity.class);
            Resource patient = this.patientService.getResource(obj);
            return ResponseEntity.status(HttpStatus.OK).body(patient);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @RequestMapping(value = "/Consent", method = RequestMethod.POST)
    public ResponseEntity<?> saveConsent(@RequestHeader("Authorization") String identity,
                                                @RequestParam("practitionerId") String practitionerId) {
        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(identity, CustomIdentity.class);
            Consent consent = this.patientService.saveConsent(obj, practitionerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(consent);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @RequestMapping(value = "/Consent", method = RequestMethod.GET)
    public ResponseEntity<?> getConsents(@RequestHeader("Authorization") String identity) {
        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(identity, CustomIdentity.class);
            List<Consent> consent = this.patientService.getConsents(obj);
            return ResponseEntity.status(HttpStatus.OK).body(consent);
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
