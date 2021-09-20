package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;
  private final Clock clock;

  public Optional<User> getLastScheduledUser() {
    return scheduleRepository.getLastScheduledUser();
  }

  public Optional<User> getNextUserToBeScheduled() {
    return scheduleRepository.getNextUserToBeScheduled();
  }

  public boolean canScheduleNextUser() {
    final Optional<Schedule> optionalLastSchedule = scheduleRepository.getLastSchedule();

    final boolean isFirstSchedule = optionalLastSchedule.isEmpty();

    if (isFirstSchedule) {
      return true;
    }

    return !isLastScheduleDateToday(optionalLastSchedule.get());
  }

  private boolean isLastScheduleDateToday(Schedule optionalLastSchedule) {
    final LocalDate lastScheduleDate = optionalLastSchedule.getScheduledAt().toLocalDate();

    return LocalDate.now(clock).equals(lastScheduleDate);
  }

  public Optional<Schedule> scheduleNextUser() {
    final Optional<User> optionalNextUserToBeScheduled = getNextUserToBeScheduled();

    if (optionalNextUserToBeScheduled.isEmpty()) {
      return Optional.empty();
    }

    final User nextUserToBeScheduled = optionalNextUserToBeScheduled.get();

    if (!canScheduleNextUser()) {
      return Optional.empty();
    }

    return Optional.of(scheduleRepository.scheduleUser(nextUserToBeScheduled));
  }

  public List<User> getRecentScheduledUsers() {
    return scheduleRepository.getRecentScheduledUsers();
  }
}
