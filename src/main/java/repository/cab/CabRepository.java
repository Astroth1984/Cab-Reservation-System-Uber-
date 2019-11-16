package repository.cab;

import org.springframework.data.mongodb.repository.MongoRepository;

import model.cab.Agency;
import model.cab.Cab;


public interface CabRepository extends MongoRepository<Cab, String> {
    Cab findByCode(String cabCode);

    Cab findByCodeAndAgency(String cabCode, Agency agency);
}
