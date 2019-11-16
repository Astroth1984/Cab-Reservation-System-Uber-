package dto.mapper;

import dto.model.cab.TripDto;
import model.cab.Trip;

public class TripMapper {
	
	public static TripDto toTripDto(Trip trip) {
        return new TripDto()
                .setId(trip.getId())
                .setAgencyCode(trip.getAgency().getCode())
                .setSourceStopCode(trip.getSourceStop().getCode())
                .setSourceStopName(trip.getSourceStop().getName())
                .setDestinationStopCode(trip.getDestStop().getCode())
                .setDestinationStopName(trip.getDestStop().getName())
                .setCabCode(trip.getCab().getCode())
                .setJourneyTime(trip.getJourneyTime())
                .setFare(trip.getFare());
    }

}
