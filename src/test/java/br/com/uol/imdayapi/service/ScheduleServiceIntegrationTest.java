package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
class ScheduleServiceIntegrationTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private ScheduleRepository scheduleRepository;

  private ScheduleService scheduleService;

  @BeforeEach
  void setUp() {
    scheduleService = new ScheduleService(scheduleRepository);
    entityManager.clear();
  }

  @Test
  void getLastScheduledUserShouldReturnEmptyOptionalOnEmptyDatabase() {
    entityManager.clear();

    final Optional<User> lastScheduledUser = scheduleService.getLastScheduledUser();

    assertThat(lastScheduledUser).isNotPresent();
  }
}
