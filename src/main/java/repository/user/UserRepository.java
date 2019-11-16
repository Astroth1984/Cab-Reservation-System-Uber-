package repository.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import model.user.User;


public interface UserRepository extends MongoRepository<User, String> {

    User findByEmail(String email);

}
