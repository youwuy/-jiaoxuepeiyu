package com.qizhifu.jiaoxuepeiyu.student.score.port;

import com.qizhifu.jiaoxuepeiyu.student.score.model.StudentSemesterScore;
import java.util.List;

public interface StudentScoreRepository {

    List<StudentSemesterScore> findSemesterScores(Long studentId);
}
