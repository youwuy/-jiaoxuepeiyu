package com.qizhifu.jiaoxuepeiyu.admin.course;

import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourse;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseChapterCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseContentCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseLog;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentContentStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentStatisticsQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.port.AdminCourseRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCourseService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_CHAPTER_DEPTH = 3;
    private static final Set<String> LEARNING_MODES = new HashSet<String>(Arrays.asList("SELF_PACED", "SEQUENTIAL", "TEACHER_LED"));
    private static final Set<String> ASSIGNMENT_RULES = new HashSet<String>(Arrays.asList("SUBMIT", "PASS_SCORE"));
    private static final Set<String> ASSIGNMENT_PUBLISH_MODES = new HashSet<String>(Arrays.asList("PRACTICE", "EXAM"));

    private final AdminCourseRepository repository;

    public AdminCourseService(AdminCourseRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AdminCourse> listCourses(AdminCourseQuery query) {
        AdminCourseQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminCourse>(
                repository.findCourses(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countCourses(normalized));
    }

    public AdminCourse getCourse(Long courseId) {
        AdminCourse course = repository.findCourse(courseId);
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }
        return course;
    }

    @Transactional
    public Long createCourse(AdminCourseCommand command, Long creatorId) {
        requireOperator(creatorId);
        AdminCourseCommand normalized = normalizedCourse(command);
        Long courseId = repository.createCourse(normalized, creatorId);
        repository.appendCourseLog(courseId, creatorId, "CREATE", "Create course");
        return courseId;
    }

    @Transactional
    public void updateCourse(Long courseId, AdminCourseCommand command, Long operatorId) {
        requireOperator(operatorId);
        getCourse(courseId);
        repository.updateCourse(courseId, normalizedCourse(command));
        repository.appendCourseLog(courseId, operatorId, "UPDATE", "Update course");
    }

    @Transactional
    public void publishCourse(Long courseId, Long operatorId) {
        requireOperator(operatorId);
        AdminCourse course = getCourse(courseId);
        if (contentCount(course) <= 0) {
            throw new BusinessException(400, "Course must contain content before publishing");
        }
        repository.updatePublishStatus(courseId, "PUBLISHED");
        repository.notifyBoundStudents(courseId,
                "New course published",
                "Course \"" + course.getCourseName() + "\" is now available.");
        repository.appendCourseLog(courseId, operatorId, "PUBLISH", "Publish course");
    }

    @Transactional
    public void cancelPublishCourse(Long courseId, Long operatorId) {
        requireOperator(operatorId);
        getCourse(courseId);
        repository.updatePublishStatus(courseId, "OFFLINE");
        repository.appendCourseLog(courseId, operatorId, "CANCEL_PUBLISH", "Cancel course publish");
    }

    @Transactional
    public void deleteCourse(Long courseId, Long operatorId) {
        requireOperator(operatorId);
        getCourse(courseId);
        repository.deleteCourse(courseId);
        repository.appendCourseLog(courseId, operatorId, "DELETE", "Delete course");
    }

    @Transactional
    public Long copyCourse(Long courseId, Long operatorId) {
        requireOperator(operatorId);
        getCourse(courseId);
        Long copiedCourseId = repository.copyCourse(courseId, operatorId);
        repository.appendCourseLog(copiedCourseId, operatorId, "COPY", "Copy course from " + courseId);
        return copiedCourseId;
    }

    public AdminCourseStatistics getStatistics(Long courseId) {
        getCourse(courseId);
        AdminCourseStatistics statistics = repository.calculateStatistics(courseId);
        if (statistics == null) {
            statistics = new AdminCourseStatistics();
            statistics.setCourseId(courseId);
        }
        return normalizedStatistics(courseId, statistics);
    }

    public List<AdminCourseLog> listCourseLogs(Long courseId) {
        getCourse(courseId);
        return repository.findCourseLogs(courseId);
    }

    public PageResponse<AdminCourseStudentStatistics> listStudentStatistics(Long courseId, AdminCourseStudentStatisticsQuery query) {
        getCourse(courseId);
        AdminCourseStudentStatisticsQuery normalized = normalizedStudentStatisticsQuery(query);
        return new PageResponse<AdminCourseStudentStatistics>(
                repository.findStudentStatistics(courseId, normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countStudentStatistics(courseId, normalized));
    }

    public List<AdminCourseStudentStatistics> exportStudentStatistics(Long courseId, AdminCourseStudentStatisticsQuery query) {
        getCourse(courseId);
        AdminCourseStudentStatisticsQuery normalized = normalizedStudentStatisticsQuery(query);
        normalized.setPage(1);
        normalized.setPageSize(MAX_PAGE_SIZE);
        return repository.findStudentStatistics(courseId, normalized);
    }

    public List<AdminCourseStudentContentStatistics> getStudentContentStatistics(Long courseId, Long studentId) {
        getCourse(courseId);
        if (studentId == null) {
            throw new BusinessException(400, "Student is required");
        }
        return repository.findStudentContentStatistics(courseId, studentId);
    }

    private AdminCourseCommand normalizedCourse(AdminCourseCommand command) {
        if (command == null) {
            throw new BusinessException(400, "Course data is required");
        }
        String courseName = trimToNull(command.getCourseName());
        if (courseName == null) {
            throw new BusinessException(400, "Course name is required");
        }
        if (courseName.length() > 20) {
            throw new BusinessException(400, "Course name cannot exceed 20 characters");
        }
        if (command.getAcademicYearId() == null || command.getSemesterId() == null) {
            throw new BusinessException(400, "Course academic year and semester are required");
        }
        if (command.getMajorId() == null) {
            throw new BusinessException(400, "Course major is required");
        }
        String coverUrl = trimToNull(command.getCoverUrl());
        if (coverUrl == null) {
            throw new BusinessException(400, "Course cover is required");
        }
        validateOpenTime(command.getOpenStartTime(), command.getOpenEndTime());

        AdminCourseCommand normalized = new AdminCourseCommand();
        normalized.setCourseName(courseName);
        normalized.setAcademicYearId(command.getAcademicYearId());
        normalized.setSemesterId(command.getSemesterId());
        normalized.setMajorId(command.getMajorId());
        normalized.setCoverUrl(coverUrl);
        normalized.setOpenStartTime(command.getOpenStartTime());
        normalized.setOpenEndTime(command.getOpenEndTime());
        normalized.setTeacherIds(normalizedIds(command.getTeacherIds(), "Course teaching teachers are required"));
        normalized.setClassIds(normalizedIds(command.getClassIds(), "Course teaching classes are required"));
        normalized.setLearningMode(normalizedEnum(command.getLearningMode(), "SELF_PACED", LEARNING_MODES,
                "Course learning mode is invalid"));
        normalized.setAssignmentCompletionRule(normalizedEnum(command.getAssignmentCompletionRule(), "SUBMIT",
                ASSIGNMENT_RULES, "Course assignment completion rule is invalid"));
        normalized.setCoursewareScoreCap(normalizedScoreCap(command.getCoursewareScoreCap()));
        normalized.setPublishStatus("DRAFT");
        normalized.setChapters(normalizedChapters(command.getChapters()));
        normalized.setCoursewareCount(Integer.valueOf(countContents(normalized.getChapters(), "COURSEWARE")));
        normalized.setAssignmentCount(Integer.valueOf(countContents(normalized.getChapters(), "ASSIGNMENT")));
        return normalized;
    }

    private List<AdminCourseChapterCommand> normalizedChapters(List<AdminCourseChapterCommand> chapters) {
        return normalizedChapters(chapters, 1);
    }

    private List<AdminCourseChapterCommand> normalizedChapters(List<AdminCourseChapterCommand> chapters, int depth) {
        List<AdminCourseChapterCommand> normalized = new ArrayList<AdminCourseChapterCommand>();
        if (chapters == null) {
            return normalized;
        }
        if (depth > MAX_CHAPTER_DEPTH) {
            throw new BusinessException(400, "Course chapters cannot exceed 3 levels");
        }
        int defaultChapterSort = 1;
        for (AdminCourseChapterCommand chapter : chapters) {
            String chapterTitle = trimToNull(chapter == null ? null : chapter.getChapterTitle());
            if (chapterTitle == null) {
                throw new BusinessException(400, "Course chapter title is required");
            }
            if (chapterTitle.length() > 20) {
                throw new BusinessException(400, "Course chapter title cannot exceed 20 characters");
            }
            AdminCourseChapterCommand normalizedChapter = new AdminCourseChapterCommand();
            normalizedChapter.setChapterTitle(chapterTitle);
            normalizedChapter.setSortOrder(chapter.getSortOrder() == null ? Integer.valueOf(defaultChapterSort) : chapter.getSortOrder());
            normalizedChapter.setContents(normalizedContents(chapter.getContents()));
            normalizedChapter.setChildren(normalizedChapters(chapter.getChildren(), depth + 1));
            normalized.add(normalizedChapter);
            defaultChapterSort++;
        }
        return normalized;
    }

    private List<AdminCourseContentCommand> normalizedContents(List<AdminCourseContentCommand> contents) {
        List<AdminCourseContentCommand> normalized = new ArrayList<AdminCourseContentCommand>();
        if (contents == null) {
            return normalized;
        }
        int defaultSort = 1;
        for (AdminCourseContentCommand content : contents) {
            String itemType = upper(trimToNull(content == null ? null : content.getItemType()));
            if (!"COURSEWARE".equals(itemType) && !"ASSIGNMENT".equals(itemType)) {
                throw new BusinessException(400, "Course content type is invalid");
            }
            String title = trimToNull(content.getTitle());
            if (title == null) {
                throw new BusinessException(400, "Course content title is required");
            }
            AdminCourseContentCommand normalizedContent = new AdminCourseContentCommand();
            normalizedContent.setItemType(itemType);
            normalizedContent.setTitle(title);
            if ("COURSEWARE".equals(itemType)) {
                if (content.getResourceId() == null) {
                    throw new BusinessException(400, "Courseware resource is required");
                }
                normalizedContent.setResourceId(content.getResourceId());
                normalizedContent.setRequiredDurationSeconds(normalizedDuration(content.getRequiredDurationSeconds()));
                validateContentTime(content.getLearningStartTime(), content.getLearningEndTime(),
                        "Courseware learning end time must be after start time");
                normalizedContent.setLearningStartTime(content.getLearningStartTime());
                normalizedContent.setLearningEndTime(content.getLearningEndTime());
            } else {
                normalizedContent.setAssignmentId(content.getAssignmentId());
                normalizedContent.setRequiredDurationSeconds(Integer.valueOf(0));
                normalizedContent.setAssignmentCompletionRule(normalizedEnum(content.getAssignmentCompletionRule(),
                        "SUBMIT", ASSIGNMENT_RULES, "Assignment completion rule is invalid"));
                normalizedContent.setPassScore(normalizedPassScore(
                        normalizedContent.getAssignmentCompletionRule(),
                        content.getPassScore(),
                        content.getAssignmentTotalScore()));
                normalizedContent.setAssignmentPublishMode(normalizedEnum(content.getAssignmentPublishMode(),
                        "PRACTICE", ASSIGNMENT_PUBLISH_MODES, "Assignment publish mode is invalid"));
                validateContentTime(content.getAnswerStartTime(), content.getAnswerEndTime(),
                        "Assignment answer end time must be after start time");
                normalizedContent.setAnswerStartTime(content.getAnswerStartTime());
                normalizedContent.setAnswerEndTime(content.getAnswerEndTime());
                normalizedContent.setAssignmentTotalScore(content.getAssignmentTotalScore() == null
                        ? Integer.valueOf(0) : content.getAssignmentTotalScore());
                List<Long> questionIds = normalizedIds(content.getQuestionIds(), null);
                List<Long> trainingIds = normalizedIds(content.getTrainingIds(), null);
                if (questionIds.isEmpty() == trainingIds.isEmpty()) {
                    throw new BusinessException(400,
                            "Course assignment must contain either theory questions or training topics");
                }
                normalizedContent.setQuestionIds(questionIds);
                normalizedContent.setTrainingIds(trainingIds);
                normalizedContent.setAssignmentType(trainingIds.isEmpty() ? "THEORY" : "TRAINING");
            }
            normalizedContent.setSortOrder(content.getSortOrder() == null ? Integer.valueOf(defaultSort) : content.getSortOrder());
            normalized.add(normalizedContent);
            defaultSort++;
        }
        return normalized;
    }

    private AdminCourseQuery normalizedQuery(AdminCourseQuery query) {
        AdminCourseQuery normalized = new AdminCourseQuery();
        if (query != null) {
            normalized.setKeyword(trimToNull(query.getKeyword()));
            normalized.setAcademicYearId(query.getAcademicYearId());
            normalized.setSemesterId(query.getSemesterId());
            normalized.setMajorId(query.getMajorId());
            normalized.setClassId(query.getClassId());
            normalized.setTeacherId(query.getTeacherId());
            normalized.setTeachingStartTime(query.getTeachingStartTime());
            normalized.setTeachingEndTime(query.getTeachingEndTime());
            normalized.setPublishStatus(normalizedPublishStatus(query.getPublishStatus()));
            normalized.setPage(query.getPage());
            normalized.setPageSize(query.getPageSize());
        }
        validateContentTime(normalized.getTeachingStartTime(), normalized.getTeachingEndTime(),
                "Course teaching filter end time must be after start time");
        if (normalized.getPage() < 1) {
            normalized.setPage(1);
        }
        if (normalized.getPageSize() < 1) {
            normalized.setPageSize(20);
        }
        if (normalized.getPageSize() > MAX_PAGE_SIZE) {
            normalized.setPageSize(MAX_PAGE_SIZE);
        }
        return normalized;
    }

    private AdminCourseStudentStatisticsQuery normalizedStudentStatisticsQuery(AdminCourseStudentStatisticsQuery query) {
        AdminCourseStudentStatisticsQuery normalized = new AdminCourseStudentStatisticsQuery();
        if (query != null) {
            normalized.setStudentName(trimToNull(query.getStudentName()));
            normalized.setStudentNo(trimToNull(query.getStudentNo()));
            normalized.setClassName(trimToNull(query.getClassName()));
            normalized.setPage(query.getPage());
            normalized.setPageSize(query.getPageSize());
        }
        if (normalized.getPage() < 1) {
            normalized.setPage(1);
        }
        if (normalized.getPageSize() < 1) {
            normalized.setPageSize(20);
        }
        if (normalized.getPageSize() > MAX_PAGE_SIZE) {
            normalized.setPageSize(MAX_PAGE_SIZE);
        }
        return normalized;
    }

    private AdminCourseStatistics normalizedStatistics(Long courseId, AdminCourseStatistics statistics) {
        statistics.setCourseId(statistics.getCourseId() == null ? courseId : statistics.getCourseId());
        statistics.setStudentCount(statistics.getStudentCount() == null ? Integer.valueOf(0) : statistics.getStudentCount());
        statistics.setCompletedCount(statistics.getCompletedCount() == null ? Integer.valueOf(0) : statistics.getCompletedCount());
        statistics.setStudyingCount(statistics.getStudyingCount() == null ? Integer.valueOf(0) : statistics.getStudyingCount());
        statistics.setNotStartedCount(statistics.getNotStartedCount() == null ? Integer.valueOf(0) : statistics.getNotStartedCount());
        statistics.setPendingReviewCount(statistics.getPendingReviewCount() == null ? Integer.valueOf(0) : statistics.getPendingReviewCount());
        statistics.setAverageScore(statistics.getAverageScore() == null ? Double.valueOf(0) : statistics.getAverageScore());
        return statistics;
    }

    private List<Long> normalizedIds(List<Long> ids, String message) {
        if (ids == null || ids.isEmpty()) {
            if (message == null) {
                return new ArrayList<Long>();
            }
            throw new BusinessException(400, message);
        }
        List<Long> normalized = new ArrayList<Long>();
        for (Long id : ids) {
            if (id != null && id.longValue() > 0 && !normalized.contains(id)) {
                normalized.add(id);
            }
        }
        if (normalized.isEmpty() && message != null) {
            throw new BusinessException(400, message);
        }
        return normalized;
    }

    private void validateOpenTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(400, "Course open time range is required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(400, "Course open end time must be after start time");
        }
    }

    private String normalizedEnum(String value, String defaultValue, Set<String> allowed, String message) {
        String normalized = upper(trimToNull(value));
        if (normalized == null) {
            normalized = defaultValue;
        }
        if (!allowed.contains(normalized)) {
            throw new BusinessException(400, message);
        }
        return normalized;
    }

    private Integer normalizedScoreCap(Integer scoreCap) {
        int value = scoreCap == null ? 100 : scoreCap.intValue();
        if (value <= 0 || value > 100) {
            throw new BusinessException(400, "Courseware score cap must be between 1 and 100");
        }
        return Integer.valueOf(value);
    }

    private Integer normalizedPassScore(String completionRule, Integer passScore, Integer totalScore) {
        if (!"PASS_SCORE".equals(completionRule)) {
            return null;
        }
        if (passScore == null) {
            throw new BusinessException(400, "Assignment pass score is required");
        }
        int score = passScore.intValue();
        int maxScore = totalScore == null ? 100 : totalScore.intValue();
        if (score < 0 || score > maxScore) {
            throw new BusinessException(400, "Assignment pass score must be between 0 and total score");
        }
        return Integer.valueOf(score);
    }

    private String normalizedPublishStatus(String status) {
        String normalized = upper(trimToNull(status));
        if (normalized == null) {
            return null;
        }
        if ("UNPUBLISHED".equals(normalized) || "NOT_PUBLISHED".equals(normalized)) {
            return "NOT_PUBLISHED";
        }
        if ("PUBLISHED".equals(normalized) || "DRAFT".equals(normalized) || "OFFLINE".equals(normalized)) {
            return normalized;
        }
        throw new BusinessException(400, "Course publish status is invalid");
    }

    private void validateContentTime(LocalDateTime startTime, LocalDateTime endTime, String message) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new BusinessException(400, message);
        }
    }

    private Integer normalizedDuration(Integer duration) {
        if (duration == null || duration.intValue() < 0) {
            return Integer.valueOf(0);
        }
        return duration;
    }

    private int countContents(List<AdminCourseChapterCommand> chapters, String itemType) {
        int count = 0;
        for (AdminCourseChapterCommand chapter : chapters) {
            if (chapter.getContents() != null) {
                for (AdminCourseContentCommand content : chapter.getContents()) {
                    if (itemType.equals(content.getItemType())) {
                        count++;
                    }
                }
            }
            if (chapter.getChildren() != null) {
                count += countContents(chapter.getChildren(), itemType);
            }
        }
        return count;
    }

    private int contentCount(AdminCourse course) {
        int courseware = course.getCoursewareCount() == null ? 0 : course.getCoursewareCount().intValue();
        int assignment = course.getAssignmentCount() == null ? 0 : course.getAssignmentCount().intValue();
        return courseware + assignment;
    }

    private void requireOperator(Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException(401, "Missing admin identity");
        }
    }

    private String trimToNull(String value) {
        return InputValidator.hasText(value) ? value.trim() : null;
    }

    private String upper(String value) {
        return value == null ? null : value.toUpperCase(Locale.ENGLISH);
    }
}
