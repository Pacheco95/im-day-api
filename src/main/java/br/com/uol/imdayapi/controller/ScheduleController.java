package br.com.uol.imdayapi.controller;

import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.service.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Api(tags = "Schedule")
@RestController
@RequestMapping("im-day")
@RequiredArgsConstructor
public class ScheduleController {
  private final ScheduleService scheduleService;

  @ApiOperation(value = "Returns a list of scheduled users from yesterday to 11 days ahead")
  @GetMapping
  public List<Optional<User>> listRecentScheduledUsers() {
    return scheduleService.getRecentScheduledUsers();
  }
}
