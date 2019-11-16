package repository.cab;

import org.springframework.data.mongodb.repository.MongoRepository;

import model.cab.Ticket;


public interface TicketRepository extends MongoRepository<Ticket, Long> {
}
