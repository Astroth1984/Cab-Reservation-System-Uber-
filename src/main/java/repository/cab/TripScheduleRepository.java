package repository.cab;

import org.springframework.data.mongodb.repository.MongoRepository;

import model.cab.Trip;
import model.cab.TripSchedule;


public interface TripScheduleRepository extends MongoRepository<TripSchedule, String> {
    TripSchedule findByTripDetailAndTripDate(Trip tripDetail, String tripDate);
}
