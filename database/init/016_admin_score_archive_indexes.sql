USE `jiaoxuepeiyu`;

CREATE INDEX `idx_score_semester_filter`
  ON `score_semester_summary` (`semester_id`, `student_id`, `comprehensive_score`);

CREATE INDEX `idx_training_attempt_admin_filter`
  ON `training_attempt` (`training_id`, `student_id`, `training_mode`, `submit_type`, `submitted_at`);
