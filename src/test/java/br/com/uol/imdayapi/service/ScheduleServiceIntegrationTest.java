package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.config.ClockConfiguration;
import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(ClockConfiguration.class)
class ScheduleServiceIntegrationTest {

  @MockBean private Clock clock;

  @Autowired private TestEntityManager entityManager;
  @Autowired private ScheduleRepository scheduleRepository;

  private ScheduleService scheduleService;

  @BeforeEach
  void setUp() {
    scheduleService = new ScheduleService(scheduleRepository, clock);

    Mockito.doReturn(Clock.systemDefaultZone().instant()).when(this.clock).instant();
    Mockito.doReturn(Clock.systemDefaultZone().getZone()).when(this.clock).getZone();
  }

  @Test
  void getLastScheduledUserShouldReturnEmptyOptionalOnEmptyDatabase() {
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

  @Test
  void canScheduleNextUserShouldReturnFalseIfDatabaseIsEmpty() {
    assertThat(scheduleService.canScheduleNextUser()).isFalse();
  }

  @Test
  void canScheduleNextUserShouldReturnFalseIfThereWasAlreadyAScheduledUserToday() {
    final List<User> users = generateUsersList(1);

    users.forEach(entityManager::persist);

    final Schedule currentScheduledUser = asScheduleRegistry(users.get(0));

    entityManager.persist(currentScheduledUser);

    final boolean canBeScheduled = scheduleService.canScheduleNextUser();

    assertThat(canBeScheduled).isFalse();
  }

  @Test
  void canScheduleNextUserShouldReturnTrueIfNoScheduledUserTodayAndThereIsAUserToBeScheduled() {
    goToPointInTime(startOfYesterday());

    final List<User> users = generateUsersList(2);

    users.forEach(entityManager::persist);

    final Schedule currentScheduledUser = asScheduleRegistry(users.get(0), clock);

    entityManager.persist(currentScheduledUser);

    goToPointInTime(startOfToday());

    assertThat(scheduleService.canScheduleNextUser()).isTrue();
  }

  @Test
  void scheduleNextUserShouldReturnEmptyOptionalIfNoUsersToSchedule() {
    assertThat(scheduleService.scheduleNextUser()).isEmpty();
  }

  @Test
  void scheduleNextUserShouldScheduleTheFirstUserInUsersTableIfNoPreviousSchedules() {
    final List<User> users = generateUsersList(3);

    users.forEach(entityManager::persist);

    assertThat(scheduleService.scheduleNextUser()).map(Schedule::getUser).hasValue(users.get(0));
  }

  @Test
  void scheduleNextUserShouldReturnEmptyOptionalIfTodayScheduleIsAlreadyDone() {
    goToPointInTime(startOfToday());

    final List<User> users = generateUsersList(3);

    users.forEach(entityManager::persist);

    entityManager.persist(asScheduleRegistry(users.get(0), clock));

    assertThat(scheduleService.scheduleNextUser()).isEmpty();
  }

  @Test
  void scheduleNextUserShouldScheduleTheSameUserAsBeforeIfOnlyOneUserInDatabase() {
    goToPointInTime(startOfYesterday());

    final User user = generateUsersList(1).get(0);

    entityManager.persist(user);

    entityManager.persist(asScheduleRegistry(user, clock));

    goToPointInTime(startOfToday());

    assertThat(scheduleService.scheduleNextUser()).map(Schedule::getUser).hasValue(user);
  }

  private Instant startOfToday() {
    return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
  }

  private Instant startOfYesterday() {
    return LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .minus(1, ChronoUnit.DAYS)
        .toInstant();
  }

  private List<User> generateUsersList(int count) {
    return IntStream.rangeClosed(1, count)
        .boxed()
        .map(userId -> User.builder().name("User " + userId).build())
        .collect(Collectors.toUnmodifiableList());
  }

  private Schedule asScheduleRegistry(User user) {
    return asScheduleRegistry(user, Clock.systemDefaultZone());
  }

  private Schedule asScheduleRegistry(User user, Clock clock) {
    return Schedule.builder().user(user).scheduledAt(LocalDateTime.now(clock)).build();
  }

  private void goToPointInTime(Instant instant) {
    final Clock newClock = Clock.fixed(instant, ZoneId.systemDefault());
    Mockito.doReturn(newClock.instant()).when(clock).instant();
    Mockito.doReturn(newClock.getZone()).when(clock).getZone();
  }
}
