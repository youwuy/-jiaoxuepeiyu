<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-page">
      <el-breadcrumb class="admin-course-breadcrumb" separator="/">
        <el-breadcrumb-item>教学实训</el-breadcrumb-item>
        <el-breadcrumb-item>实训组课</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="admin-training-toolbar">
        <div class="admin-training-filter-row">
          <el-input v-model="filters.keyword" class="admin-training-search" :prefix-icon="Search" placeholder="搜索实训课名称" clearable />
          <el-date-picker
            v-model="filters.time"
            class="admin-training-time-filter"
            type="datetimerange"
            range-separator="至"
            start-placeholder="实训开始时间"
            end-placeholder="实训结束时间"
            clearable
          />
          <el-select v-model="filters.status" class="admin-training-select" placeholder="发布状态" clearable>
            <el-option label="已发布" value="已发布" />
            <el-option label="未发布" value="未发布" />
          </el-select>
          <el-button class="admin-training-ghost" @click="refreshCourses">查询</el-button>
          <el-button class="admin-training-ghost" @click="resetFilters">重置</el-button>
        </div>
        <div class="admin-training-action-row">
          <el-button class="admin-training-primary" type="primary" :icon="Plus" @click="openCreate">新增实训课</el-button>
          <el-button class="admin-training-ghost" :icon="Upload" @click="openOfflineImport">导入线下成绩</el-button>
        </div>
      </div>

      <div class="admin-training-table-card">
        <div v-if="loading" class="admin-course-empty">实训课加载中...</div>
        <div v-else-if="courses.length === 0" class="admin-course-empty">
          <el-empty description="暂无实训课数据" />
        </div>
        <div v-else class="admin-training-table-scroll">
          <table class="admin-training-table">
            <colgroup>
              <col class="admin-training-col-name" />
              <col class="admin-training-col-type" />
              <col class="admin-training-col-time" />
              <col class="admin-training-col-target" />
              <col class="admin-training-col-teacher" />
              <col class="admin-training-col-room" />
              <col class="admin-training-col-status" />
              <col class="admin-training-col-created" />
              <col class="admin-training-col-operation" />
            </colgroup>
            <thead>
              <tr>
                <th>实训课名称</th>
                <th>类型</th>
                <th>实训起止时间</th>
                <th>参训班级/学生</th>
                <th>监考教师</th>
                <th>实训教室</th>
                <th>发布状态</th>
                <th>创建时间</th>
                <th class="admin-training-operation-cell">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="course in courses" :key="course.id">
                <td>
                  <div class="admin-training-name-cell">
                    <strong>{{ course.name }}</strong>
                    <span>{{ course.mode }} / {{ course.topicCount }} 个实训任务</span>
                  </div>
                </td>
                <td><span class="admin-training-type-pill">{{ course.type }}</span></td>
                <td><span class="admin-training-multiline">{{ course.time }}</span></td>
                <td class="admin-training-ellipsis">{{ course.target }}</td>
                <td class="admin-training-ellipsis">{{ course.teacher }}</td>
                <td>{{ course.room }}</td>
                <td>
                  <span class="status-pill" :class="{ muted: course.status === '未发布' }">
                    <i></i>{{ course.status }}
                  </span>
                </td>
                <td>{{ course.createdAt }}</td>
                <td class="admin-training-operation-cell">
                  <div class="admin-row-actions">
                    <template v-if="course.status === '未发布'">
                      <el-button link type="primary" @click="openEdit(course)">编辑</el-button>
                      <el-button link type="danger" @click="confirmDelete(course)">删除</el-button>
                      <el-button class="publish-action" link @click="openPublish(course)">发布</el-button>
                      <el-button class="log-action" link @click="openLogs(course)">操作日志</el-button>
                    </template>
                    <template v-else-if="course.exam">
                      <el-button v-if="course.mode === '协同实训' && !course.examStarted" class="primary-action" link @click="openExamStart(course)">开始考试</el-button>
                      <el-button v-else-if="isTrainingOpen(course)" class="primary-action" link @click="openMonitor(course)">监考</el-button>
                      <el-button v-if="course.mode === '单人实训' || course.examStarted" class="primary-action" link @click="openMarking(course)">阅卷</el-button>
                      <el-button v-if="course.mode === '单人实训' || course.examStarted" class="primary-action" link @click="openStats(course)">成绩统计</el-button>
                      <el-button link type="primary" @click="openEdit(course)">编辑</el-button>
                      <el-button link type="danger" @click="confirmDelete(course)">删除</el-button>
                      <el-button link @click="withdrawCourse(course)">取消发布</el-button>
                      <el-button class="log-action" link @click="openLogs(course)">操作日志</el-button>
                    </template>
                    <template v-else>
                      <el-button v-if="isTrainingOpen(course)" class="primary-action" link @click="openMonitor(course)">监考</el-button>
                      <el-button class="primary-action" link @click="openMarking(course)">阅卷</el-button>
                      <el-button class="primary-action" link @click="openStats(course)">成绩统计</el-button>
                      <el-button link type="primary" @click="openEdit(course)">编辑</el-button>
                      <el-button link type="danger" @click="confirmDelete(course)">删除</el-button>
                      <el-button link @click="withdrawCourse(course)">取消发布</el-button>
                      <el-button class="log-action" link @click="openLogs(course)">操作日志</el-button>
                    </template>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="admin-pagination">
          <span>共 {{ total }} 条记录</span>
          <el-pagination v-model:current-page="page" layout="prev, pager, next" :total="total" :page-size="pageSize" @current-change="loadCourses" />
        </div>
      </div>

      <el-drawer v-model="formVisible" direction="rtl" size="860px" class="admin-training-form-drawer" :with-header="false">
        <div class="admin-training-drawer-head">
          <div>
            <span>{{ formMode === 'create' ? '新增实训课' : '编辑实训课' }}</span>
            <h3>{{ form.name || '新增实训课' }}</h3>
          </div>
          <el-button text circle :icon="Close" @click="formVisible = false" />
        </div>

        <div class="admin-training-stepper">
          <button v-for="step in steps" :key="step.key" :class="{ active: activeStep === step.key }" @click="activeStep = step.key">
            <i>{{ step.index }}</i>
            <span>{{ step.label }}</span>
          </button>
        </div>

        <div class="admin-training-form-body">
          <section v-show="activeStep === 'base'" class="admin-training-form-card">
            <header><strong>基础信息</strong><p>配置实训课名称、类型、时间和说明</p></header>
            <div class="admin-training-form-grid">
              <label>
                <span>实训课名称 <b>*</b></span>
                <el-input v-model="form.name" maxlength="30" placeholder="请输入实训课名称" />
              </label>
              <label>
                <span>实训类型 <b>*</b></span>
                <el-radio-group v-model="form.type">
                  <el-radio-button label="考试" />
                  <el-radio-button label="练习" />
                </el-radio-group>
              </label>
              <label>
                <span>实训模式 <b>*</b></span>
                <el-radio-group v-model="form.mode">
                  <el-radio-button label="单人实训" />
                  <el-radio-button label="协同实训" />
                </el-radio-group>
              </label>
              <label>
                <span>学年学期 <b>*</b></span>
                <el-select v-model="form.semester" placeholder="请选择学期">
                  <el-option v-for="semester in semesterOptions" :key="semester.value" :label="semester.label" :value="semester.value" />
                </el-select>
              </label>
              <label class="wide">
                <span>实训时间 <b>*</b></span>
                <el-date-picker v-model="form.range" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
              </label>
              <label class="wide">
                <span>实训说明</span>
                <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入实训说明" />
              </label>
            </div>
          </section>

          <section v-show="activeStep === 'resource'" class="admin-training-form-card">
            <header>
              <div><strong>实训内容</strong><p>选择实训任务、资源课件和理论试卷</p></div>
              <div class="admin-training-card-actions">
                <el-button :icon="FolderOpened" @click="openSelector('topic')">选择实训任务</el-button>
                <el-button :icon="Document" @click="openSelector('resource')">选择资源</el-button>
                <el-button :icon="Tickets" @click="openSelector('paper')">选择理论试卷</el-button>
              </div>
            </header>
            <div class="admin-training-selected-list">
              <article v-for="item in selectedTopics" :key="item.id">
                <div>
                  <strong>{{ item.name }}</strong>
                  <span>{{ item.category }} / {{ item.duration }} 分钟 / {{ item.score }} 分</span>
                </div>
                <el-button text type="danger" @click="removeSelected('topic', item.id)">移除</el-button>
              </article>
            </div>
            <div class="admin-training-subsection">
              <p>资源课件</p>
              <div class="admin-training-resource-grid">
                <article v-for="item in selectedResources" :key="item.id">
                  <el-icon><Document /></el-icon>
                  <div><strong>{{ item.name }}</strong><span>{{ item.type }} / {{ item.size }}</span></div>
                  <button @click="removeSelected('resource', item.id)">移除</button>
                </article>
              </div>
            </div>
            <div class="admin-training-paper-row">
              <span>理论试卷</span>
              <strong>{{ selectedPaper?.name || '未选择' }}</strong>
              <el-button link type="primary" @click="openSelector('paper')">重新选择</el-button>
            </div>
          </section>

          <section v-show="activeStep === 'target'" class="admin-training-form-card">
            <header>
              <div><strong>参训对象与场地</strong><p>配置班级、学生、监考教师和实训教室</p></div>
              <div class="admin-training-card-actions">
                <el-button :icon="UserFilled" @click="openSelector('class')">选择班级/学生</el-button>
                <el-button :icon="User" @click="openSelector('teacher')">选择教师</el-button>
                <el-button :icon="OfficeBuilding" @click="openSelector('room')">选择教室</el-button>
              </div>
            </header>
            <div class="admin-training-target-grid">
              <article>
                <span>参训班级/学生</span>
                <strong>{{ selectedClasses.map((item) => item.name).join('、') || '未选择' }}</strong>
              </article>
              <article>
                <span>监考教师</span>
                <strong>{{ selectedTeachers.map((item) => item.name).join('、') || '未选择' }}</strong>
              </article>
              <article>
                <span>实训教室</span>
                <strong>{{ selectedRoom?.name || '未选择' }}</strong>
              </article>
            </div>
          </section>

          <section v-show="activeStep === 'rule'" class="admin-training-form-card">
            <header>
              <div><strong>协同角色与评分规则</strong><p>配置小组角色、流程节点和得分规则</p></div>
              <div class="admin-training-card-actions">
                <el-button :icon="Plus" @click="openRoleDialog">新增角色</el-button>
                <el-button :icon="View" @click="openPreview()">预览组课</el-button>
              </div>
            </header>
            <div class="admin-training-role-grid">
              <article v-for="role in form.roles" :key="role.name">
                <div><strong>{{ role.name }}</strong><span>{{ role.capacity }} 人 / {{ role.duty }}</span></div>
                <el-input-number v-model="role.capacity" :min="1" :max="8" size="small" />
              </article>
            </div>
            <div class="admin-training-flow">
              <article v-for="(node, index) in form.flow" :key="node.name">
                <i>{{ index + 1 }}</i>
                <div><strong>{{ node.name }}</strong><span>{{ node.rule }}</span></div>
                <el-input-number v-model="node.score" :min="0" :max="100" size="small" />
              </article>
            </div>
          </section>
        </div>

        <div class="admin-training-drawer-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button @click="saveDraft">保存草稿</el-button>
          <el-button type="primary" @click="openPublish()">保存并发布</el-button>
        </div>
      </el-drawer>

      <el-dialog v-model="selectorVisible" class="admin-training-dialog" width="760px" :show-close="false" append-to-body>
        <template #header>
          <div class="admin-training-dialog-head">
            <strong>{{ selectorTitle }}</strong>
            <el-button text circle :icon="Close" @click="selectorVisible = false" />
          </div>
        </template>
        <div class="admin-training-selector">
          <div class="admin-training-selector-filter">
            <el-input v-model="selectorKeyword" :prefix-icon="Search" placeholder="请输入关键字" clearable />
            <el-select v-if="selectorKind === 'topic' || selectorKind === 'resource'" v-model="selectorType" placeholder="类型" clearable>
              <el-option label="信号" value="信号" />
              <el-option label="站务" value="站务" />
              <el-option label="调度" value="调度" />
            </el-select>
          </div>
          <div class="admin-training-selector-list">
            <article v-for="item in selectorItems" :key="item.id" :class="{ checked: isSelected(item.id) }" @click="toggleSelect(item.id)">
              <el-checkbox :model-value="isSelected(item.id)" @click.stop @change="toggleSelect(item.id)" />
              <div><strong>{{ item.name }}</strong><span>{{ item.meta }}</span></div>
            </article>
          </div>
        </div>
        <template #footer>
          <div class="admin-training-dialog-footer">
            <el-button @click="selectorVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmSelector">确定</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="roleVisible" class="admin-training-dialog" width="520px" :show-close="false" append-to-body>
        <template #header><div class="admin-training-dialog-head"><strong>新增协同角色</strong><el-button text circle :icon="Close" @click="roleVisible = false" /></div></template>
        <div class="admin-training-mini-form">
          <label><span>角色名称</span><el-input v-model="roleForm.name" placeholder="请输入角色名称" /></label>
          <label><span>人数</span><el-input-number v-model="roleForm.capacity" :min="1" :max="8" /></label>
          <label><span>职责说明</span><el-input v-model="roleForm.duty" type="textarea" :rows="3" placeholder="请输入职责说明" /></label>
        </div>
        <template #footer><div class="admin-training-dialog-footer"><el-button @click="roleVisible = false">取消</el-button><el-button type="primary" @click="addRole">确定</el-button></div></template>
      </el-dialog>

      <el-dialog v-model="previewVisible" class="admin-training-dialog" width="820px" :show-close="false" append-to-body>
        <template #header><div class="admin-training-dialog-head"><strong>实训课预览</strong><el-button text circle :icon="Close" @click="previewVisible = false" /></div></template>
        <div class="admin-training-preview">
          <section>
            <h4>{{ previewCourse?.name || form.name || '新增实训课' }}</h4>
            <p>{{ previewCourse?.time || formatRange }}</p>
            <div><span>{{ previewCourse?.type || form.type }}</span><span>{{ previewCourse?.mode || form.mode }}</span><span>{{ previewCourse?.room || selectedRoom?.name || '未选择教室' }}</span></div>
          </section>
          <section class="admin-training-preview-grid">
            <article><span>参训对象</span><strong>{{ previewCourse?.target || selectedClasses.map((item) => item.name).join('、') || '未选择' }}</strong></article>
            <article><span>监考教师</span><strong>{{ previewCourse?.teacher || selectedTeachers.map((item) => item.name).join('、') || '未选择' }}</strong></article>
            <article><span>实训任务</span><strong>{{ previewCourse?.topicCount || selectedTopics.length }} 个</strong></article>
            <article><span>总分</span><strong>{{ totalScore }} 分</strong></article>
          </section>
          <div class="admin-training-preview-list">
            <article v-for="item in selectedTopics" :key="item.id"><strong>{{ item.name }}</strong><span>{{ item.category }} / {{ item.duration }} 分钟 / {{ item.score }} 分</span></article>
          </div>
        </div>
        <template #footer><div class="admin-training-dialog-footer"><el-button @click="previewVisible = false">关闭</el-button><el-button type="primary" @click="openPublish(previewCourse)">发布</el-button></div></template>
      </el-dialog>

      <el-dialog v-model="publishVisible" class="admin-training-dialog" width="560px" :show-close="false" append-to-body>
        <template #header><div class="admin-training-dialog-head"><strong>发布确认</strong><el-button text circle :icon="Close" @click="publishVisible = false" /></div></template>
        <div class="admin-training-publish-confirm">
          <strong>{{ publishTarget?.name || form.name || '新增实训课' }}</strong>
          <p>发布后学员端将看到该实训课，参训对象会进入实训任务列表。</p>
          <label><el-checkbox v-model="publishNotify" /> 发布后通知参训学员和监考教师</label>
        </div>
        <template #footer><div class="admin-training-dialog-footer"><el-button @click="publishVisible = false">取消</el-button><el-button type="primary" @click="confirmPublish">确认发布</el-button></div></template>
      </el-dialog>

      <el-dialog v-model="importVisible" class="admin-training-dialog" width="760px" :show-close="false" append-to-body>
        <template #header><div class="admin-training-dialog-head"><strong>导入线下成绩</strong><el-button text circle :icon="Close" @click="importVisible = false" /></div></template>
        <div class="admin-training-import">
          <section class="offline-import-section">
            <strong>1. 下载模板</strong>
            <p>请选择已结束的实训组课，模板将按该组课的实训题生成成绩列。</p>
            <div class="offline-import-row">
              <el-select v-model="offlineTrainingId" placeholder="请选择已结束的实训组课" filterable @change="loadOfflineTopics">
                <el-option v-for="item in offlineTrainingOptions" :key="item.trainingId" :label="item.trainingName" :value="item.trainingId" />
              </el-select>
              <el-button :disabled="!offlineTrainingId" @click="downloadOfflineTemplate">下载导入模板</el-button>
            </div>
          </section>
          <div class="admin-training-upload-box">
            <el-icon><UploadFilled /></el-icon>
            <strong>2. 上传文件</strong>
            <span>仅支持系统模板生成的 .xlsx 文件，禁止修改表头</span>
            <input ref="importInput" class="admin-training-file-input" type="file" accept=".xlsx" @change="handleImportChange" />
            <el-button type="primary" plain :disabled="!offlineTrainingId" @click="importInput?.click()">选择文件</el-button>
            <small v-if="offlineImportFileName">{{ offlineImportFileName }}，共 {{ offlineImportRows.length }} 条</small>
          </div>
          <div class="admin-training-import-result" :class="{ active: offlineImportResult }">
            <strong>3. 导入记录提示</strong>
            <p v-if="offlineImportResult">共 {{ offlineImportResult.totalCount }} 条，成功 {{ offlineImportResult.successCount }} 条，失败 {{ offlineImportResult.failureCount }} 条。</p>
            <p v-else>导入后将展示成功数量和错误明细；重复导入只覆盖原线下成绩。</p>
            <el-button v-if="offlineImportResult?.failureCount" link type="primary" @click="downloadOfflineErrors">下载错误日志</el-button>
          </div>
        </div>
        <template #footer><div class="admin-training-dialog-footer"><el-button @click="importVisible = false">取消</el-button><el-button type="primary" :loading="importLoading" :disabled="!offlineTrainingId || !offlineImportRows.length" @click="confirmImport">确认导入</el-button></div></template>
      </el-dialog>

      <el-drawer v-model="logVisible" class="admin-training-log-drawer" direction="rtl" size="520px" :with-header="false">
        <div class="admin-training-drawer-head compact">
          <div><span>操作日志</span><h3>{{ selectedCourse?.name || '实训课记录' }}</h3></div>
          <el-button text circle :icon="Close" @click="logVisible = false" />
        </div>
        <article v-for="item in logs" :key="item.time" class="admin-training-log-row">
          <header><strong>{{ item.action }}</strong><span>{{ item.time }}</span></header>
          <p>{{ item.content }}</p>
          <small>{{ item.operator }}</small>
        </article>
        <el-empty v-if="logs.length === 0" description="暂无操作日志" />
      </el-drawer>

    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Document, FolderOpened, OfficeBuilding, Plus, Search, Tickets, Upload, UploadFilled, User, UserFilled, View } from '@element-plus/icons-vue';
import * as XLSX from 'xlsx';
import AdminShell from '../../components/admin/AdminShell.vue';
import { fetchAdminPapers } from '../../api/admin-paper';
import { fetchAdminResources } from '../../api/admin-resource';
import {
  cancelPublishAdminTraining,
  createAdminTraining,
  deleteAdminTraining,
  fetchAdminTraining,
  fetchAdminTrainingLogs,
  fetchAdminTrainingTopics,
  fetchAdminTrainings,
  importAdminTrainingOfflineScores,
  publishAdminTraining,
  updateAdminTraining,
  type AdminTraining,
  type AdminTrainingLog,
  type AdminTrainingOfflineScoreImportResult,
  type AdminTrainingOfflineScoreImportRow,
  type AdminTrainingTopic
} from '../../api/admin-training';
import { fetchAdminAcademicYears, fetchAdminClassrooms, fetchAdminClasses as fetchAdminSettingsClasses, fetchAdminMajors } from '../../api/admin-settings';
import { fetchAdminTeachers } from '../../api/admin-course';
import trainingCoverUrl from '../../assets/course-station-preview.png';

const router = useRouter();

type CourseStatus = '已发布' | '未发布';
type SelectorKind = 'topic' | 'resource' | 'paper' | 'class' | 'teacher' | 'room';
type StepKey = 'base' | 'resource' | 'target' | 'rule';

interface CourseRow {
  id: number;
  name: string;
  type: '考试' | '练习';
  mode: '单人实训' | '协同实训';
  time: string;
  target: string;
  teacher: string;
  room: string;
  status: CourseStatus;
  createdAt: string;
  topicCount: number;
  openStartTime?: string;
  openEndTime?: string;
  exam?: boolean;
  examStarted?: boolean;
}

interface SelectableItem {
  id: number;
  name: string;
  meta: string;
  majorId?: number;
  capacity?: number;
  category?: string;
  duration?: number;
  score?: number;
  type?: string;
  size?: string;
}

interface TrainingRole {
  name: string;
  capacity: number;
  duty: string;
}

interface TrainingFlowNode {
  name: string;
  rule: string;
  score: number;
}

const filters = reactive({ keyword: '', time: [] as Date[], status: '' });
const loading = ref(false);
const page = ref(1);
const pageSize = 8;
const total = ref(0);
const formVisible = ref(false);
const formMode = ref<'create' | 'edit'>('create');
const activeStep = ref<StepKey>('base');
const selectedCourse = ref<CourseRow>();
const previewCourse = ref<CourseRow>();
const logVisible = ref(false);
const selectorVisible = ref(false);
const selectorKind = ref<SelectorKind>('topic');
const selectorKeyword = ref('');
const selectorType = ref('');
const selectorDraft = ref<number[]>([]);
const roleVisible = ref(false);
const previewVisible = ref(false);
const publishVisible = ref(false);
const publishTarget = ref<CourseRow>();
const publishNotify = ref(true);
const importVisible = ref(false);
const importInput = ref<HTMLInputElement>();
const importLoading = ref(false);
const offlineTrainingId = ref<number>();
const offlineTrainingOptions = ref<AdminTraining[]>([]);
const offlineTrainingTopics = ref<AdminTrainingTopic[]>([]);
const offlineImportFileName = ref('');
const offlineImportRows = ref<AdminTrainingOfflineScoreImportRow[]>([]);
const offlineImportResult = ref<AdminTrainingOfflineScoreImportResult>();

const form = reactive({
  id: 0,
  name: '',
  type: '考试' as '考试' | '练习',
  mode: '协同实训' as '单人实训' | '协同实训',
  semester: '',
  majorId: undefined as number | undefined,
  range: [] as string[],
  description: '',
  teacherIds: [] as number[],
  classIds: [] as number[],
  roles: [] as TrainingRole[],
  flow: [] as TrainingFlowNode[]
});

const roleForm = reactive({ name: '', capacity: 1, duty: '' });

const steps = [
  { key: 'base' as StepKey, index: 1, label: '基础信息' },
  { key: 'resource' as StepKey, index: 2, label: '实训内容' },
  { key: 'target' as StepKey, index: 3, label: '对象场地' },
  { key: 'rule' as StepKey, index: 4, label: '角色规则' }
];

const courses = ref<CourseRow[]>([]);

const topicOptions = ref<SelectableItem[]>([]);
const semesterOptions = computed(() =>
  academicYears.value.flatMap((year) =>
    (year.semesters ?? []).map((semester) => ({
      academicYearId: year.academicYearId,
      semesterId: semester.semesterId,
      value: `${year.yearName} ${semester.semesterName}`,
      label: `${year.yearName} ${semester.semesterName}`
    }))
  )
);

const classOptions = ref<SelectableItem[]>([]);
const majorOptions = ref<Array<{ majorId: number; majorName: string; enabled?: boolean }>>([]);
const academicYears = ref<Array<{ academicYearId: number; yearName: string; semesters?: Array<{ semesterId: number; semesterName: string; current?: boolean }> }>>([]);

const selectedTopicIds = ref<number[]>([]);
const selectedResourceIds = ref<number[]>([]);
const selectedPaperId = ref<number>(0);
const selectedClassIds = ref<number[]>([]);
const selectedTeacherIds = ref<number[]>([]);
const selectedRoomId = ref<number>(0);

const logs = ref<Array<{ time: string; operator: string; action: string; content: string }>>([]);

const resourceOptions = ref<SelectableItem[]>([]);
const paperOptions = ref<SelectableItem[]>([]);
const teacherOptions = ref<SelectableItem[]>([]);
const roomOptions = ref<SelectableItem[]>([]);

const selectedTopics = computed(() => topicOptions.value.filter((item) => selectedTopicIds.value.includes(item.id)));
const selectedResources = computed(() => resourceOptions.value.filter((item) => selectedResourceIds.value.includes(item.id)));
const selectedPaper = computed(() => paperOptions.value.find((item) => item.id === selectedPaperId.value));
const selectedClasses = computed(() => classOptions.value.filter((item) => selectedClassIds.value.includes(item.id)));
const selectedTeachers = computed(() => teacherOptions.value.filter((item) => selectedTeacherIds.value.includes(item.id)));
const selectedRoom = computed(() => roomOptions.value.find((item) => item.id === selectedRoomId.value));
const totalScore = computed(() => selectedTopics.value.reduce((sum, item) => sum + (item.score ?? 0), 0));
const formatRange = computed(() => form.range.length === 2 ? `${form.range[0]}\n至 ${form.range[1]}` : '未选择时间');

const selectorTitle = computed(() => ({
  topic: '选择实训任务',
  resource: '选择资源课件',
  paper: '选择理论试卷',
  class: '选择班级/学生',
  teacher: '选择监考教师',
  room: '选择实训教室'
})[selectorKind.value]);

const selectorItems = computed(() => {
  const source = {
    topic: topicOptions.value,
    resource: resourceOptions.value,
    paper: paperOptions.value,
    class: classOptions.value,
    teacher: teacherOptions.value,
    room: roomOptions.value
  }[selectorKind.value];

  return source.filter((item) => {
    const keywordMatched = !selectorKeyword.value || item.name.includes(selectorKeyword.value) || item.meta.includes(selectorKeyword.value);
    const typeMatched = !selectorType.value || item.category === selectorType.value || item.name.includes(selectorType.value);
    return keywordMatched && typeMatched;
  });
});

function resetFilters() {
  filters.keyword = '';
  filters.time = [];
  filters.status = '';
  page.value = 1;
  void loadCourses();
}

function refreshCourses() {
  page.value = 1;
  void loadCourses();
}

function openCreate() {
  router.push({ name: 'admin-training-new' });
}

async function openEdit(course: CourseRow) {
  if (course.status === '已发布') {
    try {
      await ElMessageBox.confirm(`确定要编辑【${course.name}】吗？编辑后若有学习数据将无法恢复，请谨慎操作`, '编辑实训课', {
        type: 'warning',
        confirmButtonText: '继续编辑',
        cancelButtonText: '取消'
      });
    } catch {
      return;
    }
  }
  router.push({ name: 'admin-training-edit', params: { id: course.id } });
}

async function saveDraft() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入实训课名称');
    return;
  }

  try {
    const command = buildTrainingCommand('DRAFT');
    if (formMode.value === 'edit' && form.id) {
      await updateAdminTraining(form.id, command);
    } else {
      await createAdminTraining(command);
    }
    ElMessage.success('草稿已保存');
    formVisible.value = false;
    await loadCourses();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存草稿失败');
  }
}

function openSelector(kind: SelectorKind) {
  selectorKind.value = kind;
  selectorKeyword.value = '';
  selectorType.value = '';
  selectorDraft.value = currentSelectedIds(kind);
  selectorVisible.value = true;
}

function currentSelectedIds(kind: SelectorKind) {
  if (kind === 'topic') return [...selectedTopicIds.value];
  if (kind === 'resource') return [...selectedResourceIds.value];
  if (kind === 'paper') return selectedPaperId.value ? [selectedPaperId.value] : [];
  if (kind === 'class') return [...selectedClassIds.value];
  if (kind === 'teacher') return [...selectedTeacherIds.value];
  return selectedRoomId.value ? [selectedRoomId.value] : [];
}

function isSelected(id: number) {
  return selectorDraft.value.includes(id);
}

function toggleSelect(id: number) {
  const single = selectorKind.value === 'paper' || selectorKind.value === 'room';
  if (single) {
    selectorDraft.value = [id];
    return;
  }

  selectorDraft.value = selectorDraft.value.includes(id)
    ? selectorDraft.value.filter((item) => item !== id)
    : [...selectorDraft.value, id];
}

function confirmSelector() {
  if (selectorKind.value === 'topic') selectedTopicIds.value = [...selectorDraft.value];
  if (selectorKind.value === 'resource') selectedResourceIds.value = [...selectorDraft.value];
  if (selectorKind.value === 'paper') selectedPaperId.value = selectorDraft.value[0] ?? 0;
  if (selectorKind.value === 'class') selectedClassIds.value = [...selectorDraft.value];
  if (selectorKind.value === 'teacher') selectedTeacherIds.value = [...selectorDraft.value];
  if (selectorKind.value === 'room') selectedRoomId.value = selectorDraft.value[0] ?? 0;
  selectorVisible.value = false;
}

function removeSelected(kind: 'topic' | 'resource', id: number) {
  if (kind === 'topic') selectedTopicIds.value = selectedTopicIds.value.filter((item) => item !== id);
  if (kind === 'resource') selectedResourceIds.value = selectedResourceIds.value.filter((item) => item !== id);
}

function openRoleDialog() {
  roleForm.name = '';
  roleForm.capacity = 1;
  roleForm.duty = '';
  roleVisible.value = true;
}

function addRole() {
  if (!roleForm.name.trim()) {
    ElMessage.warning('请输入角色名称');
    return;
  }
  form.roles.push({ name: roleForm.name, capacity: roleForm.capacity, duty: roleForm.duty || '待配置职责' });
  roleVisible.value = false;
}

function openPreview(course?: CourseRow) {
  previewCourse.value = course;
  previewVisible.value = true;
}

function openPublish(course?: CourseRow) {
  publishTarget.value = course;
  publishVisible.value = true;
}

async function confirmPublish() {
  try {
    if (publishTarget.value) {
      await publishAdminTraining(publishTarget.value.id);
    } else {
      const command = buildTrainingCommand('PUBLISHED');
      const result = formMode.value === 'edit' && form.id
        ? (await updateAdminTraining(form.id, command), { trainingId: form.id })
        : await createAdminTraining(command);
      await publishAdminTraining(result.trainingId);
      formVisible.value = false;
    }
    publishVisible.value = false;
    ElMessage.success(publishNotify.value ? '已发布并发送通知' : '已发布');
    await loadCourses();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发布失败');
  }
}

async function openOfflineImport() {
  importVisible.value = true;
  offlineTrainingId.value = undefined;
  offlineTrainingTopics.value = [];
  offlineImportFileName.value = '';
  offlineImportRows.value = [];
  offlineImportResult.value = undefined;
  if (importInput.value) importInput.value.value = '';
  try {
    const result = await fetchAdminTrainings({ publishStatus: 'PUBLISHED', page: 1, pageSize: 100 });
    const now = Date.now();
    offlineTrainingOptions.value = result.records.filter((item) => item.openEndTime && new Date(item.openEndTime).getTime() < now);
  } catch (error) {
    offlineTrainingOptions.value = [];
    ElMessage.error(error instanceof Error ? error.message : '已结束实训组课加载失败');
  }
}

async function loadOfflineTopics() {
  offlineImportFileName.value = '';
  offlineImportRows.value = [];
  offlineImportResult.value = undefined;
  if (importInput.value) importInput.value.value = '';
  if (!offlineTrainingId.value) {
    offlineTrainingTopics.value = [];
    return;
  }
  try {
    const [detail, topics] = await Promise.all([fetchAdminTraining(offlineTrainingId.value), fetchAdminTrainingTopics()]);
    const topicIds = new Set(detail.topicIds || []);
    offlineTrainingTopics.value = topics.filter((item) => topicIds.has(item.topicId));
  } catch (error) {
    offlineTrainingTopics.value = [];
    ElMessage.error(error instanceof Error ? error.message : '实训题信息加载失败');
  }
}

function offlineTopicHeader(topic: AdminTrainingTopic) {
  return `${topic.topicName}（题目ID:${topic.topicId}）`;
}

function downloadOfflineTemplate() {
  if (!offlineTrainingId.value || !offlineTrainingTopics.value.length) {
    ElMessage.warning('该实训组课未配置实训题');
    return;
  }
  const headers = ['学员姓名', '工号', '班级', ...offlineTrainingTopics.value.map(offlineTopicHeader), '总成绩', '训练备注'];
  const worksheet = XLSX.utils.aoa_to_sheet([headers]);
  worksheet['!cols'] = headers.map((header) => ({ wch: Math.max(14, Math.min(32, header.length + 4)) }));
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '线下实训成绩');
  const training = offlineTrainingOptions.value.find((item) => item.trainingId === offlineTrainingId.value);
  XLSX.writeFile(workbook, `${training?.trainingName || '实训组课'}-线下成绩导入模板.xlsx`);
}

function numericCell(value: unknown) {
  if (value === '' || value === null || value === undefined) return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

async function handleImportChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;
  if (!/\.xlsx$/i.test(file.name)) {
    ElMessage.error('仅支持 .xlsx 格式');
    return;
  }
  try {
    const workbook = XLSX.read(await file.arrayBuffer(), { type: 'array' });
    const sheet = workbook.Sheets[workbook.SheetNames[0]];
    const rows = XLSX.utils.sheet_to_json<Record<string, unknown>>(sheet, { defval: '' });
    const requiredHeaders = ['学员姓名', '工号', '班级', ...offlineTrainingTopics.value.map(offlineTopicHeader), '总成绩', '训练备注'];
    const actualHeaders = rows[0] ? Object.keys(rows[0]).map((item) => item.trim()) : [];
    if (requiredHeaders.some((header) => !actualHeaders.includes(header))) {
      ElMessage.error('模板表头不完整，请重新下载当前组课的导入模板');
      return;
    }
    offlineImportFileName.value = file.name;
    offlineImportResult.value = undefined;
    offlineImportRows.value = rows.map((row, index) => ({
      rowNumber: index + 2,
      studentName: String(row['学员姓名'] || '').trim(),
      studentNo: String(row['工号'] || '').trim(),
      className: String(row['班级'] || '').trim(),
      totalScore: numericCell(row['总成绩']),
      remark: String(row['训练备注'] || '').trim() || undefined,
      topicScores: Object.fromEntries(offlineTrainingTopics.value.map((topic) => [topic.topicId, numericCell(row[offlineTopicHeader(topic)])]))
    }));
    if (!rows.length) ElMessage.warning('导入文件没有可识别的数据行');
  } catch {
    ElMessage.error('文件解析失败，请使用当前组课下载的 .xlsx 模板');
  }
}

async function confirmImport() {
  if (!offlineTrainingId.value || !offlineImportFileName.value || !offlineImportRows.value.length) return;
  importLoading.value = true;
  try {
    offlineImportResult.value = await importAdminTrainingOfflineScores({
      trainingId: offlineTrainingId.value,
      fileName: offlineImportFileName.value,
      rows: offlineImportRows.value
    });
    if (offlineImportResult.value.failureCount) {
      ElMessage.warning(`成功 ${offlineImportResult.value.successCount} 条，失败 ${offlineImportResult.value.failureCount} 条`);
    } else {
      ElMessage.success(`已导入 ${offlineImportResult.value.successCount} 条线下成绩`);
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '线下成绩导入失败');
  } finally {
    importLoading.value = false;
  }
}

function downloadOfflineErrors() {
  if (!offlineImportResult.value?.errors.length) return;
  const worksheet = XLSX.utils.json_to_sheet(offlineImportResult.value.errors.map((item) => ({
    行号: item.rowNumber,
    工号: item.studentNo || '',
    错误原因: item.message
  })));
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '错误明细');
  XLSX.writeFile(workbook, `线下成绩导入错误日志-批次${offlineImportResult.value.batchId}.xlsx`);
}

function openMonitor(row: CourseRow) {
  router.push({
    name: 'admin-training-monitor',
    params: { id: row.id },
    query: { title: row.name, time: row.time, room: row.room }
  });
}

function openExamStart(row: CourseRow) {
  router.push({
    name: 'admin-training-exam-start',
    params: { id: row.id },
    query: { title: row.name, time: row.time, room: row.room }
  });
}

function openMarking(row: CourseRow) {
  router.push({
    name: 'admin-training-reviews',
    params: { id: row.id },
    query: { title: row.name }
  });
}

function openStats(row: CourseRow) {
  router.push({
    name: 'admin-training-statistics',
    params: { id: row.id },
    query: { title: row.name, target: row.target }
  });
}

async function openLogs(row: CourseRow) {
  selectedCourse.value = row;
  try {
    logs.value = (await fetchAdminTrainingLogs(row.id)).map(mapLog);
  } catch (error) {
    logs.value = [];
    ElMessage.error(error instanceof Error ? error.message : '操作日志加载失败');
  }
  logVisible.value = true;
}

async function confirmDelete(course: CourseRow) {
  await ElMessageBox.confirm(`确定要删除【${course.name}】吗？删除后实训课及学习数据将无法恢复，请谨慎操作`, '删除实训课', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' });
  try {
    await deleteAdminTraining(course.id);
    ElMessage.success('实训课已删除');
    await loadCourses();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  }
}

async function withdrawCourse(course: CourseRow) {
  try {
    await ElMessageBox.confirm(`确定要取消发布【${course.name}】吗？取消后学员将无法继续访问该实训`, '取消发布', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });
    await cancelPublishAdminTraining(course.id);
    ElMessage.success('已取消发布');
    await loadCourses();
  } catch (error) {
    if (error === 'cancel' || error === 'close') return;
    ElMessage.error(error instanceof Error ? error.message : '取消发布失败');
  }
}

async function loadCourses() {
  loading.value = true;
  try {
    const result = await fetchAdminTrainings({
      keyword: filters.keyword.trim() || undefined,
      publishStatus: statusToApi(filters.status),
      rangeStart: filters.time[0] ? formatLocalDateTime(filters.time[0]) : undefined,
      rangeEnd: filters.time[1] ? formatLocalDateTime(filters.time[1]) : undefined,
      page: page.value,
      pageSize
    });
    courses.value = result.records.map(mapCourse);
    total.value = result.total;
  } catch (error) {
    courses.value = [];
    total.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '实训课列表加载失败');
  } finally {
    loading.value = false;
  }
}

async function loadOptions() {
  try {
    const [years, majors, classes, teachers, papers, resources, classrooms] = await Promise.all([
      fetchAdminAcademicYears(),
      fetchAdminMajors(),
      fetchAdminSettingsClasses(),
      fetchAdminTeachers(),
      fetchAdminPapers({ page: 1, pageSize: 200 }),
      fetchAdminResources({ page: 1, pageSize: 200 }),
      fetchAdminClassrooms()
    ]);
    academicYears.value = years;
    majorOptions.value = majors.filter((item) => item.enabled !== false);
    classOptions.value = classes.filter((item) => item.enabled !== false).map((item) => ({
      id: item.classId,
      name: item.className,
      meta: item.majorName ? `${item.majorName}` : '班级',
      category: item.majorName || 'class'
    }));
    teacherOptions.value = teachers.filter((item) => item.enabled !== false).map((item) => ({
      id: item.userId,
      name: item.realName || item.accountNo || `教师${item.userId}`,
      meta: item.accountNo ? item.accountNo : '教师',
      category: 'teacher'
    }));
    paperOptions.value = (papers.records || []).map((item) => ({
      id: item.paperId,
      name: item.paperName,
      meta: `${item.questionCount || 0} 题 / ${item.totalScore || 0} 分`,
      category: item.publishStatus
    }));
    resourceOptions.value = (resources.records || []).map((item) => ({
      id: item.resourceId,
      name: item.resourceName,
      meta: `${item.resourceType || '资源'} / ${item.fileSize ? `${(item.fileSize / 1024 / 1024).toFixed(1)} MB` : '-'}`,
      category: item.resourceType,
      type: item.resourceType,
      size: item.fileSize ? `${(item.fileSize / 1024 / 1024).toFixed(1)} MB` : '-'
    }));
    roomOptions.value = classrooms.map((item) => ({
      id: item.classroomId,
      name: item.roomName,
      meta: `${item.cameraCount} 路摄像头`,
      category: 'room'
    }));
    form.semester = '';
    form.majorId = undefined;
    form.teacherIds = [];
    form.classIds = [];
    selectedResourceIds.value = [];
    selectedPaperId.value = 0;
    selectedTeacherIds.value = [];
    selectedRoomId.value = 0;
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '课程基础数据加载失败');
  }
}

function mapCourse(item: AdminTraining): CourseRow {
  const type = apiTrainingTypeToText(item.trainingType);
  return {
    id: item.trainingId,
    name: item.trainingName || '-',
    type,
    mode: apiTrainingModeToText(item.trainingMode),
    time: `${formatDateTime(item.openStartTime)}\n至 ${formatDateTime(item.openEndTime)}`,
    target: item.classNames || '-',
    teacher: item.teacherNames || '-',
    room: item.classroomName || '-',
    status: apiStatusToText(item.publishStatus),
    createdAt: formatDateTime(item.createdAt),
    topicCount: item.topicCount || 0,
    openStartTime: item.openStartTime,
    openEndTime: item.openEndTime,
    exam: type === '考试',
    examStarted: Boolean(item.examStartedAt)
  };
}

function isTrainingOpen(course: CourseRow) {
  if (!course.openStartTime || !course.openEndTime) return false;
  const now = Date.now();
  return now >= new Date(course.openStartTime).getTime() && now <= new Date(course.openEndTime).getTime();
}

function buildTrainingCommand(publishStatus: string) {
  const semester = semesterOptions.value.find((item) => item.value === form.semester);
  const majorId = form.majorId || classOptions.value.find((item) => selectedClassIds.value.includes(item.id))?.majorId;
  return {
    trainingName: form.name.trim(),
    academicYearId: semester?.academicYearId,
    semesterId: semester?.semesterId,
    majorId,
    coverUrl: trainingCoverUrl,
    trainingType: trainingTypeToApi(form.type),
    trainingMode: trainingModeToApi(form.mode),
    paperMode: selectedPaperId.value ? 'THEORY_PAPER' : 'NONE',
    paperId: selectedPaperId.value || undefined,
    openStartTime: form.range[0],
    openEndTime: form.range[1],
    teamSize: form.roles.reduce((sum, role) => sum + Number(role.capacity || 0), 0) || 1,
    appRequired: true,
    classroomId: selectedRoomId.value || undefined,
    teacherIds: [...selectedTeacherIds.value],
    scoreBasis: 'HIGHEST' as const,
    topicIds: [...selectedTopicIds.value],
    classIds: [...selectedClassIds.value],
    roles: form.roles.map((role, index) => ({
      roleName: role.name,
      roleCode: role.name,
      capacity: Number(role.capacity || 1),
      aiFillEnabled: true,
      sortOrder: index + 1
    })),
    publishStatus
  };
}

function mapLog(item: AdminTrainingLog) {
  const actionText: Record<string, string> = {
    CREATE: '新增',
    UPDATE: '编辑',
    PUBLISH: '发布',
    CANCEL_PUBLISH: '取消发布',
    START_EXAM: '开始考试'
  };
  const action = String(item.action || '').toUpperCase();
  return {
    time: formatDateTime(item.createdAt),
    operator: item.operatorName || '-',
    action: actionText[action] || item.action || '-',
    content: item.content || '-'
  };
}

function apiStatusToText(status?: string): CourseStatus {
  return status === 'PUBLISHED' || status === 'published' ? '已发布' : '未发布';
}

function statusToApi(status?: string) {
  if (status === '已发布') return 'PUBLISHED';
  if (status === '未发布') return 'UNPUBLISHED';
  return undefined;
}

function trainingTypeToApi(type?: string) {
  if (type === '考试') return 'EXAM';
  if (type === '练习') return 'PRACTICE';
  return undefined;
}

function apiTrainingTypeToText(type?: string): '考试' | '练习' {
  return type === 'PRACTICE' ? '练习' : '考试';
}

function trainingModeToApi(mode?: string) {
  if (mode === '单人实训') return 'SINGLE';
  if (mode === '协同实训') return 'TEAM';
  return undefined;
}

function apiTrainingModeToText(mode?: string): '单人实训' | '协同实训' {
  return mode === 'SINGLE' ? '单人实训' : '协同实训';
}

function formatDateTime(value?: string) {
  if (!value) return '-';
  return value.replace('T', ' ').slice(0, 16);
}

function formatLocalDateTime(value: Date) {
  const pad = (part: number) => String(part).padStart(2, '0');
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`;
}

onMounted(() => {
  void loadOptions();
  void loadCourses();
});
</script>

<style scoped>
.admin-training-toolbar,
.admin-training-table-card,
.admin-training-form-card {
  border: 1px solid #dfe6f0;
  border-radius: 10px;
  background: #ffffff;
}

.admin-training-toolbar {
  min-height: 124px;
  padding: 16px 20px;
}

.admin-training-filter-row,
.admin-training-action-row,
.admin-training-card-actions,
.admin-training-dialog-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-training-action-row {
  margin-top: 12px;
}

.admin-training-search {
  width: 240px;
}

.admin-training-select {
  width: 128px;
}

.admin-training-primary.el-button,
.admin-training-ghost.el-button {
  height: 40px;
  border-radius: 8px;
  font-weight: 800;
}

.admin-training-primary.el-button {
  border: 0;
  background: #3478f6;
}

.admin-training-ghost.el-button {
  border-color: #dfe6f0;
  background: #ffffff;
  color: #536681;
}

.admin-training-table-card {
  overflow: hidden;
}

.admin-training-table-scroll {
  width: 100%;
  overflow-x: auto;
}

.admin-training-table {
  width: 100%;
  min-width: 1584px;
  border-collapse: collapse;
  table-layout: fixed;
}

.admin-training-col-name {
  width: 220px;
}

.admin-training-col-type {
  width: 82px;
}

.admin-training-col-time {
  width: 170px;
}

.admin-training-col-target {
  width: 170px;
}

.admin-training-col-teacher {
  width: 120px;
}

.admin-training-col-room {
  width: 120px;
}

.admin-training-col-status {
  width: 100px;
}

.admin-training-col-created {
  width: 142px;
}

.admin-training-col-operation {
  width: 360px;
}

.admin-training-table th {
  height: 52px;
  padding: 0 12px;
  border-bottom: 1px solid #edf2f8;
  background: #f8fafc;
  color: #263a55;
  font-size: 13px;
  text-align: left;
}

.admin-training-table td {
  height: 68px;
  padding: 0 12px;
  border-bottom: 1px solid #edf2f8;
  color: #334155;
  font-size: 13px;
  vertical-align: middle;
}

.admin-training-table th.admin-training-operation-cell,
.admin-training-table td.admin-training-operation-cell {
  padding-right: 14px;
  padding-left: 14px;
}

.admin-training-table td.admin-training-operation-cell {
  vertical-align: middle;
}

.admin-training-operation-cell .admin-row-actions {
  flex-wrap: nowrap;
  max-width: none;
  padding: 0;
}

.admin-training-name-cell {
  display: grid;
  gap: 4px;
}

.admin-training-name-cell strong {
  color: #1e293b;
  font-size: 14px;
}

.admin-training-name-cell span,
.admin-training-subsection p,
.admin-training-paper-row span {
  color: #64748b;
  font-size: 12px;
}

.admin-training-type-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 24px;
  border-radius: 12px;
  background: #eef5ff;
  color: #3478f6;
  font-size: 12px;
  font-weight: 800;
}

.admin-training-multiline {
  white-space: pre-line;
  line-height: 1.45;
}

.admin-training-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-training-drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #edf2f8;
  background: #ffffff;
}

.admin-training-drawer-head.compact {
  margin: -20px -20px 20px;
}

.admin-training-drawer-head span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.admin-training-drawer-head h3 {
  margin: 6px 0 0;
  color: #17233d;
  font-size: 20px;
  line-height: 28px;
}

.admin-training-stepper {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  padding: 16px 24px;
  background: #f8fafc;
}

.admin-training-stepper button {
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #dfe6f0;
  border-radius: 8px;
  background: #ffffff;
  color: #64748b;
  cursor: pointer;
  font: inherit;
  font-weight: 800;
}

.admin-training-stepper button.active {
  border-color: #3478f6;
  color: #3478f6;
  background: #eef5ff;
}

.admin-training-stepper i {
  width: 22px;
  height: 22px;
  display: inline-grid;
  place-items: center;
  border-radius: 50%;
  background: currentColor;
  color: #ffffff;
  font-style: normal;
  font-size: 12px;
}

.admin-training-form-body {
  display: grid;
  gap: 16px;
  padding: 0 24px 88px;
  background: #f8fafc;
}

.admin-training-form-card {
  padding: 18px 20px;
}

.admin-training-form-card header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.admin-training-form-card header strong {
  color: #17233d;
  font-size: 16px;
  font-weight: 800;
}

.admin-training-form-card header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.admin-training-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.admin-training-form-grid label,
.admin-training-mini-form label {
  display: grid;
  gap: 8px;
}

.admin-training-form-grid label.wide {
  grid-column: 1 / -1;
}

.admin-training-form-grid span,
.admin-training-mini-form span {
  color: #425268;
  font-size: 13px;
  font-weight: 800;
}

.admin-training-form-grid b {
  color: #ef4444;
}

.admin-training-selected-list,
.admin-training-resource-grid,
.admin-training-role-grid,
.admin-training-flow,
.admin-training-preview-list,
.admin-training-mini-form {
  display: grid;
  gap: 10px;
}

.admin-training-selected-list article,
.admin-training-resource-grid article,
.admin-training-role-grid article,
.admin-training-flow article,
.admin-training-selector-list article,
.admin-training-log-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  background: #ffffff;
}

.admin-training-selected-list strong,
.admin-training-resource-grid strong,
.admin-training-role-grid strong,
.admin-training-flow strong,
.admin-training-selector-list strong {
  color: #17233d;
  font-size: 13px;
}

.admin-training-selected-list span,
.admin-training-resource-grid span,
.admin-training-role-grid span,
.admin-training-flow span,
.admin-training-selector-list span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.admin-training-subsection {
  margin-top: 16px;
}

.admin-training-resource-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.admin-training-resource-grid article {
  justify-content: flex-start;
}

.admin-training-resource-grid .el-icon {
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  border-radius: 8px;
  background: #eef5ff;
  color: #3478f6;
}

.admin-training-resource-grid button {
  margin-left: auto;
  border: 0;
  background: transparent;
  color: #ef4444;
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  font-weight: 800;
}

.admin-training-paper-row {
  min-height: 48px;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 16px;
  padding: 0 14px;
  border-radius: 8px;
  background: #f8fafc;
}

.admin-training-paper-row strong {
  color: #17233d;
  font-size: 14px;
}

.admin-training-target-grid,
.admin-training-preview-grid,
.admin-training-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.admin-training-target-grid article,
.admin-training-preview-grid article,
.admin-training-stats-grid article {
  min-height: 86px;
  padding: 14px 16px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  background: #f8fafc;
}

.admin-training-target-grid span,
.admin-training-preview-grid span,
.admin-training-stats-grid span {
  color: #64748b;
  font-size: 12px;
}

.admin-training-target-grid strong,
.admin-training-preview-grid strong,
.admin-training-stats-grid strong {
  display: block;
  margin-top: 8px;
  color: #17233d;
  font-size: 15px;
  line-height: 22px;
}

.admin-training-flow article i {
  width: 26px;
  height: 26px;
  flex: 0 0 auto;
  display: inline-grid;
  place-items: center;
  border-radius: 50%;
  background: #3478f6;
  color: #ffffff;
  font-style: normal;
  font-weight: 800;
}

.admin-training-drawer-footer {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 24px;
  border-top: 1px solid #edf2f8;
  background: #ffffff;
}

.admin-training-dialog-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.admin-training-dialog-head strong {
  color: #17233d;
  font-size: 17px;
}

.admin-training-dialog-footer {
  justify-content: flex-end;
}

.admin-training-selector-filter {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 140px;
  gap: 12px;
  margin-bottom: 12px;
}

.admin-training-selector-list {
  max-height: 420px;
  display: grid;
  gap: 8px;
  overflow: auto;
}

.admin-training-selector-list article {
  justify-content: flex-start;
  cursor: pointer;
}

.admin-training-selector-list article.checked {
  border-color: #3478f6;
  background: #f4f8ff;
}

.admin-training-preview section:first-child {
  padding: 16px 18px;
  border-radius: 10px;
  background: #eef5ff;
}

.admin-training-preview h4 {
  margin: 0;
  color: #17233d;
  font-size: 20px;
}

.admin-training-preview p {
  margin: 8px 0 0;
  color: #52647b;
  white-space: pre-line;
}

.admin-training-preview section:first-child div {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.admin-training-preview section:first-child span {
  padding: 4px 10px;
  border-radius: 999px;
  background: #ffffff;
  color: #3478f6;
  font-size: 12px;
  font-weight: 800;
}

.admin-training-preview-grid,
.admin-training-preview-list {
  margin-top: 12px;
}

.admin-training-import {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.offline-import-section {
  grid-column: 1 / -1;
  display: grid;
  gap: 10px;
  padding: 18px;
  border: 1px solid #dfe7f1;
  border-radius: 8px;
}

.offline-import-section > p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.offline-import-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}

.admin-training-upload-box,
.admin-training-import-result,
.admin-training-publish-confirm {
  display: grid;
  gap: 10px;
  padding: 18px;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: #f8fafc;
}

.admin-training-upload-box .el-icon {
  color: #3478f6;
  font-size: 34px;
}

.admin-training-import-result.active {
  border-style: solid;
  border-color: #10b981;
  background: #ecfdf5;
}

.admin-training-publish-confirm strong,
.admin-training-upload-box strong,
.admin-training-import-result strong {
  color: #17233d;
  font-size: 15px;
}

.admin-training-publish-confirm p,
.admin-training-upload-box span,
.admin-training-import-result p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.admin-training-log-row {
  align-items: stretch;
  flex-direction: column;
  margin-bottom: 10px;
}

.admin-training-log-row header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.admin-training-log-row p {
  margin: 0;
  color: #334155;
  font-size: 13px;
  line-height: 20px;
}

.admin-training-log-row span,
.admin-training-log-row small {
  color: #64748b;
  font-size: 12px;
}

.admin-training-stats-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-bottom: 16px;
}

@media (max-width: 980px) {
  .admin-training-filter-row,
  .admin-training-action-row {
    flex-wrap: wrap;
  }

  .admin-training-form-drawer.el-drawer {
    width: 100% !important;
  }

  .admin-training-form-grid,
  .admin-training-resource-grid,
  .admin-training-target-grid,
  .admin-training-preview-grid,
  .admin-training-stats-grid,
  .admin-training-import {
    grid-template-columns: 1fr;
  }
}
</style>
