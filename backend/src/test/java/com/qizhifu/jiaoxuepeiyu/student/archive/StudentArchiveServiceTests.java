package com.qizhifu.jiaoxuepeiyu.student.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveStep;
import com.qizhifu.jiaoxuepeiyu.student.archive.port.StudentArchiveRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StudentArchiveServiceTests {

    @Test
    void normalizesFiltersWhenListingArchives() {
        FakeArchives repository = new FakeArchives();
        StudentArchiveService service = new StudentArchiveService(repository);

        List<StudentTrainingArchive> archives = service.listArchives(7L, " TEAM ", "  emergency ");

        assertEquals(1, archives.size());
        assertEquals("TEAM", repository.mode);
        assertEquals("emergency", repository.keyword);
        assertEquals("Emergency Handling", archives.get(0).getTrainingName());
    }

    @Test
    void returnsArchiveDetailWithStepsForCurrentStudent() {
        StudentArchiveService service = new StudentArchiveService(new FakeArchives());

        StudentTrainingArchiveDetail detail = service.getArchiveDetail(7L, 31L);

        assertEquals("Emergency Handling", detail.getTrainingName());
        assertEquals("Dispatcher", detail.getRoleName());
        assertEquals(2, detail.getSteps().size());
        assertEquals("Confirm alarm", detail.getSteps().get(0).getStepName());
    }

    private static class FakeArchives implements StudentArchiveRepository {
        private String mode;
        private String keyword;

        @Override
        public List<StudentTrainingArchive> findArchives(Long studentId, String mode, String keyword) {
            this.mode = mode;
            this.keyword = keyword;
            StudentTrainingArchive archive = new StudentTrainingArchive();
            archive.setArchiveId(31L);
            archive.setTrainingName("Emergency Handling");
            archive.setTrainingMode("TEAM");
            archive.setRoleName("Dispatcher");
            archive.setPersonalScore(new BigDecimal("88.5"));
            archive.setTeamScore(new BigDecimal("92.0"));
            return Arrays.asList(archive);
        }

        @Override
        public Optional<StudentTrainingArchiveDetail> findArchiveDetail(Long studentId, Long archiveId) {
            StudentTrainingArchiveDetail detail = new StudentTrainingArchiveDetail();
            detail.setArchiveId(archiveId);
            detail.setTrainingName("Emergency Handling");
            detail.setTrainingMode("TEAM");
            detail.setRoleName("Dispatcher");
            detail.setStudentName("Chen Student");
            return Optional.of(detail);
        }

        @Override
        public List<StudentTrainingArchiveStep> findArchiveSteps(Long archiveId) {
            StudentTrainingArchiveStep first = new StudentTrainingArchiveStep();
            first.setStepId(1L);
            first.setStepName("Confirm alarm");
            StudentTrainingArchiveStep second = new StudentTrainingArchiveStep();
            second.setStepId(2L);
            second.setStepName("Dispatch team");
            return Arrays.asList(first, second);
        }
    }
}
