package com.qizhifu.jiaoxuepeiyu.admin.config.repository;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYear;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClass;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajor;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminSemester;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminEducationConfigMapper {

    @Select("SELECT id AS academic_year_id, year_name FROM edu_academic_year ORDER BY year_name DESC, id DESC")
    List<AdminAcademicYear> findAcademicYears();

    @Select("SELECT id AS semester_id, academic_year_id, semester_name, "
            + "CASE WHEN current_flag = 1 THEN TRUE ELSE FALSE END AS current "
            + "FROM edu_semester ORDER BY academic_year_id DESC, id ASC")
    List<AdminSemester> findSemesters();

    @Insert("INSERT INTO edu_academic_year (year_name, created_at) VALUES (#{yearName}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "academicYearId")
    void insertAcademicYear(AdminAcademicYear year);

    @Insert("INSERT INTO edu_semester (academic_year_id, semester_name, current_flag, created_at) "
            + "VALUES (#{academicYearId}, #{semesterName}, 0, NOW())")
    void insertSemester(@Param("academicYearId") Long academicYearId, @Param("semesterName") String semesterName);

    @Update("UPDATE edu_semester SET current_flag = 0 WHERE current_flag = 1")
    void clearCurrentSemesters();

    @Update("UPDATE edu_semester SET current_flag = 1 WHERE id = #{semesterId}")
    void markCurrentSemester(@Param("semesterId") Long semesterId);

    @Select("SELECT id AS major_id, major_name, "
            + "CASE WHEN status = 1 THEN TRUE ELSE FALSE END AS enabled "
            + "FROM edu_major ORDER BY major_name ASC, id ASC")
    List<AdminMajor> findMajors();

    @Insert("INSERT INTO edu_major (major_name, status, created_at, updated_at) "
            + "VALUES (#{majorName}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "majorId")
    void insertMajor(AdminMajor major);

    @Update("UPDATE edu_major SET status = #{status}, updated_at = NOW() WHERE id = #{majorId}")
    void updateMajorStatus(@Param("majorId") Long majorId, @Param("status") int status);

    @Select("<script>"
            + "SELECT c.id AS class_id, c.major_id, m.major_name, c.class_name, "
            + "CASE WHEN c.status = 1 THEN TRUE ELSE FALSE END AS enabled "
            + "FROM edu_class c JOIN edu_major m ON m.id = c.major_id "
            + "WHERE 1 = 1 "
            + "<if test='majorId != null'>AND c.major_id = #{majorId}</if> "
            + "ORDER BY m.major_name ASC, c.class_name ASC, c.id ASC "
            + "</script>")
    List<AdminClass> findClasses(@Param("majorId") Long majorId);

    @Insert("INSERT INTO edu_class (major_id, class_name, status, created_at, updated_at) "
            + "VALUES (#{majorId}, #{className}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "classId")
    void insertClass(AdminClass adminClass);
}
