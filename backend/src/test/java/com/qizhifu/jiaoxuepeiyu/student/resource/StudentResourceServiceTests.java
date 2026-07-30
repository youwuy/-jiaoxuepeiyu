package com.qizhifu.jiaoxuepeiyu.student.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.qizhifu.jiaoxuepeiyu.student.resource.model.PublicResourceCard;
import com.qizhifu.jiaoxuepeiyu.student.resource.port.StudentResourceRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class StudentResourceServiceTests {

    @Test
    void normalizesBlankFiltersBeforeQueryingPublicResources() {
        FakeResources repository = new FakeResources();
        StudentResourceService service = new StudentResourceService(repository);

        service.listPublicResources("  ", " VIDEO ", 3L);

        assertNull(repository.keyword);
        assertEquals("VIDEO", repository.resourceType);
        assertEquals(3L, repository.majorId.longValue());
    }

    private static class FakeResources implements StudentResourceRepository {
        private String keyword;
        private String resourceType;
        private Long majorId;

        @Override
        public List<PublicResourceCard> findPublicResources(String keyword, String resourceType, Long majorId) {
            this.keyword = keyword;
            this.resourceType = resourceType;
            this.majorId = majorId;
            return Collections.emptyList();
        }
    }
}
