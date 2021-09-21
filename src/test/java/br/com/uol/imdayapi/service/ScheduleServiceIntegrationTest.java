package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.config.ClockConfiguration;
import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import br.com.uol.imdayapi.utils.DateTimeUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static br.com.uol.imdayapi.utils.DateTimeUtils.getDateRange;
import static br.com.uol.imdayapi.utils.DateTimeUtils.isWeekend;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(ClockConfiguration.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
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

    final Schedule currentScheduledUser = asScheduleRegistry(users.get(0));

    entityManager.persist(currentScheduledUser);

    goToPointInTime(startOfTodaySystem());

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
    goToPointInTime(startOfTodaySystem());

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

    goToPointInTime(startOfTodaySystem());

    assertThat(scheduleService.scheduleNextUser()).map(Schedule::getUser).hasValue(user);
  }

  @Test
  void
      whenCurrentScheduledUserIsNotTheLastDatabaseUser_Then_ScheduleNextUser_ShouldScheduleTheNextUserWhoseIdIsTheLowestIdGreaterThanTheCurrentScheduledUserId() {
    goToPointInTime(startOfYesterday());

    final User currentScheduledUser = User.builder().name("User 1").build();
    final User expectedNextScheduledUser = User.builder().name("User 2").build();

    Stream.of(currentScheduledUser, expectedNextScheduledUser).forEach(entityManager::persist);

    entityManager.persist(asScheduleRegistry(currentScheduledUser, clock));

    goToPointInTime(startOfTodaySystem());

    assertThat(scheduleService.scheduleNextUser())
        .map(Schedule::getUser)
        .hasValue(expectedNextScheduledUser);
  }

  @Test
  void getRecentScheduledUsersShouldReturnAListOf11Elements() {
    assertThat(scheduleService.getRecentScheduledUsers().size()).isEqualTo(11);
  }

  @Test
  void getRecentScheduledUsersShouldReturnAListWithAllValuesEmptyIfEmptyDatabase() {
    assertThat(scheduleService.getRecentScheduledUsers()).allMatch(Optional::isEmpty);
  }

  @Test
  void getRecentScheduledUsersShouldReturnAListWithEmptyValuesForDaysCorrespondingToWeekends() {
    entityManager.persist(new User());

    final List<Boolean> weekendsBool =
        getDateRange(yesterday(), 11)
            .map(DateTimeUtils::isWeekend)
            .collect(Collectors.toUnmodifiableList());

    final List<Boolean> scheduledUsersBool =
        scheduleService.getRecentScheduledUsers().stream()
            .map(Optional::isEmpty)
            .collect(Collectors.toUnmodifiableList());

    assertThat(weekendsBool).isEqualTo(scheduledUsersBool);
  }

  @Test
  void
      givenOnlyOneUserInDatabase_thenGetRecentScheduledUsersShouldReturnTheSameUserToAllCorrespondingWeekdaysSinceYesterdayOtherwiseEmpty() {
    final User luckyUser = generateUsersList(1).get(0);

    entityManager.persist(luckyUser);

    final List<Optional<User>> expectedRecentScheduledUsers =
        getDateRange(yesterday(), 11)
            .map(date -> Optional.ofNullable(isWeekend(date) ? null : luckyUser))
            .collect(Collectors.toUnmodifiableList());

    assertThat(scheduleService.getRecentScheduledUsers()).isEqualTo(expectedRecentScheduledUsers);
  }

  @Test
  void
      givenAnAlreadyScheduledUserYesterdayAndOnlyOneUserInDatabase_thenGetRecentScheduledUsersShouldReturnThisUserInTheFirstArrayPosition() {
    goToPointInTime(MONDAY);

    final User luckyUser = generateUsersList(1).get(0);

    entityManager.persist(luckyUser);
    entityManager.persist(asScheduleRegistry(luckyUser, clock));

    goToPointInTime(TUESDAY);

    assertThat(scheduleService.getRecentScheduledUsers().get(0)).hasValue(luckyUser);
  }

  @ParameterizedTest
  @EnumSource(DayOfWeek.class)
  void
      givenNoPreviousSchedulesAndMoreThanOneUserInDatabase_thenGetRecentScheduledUsersShouldCycleUsersInWeekdaysExceptYesterday(
          DayOfWeek dayOfWeek) {

    final LocalDate today = LocalDate.ofInstant(goToPointInTime(dayOfWeek), ZoneId.systemDefault());

    final List<User> users = generateUsersList(5);

    users.forEach(entityManager::persist);

    final Iterator<User> usersCyclicIterator = Iterators.cycle(users);

    final LocalDate yesterday = today.minus(1, ChronoUnit.DAYS);

    final var expectedRecentScheduledUsers =
        getDateRange(yesterday, 11)
            .map(
                date -> {
                  if (isWeekend(date) || date.equals(yesterday)) {
                    return Optional.empty();
                  }
                  return Optional.of(usersCyclicIterator.next());
                })
            .collect(Collectors.toUnmodifiableList());

    assertThat(scheduleService.getRecentScheduledUsers()).isEqualTo(expectedRecentScheduledUsers);
  }

  private Instant startOfTodaySystem() {
    return startOfToday(Clock.systemDefaultZone());
  }

  private Instant startOfToday(Clock clock) {
    return LocalDate.now(clock).atStartOfDay(ZoneId.systemDefault()).toInstant();
  }

  private LocalDate yesterday() {
    return yesterday(clock);
  }

  private LocalDate yesterday(Clock clock) {
    return LocalDate.now(clock).minus(1, ChronoUnit.DAYS);
  }

  private Instant startOfYesterday() {
    return startOfYesterday(clock);
  }

  private Instant startOfYesterday(Clock clock) {
    return yesterday(clock).atStartOfDay(ZoneId.systemDefault()).toInstant();
  }

  private Instant goToPointInTime(DayOfWeek dayOfWeek) {
    return goToPointInTime(dayOfWeek, clock);
  }

  private Instant goToPointInTime(DayOfWeek dayOfWeek, Clock clock) {
    return goToPointInTime(next(dayOfWeek), clock);
  }

  private Instant goToPointInTime(LocalDate date, Clock clock) {
    return goToPointInTime(date.atStartOfDay(ZoneId.systemDefault()).toInstant(), clock);
  }

  private void goToPointInTime(Instant instant) {
    goToPointInTime(instant, clock);
  }

  private Instant goToPointInTime(Instant instant, Clock clock) {
    final Clock newClock = Clock.fixed(instant, ZoneId.systemDefault());
    Mockito.doReturn(newClock.instant()).when(clock).instant();
    Mockito.doReturn(newClock.getZone()).when(clock).getZone();
    return instant;
  }

  private LocalDate next(DayOfWeek dayOfWeek) {
    return LocalDate.now(clock).with(TemporalAdjusters.next(dayOfWeek));
  }

  private List<User> generateUsersList(int count) {
    return IntStream.rangeClosed(1, count)
        .boxed()
        .map(userId -> User.builder().name("User " + userId).build())
        .collect(Collectors.toUnmodifiableList());
  }

  private Schedule asScheduleRegistry(User user) {
    return asScheduleRegistry(user, clock);
  }

  private Schedule asScheduleRegistry(User user, Clock clock) {
    return Schedule.builder().user(user).scheduledAt(LocalDateTime.now(clock)).build();
  }
}
