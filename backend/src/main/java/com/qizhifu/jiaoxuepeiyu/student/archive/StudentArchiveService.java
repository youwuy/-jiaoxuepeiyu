package com.qizhifu.jiaoxuepeiyu.student.archive;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.student.archive.port.StudentArchiveRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudentArchiveService {

    private final StudentArchiveRepository repository;

    public StudentArchiveService(StudentArchiveRepository repository) {
        this.repository = repository;
    }

    public List<StudentTrainingArchive> listArchives(Long studentId, String mode, String keyword) {
        return repository.findArchives(studentId, normalize(mode), normalize(keyword));
    }

    public StudentTrainingArchiveDetail getArchiveDetail(Long studentId, Long archiveId) {
        StudentTrainingArchiveDetail detail = repository.findArchiveDetail(studentId, archiveId)
                .orElseThrow(() -> new BusinessException(404, "Training archive not found"));
        detail.setSteps(repository.findArchiveSteps(archiveId));
        return detail;
    }

    private String normalize(String value) {
        return value == null || value.trim().length() == 0 ? null : value.trim();
    }
}
