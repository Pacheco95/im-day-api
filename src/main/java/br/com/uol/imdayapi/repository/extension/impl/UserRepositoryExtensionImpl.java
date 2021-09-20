package br.com.uol.imdayapi.repository.extension.impl;

import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.UserRepository;
import br.com.uol.imdayapi.repository.extension.UserRepositoryExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryExtensionImpl implements UserRepositoryExtension {

  private UserRepository userRepository;

  @Override
  public Optional<User> getFirstCreatedUser() {
    return userRepository.findFirstByOrderById();
  }

  @Autowired
  @Lazy
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
}
