package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Train train = new Train();
        train =trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        if((train.getNoOfSeats()-train.getBookedTickets().size())<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception(("Less tickets are available"));
        }

        train.setTrainId(bookTicketEntryDto.getTrainId());
        String trainRoute = train.getRoute();
        String []trainRouteArr =trainRoute.split(",");

        List<Passenger> passengerList = new ArrayList<>();
        List<Integer> ids = bookTicketEntryDto.getPassengerIds();
        for(int id: ids){
            passengerList.add(passengerRepository.findById(id).get());
        }

        String fromStation = String.valueOf(bookTicketEntryDto.getFromStation());
        String toStation = String.valueOf(bookTicketEntryDto.getToStation());

        int boardingIndex = -1;
        int endIndex = -1;

        for(int i=0;i<trainRouteArr.length;i++){
            if(fromStation.equals(trainRouteArr[i]));
            {
                 boardingIndex = i;
            }
            if(toStation.equals(trainRouteArr[i])){
                 endIndex = i;
            }
        }

        if(boardingIndex == -1 || endIndex == -1 || (endIndex - boardingIndex)<=0)
            throw new Exception("Invalid stations");


        int totalCost = (endIndex-boardingIndex)*300*bookTicketEntryDto.getNoOfSeats();

        Ticket ticket = new Ticket();

        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalCost);
        ticket.setPassengersList(passengerList);

        train.getBookedTickets().add(ticket);
        train.setNoOfSeats(train.getNoOfSeats()-bookTicketEntryDto.getNoOfSeats());

        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);

        trainRepository.save(train);

        ticketRepository.save(ticket);


       return ticket.getTicketId() ;

    }
}
