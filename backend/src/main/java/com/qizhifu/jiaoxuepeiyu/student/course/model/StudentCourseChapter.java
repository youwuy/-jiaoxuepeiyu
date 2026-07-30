package com.qizhifu.jiaoxuepeiyu.student.course.model;

import java.util.ArrayList;
import java.util.List;

public class StudentCourseChapter {

    private Long chapterId;
    private String chapterTitle;
    private int sortOrder;
    private List<StudentCourseItem> items = new ArrayList<StudentCourseItem>();

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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<StudentCourseItem> getItems() {
        return items;
    }

    public void setItems(List<StudentCourseItem> items) {
        this.items = items;
    }
}
