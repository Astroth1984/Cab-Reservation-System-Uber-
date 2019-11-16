package repository.cab;

import org.springframework.data.mongodb.repository.MongoRepository;

import model.cab.Agency;
import model.cab.Cab;
import model.cab.Stop;
import model.cab.Trip;

import java.util.List;


public interface TripRepository extends MongoRepository<Trip, String> {
    Trip findBySourceStopAndDestStopAndCab(Stop source, Stop destination, Cab cab);

    List<Trip> findAllBySourceStopAndDestStop(Stop source, Stop destination);

    List<Trip> findByAgency(Agency agency);
}