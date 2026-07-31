package com.qizhifu.jiaoxuepeiyu.student.training;

import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptCommand;
import javax.validation.constraints.NotNull;

public class StudentTrainingAttemptRequest extends TrainingAttemptCommand {

    @NotNull
    private Long trainingId;

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }
}
