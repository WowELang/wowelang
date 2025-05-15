package org.example.wowelang_backend.report.repository;

import org.example.wowelang_backend.report.domain.ReportCategory;
import org.example.wowelang_backend.report.domain.ReportCategoryName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Long> {

    Optional<ReportCategory> findByReportCategoryName(ReportCategoryName reportCategoryName);
}
