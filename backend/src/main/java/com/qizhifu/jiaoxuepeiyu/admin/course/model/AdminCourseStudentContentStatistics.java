package com.qizhifu.jiaoxuepeiyu.admin.course.model;

import java.math.BigDecimal;

public class AdminCourseStudentContentStatistics {

    private Long chapterId;
    private Long parentChapterId;
    private String chapterTitle;
    private Integer chapterSortOrder;
    private Long contentId;
    private String itemType;
    private String contentTitle;
    private Integer contentSortOrder;
    private String completionStatus;
    private BigDecimal score;

    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }
    public Long getParentChapterId() { return parentChapterId; }
    public void setParentChapterId(Long parentChapterId) { this.parentChapterId = parentChapterId; }
    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }
    public Integer getChapterSortOrder() { return chapterSortOrder; }
    public void setChapterSortOrder(Integer chapterSortOrder) { this.chapterSortOrder = chapterSortOrder; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }
    public Integer getContentSortOrder() { return contentSortOrder; }
    public void setContentSortOrder(Integer contentSortOrder) { this.contentSortOrder = contentSortOrder; }
    public String getCompletionStatus() { return completionStatus; }
    public void setCompletionStatus(String completionStatus) { this.completionStatus = completionStatus; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
}
