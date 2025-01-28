package io.reactivestax.service;

import io.reactivestax.domain.Ens;
import io.reactivestax.dto.EnsDTO;
import io.reactivestax.messaging.ArtemisProducer;
import io.reactivestax.repository.EnsRepository;
import io.reactivestax.validations.enums.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnsService {

    @Autowired
    EnsRepository ensRepository;

    @Autowired
    ArtemisProducer artemisProducer;


    public EnsDTO save(EnsDTO ensDTO) {
         Ens ens = convertToEntity(ensDTO);
         ensDTO = convertToDTO(ensRepository.save(ens));
        artemisProducer.sendMessage("ens-queue", String.valueOf(ensDTO.getId()));
        return ensDTO;
    }

    public EnsDTO convertToDTO(Ens ens) {
        EnsDTO ensDTO = new EnsDTO();
        ensDTO.setId(ens.getId());
        ensDTO.setMessage(ens.getMessage());
        ensDTO.setEmail(ens.getEmail());
        ensDTO.setPhoneNumber(ens.getPhoneNumber());
        ensDTO.setNotificationType(NotificationType.valueOf(ens.getNotificationType()));
        return ensDTO;
    }

    public Ens convertToEntity(EnsDTO ensDTO) {
        Ens ens = new Ens();
        ens.setId(ensDTO.getId());
        ens.setEmail(ensDTO.getEmail());
        ens.setPhoneNumber(ensDTO.getPhoneNumber());
        ens.setMessage(ensDTO.getMessage());
        ens.setNotificationType(String.valueOf(ensDTO.getNotificationType()));
        return ens;
    }

}
