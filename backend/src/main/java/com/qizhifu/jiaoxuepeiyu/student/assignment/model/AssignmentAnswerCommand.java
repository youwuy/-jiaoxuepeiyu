package com.qizhifu.jiaoxuepeiyu.student.assignment.model;

import java.util.ArrayList;
import java.util.List;

public class AssignmentAnswerCommand {

    private List<AnswerItem> answers = new ArrayList<AnswerItem>();

    public AssignmentAnswerCommand() {
    }

    public AssignmentAnswerCommand(List<AnswerItem> answers) {
        this.answers = answers;
    }

    public List<AnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerItem> answers) {
        this.answers = answers;
    }

    public static class AnswerItem {
        private Long questionId;
        private String answerContent;

        public AnswerItem() {
        }

        public AnswerItem(Long questionId, String answerContent) {
            this.questionId = questionId;
            this.answerContent = answerContent;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getAnswerContent() {
            return answerContent;
        }

        public void setAnswerContent(String answerContent) {
            this.answerContent = answerContent;
        }
    }
}
