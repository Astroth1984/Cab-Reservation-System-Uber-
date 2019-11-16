package repository.cab;

import org.springframework.data.mongodb.repository.MongoRepository;

import model.cab.Stop;


public interface StopRepository extends MongoRepository<Stop, String> {
    Stop findByCode(String code);
}
