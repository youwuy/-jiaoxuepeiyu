<template>
  <AdminShell activeKey="admin-courses">
    <section v-loading="loading" class="admin-course-form-page">
      <header class="admin-course-form-topbar">
        <el-breadcrumb class="admin-course-form-breadcrumb" separator="/">
          <el-breadcrumb-item>教学实训</el-breadcrumb-item>
          <el-breadcrumb-item>教学课程</el-breadcrumb-item>
          <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
        </el-breadcrumb>

        <el-button class="admin-course-form-back" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </header>

      <section class="admin-course-form-card basic">
        <header class="admin-course-form-card-title">
          <el-icon><InfoFilled /></el-icon>
          <strong>基本信息</strong>
        </header>

        <div class="admin-course-form-fields">
          <label class="admin-course-form-field full">
            <span>课程名称 <b>*</b></span>
            <el-input v-model="form.courseName" placeholder="请输入课程名称" maxlength="20" />
          </label>

          <label class="admin-course-form-field">
            <span>
              教学开始时间 <b>*</b>
              <el-tooltip content="学员仅可在开始到结束时间内学习，结束时间后可以回看" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </span>
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="请选择开始时间" />
          </label>

          <label class="admin-course-form-field">
            <span>
              教学结束时间 <b>*</b>
              <el-tooltip content="学员仅可在开始到结束时间内学习，结束时间后可以回看" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </span>
            <el-date-picker v-model="form.endTime" type="datetime" placeholder="请选择结束时间" />
          </label>

          <label class="admin-course-form-field">
            <span>所属学年学期 <b>*</b></span>
            <el-select v-model="form.semesterKey" placeholder="请选择学期" filterable>
              <el-option
                v-for="item in semesterOptions"
                :key="item.key"
                :label="item.label"
                :value="item.key"
              />
            </el-select>
          </label>

          <label class="admin-course-form-field">
            <span>
              课件完成度满分 <b>*</b>
              <el-tooltip content="学员实际完成进度乘以该满分，即为该课程的课件学习得分，参与本学期的综合成绩统计" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </span>
            <el-input-number
              v-model="form.coursewareScore"
              placeholder="请输入满分值"
              :min="1"
              :precision="0"
              :controls="false"
            />
          </label>

          <div class="admin-course-form-field full radio">
            <span>
              学生学习模式 <b>*</b>
              <el-tooltip
                content="自由学习可任意跳转学习内容；顺序解锁需按层级依次完成上一项后才能访问下一项"
                placement="top"
              >
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </span>
            <el-radio-group v-model="form.learningMode" class="admin-course-form-radio">
              <el-radio label="SELF_PACED">自由学习</el-radio>
              <el-radio label="SEQUENTIAL">顺序解锁</el-radio>
            </el-radio-group>
          </div>
        </div>
      </section>

      <section class="admin-course-form-card compact">
        <header class="admin-course-form-card-title">
          <el-icon><UserFilled /></el-icon>
          <strong>教学团队</strong>
        </header>
        <div class="admin-course-chip-picker">
          <el-tag
            v-for="item in selectedTeachers"
            :key="item.userId"
            :closable="canModifyCourse"
            class="admin-course-chip teacher"
            @close="removeTeacher(item.userId)"
          >
            {{ item.realName || item.accountNo }}
          </el-tag>
          <el-popover placement="bottom-start" trigger="click" width="320" popper-class="admin-course-picker-popover">
            <template #reference>
              <el-button class="admin-course-add-chip" :disabled="!canModifyCourse">
                <el-icon><Plus /></el-icon>
                添加教师
              </el-button>
            </template>
            <el-select
              v-model="form.teacherIds"
              :disabled="!canModifyCourse"
              class="admin-course-form-picker"
              multiple
              filterable
              collapse-tags
              collapse-tags-tooltip
              placeholder="请选择教师"
            >
              <el-option
                v-for="item in teacherOptions"
                :key="item.userId"
                :label="item.realName || item.accountNo"
                :value="item.userId"
              >
                <div class="admin-course-form-option">
                  <strong>{{ item.realName || item.accountNo }}</strong>
                  <span>{{ item.accountNo || '未配置工号' }}</span>
                </div>
              </el-option>
            </el-select>
          </el-popover>
        </div>
      </section>

      <section class="admin-course-form-card compact">
        <header class="admin-course-form-card-title">
          <el-icon><User /></el-icon>
          <strong>授课班级</strong>
        </header>
        <div class="admin-course-chip-picker">
          <el-tag
            v-for="item in selectedClasses"
            :key="item.classId"
            :closable="canModifyCourse"
            class="admin-course-chip class"
            @close="removeClass(item.classId)"
          >
            {{ item.className }}
          </el-tag>
          <el-popover placement="bottom-start" trigger="click" width="320" popper-class="admin-course-picker-popover">
            <template #reference>
              <el-button class="admin-course-add-chip" :disabled="!canModifyCourse">
                <el-icon><Plus /></el-icon>
                添加班级
              </el-button>
            </template>
            <el-select
              v-model="form.classIds"
              :disabled="!canModifyCourse"
              class="admin-course-form-picker"
              multiple
              filterable
              collapse-tags
              collapse-tags-tooltip
              placeholder="请选择班级"
            >
              <el-option
                v-for="item in classOptions"
                :key="item.classId"
                :label="item.majorName ? `${item.className}（${item.majorName}）` : item.className"
                :value="item.classId"
              >
                <div class="admin-course-form-option">
                  <strong>{{ item.className }}</strong>
                  <span>{{ item.majorName || '未配置专业' }}</span>
                </div>
              </el-option>
            </el-select>
          </el-popover>
        </div>
      </section>

      <section class="admin-course-form-card content">
        <header class="admin-course-form-card-title split">
          <span>
            <el-icon><Menu /></el-icon>
            <strong>教学内容</strong>
          </span>
          <el-button type="primary" class="admin-course-form-primary" :disabled="!canModifyCourse" @click="addChapter">
            <el-icon><Plus /></el-icon>
            新增章节
          </el-button>
        </header>

        <div class="admin-course-outline">
          <div v-if="chapters.length === 0" class="admin-course-empty inline">
            <el-empty description="暂无教学内容，请先新增章节" />
          </div>
          <article v-else v-for="chapter in chapters" :key="chapter.id" class="admin-course-outline-chapter">
            <div
              class="admin-course-outline-row admin-course-outline-chapter-row"
              @dragover.prevent
              @drop="dropChapter(chapters.indexOf(chapter))"
            >
              <span class="admin-course-outline-left">
                <span
                  class="admin-course-outline-drag"
                  :draggable="canModifyCourse"
                  title="拖动排序"
                  @dragstart="startChapterDrag(chapters.indexOf(chapter))"
                  @click.stop
                >::</span>
                <el-icon
                  class="admin-course-outline-collapse"
                  :class="{ collapsed: isOutlineCollapsed(chapter.id) }"
                  @click.stop="toggleOutlineCollapsed(chapter.id)"
                ><ArrowDown /></el-icon>
                <el-icon class="folder"><Folder /></el-icon>
                <strong>{{ chapter.title }}</strong>
              </span>
              <span class="admin-course-outline-actions">
                <el-button text type="success" :disabled="!canModifyCourse" @click="addOutlineItem(chapter, 'resource')">添加课件资源</el-button>
                <el-button text type="success" :disabled="!canModifyCourse" @click="addOutlineItem(chapter, 'homework')">添加作业</el-button>
                <el-button text type="primary" :disabled="!canModifyCourse" @click="editChapter(chapter)">编辑</el-button>
                <el-button text type="danger" :disabled="!canModifyCourse" @click="removeChapter(chapters.indexOf(chapter))">删除</el-button>
              </span>
            </div>

            <div
                v-for="item in chapter.items"
              :key="item.id"
                class="admin-course-outline-row admin-course-outline-resource-row level-one-content"
                v-show="!isOutlineCollapsed(chapter.id)"
                @dragover.prevent
                @drop="dropItem(chapter, chapter.items.indexOf(item))"
              >
              <span
                class="admin-course-outline-drag"
                :draggable="canModifyCourse"
                title="拖动排序"
                @dragstart="startItemDrag(chapter, chapter.items.indexOf(item))"
                @click.stop
              >::</span>
              <span class="admin-course-outline-icon" :class="item.type">
                <el-icon><component :is="item.type === 'homework' ? Checked : Document" /></el-icon>
              </span>
              <span class="admin-course-outline-info"><strong>{{ item.title }}</strong><small>{{ item.desc }}</small></span>
              <span class="admin-course-outline-actions compact-actions">
                <el-button text type="primary" :disabled="!canModifyCourse" @click="editOutlineItem(item)">编辑</el-button>
                <el-button text type="danger" :disabled="!canModifyCourse" @click="removeOutlineItem(chapter, chapter.items.indexOf(item))">删除</el-button>
              </span>
            </div>

            <template v-for="section in chapter.sections" :key="section.id">
              <div
                class="admin-course-outline-row admin-course-outline-section-row"
                v-show="!isOutlineCollapsed(chapter.id)"
                @dragover.prevent
                @drop="dropSection(chapter, chapter.sections.indexOf(section))"
              >
                <span class="admin-course-outline-left">
                  <span
                    class="admin-course-outline-drag"
                    :draggable="canModifyCourse"
                    title="拖动排序"
                    @dragstart="startSectionDrag(chapter, chapter.sections.indexOf(section))"
                    @click.stop
                  >::</span>
                  <el-icon
                    class="admin-course-outline-collapse"
                    :class="{ collapsed: isOutlineCollapsed(section.id) }"
                    @click.stop="toggleOutlineCollapsed(section.id)"
                  ><ArrowDown /></el-icon>
                  <strong>{{ section.title }}</strong>
                </span>
                <span class="admin-course-outline-actions">
                  <el-button text type="success" :disabled="!canModifyCourse" @click="addOutlineItem(section, 'resource')">添加课件资源</el-button>
                  <el-button text type="success" :disabled="!canModifyCourse" @click="addOutlineItem(section, 'homework')">添加作业</el-button>
                  <el-button text type="primary" :disabled="!canModifyCourse" @click="editSection(section)">编辑</el-button>
                  <el-button text type="danger" :disabled="!canModifyCourse" @click="removeSection(chapter, chapter.sections.indexOf(section))">删除</el-button>
                </span>
              </div>

              <div
                v-for="item in section.items"
                :key="item.id"
                class="admin-course-outline-row admin-course-outline-resource-row"
                v-show="!isOutlineCollapsed(chapter.id) && !isOutlineCollapsed(section.id)"
                @dragover.prevent
                @drop="dropItem(section, section.items.indexOf(item))"
              >
                <span
                  class="admin-course-outline-drag"
                  :draggable="canModifyCourse"
                  title="拖动排序"
                  @dragstart="startItemDrag(section, section.items.indexOf(item))"
                  @click.stop
                >::</span>
                <span class="admin-course-outline-icon" :class="item.type">
                  <el-icon><component :is="item.type === 'homework' ? Checked : Document" /></el-icon>
                </span>
                <span class="admin-course-outline-info">
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.desc }}</small>
                </span>
                <span class="admin-course-outline-actions compact-actions">
                  <el-button text type="primary" :disabled="!canModifyCourse" @click="editOutlineItem(item)">编辑</el-button>
                  <el-button text type="danger" :disabled="!canModifyCourse" @click="removeOutlineItem(section, section.items.indexOf(item))">删除</el-button>
                </span>
              </div>

              <template v-for="subsection in section.children" :key="subsection.id">
                <div
                  class="admin-course-outline-row admin-course-outline-section-row level-three"
                  v-show="!isOutlineCollapsed(chapter.id) && !isOutlineCollapsed(section.id)"
                  @dragover.prevent
                  @drop="dropSection(section, section.children.indexOf(subsection))"
                >
                  <span class="admin-course-outline-left">
                    <span
                      class="admin-course-outline-drag"
                      :draggable="canModifyCourse"
                      title="拖动排序"
                      @dragstart="startSectionDrag(section, section.children.indexOf(subsection))"
                      @click.stop
                    >::</span>
                    <el-icon
                      class="admin-course-outline-collapse"
                      :class="{ collapsed: isOutlineCollapsed(subsection.id) }"
                      @click.stop="toggleOutlineCollapsed(subsection.id)"
                    ><ArrowDown /></el-icon>
                    <strong>{{ subsection.title }}</strong>
                  </span>
                  <span class="admin-course-outline-actions">
                    <el-button text type="success" :disabled="!canModifyCourse" @click="addOutlineItem(subsection, 'resource')">添加课件资源</el-button>
                    <el-button text type="success" :disabled="!canModifyCourse" @click="addOutlineItem(subsection, 'homework')">添加作业</el-button>
                    <el-button text type="primary" :disabled="!canModifyCourse" @click="editSection(subsection)">编辑</el-button>
                    <el-button text type="danger" :disabled="!canModifyCourse" @click="removeSection(section, section.children.indexOf(subsection))">删除</el-button>
                  </span>
                </div>
                <div
                  v-for="item in subsection.items"
                  :key="item.id"
                  class="admin-course-outline-row admin-course-outline-resource-row level-three-content"
                  v-show="!isOutlineCollapsed(chapter.id) && !isOutlineCollapsed(section.id) && !isOutlineCollapsed(subsection.id)"
                  @dragover.prevent
                  @drop="dropItem(subsection, subsection.items.indexOf(item))"
                >
                  <span
                    class="admin-course-outline-drag"
                    :draggable="canModifyCourse"
                    title="拖动排序"
                    @dragstart="startItemDrag(subsection, subsection.items.indexOf(item))"
                    @click.stop
                  >::</span>
                  <span class="admin-course-outline-icon" :class="item.type">
                    <el-icon><component :is="item.type === 'homework' ? Checked : Document" /></el-icon>
                  </span>
                  <span class="admin-course-outline-info"><strong>{{ item.title }}</strong><small>{{ item.desc }}</small></span>
                  <span class="admin-course-outline-actions compact-actions">
                    <el-button text type="primary" :disabled="!canModifyCourse" @click="editOutlineItem(item)">编辑</el-button>
                    <el-button text type="danger" :disabled="!canModifyCourse" @click="removeOutlineItem(subsection, subsection.items.indexOf(item))">删除</el-button>
                  </span>
                </div>
              </template>
            </template>
          </article>
        </div>
      </section>

      <footer class="admin-course-form-footer">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!canModifyCourse" @click="saveCourse">保存</el-button>
      </footer>
    </section>

    <el-dialog
      v-model="outlineDialogVisible"
      :class="['admin-course-outline-dialog', { homework: outlineDialogKind === 'homework' }]"
      :width="outlineDialogKind === 'homework' ? '760px' : '520px'"
      :show-close="false"
      append-to-body
    >
      <template #header>
        <div class="admin-course-outline-dialog-head">
          <strong>{{ outlineDialogTitle }}</strong>
          <el-button text circle :icon="Close" @click="closeOutlineDialog" />
        </div>
      </template>

      <div class="admin-course-outline-dialog-body">
        <label
          v-if="outlineDialogKind === 'chapter' && outlineDialogMode === 'create'"
          class="admin-course-outline-dialog-field"
        >
          <span>所属章节</span>
          <el-select v-model="outlineForm.parentId" clearable placeholder="请选择，一级章节可不选">
            <el-option
              v-for="item in outlineParentOptions"
              :key="item.id"
              :label="item.label"
              :value="item.id"
            />
          </el-select>
        </label>
        <label v-if="outlineDialogKind !== 'homework'" class="admin-course-outline-dialog-field">
          <span>{{ outlineNameLabel }} <b>*</b></span>
          <el-input
            v-model="outlineForm.title"
            :placeholder="outlineNamePlaceholder"
            :maxlength="20"
            show-word-limit
          />
        </label>
        <div v-if="outlineDialogKind === 'homework'" class="admin-course-homework-settings">
          <label class="admin-course-outline-dialog-field">
            <span>学员完成标准 <b>*</b></span>
            <el-radio-group v-model="outlineForm.completionRule">
              <el-radio label="SUBMIT">提交即完成</el-radio>
              <el-radio label="PASS_SCORE">达到合格分数</el-radio>
            </el-radio-group>
          </label>
          <label v-if="outlineForm.completionRule === 'PASS_SCORE'" class="admin-course-outline-dialog-field">
            <span>合格分数 <b>*</b></span>
            <el-input-number
              v-model="outlineForm.passScore"
              :min="1"
              :max="selectedHomeworkTotalScore || 999999"
              :controls="false"
            />
          </label>
          <label class="admin-course-outline-dialog-field">
            <span>作业发布模式 <b>*</b></span>
            <el-radio-group v-model="outlineForm.publishMode">
              <el-radio label="PRACTICE">练习模式</el-radio>
              <el-radio label="EXAM">考试模式</el-radio>
            </el-radio-group>
            <small class="admin-course-homework-mode-help">
              练习模式可查看答案解析或实训指导；考试模式不展示答案解析与操作提示。
            </small>
          </label>
          <label class="admin-course-outline-dialog-field full">
            <span>作业答题时段 <b>*</b></span>
            <div class="admin-course-resource-date-range">
              <el-date-picker v-model="outlineForm.answerStartTime" type="datetime" placeholder="开始答题时间" />
              <i>至</i>
              <el-date-picker v-model="outlineForm.answerEndTime" type="datetime" placeholder="结束答题时间" />
            </div>
          </label>
        </div>
        <section v-if="outlineDialogKind === 'homework'" class="admin-course-homework-question-section">
          <div class="admin-course-homework-question-head">
            <div>
              <strong>作业题目</strong>
              <small>可从实训题库或理论试卷中选择作业内容</small>
            </div>
            <span>已选 {{ outlineForm.questions.length }}{{ homeworkQuestionTab === 'theory' ? ' 套试卷' : ' 道实训题' }}</span>
          </div>
          <el-tabs
            v-model="homeworkQuestionTab"
            class="admin-course-homework-question-tabs"
            @tab-change="handleQuestionTabChange"
          >
            <el-tab-pane v-for="tab in questionTabs" :key="tab.name" :label="tab.label" :name="tab.name">
              <div class="admin-course-question-picker-body">
                <div class="admin-course-question-picker-toolbar">
                  <el-input
                    v-model="questionKeyword"
                    :prefix-icon="Search"
                    clearable
                    :placeholder="`请输入${tab.label}名称搜索`"
                    @keyup.enter="loadQuestionRows"
                  />
                  <el-input
                    v-if="tab.name === 'theory'"
                    v-model="questionCourseKeyword"
                    :prefix-icon="Search"
                    clearable
                    placeholder="请输入所属课程名称搜索"
                    @keyup.enter="loadQuestionRows"
                  />
                  <el-button type="primary" @click="loadQuestionRows">查询</el-button>
                  <el-button @click="resetQuestionFilters">重置</el-button>
                </div>
                <div class="admin-course-question-picker-summary">
                  <span>
                    共 {{ questionTotal }}{{ tab.name === 'theory' ? ' 套试卷' : ' 条实训题' }}，
                    已选 {{ selectedQuestionCount(tab.name) }}{{ tab.name === 'theory' ? ' 套' : ' 条' }}
                  </span>
                  <small>灰色复选框表示已绑定当前课程，不可取消</small>
                </div>
                <div v-loading="questionPickerLoading" class="admin-course-question-picker-list">
                  <article
                    v-for="item in questionRows"
                    :key="`${item.kind}-${item.id}`"
                    class="admin-course-question-picker-row"
                    :class="{ selected: isQuestionSelected(item), disabled: isQuestionBound(item) }"
                    @click="!isQuestionBound(item) && toggleQuestionSelection(item)"
                  >
                    <el-checkbox
                      :model-value="isQuestionSelected(item)"
                      :disabled="isQuestionBound(item)"
                      @click.stop
                      @change="toggleQuestionSelection(item)"
                    />
                    <div>
                      <strong>{{ item.title }}</strong>
                      <small>{{ item.typeLabel }} · {{ item.meta }}</small>
                    </div>
                    <b v-if="item.score">{{ item.score }} 分</b>
                    <el-button text type="primary" @click.stop="openQuestionDetail(item)">查看详情</el-button>
                  </article>
                  <el-empty v-if="!questionPickerLoading && questionRows.length === 0" description="暂无可添加题目" />
                </div>
                <div class="admin-course-question-picker-footer">
                  <span>已选 {{ selectedQuestionCount(tab.name) }}{{ tab.name === 'theory' ? ' 套试卷' : ' 道实训题' }}</span>
                  <el-pagination
                    layout="prev, pager, next"
                    :current-page="questionPage"
                    :page-size="questionPageSize"
                    :total="questionTotal"
                    @current-change="handleQuestionPageChange"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>

          <div v-if="outlineForm.questions.length" class="admin-course-homework-question-list">
            <div class="admin-course-homework-question-list-head">
              <strong>已添加题目</strong>
              <span>点击“查看详情”可预览题干内容</span>
            </div>
            <article v-for="item in outlineForm.questions" :key="`${item.kind}-${item.id}`">
              <span class="admin-course-homework-question-type" :class="item.kind">
                {{ item.kind === 'practice' ? '实训题' : '理论试卷' }}
              </span>
              <div>
                <strong>{{ item.title }}</strong>
                <small>{{ item.typeLabel }} · {{ item.score ? `${item.score} 分` : item.meta }}</small>
              </div>
              <el-button text type="primary" @click="openQuestionDetail(item)">查看详情</el-button>
              <el-button text type="danger" :disabled="!canModifyCourse" @click="removeHomeworkQuestion(item)">移除</el-button>
            </article>
          </div>
          <div v-else class="admin-course-homework-question-empty">
            <el-icon><Document /></el-icon>
            <span>暂未添加题目，请在上方选择实训题或理论试卷</span>
          </div>
        </section>
      </div>

      <template #footer>
        <div class="admin-course-outline-dialog-footer">
          <el-button @click="closeOutlineDialog">取消</el-button>
          <el-button type="primary" :disabled="!canModifyCourse" @click="confirmOutlineDialog">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="questionDetailVisible"
      class="admin-course-question-detail-dialog"
      width="720px"
      :show-close="false"
      append-to-body
    >
      <template #header>
        <div class="admin-course-question-dialog-head">
          <div>
            <strong>{{ questionDetail?.kind === 'practice' ? '实训题详情' : '理论试卷详情' }}</strong>
            <span>{{ questionDetail?.typeLabel || '题目详情' }}</span>
          </div>
          <el-button text circle :icon="Close" @click="questionDetailVisible = false" />
        </div>
      </template>

      <div v-if="questionDetailLoading" class="admin-course-question-detail-loading">题目详情加载中...</div>
      <div v-else-if="questionDetail" class="admin-course-question-detail-body">
        <div class="admin-course-question-detail-meta">
          <span class="admin-course-homework-question-type" :class="questionDetail.kind">
            {{ questionDetail.kind === 'practice' ? '实训题' : '理论试卷' }}
          </span>
          <span>{{ questionDetail.typeLabel }}</span>
          <span v-if="questionDetail.score">{{ questionDetail.score }} 分</span>
          <span>{{ questionDetail.meta }}</span>
        </div>
        <h2>{{ questionDetail.title }}</h2>
        <section v-if="questionDetail.options?.length" class="admin-course-question-detail-block">
          <strong>选项</strong>
          <ol>
            <li v-for="option in questionDetail.options" :key="option.label" :class="{ correct: option.correct }">
              <span>{{ option.label }}</span>
              <b v-if="option.correct">正确答案</b>
            </li>
          </ol>
        </section>
        <section v-if="questionDetail.coverUrl" class="admin-course-question-detail-block">
          <strong>实训封面</strong>
          <img :src="questionDetail.coverUrl" alt="实训题封面" class="admin-course-question-detail-cover" />
        </section>
        <section v-if="questionDetail.answer" class="admin-course-question-detail-block">
          <strong>{{ questionDetail.kind === 'practice' ? '任务说明' : '标准答案' }}</strong>
          <p>{{ questionDetail.answer }}</p>
        </section>
        <section v-if="questionDetail.extra" class="admin-course-question-detail-block">
          <strong>{{ questionDetail.kind === 'practice' ? '实训信息' : '题目来源' }}</strong>
          <p>{{ questionDetail.extra }}</p>
        </section>
      </div>

      <template #footer>
        <div class="admin-course-question-dialog-footer">
          <el-button type="primary" @click="questionDetailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="resourceDialogVisible"
      class="admin-course-resource-dialog"
      width="920px"
      :show-close="false"
      append-to-body
    >
      <template #header>
        <div class="admin-course-resource-dialog-head">
          <strong>{{ resourceEditingItem ? '编辑教学资源' : '添加教学资源' }}</strong>
          <el-button text circle :icon="Close" @click="resourceDialogVisible = false" />
        </div>
      </template>

      <div class="admin-course-resource-dialog-body">
        <section class="admin-course-resource-step-card">
          <div class="admin-course-resource-step-title">
            <span>1</span>
            <strong>设置学习限制</strong>
          </div>
          <div class="admin-course-resource-limit-fields">
            <label class="admin-course-resource-limit-field">
              <span>开放时段</span>
              <div class="admin-course-resource-date-range">
                <el-date-picker
                  v-model="resourceOpenStartTime"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm"
                  placeholder="开始时间"
                />
                <i>至</i>
                <el-date-picker
                  v-model="resourceOpenEndTime"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm"
                  placeholder="结束时间"
                />
              </div>
            </label>
            <label class="admin-course-resource-limit-field">
              <span>最低学习时长</span>
              <div class="admin-course-resource-duration">
                <el-input-number
                  v-model="resourceStudyMinutes"
                  :min="0"
                  :max="999"
                  :controls="false"
                  placeholder="请输入"
                />
                <em>分</em>
                <el-input-number
                  v-model="resourceStudySeconds"
                  :min="0"
                  :max="59"
                  :controls="false"
                  placeholder="请输入"
                />
                <em>秒</em>
                <small>（学习时长达到才算完成）</small>
              </div>
            </label>
          </div>
        </section>

        <section class="admin-course-resource-step-card admin-course-resource-selection-card">
          <div class="admin-course-resource-step-title">
            <span>2</span>
            <strong>选择资源</strong>
          </div>

          <div class="admin-course-resource-filter">
            <div class="admin-course-resource-filter-row">
              <label class="admin-course-resource-filter-field resource-name">
                <span>资源名称</span>
                <el-input
                  v-model="resourceKeyword"
                  :prefix-icon="Search"
                  clearable
                  placeholder="请输入资源名称搜索"
                  @keyup.enter="handleResourceSearch"
                  @clear="handleResourceSearch"
                />
              </label>
              <label class="admin-course-resource-filter-field resource-type">
                <span>资源分类</span>
                <el-select v-model="resourceTypeFilter" placeholder="请选择资源分类" clearable>
                  <el-option v-for="item in resourceTypeTabs" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </label>
            </div>
            <div class="admin-course-resource-filter-row">
              <label class="admin-course-resource-filter-field resource-major">
                <span>所属专业</span>
                <el-select v-model="resourceMajorId" placeholder="请选择所属专业" clearable>
                  <el-option v-for="item in majorOptions" :key="item.majorId" :label="item.majorName" :value="item.majorId" />
                </el-select>
              </label>
              <label class="admin-course-resource-filter-field resource-course">
                <span>适用课程</span>
                <el-input
                  v-model="resourceCourseKeyword"
                  :prefix-icon="Search"
                  clearable
                  placeholder="请输入适用课程搜索"
                  @keyup.enter="handleResourceSearch"
                  @clear="handleResourceSearch"
                />
              </label>
              <div class="admin-course-resource-filter-actions">
                <el-button type="primary" @click="handleResourceSearch">查询</el-button>
                <el-button @click="resetResourceFilters">重置</el-button>
              </div>
            </div>
          </div>

          <div class="admin-course-resource-selection-summary">
            <span>共 <b>{{ resourceTotal }}</b> 条资源，已选 <b>{{ resourceSelectedCount }}</b> 条</span>
            <small>灰色复选框表示资源已绑定当前课程，不可取消</small>
          </div>

          <div class="admin-course-resource-table" v-loading="resourceLoading">
            <div class="admin-course-resource-table-head">
              <span class="check-cell">
                <el-checkbox
                  :model-value="resourcePageSelected"
                  :indeterminate="resourcePageIndeterminate"
                  @change="toggleResourcePageSelection"
                />
              </span>
              <span>序号</span>
              <span>资源名称</span>
              <span>分类</span>
              <span>所属专业</span>
              <span>所属课程</span>
            </div>
            <div
              v-for="(item, index) in resourceRows"
              :key="item.resourceId"
              class="admin-course-resource-row"
              :class="{ active: isResourceSelected(item), disabled: isResourceBound(item) }"
              @click="!isResourceBound(item) && toggleResourceSelection(item, !isResourceSelected(item))"
            >
              <span class="check-cell" @click.stop>
                <el-checkbox
                  :model-value="isResourceSelected(item)"
                  :disabled="isResourceBound(item)"
                  @change="handleResourceCheckboxChange(item, $event)"
                />
              </span>
              <span>{{ (resourcePage - 1) * resourcePageSize + index + 1 }}</span>
              <span class="admin-course-resource-name" :title="item.resourceName">{{ item.resourceName }}</span>
              <span>
                <b class="admin-course-resource-type-tag" :class="resourceTypeTone(item.resourceType)">
                  {{ resourceCategoryLabel(item.resourceType) }}
                </b>
              </span>
              <span class="admin-course-resource-text" :title="item.majorName">{{ item.majorName || '-' }}</span>
              <span class="admin-course-resource-text" :title="item.courseName">{{ item.courseName || '-' }}</span>
            </div>
            <el-empty v-if="!resourceLoading && resourceRows.length === 0" description="暂无可添加资源" />
          </div>

          <div class="admin-course-resource-pagination">
            <span>显示 {{ resourcePageStart }} 到 {{ resourcePageEnd }} 条，共 {{ resourceTotal }} 条记录</span>
            <el-pagination
              layout="prev, pager, next"
              :current-page="resourcePage"
              :page-size="resourcePageSize"
              :total="resourceTotal"
              @current-change="handleResourcePageChange"
            />
          </div>
        </section>
      </div>

      <template #footer>
        <div class="admin-course-resource-dialog-footer">
          <el-button @click="resourceDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!canModifyCourse || !resourceSelectedCount" @click="confirmAddResource">
            {{ resourceEditingItem ? '确定' : '确定添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ArrowDown,
  ArrowLeft,
  Checked,
  Close,
  Document,
  Folder,
  InfoFilled,
  Menu,
  Plus,
  Search,
  User,
  UserFilled
} from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  createAdminCourse,
  fetchAdminAcademicYears,
  fetchAdminClasses,
  fetchAdminCourseDetail,
  fetchAdminMajors,
  fetchAdminTeachers,
  updateAdminCourse,
  type AdminCourseCommand,
  type AdminAcademicYearOption,
  type AdminClassOption,
  type AdminMajorOption,
  type AdminTeacherOption
} from '../../api/admin-course';
import type { AdminCourseChapter, AdminCourseContent } from '../../features/admin/courses';
import {
  fetchAdminQuestion,
  type AdminQuestion
} from '../../api/admin-question';
import { fetchAdminPaper, fetchAdminPapers, type AdminPaper } from '../../api/admin-paper';
import {
  fetchAdminTraining,
  fetchAdminTrainings,
  type AdminTraining
} from '../../api/admin-training';
import {
  fetchAdminPublicResources,
  fetchAdminResources,
  type AdminResource,
  type AdminResourceQuery
} from '../../api/admin-resource';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

const route = useRoute();
const router = useRouter();
const courseId = computed(() => Number(route.params.id));
const isEditMode = computed(() => route.name === 'admin-course-edit');
const copySourceId = computed(() => Number(route.query.copyFrom));
const isCopyMode = computed(() => !isEditMode.value && Number.isFinite(copySourceId.value) && copySourceId.value > 0);
const { can } = useAdminPermissions('teaching:course');
const canModifyCourse = computed(() => can(isEditMode.value ? 'update' : 'create'));
const pageTitle = computed(() => (isEditMode.value ? '编辑课程' : '新增课程'));

const form = reactive({
  courseName: '',
  startTime: undefined as Date | undefined,
  endTime: undefined as Date | undefined,
  semesterKey: '',
  majorId: undefined as number | undefined,
  coverUrl: '',
  coursewareScore: undefined as number | undefined,
  learningMode: 'SELF_PACED',
  teacherIds: [] as number[],
  classIds: [] as number[]
});

const saving = ref(false);
const loading = ref(false);
const academicYears = ref<AdminAcademicYearOption[]>([]);
const majorOptions = ref<AdminMajorOption[]>([]);
const classOptions = ref<AdminClassOption[]>([]);
const teacherOptions = ref<AdminTeacherOption[]>([]);

const semesterOptions = computed(() =>
  academicYears.value.flatMap((year) =>
    (year.semesters ?? []).map((semester) => ({
      key: `${year.academicYearId}:${semester.semesterId}`,
      label: `${year.yearName} ${semester.semesterName}`,
      academicYearId: year.academicYearId,
      semesterId: semester.semesterId,
      current: semester.current
    }))
  )
);

const selectedTeachers = computed(() => teacherOptions.value.filter((item) => form.teacherIds.includes(item.userId)));
const selectedClasses = computed(() => classOptions.value.filter((item) => form.classIds.includes(item.classId)));

interface OutlineItem {
  id: number;
  contentId?: number;
  type: 'homework' | 'resource';
  title: string;
  desc: string;
  questions?: HomeworkQuestion[];
  resourceId?: number;
  assignmentId?: number;
  requiredDurationSeconds?: number;
  learningStartTime?: string;
  learningEndTime?: string;
  assignmentCompletionRule?: 'SUBMIT' | 'PASS_SCORE';
  passScore?: number;
  assignmentPublishMode?: 'PRACTICE' | 'EXAM';
  answerStartTime?: string;
  answerEndTime?: string;
  trainingIds?: number[];
}

interface OutlineSection {
  id: number;
  chapterId?: number;
  title: string;
  items: OutlineItem[];
  children: OutlineSection[];
}

interface OutlineChapter {
  id: number;
  chapterId?: number;
  title: string;
  items: OutlineItem[];
  sections: OutlineSection[];
}

type OutlineContentContainer = OutlineChapter | OutlineSection;

type OutlineDialogKind = 'chapter' | 'section' | 'homework';
type OutlineDialogMode = 'create' | 'edit';
type HomeworkQuestionKind = 'practice' | 'theory';

interface HomeworkQuestion {
  id: number;
  kind: HomeworkQuestionKind;
  title: string;
  typeLabel: string;
  score: number;
  meta: string;
  coverUrl?: string;
  paperId?: number;
  questionIds?: number[];
  courseName?: string;
  creatorName?: string;
  createdAt?: string;
}

interface HomeworkQuestionOption {
  label: string;
  correct: boolean;
}

interface HomeworkQuestionDetail extends HomeworkQuestion {
  answer?: string;
  options?: HomeworkQuestionOption[];
  extra?: string;
}

const chapters = ref<OutlineChapter[]>([]);
const collapsedOutlineIds = ref<Set<number>>(new Set());

function isOutlineCollapsed(id: number) {
  return collapsedOutlineIds.value.has(id);
}

function toggleOutlineCollapsed(id: number) {
  const next = new Set(collapsedOutlineIds.value);
  if (next.has(id)) {
    next.delete(id);
  } else {
    next.add(id);
  }
  collapsedOutlineIds.value = next;
}
let outlineIdSeed = 1;
type OutlineDragState =
  | { type: 'chapter'; source: null; index: number }
  | { type: 'section'; source: OutlineContentContainer; index: number }
  | { type: 'item'; source: OutlineContentContainer; index: number };
const outlineDragState = ref<OutlineDragState | null>(null);
const outlineDialogVisible = ref(false);
const outlineDialogKind = ref<OutlineDialogKind>('chapter');
const outlineDialogMode = ref<OutlineDialogMode>('create');
const outlineForm = reactive({
  title: '',
  parentId: undefined as number | undefined,
  desc: '',
  questions: [] as HomeworkQuestion[],
  completionRule: 'SUBMIT' as 'SUBMIT' | 'PASS_SCORE',
  passScore: 60,
  publishMode: 'PRACTICE' as 'PRACTICE' | 'EXAM',
  answerStartTime: undefined as Date | undefined,
  answerEndTime: undefined as Date | undefined
});
const outlineTargetChapter = ref<OutlineChapter | null>(null);
const outlineTargetSection = ref<OutlineContentContainer | null>(null);
const outlineTargetItem = ref<OutlineItem | null>(null);
const homeworkQuestionTab = ref<HomeworkQuestionKind>('practice');
const questionTabs: Array<{ label: string; name: HomeworkQuestionKind }> = [
  { label: '实训题', name: 'practice' },
  { label: '理论试卷', name: 'theory' }
];
const questionPickerLoading = ref(false);
const questionKeyword = ref('');
const questionCourseKeyword = ref('');
const questionPage = ref(1);
const questionPageSize = 8;
const questionTotal = ref(0);
const questionRows = ref<HomeworkQuestion[]>([]);
const questionDetailVisible = ref(false);
const questionDetailLoading = ref(false);
const questionDetail = ref<HomeworkQuestionDetail>();
const resourceDialogVisible = ref(false);
const resourceLoading = ref(false);
const resourceKeyword = ref('');
const resourceTypeFilter = ref('');
const resourceMajorId = ref<number | null>(null);
const resourceCourseKeyword = ref('');
const resourceRows = ref<AdminResource[]>([]);
const resourceTotal = ref(0);
const resourcePage = ref(1);
const resourcePageSize = 8;
const resourceSelectedIds = ref<number[]>([]);
const resourceSelectedMap = ref<Record<number, AdminResource>>({});
const resourceOpenStartTime = ref<Date>();
const resourceOpenEndTime = ref<Date>();
const resourceStudyMinutes = ref(0);
const resourceStudySeconds = ref(0);
const resourceTargetSection = ref<OutlineContentContainer | null>(null);
const resourceEditingItem = ref<OutlineItem | null>(null);
const resourceTypeTabs = [
  { label: '全部资源', value: '' },
  { label: '文本文件', value: 'DOCUMENT' },
  { label: '演示文稿', value: 'PRESENTATION' },
  { label: '视频', value: 'VIDEO' },
  { label: '音频', value: 'AUDIO' },
  { label: '图像', value: 'IMAGE' }
];

const resourceSelectedCount = computed(() => resourceSelectedIds.value.length);
const selectedHomeworkTotalScore = computed(() =>
  outlineForm.questions.reduce((total, item) => total + Number(item.score || 0), 0)
);
const boundResourceIds = computed(() =>
  new Set(
    flattenOutlineItems()
      .filter((item) => item.type === 'resource' && item.resourceId)
      .map((item) => item.resourceId as number)
  )
);
const resourcePageStart = computed(() =>
  resourceTotal.value === 0 ? 0 : (resourcePage.value - 1) * resourcePageSize + 1
);
const resourcePageEnd = computed(() => Math.min(resourcePage.value * resourcePageSize, resourceTotal.value));
const resourcePageSelected = computed(
  () => {
    const selectableRows = resourceRows.value.filter((item) => !isResourceBound(item));
    return selectableRows.length > 0 && selectableRows.every((item) => resourceSelectedIds.value.includes(item.resourceId));
  }
);
const resourcePageIndeterminate = computed(
  () =>
    !resourcePageSelected.value &&
    resourceRows.value.some((item) => resourceSelectedIds.value.includes(item.resourceId))
);

const outlineParentOptions = computed(() =>
  chapters.value.flatMap((chapter) => [
    { id: chapter.id, label: chapter.title },
    ...chapter.sections.map((section) => ({ id: section.id, label: `　${section.title}` }))
  ])
);

const outlineDialogTitle = computed(() => {
  const action = outlineDialogMode.value === 'create' ? '新增' : '编辑';
  const names: Record<OutlineDialogKind, string> = {
    chapter: '章节',
    section: '小节',
    homework: '作业'
  };
  return `${action}${names[outlineDialogKind.value]}`;
});

const outlineNameLabel = computed(() => {
  const labels: Record<OutlineDialogKind, string> = {
    chapter: '章节名称',
    section: '小节名称',
    homework: '作业名称'
  };
  return labels[outlineDialogKind.value];
});

const outlineNamePlaceholder = computed(() => `请输入${outlineNameLabel.value}`);

function goBack() {
  router.push('/admin/courses');
}

function nextOutlineId() {
  outlineIdSeed += 1;
  return outlineIdSeed;
}

function childSections(parent: OutlineContentContainer) {
  return 'sections' in parent ? parent.sections : parent.children;
}

function flattenSectionItems(sections: OutlineSection[]): OutlineItem[] {
  return sections.flatMap((section) => [...section.items, ...flattenSectionItems(section.children)]);
}

function flattenOutlineItems(): OutlineItem[] {
  return chapters.value.flatMap((chapter) => [...chapter.items, ...flattenSectionItems(chapter.sections)]);
}

function resetOutlineDialog() {
  outlineForm.title = '';
  outlineForm.parentId = undefined;
  outlineForm.desc = '';
  outlineForm.questions = [];
  outlineForm.completionRule = 'SUBMIT';
  outlineForm.passScore = 60;
  outlineForm.publishMode = 'PRACTICE';
  outlineForm.answerStartTime = undefined;
  outlineForm.answerEndTime = undefined;
  outlineTargetChapter.value = null;
  outlineTargetSection.value = null;
  outlineTargetItem.value = null;
}

function closeOutlineDialog() {
  outlineDialogVisible.value = false;
  resetOutlineDialog();
}

function openOutlineDialog(kind: OutlineDialogKind, mode: OutlineDialogMode) {
  outlineDialogKind.value = kind;
  outlineDialogMode.value = mode;
  outlineDialogVisible.value = true;
  if (kind === 'homework') {
    homeworkQuestionTab.value = 'practice';
    questionKeyword.value = '';
    questionPage.value = 1;
    questionRows.value = [];
    questionTotal.value = 0;
    void loadQuestionRows();
  }
}

function addChapter() {
  resetOutlineDialog();
  openOutlineDialog('chapter', 'create');
}

function startChapterDrag(index: number) {
  outlineDragState.value = { type: 'chapter', source: null, index };
}

function startSectionDrag(source: OutlineContentContainer, index: number) {
  outlineDragState.value = { type: 'section', source, index };
}

function startItemDrag(source: OutlineContentContainer, index: number) {
  outlineDragState.value = { type: 'item', source, index };
}

function clearOutlineDrag() {
  outlineDragState.value = null;
}

function dropChapter(targetIndex: number) {
  const drag = outlineDragState.value;
  clearOutlineDrag();
  if (!drag || drag.type !== 'chapter' || drag.index === targetIndex) {
    return;
  }
  const [moved] = chapters.value.splice(drag.index, 1);
  chapters.value.splice(targetIndex, 0, moved);
}

function dropSection(parent: OutlineContentContainer, targetIndex: number) {
  const drag = outlineDragState.value;
  clearOutlineDrag();
  if (!drag || drag.type !== 'section' || drag.source !== parent || drag.index === targetIndex) {
    return;
  }
  const sections = childSections(parent);
  const [moved] = sections.splice(drag.index, 1);
  sections.splice(targetIndex, 0, moved);
}

function dropItem(parent: OutlineContentContainer, targetIndex: number) {
  const drag = outlineDragState.value;
  clearOutlineDrag();
  if (!drag || drag.type !== 'item' || drag.source !== parent || drag.index === targetIndex) {
    return;
  }
  const [moved] = parent.items.splice(drag.index, 1);
  parent.items.splice(targetIndex, 0, moved);
}

function editChapter(chapter: OutlineChapter) {
  resetOutlineDialog();
  outlineForm.title = chapter.title;
  outlineTargetChapter.value = chapter;
  openOutlineDialog('chapter', 'edit');
}

async function removeChapter(index: number) {
  try {
    await ElMessageBox.confirm('确认删除该章节？', '删除章节', { type: 'warning' });
    chapters.value.splice(index, 1);
  } catch {
    return;
  }
}

function editSection(section: OutlineSection) {
  resetOutlineDialog();
  outlineForm.title = section.title;
  outlineTargetSection.value = section;
  openOutlineDialog('section', 'edit');
}

async function removeSection(parent: OutlineContentContainer, index: number) {
  try {
    await ElMessageBox.confirm('确认删除该小节？', '删除小节', { type: 'warning' });
    childSections(parent).splice(index, 1);
  } catch {
    return;
  }
}

async function addOutlineItem(section: OutlineContentContainer, type: OutlineItem['type']) {
  if (type === 'resource') {
    openResourceDialog(section);
    return;
  }

  resetOutlineDialog();
  outlineTargetSection.value = section;
  openOutlineDialog('homework', 'create');
}

function questionKey(item: HomeworkQuestion) {
  return `${item.kind}-${item.id}`;
}

function questionTypeLabel(type?: string) {
  const labels: Record<string, string> = {
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    JUDGE: '判断题',
    FILL_BLANK: '填空题',
    SHORT_ANSWER: '简答题'
  };
  return type ? labels[type.toUpperCase()] || type : '理论试题';
}

function mapTheoryQuestion(item: AdminQuestion): HomeworkQuestion {
  return {
    id: item.questionId,
    kind: 'theory',
    title: item.title,
    typeLabel: questionTypeLabel(item.questionType),
    score: Number(item.score || 0),
    meta: item.creatorName ? `添加人：${item.creatorName}` : '理论题库'
  };
}

function mapTheoryPaper(item: AdminPaper): HomeworkQuestion {
  return {
    id: item.paperId,
    paperId: item.paperId,
    kind: 'theory',
    title: item.paperName,
    typeLabel: '理论试卷',
    score: Number(item.totalScore || 0),
    meta: `${item.questionCount || item.questions?.length || 0} 道题 · ${item.courseName || '未绑定课程'} · ${item.creatorName || '-'}`,
    courseName: item.courseName,
    creatorName: item.creatorName,
    createdAt: item.createdAt
  };
}

function mapPracticeQuestion(item: AdminTraining): HomeworkQuestion {
  const meta = [item.majorName, item.trainingMode, item.publishStatus].filter(Boolean).join(' · ');
  return {
    id: item.trainingId,
    kind: 'practice',
    title: item.trainingName || `实训题 ${item.trainingId}`,
    typeLabel: item.trainingType || '实训任务',
    score: Number(item.totalScore || 0),
    meta: meta || '实训题库',
    coverUrl: item.coverUrl
  };
}

function selectedQuestionCount(kind: HomeworkQuestionKind) {
  return outlineForm.questions.filter((item) => item.kind === kind).length;
}

function isQuestionSelected(item: HomeworkQuestion) {
  return outlineForm.questions.some((question) => questionKey(question) === questionKey(item));
}

function isQuestionBound(item: HomeworkQuestion) {
  return flattenOutlineItems().some((outlineItem) =>
    outlineItem.questions?.some((question) => questionKey(question) === questionKey(item))
  );
}

function handleQuestionTabChange(value: string | number) {
  homeworkQuestionTab.value = value === 'theory' ? 'theory' : 'practice';
  questionKeyword.value = '';
  questionCourseKeyword.value = '';
  questionPage.value = 1;
  void loadQuestionRows();
}

async function loadQuestionRows() {
  questionPickerLoading.value = true;
  try {
    if (homeworkQuestionTab.value === 'theory') {
      const result = await fetchAdminPapers({
        keyword: questionKeyword.value,
        courseName: questionCourseKeyword.value,
        publishStatus: 'PUBLISHED',
        page: questionPage.value,
        pageSize: questionPageSize
      });
      questionRows.value = result.records.map(mapTheoryPaper);
      questionTotal.value = result.total;
    } else {
      const result = await fetchAdminTrainings({
        keyword: questionKeyword.value,
        page: questionPage.value,
        pageSize: questionPageSize
      });
      questionRows.value = result.records.map(mapPracticeQuestion);
      questionTotal.value = result.total;
    }
  } catch (error) {
    questionRows.value = [];
    questionTotal.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '题目列表加载失败');
  } finally {
    questionPickerLoading.value = false;
  }
}

function resetQuestionFilters() {
  questionKeyword.value = '';
  questionCourseKeyword.value = '';
  questionPage.value = 1;
  void loadQuestionRows();
}

async function toggleQuestionSelection(item: HomeworkQuestion) {
  if (isQuestionBound(item)) {
    return;
  }
  const selectedIndex = outlineForm.questions.findIndex(
    (question) => questionKey(question) === questionKey(item)
  );
  if (selectedIndex >= 0) {
    outlineForm.questions.splice(selectedIndex, 1);
  } else {
    if (outlineForm.questions.some((question) => question.kind !== item.kind)) {
      ElMessage.warning('同一作业只能选择实训题或理论试卷中的一种');
      return;
    }
    let selected = { ...item };
    if (item.kind === 'theory' && item.paperId && !item.questionIds?.length) {
      try {
        const detail = await fetchAdminPaper(item.paperId);
        const questionIds = (detail.questions ?? []).map((question) => question.questionId).filter((id): id is number => Number.isFinite(id));
        if (!questionIds.length) {
          ElMessage.warning('该理论试卷暂无可用试题');
          return;
        }
        selected = {
          ...item,
          title: detail.paperName || item.title,
          score: Number(detail.totalScore || item.score || 0),
          questionIds,
          meta: `${questionIds.length} 道题 · ${detail.courseName || '未绑定课程'}`
        };
      } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '理论试卷详情加载失败');
        return;
      }
    }
    outlineForm.questions.push(selected);
  }
}

function handleQuestionPageChange(page: number) {
  questionPage.value = page;
  void loadQuestionRows();
}

function removeHomeworkQuestion(item: HomeworkQuestion) {
  outlineForm.questions = outlineForm.questions.filter((question) => questionKey(question) !== questionKey(item));
}

async function openQuestionDetail(item: HomeworkQuestion) {
  questionDetailVisible.value = true;
  questionDetailLoading.value = true;
  questionDetail.value = { ...item };
  try {
    if (item.kind === 'theory') {
      if (item.paperId) {
        const detail = await fetchAdminPaper(item.paperId);
        const firstQuestion = detail.questions?.[0];
        questionDetail.value = {
          ...item,
          title: detail.paperName || item.title,
          typeLabel: '理论试卷',
          score: Number(detail.totalScore || item.score || 0),
          answer: `共 ${detail.questionCount || detail.questions?.length || item.questionIds?.length || 0} 道试题`,
          extra: detail.creatorName ? `添加人：${detail.creatorName}` : '理论试卷',
          options: firstQuestion?.options?.map((option) => ({
            label: `${option.optionKey || ''} ${option.optionText || ''}`.trim() || '未命名选项',
            correct: Boolean(option.correct)
          }))
        };
      } else {
        const detail = await fetchAdminQuestion(item.id);
        const standardAnswers = new Set(
          (detail.standardAnswer ?? '')
            .split(/[,\s，、;；]+/)
            .map((answer: string) => answer.trim().toUpperCase())
            .filter(Boolean)
        );
        questionDetail.value = {
          ...item,
          title: detail.title || item.title,
          typeLabel: questionTypeLabel(detail.questionType),
          score: Number(detail.score || item.score || 0),
          options: detail.options?.map((option) => {
            const optionKey = option.optionKey?.trim().toUpperCase();
            return {
              label: `${option.optionKey || ''} ${option.optionText || ''}`.trim() || '未命名选项',
              correct: Boolean(option.correct || (optionKey && standardAnswers.has(optionKey)))
            };
          }),
          answer: detail.standardAnswer || '暂未配置标准答案',
          extra: detail.creatorName ? `添加人：${detail.creatorName}` : '理论试卷'
        };
      }
    } else {
      const detail = await fetchAdminTraining(item.id);
      questionDetail.value = {
        ...item,
        title: detail.trainingName || item.title,
        typeLabel: detail.trainingType || item.typeLabel,
        meta: [detail.majorName, detail.trainingMode].filter(Boolean).join(' · ') || item.meta,
        coverUrl: detail.coverUrl || item.coverUrl,
        answer: detail.coverUrl ? '已配置实训任务封面和实训内容' : '该实训题暂无任务说明',
        extra: `开放时间：${formatDateTime(detail.openStartTime)} 至 ${formatDateTime(detail.openEndTime)}`
      };
    }
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '题目详情加载失败');
  } finally {
    questionDetailLoading.value = false;
  }
}

function openResourceDialog(section: OutlineContentContainer | null, editingItem?: OutlineItem) {
  resourceTargetSection.value = section;
  resourceEditingItem.value = editingItem ?? null;
  resourceSelectedIds.value = editingItem?.resourceId ? [editingItem.resourceId] : [];
  resourceSelectedMap.value = editingItem?.resourceId
    ? {
        [editingItem.resourceId]: {
          resourceId: editingItem.resourceId,
          resourceName: editingItem.title
        }
      }
    : {};
  resourceKeyword.value = '';
  resourceTypeFilter.value = '';
  resourceMajorId.value = null;
  resourceCourseKeyword.value = '';
  resourcePage.value = 1;
  resourceOpenStartTime.value = parseDateTime(editingItem?.learningStartTime);
  resourceOpenEndTime.value = parseDateTime(editingItem?.learningEndTime);
  const duration = editingItem?.requiredDurationSeconds ?? 0;
  resourceStudyMinutes.value = Math.floor(duration / 60);
  resourceStudySeconds.value = duration % 60;
  resourceDialogVisible.value = true;
  void loadResourceRows();
}

async function loadResourceRows() {
  resourceLoading.value = true;
  try {
    const query: AdminResourceQuery = {
      keyword: resourceKeyword.value.trim(),
      resourceType: resourceTypeFilter.value,
      majorId: resourceMajorId.value,
      courseName: resourceCourseKeyword.value.trim()
    };
    const [personalResources, publicResources] = await Promise.all([
      loadAllResourcePages(fetchAdminResources, query),
      loadAllResourcePages(fetchAdminPublicResources, query)
    ]);
    const merged = new Map<number, AdminResource>();
    [...personalResources, ...publicResources].forEach((item) => {
      const sourceId = item.sourceResourceId ?? item.resourceId;
      if (!merged.has(sourceId)) {
        merged.set(sourceId, { ...item, resourceId: sourceId });
      }
    });
    const records = Array.from(merged.values());
    records.forEach((item) => {
      if (resourceSelectedIds.value.includes(item.resourceId)) {
        resourceSelectedMap.value[item.resourceId] = item;
      }
    });
    resourceTotal.value = records.length;
    const offset = (resourcePage.value - 1) * resourcePageSize;
    resourceRows.value = records.slice(offset, offset + resourcePageSize);
  } catch (error) {
    resourceRows.value = [];
    resourceTotal.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '资源列表加载失败');
  } finally {
    resourceLoading.value = false;
  }
}

async function loadAllResourcePages(
  fetchPage: (query?: AdminResourceQuery) => Promise<{ records: AdminResource[]; total: number }>,
  query: AdminResourceQuery
) {
  const records: AdminResource[] = [];
  const pageSize = 100;
  let page = 1;
  let total = 0;
  do {
    const result = await fetchPage({ ...query, page, pageSize });
    records.push(...result.records);
    total = result.total;
    if (result.records.length === 0) {
      break;
    }
    page += 1;
  } while (records.length < total);
  return records;
}

function handleResourceSearch() {
  resourcePage.value = 1;
  void loadResourceRows();
}

function resetResourceFilters() {
  resourceKeyword.value = '';
  resourceTypeFilter.value = '';
  resourceMajorId.value = null;
  resourceCourseKeyword.value = '';
  handleResourceSearch();
}

function handleResourcePageChange(page: number) {
  resourcePage.value = page;
  void loadResourceRows();
}

function isResourceSelected(item: AdminResource) {
  return resourceSelectedIds.value.includes(item.resourceId);
}

function isResourceBound(item: AdminResource) {
  return boundResourceIds.value.has(item.resourceId) && resourceEditingItem.value?.resourceId !== item.resourceId;
}

function toggleResourceSelection(item: AdminResource, checked: boolean) {
  if (isResourceBound(item)) {
    return;
  }
  const nextIds = new Set(resourceSelectedIds.value);
  if (checked) {
    if (resourceEditingItem.value) {
      resourceSelectedMap.value = { [item.resourceId]: item };
      resourceSelectedIds.value = [item.resourceId];
      return;
    }
    nextIds.add(item.resourceId);
    resourceSelectedMap.value[item.resourceId] = item;
  } else {
    nextIds.delete(item.resourceId);
    delete resourceSelectedMap.value[item.resourceId];
  }
  resourceSelectedIds.value = Array.from(nextIds);
}

function toggleResourcePageSelection(value: boolean | string | number) {
  const checked = Boolean(value);
  resourceRows.value.filter((item) => !isResourceBound(item)).forEach((item) => toggleResourceSelection(item, checked));
}

function handleResourceCheckboxChange(item: AdminResource, value: boolean | string | number) {
  toggleResourceSelection(item, Boolean(value));
}

function resourceCategoryLabel(type?: string) {
  const labels: Record<string, string> = {
    DOCUMENT: '文本文件',
    PRESENTATION: '演示文稿',
    VIDEO: '视频',
    AUDIO: '音频',
    IMAGE: '图像',
    EXAM: '试题'
  };
  return type ? labels[type] || type : '未分类';
}

function resourceTypeTone(type?: string) {
  const tones: Record<string, string> = {
    DOCUMENT: 'document',
    PRESENTATION: 'presentation',
    VIDEO: 'video',
    AUDIO: 'audio',
    IMAGE: 'image',
    EXAM: 'exam'
  };
  return tones[type || ''] || 'default';
}

function confirmAddResource() {
  if (!resourceSelectedIds.value.length || (!resourceTargetSection.value && !resourceEditingItem.value)) {
    ElMessage.warning('请选择教学资源');
    return;
  }

  const requiredDurationSeconds = resourceStudyMinutes.value * 60 + resourceStudySeconds.value;
  const learningStartTime = formatLocalDateTime(resourceOpenStartTime.value);
  const learningEndTime = formatLocalDateTime(resourceOpenEndTime.value);
  if (resourceOpenStartTime.value && resourceOpenEndTime.value && resourceOpenEndTime.value <= resourceOpenStartTime.value) {
    ElMessage.warning('资源开放结束时间必须晚于开始时间');
    return;
  }
  const selectedCount = resourceSelectedCount.value;
  const selectedResources = resourceSelectedIds.value
    .map((resourceId) => resourceSelectedMap.value[resourceId])
    .filter((resource): resource is AdminResource => Boolean(resource));
  if (resourceEditingItem.value) {
    const resource = selectedResources[0];
    if (!resource) {
      ElMessage.warning('请选择教学资源');
      return;
    }
    Object.assign(resourceEditingItem.value, {
      title: resource.resourceName,
      desc:
        resource.resourceId === resourceEditingItem.value.resourceId && !resource.resourceType
          ? resourceEditingItem.value.desc
          : `${resourceCategoryLabel(resource.resourceType)} · ${resource.courseName || resource.fileName || '教学资源'}`,
      resourceId: resource.resourceId,
      requiredDurationSeconds: requiredDurationSeconds || undefined,
      learningStartTime,
      learningEndTime
    });
  } else {
    selectedResources.forEach((resource) => {
      resourceTargetSection.value?.items.push({
        id: nextOutlineId(),
        type: 'resource',
        title: resource.resourceName,
        desc: `${resourceCategoryLabel(resource.resourceType)} · ${resource.courseName || resource.fileName || '教学资源'}`,
        resourceId: resource.resourceId,
        requiredDurationSeconds: requiredDurationSeconds || undefined,
        learningStartTime,
        learningEndTime
      });
    });
  }
  resourceDialogVisible.value = false;
  resourceEditingItem.value = null;
  ElMessage.success(selectedCount > 1 ? `已添加 ${selectedCount} 条教学资源` : '教学资源已保存');
}

async function editOutlineItem(item: OutlineItem) {
  try {
    await ElMessageBox.confirm(
      `确定要编辑【${item.title}】吗？编辑后对应的学习数据将无法恢复，请谨慎操作。`,
      '编辑确认',
      { type: 'warning' }
    );
  } catch {
    return;
  }
  if (item.type === 'homework') {
    resetOutlineDialog();
    outlineForm.title = item.title;
    outlineForm.desc = item.desc === '-' ? '' : item.desc;
    outlineForm.questions = item.questions ? item.questions.map((question) => ({ ...question })) : [];
    outlineForm.completionRule = item.assignmentCompletionRule || 'SUBMIT';
    outlineForm.passScore = item.passScore || 60;
    outlineForm.publishMode = item.assignmentPublishMode || 'PRACTICE';
    outlineForm.answerStartTime = parseDateTime(item.answerStartTime);
    outlineForm.answerEndTime = parseDateTime(item.answerEndTime);
    outlineTargetItem.value = item;
    openOutlineDialog('homework', 'edit');
    return;
  }

  openResourceDialog(null, item);
}

function confirmOutlineDialog() {
  const title = outlineDialogKind.value === 'homework'
    ? (outlineForm.title.trim() || outlineForm.questions.map((item) => item.title).join('、').slice(0, 30) || '课程作业')
    : outlineForm.title.trim();
  if (!title && outlineDialogKind.value !== 'homework') {
    ElMessage.warning(`请输入${outlineNameLabel.value}`);
    return;
  }

  if (outlineDialogKind.value === 'chapter') {
    if (outlineDialogMode.value === 'edit' && outlineTargetChapter.value) {
      outlineTargetChapter.value.title = title;
    } else {
      const parentChapter = chapters.value.find((chapter) => chapter.id === outlineForm.parentId);
      const parentSection = chapters.value
        .flatMap((chapter) => chapter.sections)
        .find((section) => section.id === outlineForm.parentId);
      if (parentSection) {
        parentSection.children.push({ id: nextOutlineId(), title, items: [], children: [] });
      } else if (parentChapter) {
        parentChapter.sections.push({ id: nextOutlineId(), title, items: [], children: [] });
      } else {
        chapters.value.push({ id: nextOutlineId(), title, items: [], sections: [] });
      }
    }
    closeOutlineDialog();
    return;
  }

  if (outlineDialogKind.value === 'section') {
    if (outlineDialogMode.value === 'edit' && outlineTargetSection.value) {
      outlineTargetSection.value.title = title;
    }
    closeOutlineDialog();
    return;
  }

  if (outlineDialogKind.value === 'homework') {
    const desc = outlineForm.desc.trim() || '-';
    if (!outlineForm.answerStartTime || !outlineForm.answerEndTime) {
      ElMessage.warning('请选择作业答题起止时间');
      return;
    }
    if (outlineForm.answerEndTime <= outlineForm.answerStartTime) {
      ElMessage.warning('作业答题结束时间必须晚于开始时间');
      return;
    }
    if (!outlineForm.questions.length) {
      ElMessage.warning('请至少选择一道有效题目');
      return;
    }
    if (
      outlineForm.completionRule === 'PASS_SCORE' &&
      (!Number.isInteger(outlineForm.passScore) ||
        outlineForm.passScore <= 0 ||
        (selectedHomeworkTotalScore.value > 0 && outlineForm.passScore > selectedHomeworkTotalScore.value))
    ) {
      ElMessage.warning('请输入有效的合格分数');
      return;
    }
    const assignmentSettings = {
      assignmentCompletionRule: outlineForm.completionRule,
      passScore: outlineForm.completionRule === 'PASS_SCORE' ? outlineForm.passScore : undefined,
      assignmentPublishMode: outlineForm.publishMode,
      answerStartTime: formatLocalDateTime(outlineForm.answerStartTime),
      answerEndTime: formatLocalDateTime(outlineForm.answerEndTime)
    };
    if (outlineDialogMode.value === 'edit' && outlineTargetItem.value) {
      outlineTargetItem.value.title = title;
      outlineTargetItem.value.desc = desc;
      outlineTargetItem.value.questions = outlineForm.questions.map((question) => ({ ...question }));
      Object.assign(outlineTargetItem.value, assignmentSettings);
    } else if (outlineTargetSection.value) {
      outlineTargetSection.value.items.push({
        id: nextOutlineId(),
        type: 'homework',
        title,
        desc,
        questions: outlineForm.questions.map((question) => ({ ...question })),
        ...assignmentSettings
      });
    }
    closeOutlineDialog();
  }
}

async function removeOutlineItem(section: OutlineContentContainer, index: number) {
  const item = section.items[index];
  try {
    await ElMessageBox.confirm(
      `确定要删除【${item?.title || '该内容'}】吗？删除后对应的学习数据将无法恢复，请谨慎操作。`,
      '删除内容',
      { type: 'warning' }
    );
    section.items.splice(index, 1);
  } catch {
    return;
  }
}

function formatLocalDateTime(value?: Date) {
  if (!value) {
    return undefined;
  }

  const pad = (num: number) => String(num).padStart(2, '0');
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(
    value.getMinutes()
  )}:${pad(value.getSeconds())}`;
}

function formatDateTime(value?: string) {
  if (!value) {
    return '未设置';
  }
  return value.replace('T', ' ').slice(0, 16);
}

function selectedSemester() {
  return semesterOptions.value.find((item) => item.key === form.semesterKey);
}

function removeTeacher(userId: number) {
  form.teacherIds = form.teacherIds.filter((id) => id !== userId);
}

function removeClass(classId: number) {
  form.classIds = form.classIds.filter((id) => id !== classId);
}

function validateForm() {
  const semester = selectedSemester();
  if (!form.courseName.trim()) {
    throw new Error('请输入课程名称');
  }
  if (form.courseName.trim().length > 20) {
    throw new Error('课程名称不能超过 20 个字符');
  }
  if (!form.startTime || !form.endTime) {
    throw new Error('请选择教学起止时间');
  }
  if (form.endTime <= form.startTime) {
    throw new Error('教学结束时间必须晚于开始时间');
  }
  if (!semester) {
    throw new Error('请选择学年学期');
  }
  if (!form.teacherIds.length) {
    throw new Error('请选择教学团队');
  }
  if (!form.classIds.length) {
    throw new Error('请选择授课班级');
  }

  const scoreCap = Number(form.coursewareScore);
  if (!Number.isInteger(scoreCap) || scoreCap <= 0) {
    throw new Error('课件完成度满分需为正整数');
  }

  return { semester, scoreCap };
}

type CourseChapterCommand = NonNullable<AdminCourseCommand['chapters']>[number];

function serializeOutlineItems(items: OutlineItem[]): NonNullable<CourseChapterCommand['contents']> {
  return items.map((item, itemIndex) => ({
    contentId: isEditMode.value ? item.contentId : undefined,
    itemType: item.type === 'homework' ? 'ASSIGNMENT' : 'COURSEWARE',
    title: item.title.slice(0, 30),
    sortOrder: itemIndex + 1,
    resourceId: item.resourceId,
    assignmentId: item.assignmentId,
    questionIds: item.questions
      ?.filter((question) => question.kind === 'theory')
      .flatMap((question) => question.questionIds ?? [question.id]),
    trainingIds: item.questions
      ?.filter((question) => question.kind === 'practice')
      .map((question) => question.id),
    assignmentTotalScore: item.questions
      ?.filter((question) => question.kind === 'theory')
      .reduce((total, question) => total + Number(question.score || 0), 0),
    assignmentCompletionRule: item.assignmentCompletionRule,
    passScore: item.passScore,
    assignmentPublishMode: item.assignmentPublishMode,
    answerStartTime: item.answerStartTime,
    answerEndTime: item.answerEndTime,
    requiredDurationSeconds: item.requiredDurationSeconds,
    learningStartTime: item.learningStartTime,
    learningEndTime: item.learningEndTime
  }));
}

function serializeOutlineSection(section: OutlineSection, index: number): CourseChapterCommand {
  return {
    chapterId: isEditMode.value ? section.chapterId : undefined,
    chapterTitle: section.title.slice(0, 20),
    sortOrder: index + 1,
    contents: serializeOutlineItems(section.items),
    children: section.children.map(serializeOutlineSection)
  };
}

async function saveCourse() {
  let payload: ReturnType<typeof validateForm>;
  try {
    payload = validateForm();
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善课程信息');
    return;
  }

  saving.value = true;
  try {
    const command = {
      courseName: form.courseName.trim(),
      academicYearId: payload.semester.academicYearId,
      semesterId: payload.semester.semesterId,
      majorId: form.majorId,
      coverUrl: form.coverUrl || undefined,
      openStartTime: formatLocalDateTime(form.startTime),
      openEndTime: formatLocalDateTime(form.endTime),
      teacherIds: form.teacherIds,
      classIds: form.classIds,
      learningMode: form.learningMode,
      assignmentCompletionRule: 'SUBMIT',
      coursewareScoreCap: payload.scoreCap,
      chapters: chapters.value.map((chapter, index) => ({
        chapterId: isEditMode.value ? chapter.chapterId : undefined,
        chapterTitle: chapter.title.slice(0, 20),
        sortOrder: index + 1,
        contents: serializeOutlineItems(chapter.items),
        children: chapter.sections.map(serializeOutlineSection)
      }))
    };
    if (isEditMode.value && courseId.value) {
      await updateAdminCourse(courseId.value, command);
    } else {
      await createAdminCourse(command);
    }
    ElMessage.success(isEditMode.value ? '课程修改已保存' : '课程已保存');
    goBack();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程保存失败');
  } finally {
    saving.value = false;
  }
}

async function loadOptions() {
  try {
    const [years, majors, classes, teachers] = await Promise.all([
      fetchAdminAcademicYears(),
      fetchAdminMajors(),
      fetchAdminClasses(),
      fetchAdminTeachers()
    ]);
    academicYears.value = years;
    majorOptions.value = majors.filter((item) => item.enabled !== false);
    classOptions.value = classes.filter((item) => item.enabled !== false);
    teacherOptions.value = teachers.filter((item) => item.enabled !== false);
    form.semesterKey = semesterOptions.value.find((item) => item.current)?.key || '';
    form.majorId = undefined;
    form.coverUrl = '';
    form.coursewareScore = undefined;
    form.teacherIds = [];
    form.classIds = [];
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '课程基础数据加载失败');
  }
}

function parseDateTime(value?: string) {
  if (!value) {
    return undefined;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? undefined : date;
}

function mapOutlineItems(contents?: AdminCourseContent[]): OutlineItem[] {
  return (contents ?? []).map((item) => ({
    id: item.contentId ?? nextOutlineId(),
    contentId: item.contentId,
    type: item.itemType?.toUpperCase() === 'ASSIGNMENT' ? 'homework' : 'resource',
    title: item.title || '未命名内容',
    desc: item.itemType?.toUpperCase() === 'ASSIGNMENT' ? '课程作业' : '教学资源',
    resourceId: item.resourceId,
    assignmentId: item.assignmentId,
    requiredDurationSeconds: item.requiredDurationSeconds,
    learningStartTime: item.learningStartTime,
    learningEndTime: item.learningEndTime,
    assignmentCompletionRule: item.assignmentCompletionRule as 'SUBMIT' | 'PASS_SCORE' | undefined,
    passScore: item.passScore,
    assignmentPublishMode: item.assignmentPublishMode as 'PRACTICE' | 'EXAM' | undefined,
    answerStartTime: item.answerStartTime,
    answerEndTime: item.answerEndTime,
    questions: []
  }));
}

function mapOutlineSection(section: AdminCourseChapter): OutlineSection {
  return {
    id: section.chapterId ?? nextOutlineId(),
    chapterId: section.chapterId,
    title: section.chapterTitle || '未命名小节',
    items: mapOutlineItems(section.contents),
    children: (section.children ?? []).map(mapOutlineSection)
  };
}

function outlineSectionIds(sections: OutlineSection[]): number[] {
  return sections.flatMap((section) => [section.id, ...section.items.map((item) => item.id), ...outlineSectionIds(section.children)]);
}

function flattenApiContents(chapters: AdminCourseChapter[]): AdminCourseContent[] {
  return chapters.flatMap((chapter) => [...(chapter.contents ?? []), ...flattenApiContents(chapter.children ?? [])]);
}

function sameIdSet(left: number[], right: number[]) {
  if (left.length !== right.length) {
    return false;
  }
  const rightSet = new Set(right);
  return new Set(left).size === rightSet.size && left.every((id) => rightSet.has(id));
}

async function findTheoryPaper(questionIds: number[]) {
  if (!questionIds.length) {
    return undefined;
  }
  const papers = await fetchAdminPapers({ publishStatus: 'PUBLISHED', page: 1, pageSize: 100 });
  const details = await Promise.all(
    papers.records.map(async (paper) => {
      try {
        return await fetchAdminPaper(paper.paperId);
      } catch {
        return undefined;
      }
    })
  );
  return details.find((paper) => {
    const ids = (paper?.questions ?? [])
      .map((question) => question.questionId)
      .filter((id): id is number => Number.isFinite(id));
    return sameIdSet(questionIds, ids);
  });
}

async function loadCourseOutline(detail: Awaited<ReturnType<typeof fetchAdminCourseDetail>>) {
  chapters.value = (detail.chapters ?? []).map((chapter) => ({
    id: chapter.chapterId ?? nextOutlineId(),
    chapterId: chapter.chapterId,
    title: chapter.chapterTitle || '未命名章节',
    items: mapOutlineItems(chapter.contents),
    sections: (chapter.children ?? []).map(mapOutlineSection)
  }));
  const outlineIds = chapters.value.flatMap((chapter) => [chapter.id, ...outlineSectionIds(chapter.sections), ...chapter.items.map((item) => item.id)]);
  outlineIdSeed = Math.max(outlineIdSeed, ...outlineIds);

  const homeworkItems = flattenOutlineItems().filter((item) => item.type === 'homework');
  const sourceContents = flattenApiContents(detail.chapters ?? []);
  await Promise.all(homeworkItems.map(async (item) => {
    const source = sourceContents.find((content) => content.contentId === item.id);
    const questionIds = source?.questionIds ?? [];
    const trainingIds = source?.trainingIds ?? [];
    if (trainingIds.length) {
      const trainings = await Promise.allSettled(trainingIds.map((trainingId) => fetchAdminTraining(trainingId)));
      item.questions = trainings
        .filter((result): result is PromiseFulfilledResult<AdminTraining> => result.status === 'fulfilled')
        .map((result) => mapPracticeQuestion(result.value));
      return;
    }
    if (!questionIds.length) {
      item.questions = [];
      return;
    }
    const paper = await findTheoryPaper(questionIds).catch(() => undefined);
    if (paper) {
      item.questions = [
        {
          ...mapTheoryPaper(paper),
          questionIds: questionIds.slice()
        }
      ];
      return;
    }
    const questions = await Promise.allSettled(questionIds.map((questionId) => fetchAdminQuestion(questionId)));
    item.questions = questions
      .filter((result): result is PromiseFulfilledResult<AdminQuestion> => result.status === 'fulfilled')
      .map((result) => mapTheoryQuestion(result.value));
  }));
}

function splitNames(value?: string) {
  return (value ?? '')
    .split(/[，,、\n]/)
    .map((item) => item.trim())
    .filter(Boolean);
}

async function loadCourseDetail(targetCourseId: number, copyMode = false) {
  if (!targetCourseId) {
    ElMessage.error('课程编号无效');
    goBack();
    return;
  }

  const detail = await fetchAdminCourseDetail(targetCourseId);
  const sourceName = detail.courseName || '';
  form.courseName = copyMode ? `${sourceName.slice(0, 17)}-复制` : sourceName;
  form.startTime = parseDateTime(detail.openStartTime);
  form.endTime = parseDateTime(detail.openEndTime);
  form.semesterKey = detail.academicYearId && detail.semesterId
    ? `${detail.academicYearId}:${detail.semesterId}`
    : semesterOptions.value.find(
        (item) => item.label === [detail.academicYearName, detail.semesterName].filter(Boolean).join(' ')
      )?.key || '';
  form.majorId = detail.majorId ?? majorOptions.value.find((item) => item.majorName === detail.majorName)?.majorId;
  form.coverUrl = detail.coverUrl || '';
  form.coursewareScore = detail.coursewareScoreCap;
  form.learningMode = detail.learningMode || 'SELF_PACED';
  const teacherNames = splitNames(detail.teacherNames);
  const classNames = splitNames(detail.classNames);
  form.teacherIds = detail.teacherIds?.length
    ? [...detail.teacherIds]
    : teacherOptions.value
        .filter((item) => teacherNames.includes(item.realName) || teacherNames.includes(item.accountNo || ''))
        .map((item) => item.userId);
  form.classIds = detail.classIds?.length
    ? [...detail.classIds]
    : classOptions.value.filter((item) => classNames.includes(item.className)).map((item) => item.classId);
  await loadCourseOutline(detail);
  if (copyMode) {
    flattenOutlineItems()
      .filter((item) => item.type === 'homework')
      .forEach((item) => {
        item.assignmentId = undefined;
      });
  }
}

onMounted(async () => {
  loading.value = true;
  try {
    await loadOptions();
    if (isEditMode.value) {
      await loadCourseDetail(courseId.value);
    } else if (isCopyMode.value) {
      await loadCourseDetail(copySourceId.value, true);
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程信息加载失败');
  } finally {
    loading.value = false;
  }
});
</script>
