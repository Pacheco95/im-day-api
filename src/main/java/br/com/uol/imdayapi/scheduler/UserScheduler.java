package br.com.uol.imdayapi.scheduler;

import br.com.uol.imdayapi.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class UserScheduler {

  private final ScheduleService scheduleService;

  @Scheduled(cron = "0 0 0 * * *")
  void scheduleNextUserAtTheStartOfNextDay() {

    if (scheduleService.canScheduleNextUser()) {
      scheduleService.scheduleNextUser();
    }
  }
}
