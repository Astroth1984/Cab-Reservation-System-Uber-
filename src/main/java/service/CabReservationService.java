package service;

import java.util.List;
import java.util.Set;

import dto.model.cab.AgencyDto;
import dto.model.cab.CabDto;
import dto.model.cab.StopDto;
import dto.model.cab.TicketDto;
import dto.model.cab.TripDto;
import dto.model.cab.TripScheduleDto;


public interface CabReservationService {

    //Stop related methods
    Set<StopDto> getAllStops();

    StopDto getStopByCode(String stopCode);

    //Agency related methods
    AgencyDto getAgency(UserDto userDto);

    AgencyDto addAgency(AgencyDto agencyDto);

    AgencyDto updateAgency(AgencyDto agencyDto, CabDto cabDto);

    //Trip related methods
    TripDto getTripById(String tripID);

    List<TripDto> addTrip(TripDto tripDto);

    List<TripDto> getAgencyTrips(String agencyCode);

    List<TripDto> getAvailableTripsBetweenStops(String sourceStopCode, String destinationStopCode);

    //Trips Schedule related methods
    List<TripScheduleDto> getAvailableTripSchedules(String sourceStopCode, String destinationStopCode, String tripDate);

    TripScheduleDto getTripSchedule(TripDto tripDto, String tripDate, boolean createSchedForTrip);

    //Ticket related method
    TicketDto bookTicket(TripScheduleDto tripScheduleDto, UserDto passenger);

}
