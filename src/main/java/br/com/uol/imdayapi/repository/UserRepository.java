package br.com.uol.imdayapi.repository;

import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.repository.extension.UserRepositoryExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, UserRepositoryExtension {
  Optional<User> findFirstByOrderById();
}
