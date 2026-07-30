package com.qizhifu.jiaoxuepeiyu.admin.score.port;

import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScore;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreQuery;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreStatistics;
import java.util.List;

public interface AdminSemesterScoreRepository {

    List<AdminSemesterScore> findScores(AdminSemesterScoreQuery query);

    long countScores(AdminSemesterScoreQuery query);

    AdminSemesterScoreStatistics calculateStatistics(AdminSemesterScoreQuery query);

    List<AdminSemesterScore> findRanking(AdminSemesterScoreQuery query);

    Long findStudentIdByStudentNo(String studentNo);

    boolean semesterExists(Long semesterId);

    void upsertScores(List<AdminSemesterScoreImportRow> rows);
}
