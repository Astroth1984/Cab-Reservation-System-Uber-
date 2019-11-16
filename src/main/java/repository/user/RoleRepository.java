package repository.user;



import org.springframework.data.mongodb.repository.MongoRepository;

import model.user.Role;


public interface RoleRepository extends MongoRepository<Role, String> {

    Role findByRole(String role);

}
