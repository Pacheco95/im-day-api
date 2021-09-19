package br.com.uol.imdayapi.repository.extension.impl;

import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import br.com.uol.imdayapi.repository.extension.ScheduleRepositoryExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryExtensionImpl implements ScheduleRepositoryExtension {

  private ScheduleRepository scheduleRepository;

  private final JdbcTemplate jdbcTemplate;

  @Override
  public Optional<User> getLastScheduledUser() {
    final Optional<Schedule> lastSchedule = this.getLastSchedule();

    if (lastSchedule.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(lastSchedule.get().getUser());
  }

  @Override
  public Optional<Schedule> getLastSchedule() {
    return scheduleRepository
        .findAll(PageRequest.of(0, 1, Sort.by(Schedule.Fields.scheduledAt).descending()))
        .stream()
        .findFirst();
  }

  @Override
  public Optional<User> getNextUserToBeScheduled() {
    final List<User> result =
        jdbcTemplate.query(
            "SELECT * FROM users ORDER BY id LIMIT 1", new BeanPropertyRowMapper<>(User.class));

    return Optional.ofNullable(DataAccessUtils.singleResult(result));
  }

  @Autowired
  @Lazy
  public void setScheduleRepository(ScheduleRepository scheduleRepository) {
    this.scheduleRepository = scheduleRepository;
  }
}
