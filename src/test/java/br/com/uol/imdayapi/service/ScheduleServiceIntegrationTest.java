package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
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

  @Test
  void getLastScheduledUserShouldReturnLastScheduledUser() {
    final List<User> users = generateUsersList(3);

    users.forEach(entityManager::persist);

    users.stream().map(this::asScheduleRegistry).forEach(entityManager::persist);

    final User expectedLastScheduledUser = Iterables.getLast(users);

    final Optional<User> actualLastScheduledUser = scheduleService.getLastScheduledUser();

    assertThat(actualLastScheduledUser).hasValue(expectedLastScheduledUser);
  }

  @Test
  void getNextUserToBeScheduledShouldReturnEmptyOptionalIfNoUsersInDatabase() {
    entityManager.clear();

    final Optional<User> actualNextUserToBeScheduled = scheduleService.getNextUserToBeScheduled();

    assertThat(actualNextUserToBeScheduled).isEmpty();
  }

  @Test
  void getNextUserToBeScheduledShouldReturnTheFirstCreatedUserIfNoScheduledUsersYet() {
    final List<User> users = generateUsersList(3);

    users.forEach(entityManager::persist);

    final User firstCreatedUser = users.get(0);

    final Optional<User> actualNextUserToBeScheduled = scheduleService.getNextUserToBeScheduled();

    assertThat(actualNextUserToBeScheduled).hasValue(firstCreatedUser);
  }

  private List<User> generateUsersList(int count) {
    return IntStream.rangeClosed(1, count)
        .boxed()
        .map(userId -> User.builder().name("User " + userId).build())
        .collect(Collectors.toUnmodifiableList());
  }

  private Schedule asScheduleRegistry(User user) {
    return Schedule.builder().user(user).scheduledAt(LocalDateTime.now()).build();
  }
}
