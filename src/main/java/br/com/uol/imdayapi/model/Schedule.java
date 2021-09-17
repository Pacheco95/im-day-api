package br.com.uol.imdayapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedNativeQuery(
    name = "Schedule.getLastScheduledUser",
    query = "SELECT u.* FROM users u JOIN schedule s USING(id) ORDER BY s.id DESC LIMIT 1",
    resultClass = User.class)
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(foreignKey = @ForeignKey(name = "one_user_has_many_schedules"))
  private User user;

  private LocalDateTime scheduledAt;
}
