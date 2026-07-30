package com.qizhifu.jiaoxuepeiyu.student.score.repository;

import com.qizhifu.jiaoxuepeiyu.student.score.model.StudentSemesterScore;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentScoreMapper {

    @Select("SELECT CONCAT(y.year_name, ' ', s.semester_name) AS academic_term, "
            + "ss.courseware_learning_score, ss.training_practice_score, "
            + "ss.course_assignment_score, ss.exam_score, "
            + "ss.courseware_weight, ss.training_practice_weight, "
            + "ss.assignment_weight, ss.exam_weight, ss.comprehensive_score "
            + "FROM score_semester_summary ss "
            + "JOIN edu_semester s ON s.id = ss.semester_id "
            + "JOIN edu_academic_year y ON y.id = s.academic_year_id "
            + "WHERE ss.student_id = #{studentId} "
            + "ORDER BY y.year_name DESC, s.id DESC")
    List<StudentSemesterScore> findSemesterScores(@Param("studentId") Long studentId);
}
