package br.com.uol.imdayapi.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {
  public static boolean isWeekend(LocalDate date) {
    return EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek());
  }

  public static Stream<LocalDate> getDateRange(LocalDate startDate, int nDays) {
    return IntStream.range(0, nDays).mapToObj(offset -> startDate.plus(offset, ChronoUnit.DAYS));
  }
}
