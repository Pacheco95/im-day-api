package br.com.uol.imdayapi.service;

import br.com.uol.imdayapi.model.User;
import br.com.uol.imdayapi.utils.DateTimeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterators;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/setup-scripts/truncate-database.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class ScheduleControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @Sql("/setup-scripts/should-get-recent-scheduled-users-successfully.sql")
  void shouldGetRecentScheduledUsersSuccessfully() throws Exception {
    final AtomicInteger idCounter = new AtomicInteger(0);

    final User michael = User.builder().id(idCounter.incrementAndGet()).name("Michael").build();
    final User ana = User.builder().id(idCounter.incrementAndGet()).name("Ana").build();
    final User carlos = User.builder().id(idCounter.incrementAndGet()).name("Carlos").build();

    final Iterator<User> userCycleIterator = Iterators.cycle(Arrays.asList(michael, ana, carlos));

    final LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);

    final List<Optional<User>> expectedRecentScheduledUsers =
        DateTimeUtils.getDateRange(yesterday, 11)
            .map(date -> DateTimeUtils.isWeekend(date) || date.equals(yesterday))
            .map(isWeekend -> Optional.ofNullable(isWeekend ? null : userCycleIterator.next()))
            .collect(Collectors.toUnmodifiableList());

    final String actualResponseJson =
        this.mockMvc
            .perform(get("/im-day"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

    final List<Optional<User>> actualRecentScheduledUsers =
        objectMapper.readValue(actualResponseJson, new TypeReference<>() {});

    Assertions.assertThat(actualRecentScheduledUsers).isEqualTo(expectedRecentScheduledUsers);
  }
}
