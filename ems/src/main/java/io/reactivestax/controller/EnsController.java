package io.reactivestax.controller;

import io.reactivestax.dto.EnsDTO;
import io.reactivestax.service.EnsService;
import io.reactivestax.validations.groups.CallGroup;
import io.reactivestax.validations.groups.EmailGroup;
import io.reactivestax.validations.groups.SmsGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ens")
public class EnsController {

    @Autowired
    EnsService service;


    @PostMapping("/sms")
    public EnsDTO createSMS(@Validated(SmsGroup.class) @RequestBody EnsDTO ensDTO){
        return service.save(ensDTO);
    }

    @PostMapping("/call")
    public EnsDTO createCall(@Validated(CallGroup.class) @RequestBody EnsDTO ensDTO){
        return service.save(ensDTO);
    }

    @PostMapping("/email")
    public EnsDTO createEmail(@Validated(EmailGroup.class) @RequestBody EnsDTO ensDTO){
        return service.save(ensDTO);
    }

}
