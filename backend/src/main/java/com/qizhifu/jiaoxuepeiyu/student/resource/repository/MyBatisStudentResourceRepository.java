package com.qizhifu.jiaoxuepeiyu.student.resource.repository;

import com.qizhifu.jiaoxuepeiyu.student.resource.model.PublicResourceCard;
import com.qizhifu.jiaoxuepeiyu.student.resource.port.StudentResourceRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisStudentResourceRepository implements StudentResourceRepository {

    private final StudentResourceMapper mapper;

    public MyBatisStudentResourceRepository(StudentResourceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<PublicResourceCard> findPublicResources(String keyword, String resourceType, Long majorId) {
        String keywordLike = keyword == null ? null : "%" + keyword + "%";
        return mapper.findPublicResources(keywordLike, resourceType, majorId);
    }
}
