package com.qizhifu.jiaoxuepeiyu.student.training;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.student.archive.StudentArchiveService;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.ue.UeTrainingCallbackService;
import org.springframework.stereotype.Service;

@Service
public class StudentTrainingAttemptService {

    private final UeTrainingCallbackService callbackService;
    private final StudentArchiveService archiveService;

    public StudentTrainingAttemptService(UeTrainingCallbackService callbackService,
                                         StudentArchiveService archiveService) {
        this.callbackService = callbackService;
        this.archiveService = archiveService;
    }

    public Long submitAttempt(Long studentId, StudentTrainingAttemptRequest request) {
        if (request == null || request.getTrainingId() == null) {
            throw new BusinessException(400, "Training id is required");
        }
        return callbackService.submitAttempt(studentId, request.getTrainingId(), request);
    }

    public StudentTrainingArchiveDetail getScoreSheet(Long studentId, Long attemptId) {
        return archiveService.getArchiveDetail(studentId, attemptId);
    }
}
