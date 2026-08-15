package com.qizhifu.jiaoxuepeiyu.admin.course.model;

import java.util.List;

public class AdminCourseChapterCommand {

    private Long chapterId;
    private String chapterTitle;
    private Integer sortOrder;
    private List<AdminCourseContentCommand> contents;
    private List<AdminCourseChapterCommand> children;

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
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

    public List<AdminCourseContentCommand> getContents() {
        return contents;
    }

    public void setContents(List<AdminCourseContentCommand> contents) {
        this.contents = contents;
    }

    public List<AdminCourseChapterCommand> getChildren() {
        return children;
    }

    public void setChildren(List<AdminCourseChapterCommand> children) {
        this.children = children;
    }
}
