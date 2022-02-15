package edu.neu.coe.csye6225.webapp.repositories;

import edu.neu.coe.csye6225.webapp.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, String> {
    UserModel findByUsername(String username);
}
