package edu.neu.coe.csye6225.webapp.repositories;

import edu.neu.coe.csye6225.webapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
