package br.com.uol.imdayapi.repository;

import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
  @Query(name = "Schedule.getLastScheduledUser", nativeQuery = true)
  Optional<User> getLastScheduledUser();
}
