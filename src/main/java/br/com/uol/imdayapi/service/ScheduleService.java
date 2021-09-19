package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    final Optional<User> nextUserToBeScheduledOptional = getNextUserToBeScheduled();
    final Optional<User> lastScheduledUserOptional = getLastScheduledUser();

    final boolean isDatabaseEmpty =
        lastScheduledUserOptional.isEmpty() && nextUserToBeScheduledOptional.isEmpty();

    if (isDatabaseEmpty) {
      return false;
    }

    if (lastScheduledUserOptional.isPresent()) {
      final Schedule lastSchedule =
          scheduleRepository.findAll(Sort.by(Sort.Order.desc("id"))).stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new ResponseStatusException(HttpStatus.NOT_FOUND, "Last schedule not found"));

      final LocalDate lastScheduleDate = lastSchedule.getScheduledAt().toLocalDate();

      final boolean lastScheduleDateIsToday = LocalDate.now(clock).equals(lastScheduleDate);

      return !lastScheduleDateIsToday;
    }

    return false;
  }
}
