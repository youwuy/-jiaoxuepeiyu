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
            <el-input v-model="form.courseName" placeholder="请输入课程名称" />
          </label>

          <label class="admin-course-form-field">
            <span>
              教学开始时间 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="请选择开始时间" />
          </label>

          <label class="admin-course-form-field">
            <span>
              教学结束时间 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
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
            <span>所属专业 <b>*</b></span>
            <el-select v-model="form.majorId" placeholder="请选择专业" filterable @change="handleMajorChange">
              <el-option v-for="item in majorOptions" :key="item.majorId" :label="item.majorName" :value="item.majorId" />
            </el-select>
          </label>

          <label class="admin-course-form-field">
            <span>
              课件完成度满分 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-input v-model="form.coursewareScore" placeholder="请输入满分值" />
          </label>

          <div class="admin-course-form-field full radio">
            <span>
              学生学习模式 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
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
            closable
            class="admin-course-chip teacher"
            @close="removeTeacher(item.userId)"
          >
            {{ item.realName || item.accountNo }}
          </el-tag>
          <el-popover placement="bottom-start" trigger="click" width="320" popper-class="admin-course-picker-popover">
            <template #reference>
              <el-button class="admin-course-add-chip">
                <el-icon><Plus /></el-icon>
                添加教师
              </el-button>
            </template>
            <el-select
              v-model="form.teacherIds"
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
            closable
            class="admin-course-chip class"
            @close="removeClass(item.classId)"
          >
            {{ item.className }}
          </el-tag>
          <el-popover placement="bottom-start" trigger="click" width="320" popper-class="admin-course-picker-popover">
            <template #reference>
              <el-button class="admin-course-add-chip">
                <el-icon><Plus /></el-icon>
                添加班级
              </el-button>
            </template>
            <el-select
              v-model="form.classIds"
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
          <el-button type="primary" class="admin-course-form-primary" @click="addChapter">
            <el-icon><Plus /></el-icon>
            新增章节
          </el-button>
        </header>

        <div class="admin-course-outline">
          <div v-if="chapters.length === 0" class="admin-course-empty inline">
            <el-empty description="暂无教学内容，请先新增章节" />
          </div>
          <article v-else v-for="chapter in chapters" :key="chapter.id" class="admin-course-outline-chapter">
            <div class="admin-course-outline-row admin-course-outline-chapter-row">
              <span class="admin-course-outline-left">
                <el-icon><ArrowDown /></el-icon>
                <el-icon class="folder"><Folder /></el-icon>
                <strong>{{ chapter.title }}</strong>
              </span>
              <span class="admin-course-outline-actions">
                <el-button text type="success" @click="addSection(chapter)">新增小节</el-button>
                <el-button text type="primary" @click="editChapter(chapter)">编辑</el-button>
                <el-button text type="danger" @click="removeChapter(chapters.indexOf(chapter))">删除</el-button>
              </span>
            </div>

            <template v-for="section in chapter.sections" :key="section.id">
              <div class="admin-course-outline-row admin-course-outline-section-row">
                <span class="admin-course-outline-left">
                  <el-icon><ArrowDown /></el-icon>
                  <strong>{{ section.title }}</strong>
                </span>
                <span class="admin-course-outline-actions">
                  <el-button text type="success" @click="addOutlineItem(section, 'resource')">添加课件资源</el-button>
                  <el-button text type="success" @click="addOutlineItem(section, 'homework')">添加作业</el-button>
                  <el-button text type="primary" @click="editSection(section)">编辑</el-button>
                  <el-button text type="danger" @click="removeSection(chapter, chapter.sections.indexOf(section))">删除</el-button>
                </span>
              </div>

              <div
                v-for="item in section.items"
                :key="item.id"
                class="admin-course-outline-row admin-course-outline-resource-row"
              >
                <span class="admin-course-outline-drag">::</span>
                <span class="admin-course-outline-icon" :class="item.type">
                  <el-icon><component :is="item.type === 'homework' ? Checked : Document" /></el-icon>
                </span>
                <span class="admin-course-outline-info">
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.desc }}</small>
                </span>
                <span class="admin-course-outline-actions compact-actions">
                  <el-button text type="primary" @click="editOutlineItem(item)">编辑</el-button>
                  <el-button text type="danger" @click="removeOutlineItem(section, section.items.indexOf(item))">删除</el-button>
                </span>
              </div>
            </template>
          </article>
        </div>
      </section>

      <footer class="admin-course-form-footer">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCourse">保存</el-button>
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
        <label class="admin-course-outline-dialog-field">
          <span>{{ outlineNameLabel }} <b>*</b></span>
          <el-input v-model="outlineForm.title" :placeholder="outlineNamePlaceholder" maxlength="30" show-word-limit />
        </label>
        <label v-if="outlineDialogKind === 'homework'" class="admin-course-outline-dialog-field">
          <span>作业说明</span>
          <el-input
            v-model="outlineForm.desc"
            type="textarea"
            :rows="4"
            maxlength="120"
            show-word-limit
            placeholder="请输入作业说明"
          />
        </label>
        <section v-if="outlineDialogKind === 'homework'" class="admin-course-homework-question-section">
          <div class="admin-course-homework-question-head">
            <div>
              <strong>作业题目</strong>
              <small>可从实训题库和理论题库中选择题目</small>
            </div>
            <span>已选 {{ outlineForm.questions.length }} 题</span>
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
                  <el-button type="primary" @click="loadQuestionRows">查询</el-button>
                </div>
                <div v-loading="questionPickerLoading" class="admin-course-question-picker-list">
                  <article
                    v-for="item in questionRows"
                    :key="`${item.kind}-${item.id}`"
                    class="admin-course-question-picker-row"
                    :class="{ selected: isQuestionSelected(item) }"
                    @click="toggleQuestionSelection(item)"
                  >
                    <el-checkbox
                      :model-value="isQuestionSelected(item)"
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
                  <span>已选 {{ selectedQuestionCount(tab.name) }} 题</span>
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
                {{ item.kind === 'practice' ? '实训题' : '理论题' }}
              </span>
              <div>
                <strong>{{ item.title }}</strong>
                <small>{{ item.typeLabel }} · {{ item.score ? `${item.score} 分` : item.meta }}</small>
              </div>
              <el-button text type="primary" @click="openQuestionDetail(item)">查看详情</el-button>
              <el-button text type="danger" @click="removeHomeworkQuestion(item)">移除</el-button>
            </article>
          </div>
          <div v-else class="admin-course-homework-question-empty">
            <el-icon><Document /></el-icon>
            <span>暂未添加题目，请在上方选择实训题或理论题</span>
          </div>
        </section>
      </div>

      <template #footer>
        <div class="admin-course-outline-dialog-footer">
          <el-button @click="closeOutlineDialog">取消</el-button>
          <el-button type="primary" @click="confirmOutlineDialog">确定</el-button>
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
            <strong>{{ questionDetail?.kind === 'practice' ? '实训题详情' : '理论题详情' }}</strong>
            <span>{{ questionDetail?.typeLabel || '题目详情' }}</span>
          </div>
          <el-button text circle :icon="Close" @click="questionDetailVisible = false" />
        </div>
      </template>

      <div v-if="questionDetailLoading" class="admin-course-question-detail-loading">题目详情加载中...</div>
      <div v-else-if="questionDetail" class="admin-course-question-detail-body">
        <div class="admin-course-question-detail-meta">
          <span class="admin-course-homework-question-type" :class="questionDetail.kind">
            {{ questionDetail.kind === 'practice' ? '实训题' : '理论题' }}
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
          <strong>添加教学资源</strong>
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
              :class="{ active: isResourceSelected(item) }"
              @click="toggleResourceSelection(item, !isResourceSelected(item))"
            >
              <span class="check-cell" @click.stop>
                <el-checkbox
                  :model-value="isResourceSelected(item)"
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
          <el-button type="primary" :disabled="!resourceSelectedCount" @click="confirmAddResource">确定添加</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
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
  type AdminAcademicYearOption,
  type AdminClassOption,
  type AdminMajorOption,
  type AdminTeacherOption
} from '../../api/admin-course';
import {
  fetchAdminQuestion,
  fetchAdminQuestions,
  type AdminQuestion
} from '../../api/admin-question';
import {
  fetchAdminTraining,
  fetchAdminTrainings,
  type AdminTraining
} from '../../api/admin-training';
import { fetchAdminResources, type AdminResource } from '../../api/admin-resource';

const route = useRoute();
const router = useRouter();
const courseId = computed(() => Number(route.params.id));
const isEditMode = computed(() => route.name === 'admin-course-edit');
const pageTitle = computed(() => (isEditMode.value ? '编辑课程' : '新增课程'));

const form = reactive({
  courseName: '',
  startTime: undefined as Date | undefined,
  endTime: undefined as Date | undefined,
  semesterKey: '',
  majorId: undefined as number | undefined,
  coursewareScore: '100',
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
  type: 'homework' | 'resource';
  title: string;
  desc: string;
  questions?: HomeworkQuestion[];
  resourceId?: number;
  assignmentId?: number;
  requiredDurationSeconds?: number;
  learningStartTime?: string;
  learningEndTime?: string;
}

interface OutlineSection {
  id: number;
  title: string;
  items: OutlineItem[];
}

interface OutlineChapter {
  id: number;
  title: string;
  sections: OutlineSection[];
}

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
let outlineIdSeed = 1;
const outlineDialogVisible = ref(false);
const outlineDialogKind = ref<OutlineDialogKind>('chapter');
const outlineDialogMode = ref<OutlineDialogMode>('create');
const outlineForm = reactive({
  title: '',
  desc: '',
  questions: [] as HomeworkQuestion[]
});
const outlineTargetChapter = ref<OutlineChapter | null>(null);
const outlineTargetSection = ref<OutlineSection | null>(null);
const outlineTargetItem = ref<OutlineItem | null>(null);
const homeworkQuestionTab = ref<HomeworkQuestionKind>('practice');
const questionTabs: Array<{ label: string; name: HomeworkQuestionKind }> = [
  { label: '实训题', name: 'practice' },
  { label: '理论题', name: 'theory' }
];
const questionPickerLoading = ref(false);
const questionKeyword = ref('');
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
const resourceTargetSection = ref<OutlineSection | null>(null);
const resourceTypeTabs = [
  { label: '全部资源', value: '' },
  { label: '文本文件', value: 'DOCUMENT' },
  { label: '演示文稿', value: 'PRESENTATION' },
  { label: '视频', value: 'VIDEO' },
  { label: '音频', value: 'AUDIO' },
  { label: '图像', value: 'IMAGE' }
];

const resourceSelectedCount = computed(() => resourceSelectedIds.value.length);
const resourcePageStart = computed(() =>
  resourceTotal.value === 0 ? 0 : (resourcePage.value - 1) * resourcePageSize + 1
);
const resourcePageEnd = computed(() => Math.min(resourcePage.value * resourcePageSize, resourceTotal.value));
const resourcePageSelected = computed(
  () => resourceRows.value.length > 0 && resourceRows.value.every((item) => resourceSelectedIds.value.includes(item.resourceId))
);
const resourcePageIndeterminate = computed(
  () =>
    !resourcePageSelected.value &&
    resourceRows.value.some((item) => resourceSelectedIds.value.includes(item.resourceId))
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

async function promptOutlineText(message: string, title: string, initialValue = '') {
  try {
    const result = await ElMessageBox.prompt(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: initialValue,
      inputValidator: (value) => {
        if (!value || !value.trim()) {
          return '请输入内容';
        }
        return true;
      }
    });
    return String(result.value || '').trim();
  } catch {
    return '';
  }
}

function resetOutlineDialog() {
  outlineForm.title = '';
  outlineForm.desc = '';
  outlineForm.questions = [];
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

function addSection(chapter: OutlineChapter) {
  resetOutlineDialog();
  outlineTargetChapter.value = chapter;
  openOutlineDialog('section', 'create');
}

function editSection(section: OutlineSection) {
  resetOutlineDialog();
  outlineForm.title = section.title;
  outlineTargetSection.value = section;
  openOutlineDialog('section', 'edit');
}

async function removeSection(chapter: OutlineChapter, index: number) {
  try {
    await ElMessageBox.confirm('确认删除该小节？', '删除小节', { type: 'warning' });
    chapter.sections.splice(index, 1);
  } catch {
    return;
  }
}

async function addOutlineItem(section: OutlineSection, type: OutlineItem['type']) {
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
  return type ? labels[type.toUpperCase()] || type : '理论题';
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

function mapPracticeQuestion(item: AdminTraining): HomeworkQuestion {
  const meta = [item.majorName, item.trainingMode, item.publishStatus].filter(Boolean).join(' · ');
  return {
    id: item.trainingId,
    kind: 'practice',
    title: item.trainingName || `实训题 ${item.trainingId}`,
    typeLabel: item.trainingType || '实训任务',
    score: 0,
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

function handleQuestionTabChange(value: string | number) {
  homeworkQuestionTab.value = value === 'theory' ? 'theory' : 'practice';
  questionKeyword.value = '';
  questionPage.value = 1;
  void loadQuestionRows();
}

async function loadQuestionRows() {
  questionPickerLoading.value = true;
  try {
    if (homeworkQuestionTab.value === 'theory') {
      const result = await fetchAdminQuestions({
        keyword: questionKeyword.value,
        enabled: true,
        page: questionPage.value,
        pageSize: questionPageSize
      });
      questionRows.value = result.records.map(mapTheoryQuestion);
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

function toggleQuestionSelection(item: HomeworkQuestion) {
  const selectedIndex = outlineForm.questions.findIndex(
    (question) => questionKey(question) === questionKey(item)
  );
  if (selectedIndex >= 0) {
    outlineForm.questions.splice(selectedIndex, 1);
  } else {
    outlineForm.questions.push({ ...item });
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
      const detail = await fetchAdminQuestion(item.id);
      const standardAnswers = new Set(
        (detail.standardAnswer ?? '')
          .split(/[,\s，、;；]+/)
          .map((answer) => answer.trim().toUpperCase())
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
        extra: detail.creatorName ? `添加人：${detail.creatorName}` : '理论题库'
      };
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

function openResourceDialog(section: OutlineSection) {
  resourceTargetSection.value = section;
  resourceSelectedIds.value = [];
  resourceSelectedMap.value = {};
  resourceKeyword.value = '';
  resourceTypeFilter.value = '';
  resourceMajorId.value = null;
  resourceCourseKeyword.value = '';
  resourcePage.value = 1;
  resourceOpenStartTime.value = undefined;
  resourceOpenEndTime.value = undefined;
  resourceStudyMinutes.value = 0;
  resourceStudySeconds.value = 0;
  resourceDialogVisible.value = true;
  void loadResourceRows();
}

async function loadResourceRows() {
  resourceLoading.value = true;
  try {
    const result = await fetchAdminResources({
      keyword: resourceKeyword.value,
      resourceType: resourceTypeFilter.value,
      majorId: resourceMajorId.value,
      courseName: resourceCourseKeyword.value,
      page: resourcePage.value,
      pageSize: resourcePageSize
    });
    resourceRows.value = result.records;
    resourceTotal.value = result.total;
  } catch (error) {
    resourceRows.value = [];
    resourceTotal.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '资源列表加载失败');
  } finally {
    resourceLoading.value = false;
  }
}

function handleResourceSearch() {
  resourcePage.value = 1;
  resourceSelectedIds.value = [];
  resourceSelectedMap.value = {};
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

function toggleResourceSelection(item: AdminResource, checked: boolean) {
  const nextIds = new Set(resourceSelectedIds.value);
  if (checked) {
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
  resourceRows.value.forEach((item) => toggleResourceSelection(item, checked));
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
  if (!resourceSelectedIds.value.length || !resourceTargetSection.value) {
    ElMessage.warning('请选择教学资源');
    return;
  }

  const requiredDurationSeconds = resourceStudyMinutes.value * 60 + resourceStudySeconds.value;
  const learningStartTime = formatLocalDateTime(resourceOpenStartTime.value);
  const learningEndTime = formatLocalDateTime(resourceOpenEndTime.value);
  resourceSelectedIds.value
    .map((resourceId) => resourceSelectedMap.value[resourceId])
    .filter((resource): resource is AdminResource => Boolean(resource))
    .forEach((resource) => {
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
  resourceDialogVisible.value = false;
  ElMessage.success(`已添加 ${resourceSelectedCount.value} 条教学资源`);
}

async function editOutlineItem(item: OutlineItem) {
  if (item.type === 'homework') {
    resetOutlineDialog();
    outlineForm.title = item.title;
    outlineForm.desc = item.desc === '-' ? '' : item.desc;
    outlineForm.questions = item.questions ? item.questions.map((question) => ({ ...question })) : [];
    outlineTargetItem.value = item;
    openOutlineDialog('homework', 'edit');
    return;
  }

  const title = await promptOutlineText('请输入名称', '编辑内容', item.title);
  if (!title) return;
  item.title = title;
}

function confirmOutlineDialog() {
  const title = outlineForm.title.trim();
  if (!title) {
    ElMessage.warning(`请输入${outlineNameLabel.value}`);
    return;
  }

  if (outlineDialogKind.value === 'chapter') {
    if (outlineDialogMode.value === 'edit' && outlineTargetChapter.value) {
      outlineTargetChapter.value.title = title;
    } else {
      chapters.value.push({ id: nextOutlineId(), title, sections: [] });
    }
    closeOutlineDialog();
    return;
  }

  if (outlineDialogKind.value === 'section') {
    if (outlineDialogMode.value === 'edit' && outlineTargetSection.value) {
      outlineTargetSection.value.title = title;
    } else if (outlineTargetChapter.value) {
      outlineTargetChapter.value.sections.push({ id: nextOutlineId(), title, items: [] });
    }
    closeOutlineDialog();
    return;
  }

  if (outlineDialogKind.value === 'homework') {
    const desc = outlineForm.desc.trim() || '-';
    if (outlineDialogMode.value === 'edit' && outlineTargetItem.value) {
      outlineTargetItem.value.title = title;
      outlineTargetItem.value.desc = desc;
      outlineTargetItem.value.questions = outlineForm.questions.map((question) => ({ ...question }));
    } else if (outlineTargetSection.value) {
      outlineTargetSection.value.items.push({
        id: nextOutlineId(),
        type: 'homework',
        title,
        desc,
        questions: outlineForm.questions.map((question) => ({ ...question }))
      });
    }
    closeOutlineDialog();
  }
}

async function removeOutlineItem(section: OutlineSection, index: number) {
  try {
    await ElMessageBox.confirm('确认删除该内容？', '删除内容', { type: 'warning' });
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
  if (!form.startTime || !form.endTime) {
    throw new Error('请选择教学起止时间');
  }
  if (!semester) {
    throw new Error('请选择学年学期');
  }
  if (!form.majorId) {
    throw new Error('请选择所属专业');
  }
  if (!form.teacherIds.length) {
    throw new Error('请选择教学团队');
  }
  if (!form.classIds.length) {
    throw new Error('请选择授课班级');
  }

  const scoreCap = Number(form.coursewareScore);
  if (!Number.isFinite(scoreCap) || scoreCap <= 0 || scoreCap > 100) {
    throw new Error('课件完成度满分需为 1-100');
  }

  return { semester, scoreCap };
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
      coverUrl: '/assets/course-station-preview-Dw2Sploi.png',
      openStartTime: formatLocalDateTime(form.startTime),
      openEndTime: formatLocalDateTime(form.endTime),
      teacherIds: form.teacherIds,
      classIds: form.classIds,
      learningMode: form.learningMode,
      assignmentCompletionRule: 'SUBMIT',
      coursewareScoreCap: payload.scoreCap,
      chapters: chapters.value.map((chapter, index) => ({
        chapterTitle: chapter.title.slice(0, 20),
        sortOrder: index + 1,
        children: chapter.sections.map((section, sectionIndex) => ({
          chapterTitle: section.title.slice(0, 20),
          sortOrder: sectionIndex + 1,
          contents: section.items.map((item, itemIndex) => ({
            itemType: item.type === 'homework' ? 'ASSIGNMENT' : 'COURSEWARE',
            title: item.title.slice(0, 30),
            sortOrder: itemIndex + 1,
            resourceId: item.resourceId,
            assignmentId: item.assignmentId,
            requiredDurationSeconds: item.requiredDurationSeconds,
            learningStartTime: item.learningStartTime,
            learningEndTime: item.learningEndTime
          }))
        }))
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

function loadCourseOutline(detail: Awaited<ReturnType<typeof fetchAdminCourseDetail>>) {
  chapters.value = (detail.chapters ?? []).map((chapter) => ({
    id: chapter.chapterId ?? nextOutlineId(),
    title: chapter.chapterTitle || '未命名章节',
    sections: (chapter.children ?? []).map((section) => ({
      id: section.chapterId ?? nextOutlineId(),
      title: section.chapterTitle || '未命名小节',
      items: (section.contents ?? []).map((item) => ({
        id: item.contentId ?? nextOutlineId(),
        type: item.itemType?.toUpperCase() === 'ASSIGNMENT' ? 'homework' : 'resource',
        title: item.title || '未命名内容',
        desc: item.itemType?.toUpperCase() === 'ASSIGNMENT' ? '课程作业' : '教学资源',
        resourceId: item.resourceId,
        assignmentId: item.assignmentId,
        requiredDurationSeconds: item.requiredDurationSeconds,
        learningStartTime: item.learningStartTime,
        learningEndTime: item.learningEndTime
      }))
    }))
  }));
  const outlineIds = chapters.value.flatMap((chapter) => [
    chapter.id,
    ...chapter.sections.flatMap((section) => [section.id, ...section.items.map((item) => item.id)])
  ]);
  outlineIdSeed = Math.max(outlineIdSeed, ...outlineIds);
}

function splitNames(value?: string) {
  return (value ?? '')
    .split(/[，,、\n]/)
    .map((item) => item.trim())
    .filter(Boolean);
}

async function loadCourseDetail() {
  if (!courseId.value) {
    ElMessage.error('课程编号无效');
    goBack();
    return;
  }

  const detail = await fetchAdminCourseDetail(courseId.value);
  form.courseName = detail.courseName || '';
  form.startTime = parseDateTime(detail.openStartTime);
  form.endTime = parseDateTime(detail.openEndTime);
  form.semesterKey = detail.academicYearId && detail.semesterId
    ? `${detail.academicYearId}:${detail.semesterId}`
    : semesterOptions.value.find(
        (item) => item.label === [detail.academicYearName, detail.semesterName].filter(Boolean).join(' ')
      )?.key || '';
  form.majorId = detail.majorId ?? majorOptions.value.find((item) => item.majorName === detail.majorName)?.majorId;
  form.coursewareScore = String(detail.coursewareScoreCap ?? 100);
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
  loadCourseOutline(detail);
}

async function handleMajorChange(value?: number) {
  try {
    classOptions.value = (await fetchAdminClasses(value)).filter((item) => item.enabled !== false);
    form.classIds = [];
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '班级列表加载失败');
  }
}

watch(
  () => form.majorId,
  (majorId, oldMajorId) => {
    if (majorId && oldMajorId && majorId !== oldMajorId) {
      void handleMajorChange(majorId);
    }
  }
);

onMounted(async () => {
  loading.value = true;
  try {
    await loadOptions();
    if (isEditMode.value) {
      await loadCourseDetail();
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程信息加载失败');
  } finally {
    loading.value = false;
  }
});
</script>
