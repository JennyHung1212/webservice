package edu.neu.coe.csye6225.webapp.repositories;

import edu.neu.coe.csye6225.webapp.models.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Qualifier("user")
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
    @Transactional
    Long deleteByUsername(String username);
}
