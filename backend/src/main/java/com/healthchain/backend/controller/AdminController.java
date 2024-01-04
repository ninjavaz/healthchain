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
@RequestMapping("/healthchain/api/admin/")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private PractitonerService practitonerService;
    @Autowired
    private NetworkProperties networkProperties;

    //ADMIN PERMISSIONED
    @RequestMapping(value = "/Patient", method = RequestMethod.POST)
    public ResponseEntity<?> savePatientResource(@RequestHeader("Authorization") String adminId,
                                                 @RequestBody PatientResource patient) {
        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(adminId, CustomIdentity.class);
            patient.setResourceType("Patient");
            CustomIdentity identity = this.adminService.enrollUser(Role.PATIENT, obj, patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(identity);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @RequestMapping(value = "/Practitioner", method = RequestMethod.POST)
    public ResponseEntity<?> savePractitionerResource(@RequestHeader("Authorization") String adminId,
                                                      @RequestBody PractitionerResource practitioner) {

        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(adminId, CustomIdentity.class);
            practitioner.setResourceType("Practitioner");
            CustomIdentity identity = this.adminService.enrollUser(Role.PRACTITIONER, obj, practitioner);
            return ResponseEntity.status(HttpStatus.CREATED).body(identity);
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
