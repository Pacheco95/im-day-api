package br.com.uol.imdayapi.scheduler;

import br.com.uol.imdayapi.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class UserScheduler {

  private final AtomicInteger count = new AtomicInteger(0);
  private final ScheduleService scheduleService;

  @Scheduled(cron = "0 0 * * *")
  void scheduleNextUserAtTheStartOfNextDay() {
    count.incrementAndGet();

    if (scheduleService.canScheduleNextUser()) {
      scheduleService.scheduleNextUser();
    }
  }

  public int getInvocationCount() {
    return this.count.get();
  }
}
