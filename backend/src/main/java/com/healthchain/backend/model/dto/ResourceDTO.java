package com.healthchain.backend.model.dto;

import com.healthchain.backend.model.entity.resource.Resource;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class ResourceDTO {
    private String id;
    private String gender;
    private String contactSystem; //phone | fax | email | pager | url | sms | other
    private String contactValue;
    private String name;
    private String surname;
    private String titleOfAddress;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;
    private String hospName;


}