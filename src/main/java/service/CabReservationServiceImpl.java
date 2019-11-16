package service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dto.mapper.TripMapper;
import dto.mapper.TripScheduleMapper;
import dto.model.cab.AgencyDto;
import dto.model.cab.StopDto;
import dto.model.cab.TicketDto;
import dto.model.cab.TripDto;
import dto.model.cab.TripScheduleDto;
import dto.model.user.UserDto;
import exception.BRSException;
import exception.EntityType;
import exception.ExceptionType;
import model.cab.Agency;
import model.cab.Cab;
import model.cab.Stop;
import model.cab.Ticket;
import model.cab.Trip;
import model.cab.TripSchedule;
import model.user.User;
import repository.cab.AgencyRepository;
import repository.cab.CabRepository;
import repository.cab.StopRepository;
import repository.cab.TicketRepository;
import repository.cab.TripRepository;
import repository.cab.TripScheduleRepository;
import repository.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

import static exception.EntityType.*;
import static exception.ExceptionType.*;


@Component
public class CabReservationServiceImpl implements CabReservationService {
    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private CabRepository cabRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripScheduleRepository tripScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Retruns all the available stops in the database.
     *
     * @return
     */
    @Override
    public Set<StopDto> getAllStops() {
        return stopRepository.findAll()
                .stream()
                .map(stop -> modelMapper.map(stop, StopDto.class))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Returns the Stop details based on stop code.
     *
     * @param stopCode
     * @return
     */
    @Override
    public StopDto getStopByCode(String stopCode) {
        Optional<Stop> stop = Optional.ofNullable(stopRepository.findByCode(stopCode));
        if (stop.isPresent()) {
            return modelMapper.map(stop.get(), StopDto.class);
        }
        throw exception (STOP, ENTITY_NOT_FOUND, stopCode);
    }

    /**
     * Fetch AgencyDto from userDto
     *
     * @param userDto
     * @return
     */
    @Override
    public AgencyDto getAgency(UserDto userDto) {
        User user = getUser(userDto.getEmail());
        if (user != null) {
            Optional<Agency> agency = Optional.ofNullable(agencyRepository.findByOwner(user));
            if (agency.isPresent()) {
                return modelMapper.map(agency.get(), AgencyDto.class);
            }
            throw exceptionWithId(AGENCY, ENTITY_NOT_FOUND, "2", user.getEmail());
        }
        throw exception(USER, ENTITY_NOT_FOUND, userDto.getEmail());
    }

    /**
     * Register a new agency from the Admin signup flow
     *
     * @param agencyDto
     * @return
     */
    @Override
    public AgencyDto addAgency(AgencyDto agencyDto) {
        User admin = getUser(agencyDto.getOwner().getEmail());
        if (admin != null) {
            Optional<Agency> agency = Optional.ofNullable(agencyRepository.findByName(agencyDto.getName()));
            if (!agency.isPresent()) {
                Agency agencyModel = new Agency()
                        .setName(agencyDto.getName())
                        .setDetails(agencyDto.getDetails())
                        .setCode(RandomStringUtil.getAlphaNumericString(8, agencyDto.getName()))
                        .setOwner(admin);
                agencyRepository.save(agencyModel);
                return modelMapper.map(agencyModel, AgencyDto.class);
            }
            throw exception(AGENCY, DUPLICATE_ENTITY, agencyDto.getName());
        }
        throw exception(USER, ENTITY_NOT_FOUND, agencyDto.getOwner().getEmail());
    }

    /**
     * Updates the agency with given Cab information
     *
     * @param agencyDto
     * @param cabDto
     * @return
     */
    @Transactional
    public AgencyDto updateAgency(AgencyDto agencyDto, CabDto cabDto) {
        Agency agency = getAgency(agencyDto.getCode());
        if (agency != null) {
            if (cabDto != null) {
                Optional<Cab> cab = Optional.ofNullable(cabRepository.findByCodeAndAgency(cabDto.getCode(), agency));
                if (!cab.isPresent()) {
                    Cab cabModel = new Cab()
                            .setAgency(agency)
                            .setCode(cabDto.getCode())
                            .setCapacity(cabDto.getCapacity())
                            .setMake(cabDto.getMake());
                    cabRepository.save(cabModel);
                    if (agency.getCabs() == null) {
                        agency.setCabs(new HashSet<>());
                    }
                    agency.getCabs().add(cabModel);
                    return modelMapper.map(agencyRepository.save(agency), AgencyDto.class);
                }
                throw exceptionWithId(CAB, DUPLICATE_ENTITY, "2", cabDto.getCode(), agencyDto.getCode());
            } else {
                //update agency details case
                agency.setName(agencyDto.getName())
                        .setDetails(agencyDto.getDetails());
                return modelMapper.map(agencyRepository.save(agency), AgencyDto.class);
            }
        }
        throw exceptionWithId(AGENCY, ENTITY_NOT_FOUND, "2", agencyDto.getOwner().getEmail());
    }

    /**
     * Returns trip details based on trip_id
     *
     * @param tripID
     * @return
     */
    @Override
    public TripDto getTripById(String tripID) {
        Optional<Trip> trip = tripRepository.findById(tripID);
        if (trip.isPresent()) {
            return TripMapper.toTripDto(trip.get());
        }
        throw exception(TRIP, ENTITY_NOT_FOUND, tripID);
    }

    /**
     * Creates two new Trips with the given information in tripDto object
     *
     * @param tripDto
     * @return
     */
    @Override
    @Transactional
    public List<TripDto> addTrip(TripDto tripDto) {
        Stop sourceStop = getStop(tripDto.getSourceStopCode());
        if (sourceStop != null) {
            Stop destinationStop = getStop(tripDto.getDestinationStopCode());
            if (destinationStop != null) {
                if (!sourceStop.getCode().equalsIgnoreCase(destinationStop.getCode())) {
                    Agency agency = getAgency(tripDto.getAgencyCode());
                    if (agency != null) {
                        Cab cab = getCab(tripDto.getCabCode());
                        if (cab != null) {
                            //Each new trip creation results in a to and a fro trip
                            List<TripDto> trips = new ArrayList<>(2);
                            Trip toTrip = new Trip()
                                    .setSourceStop(sourceStop)
                                    .setDestStop(destinationStop)
                                    .setAgency(agency)
                                    .setCab(cab)
                                    .setJourneyTime(tripDto.getJourneyTime())
                                    .setFare(tripDto.getFare());
                            trips.add(TripMapper.toTripDto(tripRepository.save(toTrip)));

                            Trip froTrip = new Trip()
                                    .setSourceStop(destinationStop)
                                    .setDestStop(sourceStop)
                                    .setAgency(agency)
                                    .setCab(cab)
                                    .setJourneyTime(tripDto.getJourneyTime())
                                    .setFare(tripDto.getFare());
                            trips.add(TripMapper.toTripDto(tripRepository.save(froTrip)));
                            return trips;
                        }
                        throw exception(CAB, ENTITY_NOT_FOUND, tripDto.getCabCode());
                    }
                    throw exception(AGENCY, ENTITY_NOT_FOUND, tripDto.getAgencyCode());
                }
                throw exception(TRIP, ENTITY_EXCEPTION, "");
            }
            throw exception(STOP, ENTITY_NOT_FOUND, tripDto.getDestinationStopCode());
        }
        throw exception(STOP, ENTITY_NOT_FOUND, tripDto.getSourceStopCode());
    }

    /**
     * Fetch all the trips for a given agency
     *
     * @param agencyCode
     * @return
     */
    @Override
    public List<TripDto> getAgencyTrips(String agencyCode) {
        Agency agency = getAgency(agencyCode);
        if (agency != null) {
            List<Trip> agencyTrips = tripRepository.findByAgency(agency);
            if (!agencyTrips.isEmpty()) {
                return agencyTrips
                        .stream()
                        .map(trip -> TripMapper.toTripDto(trip))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
        throw exception(AGENCY, ENTITY_NOT_FOUND, agencyCode);
    }

    /**
     * Returns a list of trips between given source and destination stops.
     *
     * @param sourceStopCode
     * @param destinationStopCode
     * @return
     */
    @Override
    public List<TripDto> getAvailableTripsBetweenStops(String sourceStopCode, String destinationStopCode) {
        List<Trip> availableTrips = findTripsBetweenStops(sourceStopCode, destinationStopCode);
        if (!availableTrips.isEmpty()) {
            return availableTrips
                    .stream()
                    .map(trip -> TripMapper.toTripDto(trip))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Function to locate all the trips between src and dest stops and then
     * filter the results as per the given date based on data present in
     * trip schedule collection.
     *
     * @param sourceStopCode
     * @param destinationStopCode
     * @param tripDate
     * @return list of tripschedules on given date
     */
    @Override
    public List<TripScheduleDto> getAvailableTripSchedules(String sourceStopCode, String destinationStopCode, String tripDate) {
        List<Trip> availableTrips = findTripsBetweenStops(sourceStopCode, destinationStopCode);
        if (!availableTrips.isEmpty()) {
            return availableTrips
                    .stream()
                    .map(trip -> getTripSchedule(TripMapper.toTripDto(trip), tripDate, true))
                    .filter(tripScheduleDto -> tripScheduleDto != null)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Returns TripScheduleDto based on trip details and trip date,
     * optionally creates a schedule if its not found and if the createSchedForTrip
     * parameter is set to true.
     *
     * @param tripDto
     * @param tripDate
     * @param createSchedForTrip
     * @return
     */
    @Override
    public TripScheduleDto getTripSchedule(TripDto tripDto, String tripDate, boolean createSchedForTrip) {
        Optional<Trip> trip = tripRepository.findById(tripDto.getId());
        if (trip.isPresent()) {
            Optional<TripSchedule> tripSchedule = Optional.ofNullable(tripScheduleRepository.findByTripDetailAndTripDate(trip.get(), tripDate));
            if (tripSchedule.isPresent()) {
                return TripScheduleMapper.toTripScheduleDto(tripSchedule.get());
            } else {
                if (createSchedForTrip) { //create the schedule
                    TripSchedule tripSchedule1 = new TripSchedule()
                            .setTripDetail(trip.get())
                            .setTripDate(tripDate)
                            .setAvailableSeats(trip.get().getCab().getCapacity());
                    return TripScheduleMapper.toTripScheduleDto(tripScheduleRepository.save(tripSchedule1));
                } else {
                    throw exceptionWithId(TRIP, ENTITY_NOT_FOUND, "2", tripDto.getId(), tripDate);
                }
            }
        }
        throw exception(TRIP, ENTITY_NOT_FOUND, tripDto.getId());
    }

    /**
     * Method to book ticket for a given trip schedule
     *
     * @param tripScheduleDto
     * @param userDto
     * @return
     */
    @Override
    @Transactional
    public TicketDto bookTicket(TripScheduleDto tripScheduleDto, UserDto userDto) {
        User user = getUser(userDto.getEmail());
        if (user != null) {
            Optional<TripSchedule> tripSchedule = tripScheduleRepository.findById(tripScheduleDto.getId());
            if (tripSchedule.isPresent()) {
                Ticket ticket = new Ticket()
                        .setCancellable(false)
                        .setJourneyDate(tripSchedule.get().getTripDate())
                        .setPassenger(user)
                        .setTripSchedule(tripSchedule.get())
                        .setSeatNumber(tripSchedule.get().getTripDetail().getCab().getCapacity() - tripSchedule.get().getAvailableSeats());
                ticketRepository.save(ticket);
                tripSchedule.get().setAvailableSeats(tripSchedule.get().getAvailableSeats() - 1); //reduce availability by 1
                tripScheduleRepository.save(tripSchedule.get());//update schedule
                return TicketMapper.toTicketDto(ticket);
            }
            throw exceptionWithId(TRIP, ENTITY_NOT_FOUND, "2", tripScheduleDto.getTripId(), tripScheduleDto.getTripDate());
        }
        throw exception(USER, ENTITY_NOT_FOUND, userDto.getEmail());
    }

    /**
     * Search for all Trips between src and dest stops
     *
     * @param sourceStopCode
     * @param destinationStopCode
     * @return
     */
    private List<Trip> findTripsBetweenStops(String sourceStopCode, String destinationStopCode) {
        Optional<Stop> sourceStop = Optional
                .ofNullable(stopRepository.findByCode(sourceStopCode));
        if (sourceStop.isPresent()) {
            Optional<Stop> destStop = Optional
                    .ofNullable(stopRepository.findByCode(destinationStopCode));
            if (destStop.isPresent()) {
                List<Trip> availableTrips = tripRepository.findAllBySourceStopAndDestStop(sourceStop.get(), destStop.get());
                if (!availableTrips.isEmpty()) {
                    return availableTrips;
                }
                return Collections.emptyList();
            }
            throw exception(STOP, ENTITY_NOT_FOUND, destinationStopCode);
        }
        throw exception(STOP, ENTITY_NOT_FOUND, sourceStopCode);
    }

    /**
     * Fetch user from UserDto
     *
     * @param email
     * @return
     */
    private User getUser(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Fetch Stop from stopCode
     *
     * @param stopCode
     * @return
     */
    private Stop getStop(String stopCode) {
        return stopRepository.findByCode(stopCode);
    }

    /**
     * Fetch Cab from cabCode, since it is unique we don't have issues of finding duplicate Cabs
     *
     * @param cabCode
     * @return
     */
    private Cab getCab(String cabCode) {
        return cabRepository.findByCode(cabCode);
    }

    /**
     * Fetch Agency from agencyCode
     *
     * @param agencyCode
     * @return
     */
    private Agency getAgency(String agencyCode) {
        return agencyRepository.findByCode(agencyCode);
    }

    /**
     * Returns a new RuntimeException
     *
     * @param entityType
     * @param exceptionType
     * @param args
     * @return
     */
    private RuntimeException exception(EntityType entityType, ExceptionType exceptionType, String... args) {
        return BRSException.throwException(entityType, exceptionType, args);
    }

    /**
     * Returns a new RuntimeException
     *
     * @param entityType
     * @param exceptionType
     * @param args
     * @return
     */
    private RuntimeException exceptionWithId(EntityType entityType, ExceptionType exceptionType, String id, String... args) {
        return BRSException.throwExceptionWithId(entityType, exceptionType, id, args);
    }
}
