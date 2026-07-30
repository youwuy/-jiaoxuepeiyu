package com.qizhifu.jiaoxuepeiyu.admin.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveQuery;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStep;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.archive.port.AdminTrainingArchiveRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminTrainingArchiveServiceTests {

    @Test
    void listsArchivesWithPagingDefaults() {
        FakeArchives repository = new FakeArchives();
        repository.archives = Arrays.asList(new AdminTrainingArchive());
        AdminTrainingArchiveService service = new AdminTrainingArchiveService(repository);

        assertEquals(1, service.listArchives(new AdminTrainingArchiveQuery()).getRecords().size());
        assertEquals(1, repository.lastQuery.getPage());
        assertEquals(20, repository.lastQuery.getPageSize());
    }

    @Test
    void returnsDetailWithSteps() {
        FakeArchives repository = new FakeArchives();
        repository.detail = new AdminTrainingArchiveDetail();
        repository.detail.setArchiveId(7L);
        repository.steps = Arrays.asList(new AdminTrainingArchiveStep());
        AdminTrainingArchiveService service = new AdminTrainingArchiveService(repository);

        AdminTrainingArchiveDetail detail = service.getArchiveDetail(7L);

        assertEquals(7L, detail.getArchiveId().longValue());
        assertEquals(1, detail.getSteps().size());
    }

    private static class FakeArchives implements AdminTrainingArchiveRepository {
        private AdminTrainingArchiveQuery lastQuery;
        private List<AdminTrainingArchive> archives = new ArrayList<AdminTrainingArchive>();
        private AdminTrainingArchiveDetail detail;
        private List<AdminTrainingArchiveStep> steps = new ArrayList<AdminTrainingArchiveStep>();

        @Override
        public List<AdminTrainingArchive> findArchives(AdminTrainingArchiveQuery query) {
            this.lastQuery = query;
            return archives;
        }

        @Override
        public long countArchives(AdminTrainingArchiveQuery query) {
            return archives.size();
        }

        @Override
        public AdminTrainingArchiveDetail findArchiveDetail(Long archiveId) {
            return detail;
        }

        @Override
        public List<AdminTrainingArchiveStep> findArchiveSteps(Long archiveId) {
            return steps;
        }

        @Override
        public AdminTrainingArchiveStatistics calculateStatistics(AdminTrainingArchiveQuery query) {
            this.lastQuery = query;
            return null;
        }
    }
}
