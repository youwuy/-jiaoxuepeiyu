package com.qizhifu.jiaoxuepeiyu.admin.training.repository;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingTopic;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminTrainingTopicMapper {
    @Select("<script>SELECT id AS topic_id, topic_name, category, training_mode, duration_minutes, score, role_names "
            + "FROM training_topic WHERE enabled_flag = 1 AND deleted_flag = 0 "
            + "<if test='keyword != null'>AND topic_name LIKE #{keyword}</if> "
            + "<if test='trainingMode != null'>AND training_mode = #{trainingMode}</if> "
            + "ORDER BY updated_at DESC, id DESC</script>")
    List<AdminTrainingTopic> findTopics(@Param("keyword") String keyword,
                                        @Param("trainingMode") String trainingMode);
}
