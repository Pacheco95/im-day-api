package br.com.uol.imdayapi.repository.extension;

import br.com.uol.imdayapi.model.User;

import java.util.Optional;

public interface UserRepositoryExtension {
  Optional<User> getFirstCreatedUser();
}
