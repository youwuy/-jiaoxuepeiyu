package com.qizhifu.jiaoxuepeiyu.admin.course.model;

import java.util.ArrayList;
import java.util.List;

public class AdminCourseChapter {

    private Long chapterId;
    private Long courseId;
    private Long parentChapterId;
    private String chapterTitle;
    private Integer sortOrder;
    private List<AdminCourseContent> contents = new ArrayList<AdminCourseContent>();
    private List<AdminCourseChapter> children = new ArrayList<AdminCourseChapter>();

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getParentChapterId() {
        return parentChapterId;
    }

    public void setParentChapterId(Long parentChapterId) {
        this.parentChapterId = parentChapterId;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<AdminCourseContent> getContents() {
        return contents;
    }

    public void setContents(List<AdminCourseContent> contents) {
        this.contents = contents;
    }

    public List<AdminCourseChapter> getChildren() {
        return children;
    }

    public void setChildren(List<AdminCourseChapter> children) {
        this.children = children;
    }
}
