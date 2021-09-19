package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
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
    final Optional<User> optionalNextUserToBeScheduled = getNextUserToBeScheduled();
    final Optional<Schedule> optionalLastSchedule = scheduleRepository.getLastSchedule();

    final boolean isDatabaseEmpty =
        optionalLastSchedule.isEmpty() && optionalNextUserToBeScheduled.isEmpty();

    if (isDatabaseEmpty) {
      return false;
    }

    return !isLastScheduleDateToday(optionalLastSchedule);
  }

  private boolean isLastScheduleDateToday(Optional<Schedule> optionalLastSchedule) {
    if (optionalLastSchedule.isEmpty()) {
      return false;
    }

    final LocalDate lastScheduleDate = optionalLastSchedule.get().getScheduledAt().toLocalDate();

    return LocalDate.now(clock).equals(lastScheduleDate);
  }
}
