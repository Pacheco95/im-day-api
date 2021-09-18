package br.com.uol.imdayapi.repository.extension;

import br.com.uol.imdayapi.model.User;

import java.util.Optional;

public interface ScheduleRepositoryExtension {
  Optional<User> getLastScheduledUser();

  Optional<User> getNextUserToBeScheduled();
}
