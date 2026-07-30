package com.qizhifu.jiaoxuepeiyu.student.score.repository;

import com.qizhifu.jiaoxuepeiyu.student.score.model.StudentSemesterScore;
import com.qizhifu.jiaoxuepeiyu.student.score.port.StudentScoreRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisStudentScoreRepository implements StudentScoreRepository {

    private final StudentScoreMapper mapper;

    public MyBatisStudentScoreRepository(StudentScoreMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<StudentSemesterScore> findSemesterScores(Long studentId) {
        return mapper.findSemesterScores(studentId);
    }
}
