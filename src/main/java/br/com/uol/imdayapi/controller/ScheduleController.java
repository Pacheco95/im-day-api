package br.com.uol.imdayapi.controller;

import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("im-day")
@RequiredArgsConstructor
public class ScheduleController {
  private final ScheduleService scheduleService;

  @GetMapping
  public List<Optional<User>> listRecentScheduledUsers() {
    return scheduleService.getRecentScheduledUsers();
  }
}
