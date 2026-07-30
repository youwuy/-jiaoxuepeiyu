package com.qizhifu.jiaoxuepeiyu.student.archive.repository;

import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveStep;
import com.qizhifu.jiaoxuepeiyu.student.archive.port.StudentArchiveRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisStudentArchiveRepository implements StudentArchiveRepository {

    private final StudentArchiveMapper mapper;

    public MyBatisStudentArchiveRepository(StudentArchiveMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<StudentTrainingArchive> findArchives(Long studentId, String mode, String keyword) {
        String keywordLike = keyword == null ? null : "%" + keyword + "%";
        return mapper.findArchives(studentId, mode, keywordLike);
    }

    @Override
    public Optional<StudentTrainingArchiveDetail> findArchiveDetail(Long studentId, Long archiveId) {
        return Optional.ofNullable(mapper.findArchiveDetail(studentId, archiveId));
    }

    @Override
    public List<StudentTrainingArchiveStep> findArchiveSteps(Long archiveId) {
        return mapper.findArchiveSteps(archiveId);
    }
}
