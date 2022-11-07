package com.tpe.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.tpe.domain.Reservation;
import com.tpe.dto.AppLogDTO;
import com.tpe.dto.CarDTO;
import com.tpe.dto.ReservationDTO;
import com.tpe.dto.ReservationRequest;
import com.tpe.enums.AppLogLevel;
import com.tpe.enums.ReservationStatus;
import com.tpe.exception.LogException;
import com.tpe.exception.ResourceNotFoundException;
import com.tpe.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationService {
    private RestTemplate restTemplate;

    private ReservationRepository reservationRepository;

    private ModelMapper modelMapper;

    //to get info of other clients
    private EurekaClient eurekaClient;



    public void saveReservation(Long carId, ReservationRequest reservationRequest) {
        InstanceInfo instanceInfo= eurekaClient.getApplication("car-service").getInstances().get(0);
        String baseUrl= instanceInfo.getHomePageUrl();

        String path="/car/";

        //localhost:8082/car/1
        String servicePath=baseUrl+path+carId.toString();
        ResponseEntity<CarDTO> carReponse= restTemplate.getForEntity(servicePath, CarDTO.class);

        if(!(carReponse.getStatusCode()== HttpStatus.OK)) {
            throw new ResourceNotFoundException("Car not found with id:"+carId);
        }

        CarDTO carDTO= carReponse.getBody();

        Reservation reservation=new Reservation();

        reservation.setCarId(carDTO.getId());
        reservation.setPickUpTime(reservationRequest.getPickUpTime());
        reservation.setDropOffTime(reservationRequest.getDropOffTime());
        reservation.setPickUpLocation(reservationRequest.getPickUpLocation());
        reservation.setDropOffLocation(reservationRequest.getDropOffLocation());

        reservation.setStatus(ReservationStatus.CREATED);
        double tp= totalPrice(reservationRequest.getPickUpTime(), reservationRequest.getDropOffTime(),carDTO);
        reservation.setTotalPrice(tp);

        reservationRepository.save(reservation);


        InstanceInfo instanceInfoLog= eurekaClient.getApplication("log-service").getInstances().get(0);

        String baseUrlLog= instanceInfoLog.getHomePageUrl();

        String pathLog="/log";


        String servicePathLog=baseUrlLog+pathLog;

        AppLogDTO appLogDTO=new AppLogDTO();
        appLogDTO.setLevel(AppLogLevel.INFO.getName());
        appLogDTO.setDescription("Save a Reservation:"+reservation.getId());
        appLogDTO.setTime(LocalDateTime.now());

        ResponseEntity<String> logResponse= restTemplate.postForEntity(servicePathLog, appLogDTO, String.class);

        if(!(logResponse.getStatusCode()==HttpStatus.CREATED)) {
            throw new LogException("Log not created");
        }


    }


    public List<ReservationDTO> getAllReservations(){
        List<Reservation> rList= reservationRepository.findAll();

        List<ReservationDTO> rDTOList= rList.stream().map(this::mapReservationToDTO).collect(Collectors.toList());
        return rDTOList;
    }

    private ReservationDTO mapReservationToDTO(Reservation reservation) {
        ReservationDTO reservationDTO= modelMapper.map(reservation, ReservationDTO.class);
        return reservationDTO;
    }


    private Double totalPrice(LocalDateTime pickUpTime, LocalDateTime dropOffTime, CarDTO carDTO) {
        Long hours=	(new Reservation()).getTotalHours(pickUpTime, dropOffTime);
        return carDTO.getPricePerHour()*hours;
    }
}
