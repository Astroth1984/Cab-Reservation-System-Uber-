package repository.cab;

import org.springframework.data.mongodb.repository.MongoRepository;

import model.cab.Agency;
import model.user.User;


public interface AgencyRepository extends MongoRepository<Agency, String> {
    Agency findByCode(String agencyCode);

    Agency findByOwner(User owner);

    Agency findByName(String name);
}
