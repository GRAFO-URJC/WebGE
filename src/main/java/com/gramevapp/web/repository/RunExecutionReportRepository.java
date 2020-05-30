package com.gramevapp.web.repository;

import com.gramevapp.web.model.RunExecutionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RunExecutionReportRepository extends JpaRepository<RunExecutionReport, Long> {
}