package com.qizhifu.jiaoxuepeiyu.admin.training.repository;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingOfflineImportBatch;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingOfflineScore;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingTopic;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminTrainingOfflineScoreMapper {

    @Select("SELECT tt.id AS topic_id, tt.topic_name, tt.training_mode, tt.score "
            + "FROM training_topic_binding tb JOIN training_topic tt ON tt.id = tb.topic_id "
            + "WHERE tb.training_id = #{trainingId} ORDER BY tb.sort_order ASC, tb.id ASC")
    List<AdminTrainingTopic> findTopics(@Param("trainingId") Long trainingId);

    @Select("SELECT u.id AS studentId, u.username AS studentNo, u.real_name AS studentName, "
            + "COALESCE(c.class_name, '') AS className FROM training_participant tp "
            + "JOIN sys_user u ON u.id = tp.student_id LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE tp.training_id = #{trainingId} AND u.username = #{studentNo} AND u.deleted_flag = 0 LIMIT 1")
    Map<String, Object> findParticipant(@Param("trainingId") Long trainingId, @Param("studentNo") String studentNo);

    @Insert("INSERT INTO training_offline_score_import_batch "
            + "(training_id, file_name, total_count, success_count, failure_count, imported_by, created_at) "
            + "VALUES (#{trainingId}, #{fileName}, #{totalCount}, 0, 0, #{importedBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "batchId")
    int insertBatch(AdminTrainingOfflineImportBatch batch);

    @Update("UPDATE training_offline_score_import_batch SET success_count = #{successCount}, failure_count = #{failureCount} "
            + "WHERE id = #{batchId}")
    int finishBatch(AdminTrainingOfflineImportBatch batch);

    @Insert("INSERT INTO training_offline_score "
            + "(training_id, student_id, student_no, student_name, class_name, total_score, remark, import_batch_id, created_at, updated_at) "
            + "VALUES (#{trainingId}, #{studentId}, #{studentNo}, #{studentName}, #{className}, #{totalScore}, #{remark}, #{importBatchId}, NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE student_no = VALUES(student_no), student_name = VALUES(student_name), "
            + "class_name = VALUES(class_name), total_score = VALUES(total_score), remark = VALUES(remark), "
            + "import_batch_id = VALUES(import_batch_id), updated_at = NOW()")
    int upsertScore(AdminTrainingOfflineScore score);

    @Select("SELECT id FROM training_offline_score WHERE training_id = #{trainingId} AND student_id = #{studentId}")
    Long findScoreId(@Param("trainingId") Long trainingId, @Param("studentId") Long studentId);

    @Select("SELECT id AS score_id, training_id, student_id, student_no, student_name, class_name, "
            + "total_score, remark, import_batch_id FROM training_offline_score "
            + "WHERE training_id = #{trainingId} ORDER BY class_name ASC, student_no ASC, id ASC")
    List<AdminTrainingOfflineScore> findScores(@Param("trainingId") Long trainingId);

    @org.apache.ibatis.annotations.Delete("DELETE FROM training_offline_topic_score WHERE offline_score_id = #{scoreId}")
    int deleteTopicScores(@Param("scoreId") Long scoreId);

    @Insert("INSERT INTO training_offline_topic_score (offline_score_id, topic_id, score) VALUES (#{scoreId}, #{topicId}, #{score})")
    int insertTopicScore(@Param("scoreId") Long scoreId, @Param("topicId") Long topicId, @Param("score") BigDecimal score);

    @Insert("INSERT INTO training_offline_score_import_error (batch_id, row_number, student_no, error_message, created_at) "
            + "VALUES (#{batchId}, #{rowNumber}, #{studentNo}, #{message}, NOW())")
    int insertError(@Param("batchId") Long batchId,
                    @Param("rowNumber") Integer rowNumber,
                    @Param("studentNo") String studentNo,
                    @Param("message") String message);
}
