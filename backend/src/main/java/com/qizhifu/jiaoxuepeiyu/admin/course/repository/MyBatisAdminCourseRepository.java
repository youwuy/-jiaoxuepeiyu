package com.qizhifu.jiaoxuepeiyu.admin.course.repository;

import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourse;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseChapter;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseChapterCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseContent;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseContentCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseLog;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentStatisticsQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.port.AdminCourseRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminCourseRepository implements AdminCourseRepository {

    private final AdminCourseMapper mapper;

    public MyBatisAdminCourseRepository(AdminCourseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminCourse> findCourses(AdminCourseQuery query) {
        return mapper.findCourses(likeQuery(query));
    }

    @Override
    public long countCourses(AdminCourseQuery query) {
        return mapper.countCourses(likeQuery(query));
    }

    @Override
    public AdminCourse findCourse(Long courseId) {
        return mapper.findCourse(courseId);
    }

    @Override
    public Long createCourse(AdminCourseCommand command, Long creatorId) {
        AdminCourse course = toCourse(null, command, creatorId);
        mapper.insertCourse(course);
        replaceBindings(course.getCourseId(), command);
        return course.getCourseId();
    }

    @Override
    public void updateCourse(Long courseId, AdminCourseCommand command) {
        AdminCourse existing = mapper.findCourse(courseId);
        Long creatorId = existing == null ? null : existing.getCreatedBy();
        mapper.updateCourse(toCourse(courseId, command, creatorId));
        replaceBindings(courseId, command);
    }

    @Override
    public void updatePublishStatus(Long courseId, String publishStatus) {
        mapper.updatePublishStatus(courseId, publishStatus);
        mapper.updateAssignmentPublishStatus(courseId, publishStatus);
    }

    @Override
    public void deleteCourse(Long courseId) {
        mapper.deleteCourse(courseId);
        mapper.updateAssignmentPublishStatus(courseId, "OFFLINE");
    }

    @Override
    public Long copyCourse(Long sourceCourseId, Long creatorId) {
        AdminCourse source = mapper.findCourse(sourceCourseId);
        AdminCourseCommand command = commandFromCourse(source);
        command.setCourseName(source.getCourseName() + " Copy");
        command.setPublishStatus("DRAFT");
        return createCourse(command, creatorId);
    }

    @Override
    public void notifyBoundStudents(Long courseId, String title, String content) {
        AdminCourseNotification notification = new AdminCourseNotification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSourceId(courseId);
        mapper.insertNotification(notification);
        mapper.notifyBoundStudents(courseId, notification.getNotificationId());
    }

    @Override
    public AdminCourseStatistics calculateStatistics(Long courseId) {
        return mapper.calculateStatistics(courseId);
    }

    @Override
    public List<AdminCourseStudentStatistics> findStudentStatistics(Long courseId, AdminCourseStudentStatisticsQuery query) {
        return mapper.findStudentStatistics(courseId, likeStudentStatisticsQuery(query));
    }

    @Override
    public long countStudentStatistics(Long courseId, AdminCourseStudentStatisticsQuery query) {
        return mapper.countStudentStatistics(courseId, likeStudentStatisticsQuery(query));
    }

    @Override
    public void appendCourseLog(Long courseId, Long operatorId, String action, String content) {
        mapper.insertCourseLog(courseId, operatorId, action, content);
    }

    @Override
    public List<AdminCourseLog> findCourseLogs(Long courseId) {
        return mapper.findCourseLogs(courseId);
    }

    private void replaceBindings(Long courseId, AdminCourseCommand command) {
        mapper.deleteTeachers(courseId);
        int teacherSort = 1;
        for (Long teacherId : command.getTeacherIds()) {
            mapper.insertTeacher(courseId, teacherId, teacherSort++);
        }
        mapper.deleteClasses(courseId);
        int classSort = 1;
        for (Long classId : command.getClassIds()) {
            mapper.insertClass(courseId, classId, classSort++);
        }
        mapper.deleteContents(courseId);
        mapper.deleteChapters(courseId);
        for (AdminCourseChapterCommand chapterCommand : command.getChapters()) {
            insertChapterTree(courseId, null, chapterCommand);
        }
    }

    private void insertChapterTree(Long courseId, Long parentChapterId, AdminCourseChapterCommand chapterCommand) {
        AdminCourseChapter chapter = toChapter(chapterCommand);
        chapter.setParentChapterId(parentChapterId);
        mapper.insertChapter(courseId, chapter);
        for (AdminCourseContentCommand contentCommand : chapterCommand.getContents()) {
            AdminCourseContent content = toContent(contentCommand);
            mapper.insertContent(courseId, chapter.getChapterId(), content);
            if ("ASSIGNMENT".equals(content.getItemType())) {
                if (content.getAssignmentId() == null) {
                    mapper.insertAssignment(courseId, content.getContentId(), content);
                    mapper.updateContentAssignmentId(content.getContentId(), content.getAssignmentId());
                } else {
                    mapper.updateAssignmentContent(content.getAssignmentId(), courseId, content.getContentId(), content);
                }
            }
        }
        for (AdminCourseChapterCommand childCommand : chapterCommand.getChildren()) {
            insertChapterTree(courseId, chapter.getChapterId(), childCommand);
        }
    }

    private AdminCourse toCourse(Long courseId, AdminCourseCommand command, Long creatorId) {
        AdminCourse course = new AdminCourse();
        course.setCourseId(courseId);
        course.setCourseName(command.getCourseName());
        course.setAcademicYearId(command.getAcademicYearId());
        course.setSemesterId(command.getSemesterId());
        course.setAcademicTerm(mapper.findAcademicTerm(command.getSemesterId()));
        course.setMajorId(command.getMajorId());
        course.setClassId(command.getClassIds().get(0));
        course.setCoverUrl(command.getCoverUrl());
        course.setTeacherNames(namesForTeachers(command.getTeacherIds()));
        course.setClassNames(namesForClasses(command.getClassIds()));
        course.setLearningMode(command.getLearningMode());
        course.setAssignmentCompletionRule(command.getAssignmentCompletionRule());
        course.setCoursewareScoreCap(command.getCoursewareScoreCap());
        course.setCoursewareCount(command.getCoursewareCount());
        course.setAssignmentCount(command.getAssignmentCount());
        course.setPublishStatus(command.getPublishStatus());
        course.setOpenStartTime(command.getOpenStartTime());
        course.setOpenEndTime(command.getOpenEndTime());
        course.setCreatedBy(creatorId);
        return course;
    }

    private String namesForTeachers(List<Long> teacherIds) {
        if (teacherIds == null || teacherIds.isEmpty()) {
            return null;
        }
        return mapper.findTeacherNamesByIds(teacherIds);
    }

    private String namesForClasses(List<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return null;
        }
        return mapper.findClassNamesByIds(classIds);
    }

    private AdminCourseChapter toChapter(AdminCourseChapterCommand command) {
        AdminCourseChapter chapter = new AdminCourseChapter();
        chapter.setChapterTitle(command.getChapterTitle());
        chapter.setSortOrder(command.getSortOrder());
        return chapter;
    }

    private AdminCourseContent toContent(AdminCourseContentCommand command) {
        AdminCourseContent content = new AdminCourseContent();
        content.setItemType(command.getItemType());
        content.setTitle(command.getTitle());
        content.setResourceId(command.getResourceId());
        content.setAssignmentId(command.getAssignmentId());
        content.setRequiredDurationSeconds(command.getRequiredDurationSeconds());
        content.setLearningStartTime(command.getLearningStartTime());
        content.setLearningEndTime(command.getLearningEndTime());
        content.setAssignmentCompletionRule(command.getAssignmentCompletionRule());
        content.setPassScore(command.getPassScore());
        content.setAssignmentPublishMode(command.getAssignmentPublishMode());
        content.setAnswerStartTime(command.getAnswerStartTime());
        content.setAnswerEndTime(command.getAnswerEndTime());
        content.setAssignmentTotalScore(command.getAssignmentTotalScore());
        content.setSortOrder(command.getSortOrder());
        return content;
    }

    private AdminCourseCommand commandFromCourse(AdminCourse source) {
        AdminCourseCommand command = new AdminCourseCommand();
        command.setCourseName(source.getCourseName());
        command.setAcademicYearId(source.getAcademicYearId());
        command.setSemesterId(source.getSemesterId());
        command.setMajorId(source.getMajorId());
        command.setCoverUrl(source.getCoverUrl());
        command.setOpenStartTime(source.getOpenStartTime());
        command.setOpenEndTime(source.getOpenEndTime());
        command.setTeacherIds(source.getTeacherIds() == null ? Collections.<Long>emptyList() : source.getTeacherIds());
        command.setClassIds(classIdsFromSource(source));
        command.setLearningMode(source.getLearningMode() == null ? "SELF_PACED" : source.getLearningMode());
        command.setAssignmentCompletionRule(source.getAssignmentCompletionRule() == null
                ? "SUBMIT" : source.getAssignmentCompletionRule());
        command.setCoursewareScoreCap(source.getCoursewareScoreCap() == null
                ? Integer.valueOf(100) : source.getCoursewareScoreCap());
        command.setCoursewareCount(source.getCoursewareCount());
        command.setAssignmentCount(source.getAssignmentCount());
        command.setChapters(chapterCommands(source.getChapters()));
        return command;
    }

    private List<Long> classIdsFromSource(AdminCourse source) {
        if (source.getClassIds() != null && !source.getClassIds().isEmpty()) {
            return source.getClassIds();
        }
        if (source.getClassId() == null) {
            return Collections.emptyList();
        }
        List<Long> classIds = new ArrayList<Long>();
        classIds.add(source.getClassId());
        return classIds;
    }

    private List<AdminCourseChapterCommand> chapterCommands(List<AdminCourseChapter> chapters) {
        List<AdminCourseChapterCommand> commands = new ArrayList<AdminCourseChapterCommand>();
        if (chapters == null) {
            return commands;
        }
        for (AdminCourseChapter chapter : chapters) {
            AdminCourseChapterCommand command = new AdminCourseChapterCommand();
            command.setChapterTitle(chapter.getChapterTitle());
            command.setSortOrder(chapter.getSortOrder());
            command.setContents(contentCommands(chapter.getContents()));
            command.setChildren(chapterCommands(chapter.getChildren()));
            commands.add(command);
        }
        return commands;
    }

    private List<AdminCourseContentCommand> contentCommands(List<AdminCourseContent> contents) {
        List<AdminCourseContentCommand> commands = new ArrayList<AdminCourseContentCommand>();
        if (contents == null) {
            return commands;
        }
        for (AdminCourseContent content : contents) {
            AdminCourseContentCommand command = new AdminCourseContentCommand();
            command.setItemType(content.getItemType());
            command.setTitle(content.getTitle());
            command.setResourceId(content.getResourceId());
            command.setAssignmentId(content.getAssignmentId());
            command.setRequiredDurationSeconds(content.getRequiredDurationSeconds());
            command.setLearningStartTime(content.getLearningStartTime());
            command.setLearningEndTime(content.getLearningEndTime());
            command.setAssignmentCompletionRule(content.getAssignmentCompletionRule());
            command.setPassScore(content.getPassScore());
            command.setAssignmentPublishMode(content.getAssignmentPublishMode());
            command.setAnswerStartTime(content.getAnswerStartTime());
            command.setAnswerEndTime(content.getAnswerEndTime());
            command.setAssignmentTotalScore(content.getAssignmentTotalScore());
            command.setSortOrder(content.getSortOrder());
            commands.add(command);
        }
        return commands;
    }

    private AdminCourseQuery likeQuery(AdminCourseQuery source) {
        AdminCourseQuery query = new AdminCourseQuery();
        query.setKeyword(like(source.getKeyword()));
        query.setAcademicYearId(source.getAcademicYearId());
        query.setSemesterId(source.getSemesterId());
        query.setMajorId(source.getMajorId());
        query.setClassId(source.getClassId());
        query.setTeacherId(source.getTeacherId());
        query.setTeachingStartTime(source.getTeachingStartTime());
        query.setTeachingEndTime(source.getTeachingEndTime());
        query.setPublishStatus(source.getPublishStatus());
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }

    private AdminCourseStudentStatisticsQuery likeStudentStatisticsQuery(AdminCourseStudentStatisticsQuery source) {
        AdminCourseStudentStatisticsQuery query = new AdminCourseStudentStatisticsQuery();
        if (source != null) {
            query.setStudentName(like(source.getStudentName()));
            query.setStudentNo(like(source.getStudentNo()));
            query.setClassName(like(source.getClassName()));
            query.setPage(source.getPage());
            query.setPageSize(source.getPageSize());
        }
        return query;
    }

    private String like(String value) {
        return value == null ? null : "%" + value + "%";
    }
}
