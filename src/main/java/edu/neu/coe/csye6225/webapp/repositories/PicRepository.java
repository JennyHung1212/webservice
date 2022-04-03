package edu.neu.coe.csye6225.webapp.repositories;

import edu.neu.coe.csye6225.webapp.models.Pic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Qualifier("pic")
@Repository
public interface PicRepository extends JpaRepository<Pic, String> {
    Pic findByUserId(String userId);
}
