package edu.neu.coe.csye6225.webapp.repositories;

import edu.neu.coe.csye6225.webapp.models.Pic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PicRepository extends JpaRepository<Pic, String> {
    Pic findByUserId(String userId);
}
