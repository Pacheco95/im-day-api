package br.com.uol.imdayapi.repository;

import br.com.uol.imdayapi.model.Schedule;
import br.com.uol.imdayapi.repository.extension.ScheduleRepositoryExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository
    extends JpaRepository<Schedule, Integer>, ScheduleRepositoryExtension {}
