package io.reactivestax.controller;

import io.reactivestax.dto.OtpDTO;
import io.reactivestax.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {

    @Autowired
    OtpService service;

    @PostMapping("/sms")
    public OtpDTO createSmsOtp(@RequestBody OtpDTO otpDTO){
        return service.save(otpDTO);
    }

    @PostMapping("/email")
    public OtpDTO createEmailOtp(@RequestBody OtpDTO otpDTO){
        return service.save(otpDTO);
    }

    @PostMapping("/call")
    public OtpDTO createCallOtp(@RequestBody OtpDTO otpDTO){
        return service.save(otpDTO);
    }

    @PostMapping("/verify")
    public OtpDTO verifyOtp(@RequestBody OtpDTO otpDTO){
        return service.verifyOtp(otpDTO);
    }

    @GetMapping("/status/{customerId}")
    public OtpDTO getStatus(@PathVariable String  customerId){
        return service.getStatus(customerId);
    }

}
