package com.healthchain.backend.controller;

import com.google.gson.Gson;
import com.healthchain.backend.model.Mapper;
import com.healthchain.backend.model.dto.ResourceDTO;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.Role;
import com.healthchain.backend.model.util.ErrorMessage;
import com.healthchain.backend.model.util.NetworkProperties;
import com.healthchain.backend.service.AdminService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;


@RestController
@Log4j
@RequestMapping("/healthchain/api/admin/")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private NetworkProperties networkProperties;
    @Autowired
    private Mapper mapper;

    @RequestMapping(value = "/Patient", method = RequestMethod.POST)
    public ResponseEntity<?> savePatientResource(@RequestHeader("Authorization") String adminId,
                                                 @RequestBody ResourceDTO resourceDTO) {
        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(adminId, CustomIdentity.class);
//            patient.setResourceType("Patient");
            CustomIdentity identity = this.adminService.enrollUser(Role.PATIENT, obj, resourceDTO);
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
                                                      @RequestBody ResourceDTO resourceDTO) {

        try {
            Gson gson = new Gson();
            CustomIdentity obj = gson.fromJson(adminId, CustomIdentity.class);
            CustomIdentity identity = this.adminService.enrollUser(Role.PRACTITIONER, obj, resourceDTO);
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
