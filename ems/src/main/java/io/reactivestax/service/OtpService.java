package io.reactivestax.service;

import io.reactivestax.domain.Customer;
import io.reactivestax.domain.Otp;
import io.reactivestax.dto.OtpDTO;
import io.reactivestax.exception.CustomerNotFoundException;
import io.reactivestax.exception.OtpLimitExceededException;
import io.reactivestax.exception.OtpVerificationException;
import io.reactivestax.messaging.ArtemisProducer;
import io.reactivestax.repository.CustomerRepository;
import io.reactivestax.repository.OtpRepository;
import io.reactivestax.validations.enums.LockedStatus;
import io.reactivestax.validations.enums.NotificationType;
import io.reactivestax.validations.enums.Status;
import io.reactivestax.validations.enums.WindowLockStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OtpRepository repository;

    @Autowired
    ArtemisProducer artemisProducer;

    @Value("${queue.otp}")
    String otpQueue;

    @Value("${max-otp-attempts}")
    int maxOtpAttempts;

    @Value("${locked.window1}")
    int window1;

    @Value("${locked.window2}")
    int window2;

    public OtpDTO save(OtpDTO otpDTO) {
        Customer customer = customerRepository
                .findById(Long.valueOf(otpDTO.getCustomerId()))
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for ID: " + otpDTO.getCustomerId()));

        List<Otp> allOtpForCustomer = repository.getAllOtpForCustomer(String.valueOf(customer.getId()));
        otpDTO.setCurrentOtp(generateOtp());
        if (!allOtpForCustomer.isEmpty()) {
            Otp previousOtp = allOtpForCustomer.get(allOtpForCustomer.size() - 1);
            previousOtp.setStatus(Status.EXPIRED);
            repository.updateOtpStatus(previousOtp.getStatus(), String.valueOf(customer.getId()));
            Otp latestOtp = repository.getAllOtpForCustomer(String.valueOf(customer.getId())).get(allOtpForCustomer.size() - 1);
            if(Objects.equals(previousOtp.getWindow1(), String.valueOf(WindowLockStatus.IN_PROGRESS))){
                latestOtp.setWindow1(String.valueOf(WindowLockStatus.IN_PROGRESS));
                latestOtp.setWindow2(String.valueOf(WindowLockStatus.INACTIVE));
            }else{
                latestOtp.setWindow1(String.valueOf(WindowLockStatus.INACTIVE));
                latestOtp.setWindow2(String.valueOf(WindowLockStatus.INACTIVE));
//                otpDTO.setWindowOneLockedStatus(WindowLockStatus.INACTIVE);
//                otpDTO.setWindowTwoLockedStatus(WindowLockStatus.INACTIVE);
            }
            Duration duration = Duration.between(latestOtp.getUpdatedAt(), LocalDateTime.now());
            handleOtpLockStatus(otpDTO, latestOtp, duration, allOtpForCustomer);
        }

        Otp otp = convertToEntity(otpDTO);
        otp.setStatus(Status.GENERATED);
        otp.setNotificationType(String.valueOf(otpDTO.getNotificationType()));
        repository.save(otp);
        otpDTO.setStatus(Status.GENERATED);
        artemisProducer.sendMessage(otpQueue, String.valueOf(otp.getId()));

        return convertToDTO(otp);
    }

    private void handleOtpLockStatus(OtpDTO otpDTO, Otp latestOtp, Duration duration, List<Otp> allOtp) {
//        if (latestOtp.getLockedStatus() == LockedStatus.ATTEMPT_LOCK) {
//            // processSlidingWindow(otpDTO, latestOtp, duration);
//        } else if (latestOtp.getLockedStatus() == LockedStatus.VALIDATION_LOCK) {
//            //  throw new OtpLimitExceededException("Your OTP limit of " + maxOtpAttempts + " attempts is exceeded.");
//        }
        if(getExpired(allOtp).size() >= 5){
            latestOtp.setWindow1(String.valueOf(WindowLockStatus.EXPIRED));
            latestOtp.setWindow2(String.valueOf(WindowLockStatus.IN_PROGRESS));
        }
//        else{
//            latestOtp.setWindow2(String.valueOf(WindowLockStatus.INACTIVE));
//            latestOtp.setWindow1(String.valueOf(WindowLockStatus.INACTIVE));
//        }
            processSlidingWindow(otpDTO, latestOtp, duration, allOtp);
    }

    private void processSlidingWindow(OtpDTO otpDTO, Otp latestOtp, Duration duration, List<Otp> allOtp) {
        if (isWindowInactive(WindowLockStatus.valueOf(latestOtp.getWindow1())) && isWindowInactive(WindowLockStatus.valueOf(latestOtp.getWindow2()))) {
            generateNewOtp(otpDTO);
        } else if (latestOtp.getWindow1() == String.valueOf(WindowLockStatus.IN_PROGRESS)) {
            handleWindowProgress(otpDTO, allOtp, duration, window1, WindowLockStatus.EXPIRED, "Window 1");
        } else if (latestOtp.getWindow2() == String.valueOf(WindowLockStatus.IN_PROGRESS)) {
            handleWindowProgress(otpDTO, allOtp, duration, window2, WindowLockStatus.EXPIRED, "Window 2");
        }
    }

    private boolean isWindowInactive(WindowLockStatus windowLockStatus) {
        return windowLockStatus == WindowLockStatus.INACTIVE;
    }

    private void generateNewOtp(OtpDTO otpDTO) {
        otpDTO.setCurrentOtp(generateOtp());
        otpDTO.setStatus(Status.GENERATED);
        otpDTO.setLockedStatus(LockedStatus.NOT_LOCKED);
    }


    private void handleWindowProgress(OtpDTO otpDTO, List<Otp> allOtp,  Duration duration, int windowDuration, WindowLockStatus expiredStatus, String windowName) {
        if (duration.toMinutes() >= windowDuration) {
            List<Otp> expired = getExpired(allOtp);
            expired.get(0).setStatus(Status.DISCARDED);
            generateNewOtp(otpDTO);
            if (windowName.equals("Window 1")) {
                otpDTO.setWindowOneLockedStatus(expiredStatus);
            } else if (windowName.equals("Window 2")) {
                otpDTO.setWindowTwoLockedStatus(expiredStatus);
            }
        } else {
//            repository.updateLockedStatus(LockedStatus.ATTEMPT_LOCK, Long.valueOf(otpDTO.getCustomerId()));
            throw new OtpLimitExceededException("Your OTP limit of " + maxOtpAttempts + " attempts is exceeded.");
        }
    }

    private List<Otp> getExpired(List<Otp> allOtp) {
        List<Otp> expired = allOtp.stream().filter(otp -> otp.getStatus().equals(Status.EXPIRED)).toList();
        return expired;
    }

    private boolean verifyAttempts(List<Otp> allOtpForCustomer) {
        if (allOtpForCustomer.size() >= maxOtpAttempts) {
            throw new OtpLimitExceededException("Your OTP limit of " + maxOtpAttempts + " attempts is exceeded.");
        }
        return true;
    }


    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(Math.abs(random.nextLong() * 100000000));
    }

    public OtpDTO convertToDTO(Otp otp) {
        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setId(otp.getId());
        otpDTO.setStatus(otp.getStatus());
        otpDTO.setCreated_at(LocalDateTime.now());
        otpDTO.setUpdated_at(LocalDateTime.now());
        otpDTO.setCustomerId(otp.getCustomerId());
        otpDTO.setCurrentOtp(otp.getCurrentOtp());
        otpDTO.setLockedStatus(otp.getLockedStatus());
        otpDTO.setNotificationType(NotificationType.valueOf(otp.getNotificationType()));
        return otpDTO;
    }

    public Otp convertToEntity(OtpDTO otpDTO) {
        Otp otp = new Otp();
        otp.setId(otpDTO.getId());
        otp.setStatus(otpDTO.getStatus());
        otp.setCreatedAt(LocalDateTime.now());
        otp.setUpdatedAt(LocalDateTime.now());
        otp.setCustomerId(otpDTO.getCustomerId());
        otp.setCurrentOtp(otpDTO.getCurrentOtp());
        otp.setLockedStatus(otpDTO.getLockedStatus());
        otp.setNotificationType(String.valueOf(otpDTO.getNotificationType()));
        return otp;
    }

    public OtpDTO verifyOtp(OtpDTO otpDTO) {
        List<Otp> allOtpForCustomer = repository.getAllOtpForCustomer(String.valueOf(otpDTO.getCustomerId()));
        if (allOtpForCustomer.isEmpty()) {
            throw new OtpVerificationException("No OTPs found for customer ID: " + otpDTO.getCustomerId());
        }
        Otp latestOtp = allOtpForCustomer.get(allOtpForCustomer.size() - 1);
        long attemptsCount = repository.findLatestAttemptForStatus(Status.GENERATED);
        if (otpDTO.getCurrentOtp().equals(latestOtp.getCurrentOtp())) {
            if (attemptsCount > 3) {
                latestOtp.setStatus(Status.VERIFIED);
                repository.updateOtpStatus(Status.VERIFIED, otpDTO.getCustomerId());
                allOtpForCustomer.forEach(customer -> customer.setStatus(Status.DISCARDED));
                repository.updateAttempts(1, otpDTO.getCurrentOtp());
            } else {
                latestOtp.setStatus(Status.ATTEMPT_EXCEEDED);
                repository.updateOtpStatus(Status.ATTEMPT_EXCEEDED, latestOtp.getCustomerId());
                latestOtp.setWindow1(String.valueOf(WindowLockStatus.IN_PROGRESS));
                repository.save(latestOtp);
                throw new OtpVerificationException("Maximum OTP verification attempts exceeded for customer ID: " + otpDTO.getCustomerId());
            }
        } else {
//            latestOtp.setStatus(Status.EXPIRED);
            if(attemptsCount >= 3){
                latestOtp.setLockedStatus(LockedStatus.ATTEMPT_LOCK);
                latestOtp.setWindow1(String.valueOf(WindowLockStatus.IN_PROGRESS));
                repository.save(latestOtp);
                throw new OtpVerificationException("Invalid OTP for customer ID: " + otpDTO.getCustomerId());
            }else {
                long expiredCount = allOtpForCustomer.stream()
                        .filter(customer -> customer.getStatus().equals(Status.EXPIRED)).count();
                if (expiredCount >=3) {
                    latestOtp.setLockedStatus(LockedStatus.ATTEMPT_LOCK);
                    latestOtp.setWindow1(String.valueOf(WindowLockStatus.IN_PROGRESS));
                }else{
//                    latestOtp.setLockedStatus(LockedStatus.VALIDATION_LOCK);
//                    latestOtp.setWindow1(String.valueOf(WindowLockStatus.EXPIRED));
//                    latestOtp.setWindow2(String.valueOf(WindowLockStatus.IN_PROGRESS));
                    attemptsCount = attemptsCount+1;
                    repository.updateAttempts(attemptsCount, latestOtp.getCurrentOtp());
                }
               // repository.updateOtpStatus(Status.EXPIRED, otpDTO.getCustomerId());
                throw new OtpVerificationException("Invalid OTP for customer ID: " + otpDTO.getCustomerId());
            }
        }
        return convertToDTO(latestOtp);
    }

    public OtpDTO getStatus(String customerId) {
        List<Otp> allOtpForCustomer = repository.getAllOtpForCustomer(String.valueOf(customerId));
        allOtpForCustomer.forEach(System.out::println);

        if (allOtpForCustomer.isEmpty()) {
            throw new OtpVerificationException("No OTPs found for customer ID: " + customerId);
        }
        Otp otp = allOtpForCustomer.get(allOtpForCustomer.size() - 1);
        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setStatus(otp.getStatus());
        otpDTO.setCustomerId(customerId);
        return otpDTO;
    }
}
