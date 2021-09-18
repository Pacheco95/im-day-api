package br.com.uol.imdayapi.repository.extension.impl;

import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import br.com.uol.imdayapi.repository.extension.ScheduleRepositoryExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryExtensionImpl implements ScheduleRepositoryExtension {

  // Same as 'this'
  private ScheduleRepository repository;

  private final JdbcTemplate jdbcTemplate;

  @Override
  public Optional<User> getLastScheduledUser() {
    final List<User> result =
        jdbcTemplate.query(
            "SELECT u.* FROM users u JOIN schedule s USING(id) ORDER BY s.id DESC LIMIT 1",
            new BeanPropertyRowMapper<>(User.class));

    return Optional.ofNullable(DataAccessUtils.singleResult(result));
  }

  @Override
  public Optional<User> getNextUserToBeScheduled() {
    final List<User> result =
        jdbcTemplate.query(
            "SELECT * FROM users ORDER BY id LIMIT 1", new BeanPropertyRowMapper<>(User.class));

    return Optional.ofNullable(DataAccessUtils.singleResult(result));
  }

  @Autowired
  public void setRepository(ScheduleRepository repository) {
    this.repository = repository;
  }
}
