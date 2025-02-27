package io.reactivestax.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import io.reactivestax.domain.Ens;
import io.reactivestax.domain.Otp;
import io.reactivestax.messaging.ArtemisConsumer;
import io.reactivestax.repository.CustomerRepository;
import io.reactivestax.repository.EnsRepository;
import io.reactivestax.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TwilioMessageConsumer {

    @Autowired
    ArtemisConsumer artemisConsumer;

    @Autowired
    EnsRepository ensRepository;

    @Autowired
    OtpRepository otpRepository;


    @Autowired
    CustomerRepository customerRepository;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.phone.from}")
    private String twilioFromPhone;

    @Value("${queue.name}")
    private String queueName;

    @Value("${otp}")
    private String otpQueue;

    @JmsListener(destination = "${queue.name}")
    public void consumeMessage(@Payload String customerId) {
        System.out.println("Received customer ID: " + customerId);
        Ens ens = ensRepository.findCustomerDetailsById(Long.valueOf(customerId));
        switch (ens.getNotificationType()) {
            case "EMAIL" -> sendEmail(ens);
            case "SMS" -> sendSms(ens);
            case "CALL" -> makeacall(ens);
        }
    }

    @JmsListener(destination = "${otp}")
    public void consumeOTP(@Payload String otpId) {
        System.out.println("Received customer ID: " + otpId);
        Optional<Otp> byId = otpRepository.findById(Long.valueOf(otpId));
        switch (byId.get().getNotificationType()) {
            case "EMAIL" -> sendOTPByEmail(byId.get().getCurrentOtp());
            case "SMS" ->  sendOTPBySms(byId.get().getCurrentOtp());
            case "CALL" -> sendOTPByCall(byId.get().getCurrentOtp());
        }

    }

    private void sendOTPByCall(String currentOtp) {
        String response = "<Response><Say>" + currentOtp + "</Say></Response>";
        Twiml twiml = new Twiml(response);
        Call call = Call.creator(
                new PhoneNumber("6475404420"),
                new PhoneNumber("+16474904779"),
                twiml
        ).create();
    }

    private void sendOTPByEmail(String currentOtp) {
    }

    private void sendOTPBySms(String currentOtp) {
        try {

            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message.creator(
                    new PhoneNumber("6475404420"),
                    new PhoneNumber("+16474904779"),
                    "The OTP is " + currentOtp
            ).create();

            System.out.println("Otp Send successfully: " + currentOtp);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send otp: " + currentOtp);
        }
    }

    private void makeacall(Ens ens) {
        String response = "<Response><Say>" + ens.getMessage() + "</Say></Response>";
        Twiml twiml = new Twiml(response);
        Call call = Call.creator(
                new PhoneNumber("6475404420"),
                new PhoneNumber("+16474904779"),
                twiml
        ).create();
    }

    private void sendEmail(Ens ens) {
    }

    private void sendSms(Ens ens) {
        try {

            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message.creator(
                    new PhoneNumber("6475404420"),
                    new PhoneNumber("+16474904779"),
                    "Hello from Twilio 📞 " + ens.getMessage()
            ).create();

            System.out.println("Message sent successfully to customer ID: " + ens.getId());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send message for customer ID: " + ens.getId());
        }
    }
}
