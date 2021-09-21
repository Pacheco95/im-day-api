package br.com.uol.imdayapi.repository.extension.impl;

import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import br.com.uol.imdayapi.repository.UserRepository;
import br.com.uol.imdayapi.repository.extension.ScheduleRepositoryExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static br.com.uol.imdayapi.utils.DateTimeUtils.getDateRange;
import static br.com.uol.imdayapi.utils.DateTimeUtils.isWeekend;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryExtensionImpl implements ScheduleRepositoryExtension {

  private final UserRepository userRepository;
  private final JdbcTemplate jdbcTemplate;
  private final Clock clock;
  private static final int SCHEDULES_COUNT = 11;
  private ScheduleRepository scheduleRepository;

  @Override
  public Optional<User> getLastScheduledUser() {
    final Optional<Schedule> lastSchedule = scheduleRepository.getLastSchedule();

    if (lastSchedule.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(lastSchedule.get().getUser());
  }

  @Override
  public Optional<User> getNextUserToBeScheduled() {
    final Optional<Schedule> optionalLastSchedule = scheduleRepository.getLastSchedule();
    final Optional<User> optionalFirstCreatedUser = userRepository.getFirstCreatedUser();

    final boolean isDatabaseEmpty =
        optionalLastSchedule.isEmpty() && optionalFirstCreatedUser.isEmpty();

    if (isDatabaseEmpty) {
      return Optional.empty();
    }

    final boolean isTheFirstSchedule = optionalLastSchedule.isEmpty();

    if (isTheFirstSchedule) {
      return optionalFirstCreatedUser;
    }

    final Optional<Integer> nextUserIdToBeScheduled = getNextUserIdToBeScheduled();

    if (nextUserIdToBeScheduled.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(userRepository.getById(nextUserIdToBeScheduled.get()));
  }

  private Optional<Integer> getNextUserIdToBeScheduled() {
    final String query =
        "WITH scheduled_user AS (\n"
            + "    SELECT user_id\n"
            + "    FROM schedule\n"
            + "    ORDER BY scheduled_at DESC\n"
            + "    LIMIT 1\n"
            + "),\n"
            + "     last_user AS (\n"
            + "         SELECT id\n"
            + "         FROM users\n"
            + "         ORDER BY id DESC\n"
            + "         LIMIT 1\n"
            + "     )\n"
            + "SELECT CASE\n"
            + "           WHEN ((SELECT user_id FROM scheduled_user) = (SELECT id FROM last_user))\n"
            + "               THEN (SELECT id FROM users ORDER BY id LIMIT 1)\n"
            + "           ELSE (SELECT user_id_to_be_scheduled\n"
            + "                 FROM (SELECT u.id AS user_id_to_be_scheduled\n"
            + "                       FROM users u\n"
            + "                       WHERE u.id > (SELECT user_id FROM scheduled_user)\n"
            + "                       LIMIT 1) AS suilui) END;";

    return jdbcTemplate.queryForList(query, Integer.class).stream().findFirst();
  }

  @Override
  public Schedule scheduleUser(User nextUserToBeScheduled) {
    return scheduleRepository.save(
        Schedule.builder()
            .user(nextUserToBeScheduled)
            .scheduledAt(LocalDateTime.now(clock))
            .build());
  }

  @Override
  public List<Optional<User>> getRecentScheduledUsers() {
    final Optional<User> optionalFirstCreatedUser = userRepository.getFirstCreatedUser();

    if (optionalFirstCreatedUser.isEmpty()) {
      return IntStream.range(0, SCHEDULES_COUNT)
          .mapToObj(ignored -> Optional.<User>empty())
          .collect(Collectors.toUnmodifiableList());
    }

    LocalDate yesterday = LocalDate.now(clock).minus(1, ChronoUnit.DAYS);

    return getDateRange(yesterday, 11)
        .map(date -> Optional.ofNullable(isWeekend(date) ? null : optionalFirstCreatedUser.get()))
        .collect(Collectors.toUnmodifiableList());
  }

  @Autowired
  @Lazy
  public void setScheduleRepository(ScheduleRepository scheduleRepository) {
    this.scheduleRepository = scheduleRepository;
  }
}
