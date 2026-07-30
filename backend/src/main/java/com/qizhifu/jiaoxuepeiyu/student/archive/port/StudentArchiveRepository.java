package com.qizhifu.jiaoxuepeiyu.student.archive.port;

import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveStep;
import java.util.List;
import java.util.Optional;

public interface StudentArchiveRepository {

    List<StudentTrainingArchive> findArchives(Long studentId, String mode, String keyword);

    Optional<StudentTrainingArchiveDetail> findArchiveDetail(Long studentId, Long archiveId);

    List<StudentTrainingArchiveStep> findArchiveSteps(Long archiveId);
}
