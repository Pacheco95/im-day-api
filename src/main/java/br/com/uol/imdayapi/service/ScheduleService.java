package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;

  public Optional<User> getLastScheduledUser() {
    return scheduleRepository.getLastScheduledUser();
  }

  public Optional<User> getNextUserToBeScheduled() {
    return scheduleRepository.getNextUserToBeScheduled();
  }

  public boolean canScheduleNextUser() {
    final Optional<User> nextUserToBeScheduled = getNextUserToBeScheduled();

    final boolean isDatabaseEmpty =
        getLastScheduledUser().isEmpty() && nextUserToBeScheduled.isEmpty();

    if (isDatabaseEmpty) {
      return false;
    }

    return false;
  }
}
