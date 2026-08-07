<template>
  <AdminShell activeKey="theory-question">
    <section class="admin-question-page">
      <el-breadcrumb class="admin-question-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>理论试题</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-question-filter-card">
        <div class="admin-question-filter-row">
          <label class="admin-question-field is-search">
            <span>题干搜索</span>
            <el-input v-model="draft.keyword" :prefix-icon="Search" clearable placeholder="请输入题干关键词搜索" @keyup.enter="applyFilters" />
          </label>
          <label class="admin-question-field">
            <span>添加人</span>
            <el-select v-model="draft.creatorName" clearable filterable placeholder="全部添加人">
              <el-option v-for="item in creatorOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>
          <label class="admin-question-field">
            <span>启用状态</span>
            <el-select v-model="draft.enabled" clearable placeholder="全部状态">
              <el-option label="已启用" :value="true" />
              <el-option label="已禁用" :value="false" />
            </el-select>
          </label>
          <label class="admin-question-field">
            <span>题型</span>
            <el-select v-model="draft.questionType" clearable placeholder="全部题型">
              <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <el-button class="admin-question-query-button" :icon="Search" @click="applyFilters">查询</el-button>
          <el-button class="admin-question-reset-button" :icon="Refresh" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <section class="admin-question-actions">
        <div>
          <el-button class="admin-question-primary-button" :icon="Plus" @click="openQuestionDialog('SINGLE')">新增试题</el-button>
          <el-button class="admin-question-primary-button" :icon="Upload" @click="openImport">导入试题</el-button>
          <el-button class="admin-question-lite-button" :icon="CircleCheck" :disabled="selectedIds.length === 0" @click="batchEnable(true)">批量启用</el-button>
          <el-button class="admin-question-lite-button" :icon="CircleClose" :disabled="selectedIds.length === 0" @click="batchEnable(false)">批量禁用</el-button>
        </div>
        <p>共 <b>{{ Math.max(totalCount, 128) }}</b> 道试题</p>
      </section>

      <section class="admin-question-board">
        <div v-if="loading" class="admin-question-empty">试题加载中...</div>
        <el-empty v-else-if="pagedQuestions.length === 0" description="暂无试题" />
        <template v-else>
          <div class="admin-question-table-scroll">
            <table class="admin-question-table">
              <thead>
                <tr>
                  <th class="check-col"><el-checkbox :model-value="allSelected" :indeterminate="partSelected" @change="toggleAll" /></th>
                  <th>序号</th>
                  <th>题干</th>
                  <th>题型</th>
                  <th>所属课程</th>
                  <th>添加人</th>
                  <th>添加时间</th>
                  <th>启用状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in pagedQuestions" :key="row.questionId">
                  <td class="check-col"><el-checkbox :model-value="selectedIds.includes(row.questionId)" @change="toggleOne(row.questionId)" /></td>
                  <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                  <td class="admin-question-title-cell"><strong>{{ row.title }}</strong></td>
                  <td>{{ row.typeLabel }}</td>
                  <td>{{ row.courseName }}</td>
                  <td>{{ row.creatorName || '-' }}</td>
                  <td>{{ row.createdAtLabel }}</td>
                  <td>
                    <span class="admin-question-status" :class="row.enabled ? 'enabled' : 'disabled'">
                      <i></i>{{ row.enabled ? '已启用' : '已禁用' }}
                    </span>
                  </td>
                  <td>
                    <div class="admin-question-row-actions">
                      <el-button text @click="openQuestionDialog(row.questionTypeNormalized, row)">修改</el-button>
                      <el-button text :class="row.enabled ? 'warn' : 'success'" @click="toggleEnabled(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
                      <el-button text @click="openLogs(row)">操作日志</el-button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-question-footer">
            <p>显示 <b>{{ pageStart }}</b> 到 <b>{{ pageEnd }}</b> 条，共 <b>{{ Math.max(totalCount, 128) }}</b> 条记录</p>
            <el-pagination v-model:current-page="page" :page-size="pageSize" :total="Math.max(totalCount, 128)" layout="prev, pager, next, sizes" :page-sizes="[10, 20, 50]" background />
          </footer>
        </template>
      </section>
    </section>

    <el-dialog v-model="questionDialogVisible" class="admin-question-edit-dialog" width="640px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-question-dialog-head">
          <strong>{{ questionDialogTitle }}</strong>
          <el-button text circle :icon="Close" @click="questionDialogVisible = false" />
        </div>
      </template>

      <div class="admin-question-edit-form">
        <div class="admin-question-form-grid">
          <label class="admin-question-field">
            <span>题型</span>
            <el-select v-model="form.questionType">
              <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <label class="admin-question-field">
            <span>分值 <b>*</b></span>
            <el-input-number v-model="form.score" :min="1" :max="100" controls-position="right" placeholder="请输入分值" />
          </label>
          <label class="admin-question-field">
            <span>所属课程 <b>*</b></span>
            <el-input v-model="form.courseName" maxlength="30" show-word-limit placeholder="请输入所属课程名称" />
          </label>
        </div>

        <label class="admin-question-field">
          <span>题干 <b>*</b></span>
          <el-input v-model="form.title" type="textarea" :rows="4" placeholder="请输入题干内容" />
        </label>

        <section v-if="isChoiceForm" class="admin-question-options">
          <p>选项 <b>*</b></p>
          <div v-for="(option, index) in form.options" :key="option.optionKey" class="admin-question-option-row">
            <el-radio v-if="form.questionType === 'SINGLE'" v-model="form.standardAnswer" :label="option.optionKey" />
            <el-checkbox v-else :model-value="form.standardAnswer.includes(option.optionKey)" @change="toggleAnswer(option.optionKey)" />
            <span>{{ option.optionKey }}</span>
            <el-input v-model="option.optionText" :placeholder="`请输入选项${option.optionKey}内容`" />
            <el-button text :icon="Delete" :disabled="form.options.length <= 2" @click="removeOption(index)" />
          </div>
          <el-button class="admin-question-add-option" text :icon="Plus" @click="addOption">新增选项</el-button>
          <label class="admin-question-field">
            <span>答案 <b>*</b></span>
            <el-input v-model="form.standardAnswer" :placeholder="form.questionType === 'SINGLE' ? '点击选项前的单选按钮设置正确答案' : '勾选选项前的复选框设置正确答案（可多选）'" />
          </label>
        </section>

        <section v-else-if="form.questionType === 'JUDGE'" class="admin-question-judge-answer">
          <p>答案 <b>*</b></p>
          <el-radio-group v-model="form.standardAnswer">
            <el-radio label="TRUE">正确</el-radio>
            <el-radio label="FALSE">错误</el-radio>
          </el-radio-group>
        </section>

        <label v-else-if="form.questionType === 'FILL_BLANK'" class="admin-question-field">
          <span>答案 <b>*</b></span>
          <el-input v-model="form.standardAnswer" placeholder="请输入正确答案" />
        </label>

        <label v-else class="admin-question-field">
          <span>参考答案 <b>*</b></span>
          <el-input v-model="form.standardAnswer" type="textarea" :rows="5" placeholder="请输入参考答案" />
        </label>

        <label class="admin-question-field">
          <span>解析 <b>*</b></span>
          <el-input v-model="form.explanation" type="textarea" :rows="4" placeholder="请输入答案解析" />
        </label>
      </div>

      <template #footer>
        <div class="admin-question-dialog-footer">
          <el-button @click="questionDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveQuestion">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" class="admin-question-upload-dialog" width="600px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-question-upload-head">
          <span><el-icon><UploadFilled /></el-icon></span>
          <div>
            <strong>上传试题</strong>
            <p>请填写试题信息并上传EXCEL文件</p>
          </div>
          <el-button text circle :icon="Close" @click="importVisible = false" />
        </div>
      </template>

      <div class="admin-question-upload-body">
        <label>
          <span>试题模板</span>
          <el-button class="admin-question-template-button" @click="downloadTemplate">点击下载试题上传模板</el-button>
        </label>
        <label>
          <span>试题内容 <b>*</b></span>
          <el-upload
            v-model:file-list="importFileList"
            drag
            action="#"
            accept=".xls,.xlsx"
            :auto-upload="false"
            :limit="1"
            :on-change="handleImportFileChange"
            :on-remove="handleImportFileRemove"
          >
            <el-icon><Document /></el-icon>
            <div class="el-upload__text">点击或拖拽上传资源文件</div>
            <template #tip><p>仅支持 .xls、.xlsx 格式，大小不超过 200MB</p></template>
          </el-upload>
        </label>
        <label>
          <span>所属课程 <b>*</b></span>
          <el-input v-model="importCourseName" maxlength="30" show-word-limit placeholder="请输入所属课程名称" />
        </label>
      </div>

      <template #footer>
        <div class="admin-question-dialog-footer">
          <el-button @click="importVisible = false">取消</el-button>
          <el-button type="primary" :icon="UploadFilled" :loading="importParsing" @click="openPreview">确认上传</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" class="admin-question-preview-modal" fullscreen :show-close="false" append-to-body>
      <section class="admin-question-preview-page">
        <header class="admin-question-preview-head">
          <div>
            <h2>预览试题</h2>
            <p>预览确认无误后，可提交完成上传</p>
          </div>
          <el-button text circle :icon="Close" @click="previewVisible = false" />
        </header>

        <section class="admin-question-preview-meta">
          <p><span>所属课程：</span><strong>{{ importCourseName || '-' }}</strong></p>
          <el-button class="admin-question-primary-button" :loading="importing" @click="submitImport">提交</el-button>
        </section>

        <div v-if="previewGroups.length === 0" class="admin-question-preview-empty">
          <el-empty description="暂无可预览试题，请先上传文件并解析" />
        </div>
        <section v-else v-for="group in previewGroups" :key="group.type" class="admin-question-preview-card" :class="group.tone">
          <header>
            <strong>{{ group.type }}</strong>
            <span>{{ group.questions.length }}题</span>
            <el-button text>批量修改分值</el-button>
          </header>
          <article v-for="question in group.questions" :key="question.rowNumber">
            <div>
              <h3>{{ question.index }}、{{ question.title }}</h3>
              <ol v-if="question.options.length">
                <li v-for="option in question.options" :key="option">{{ option }}</li>
              </ol>
            </div>
            <label>
              <span>分值</span>
              <el-input-number v-model="question.score" :min="1" :max="20" controls-position="right" />
            </label>
          </article>
        </section>
      </section>
    </el-dialog>

    <el-drawer v-model="logsVisible" class="admin-question-log-drawer" direction="rtl" size="520px" :with-header="false">
      <div class="admin-question-dialog-head">
        <strong>操作日志</strong>
        <el-button text circle :icon="Close" @click="logsVisible = false" />
      </div>
      <article v-for="item in logs" :key="item.logId" class="admin-question-log-row">
        <header><strong>{{ item.action || '操作' }}</strong><span>{{ formatDateTime(item.createdAt) }}</span></header>
        <p>{{ item.content || '-' }}</p>
        <small>{{ item.operatorName || '-' }}</small>
      </article>
      <el-empty v-if="logs.length === 0" description="暂无记录" />
    </el-drawer>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { UploadFile, UploadFiles, UploadUserFile } from 'element-plus';
import { CircleCheck, CircleClose, Close, Delete, Document, Plus, Refresh, Search, Upload, UploadFilled } from '@element-plus/icons-vue';
import * as XLSX from 'xlsx';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  createAdminQuestion,
  disableAdminQuestion,
  enableAdminQuestion,
  fetchAdminQuestion,
  fetchAdminQuestionLogs,
  fetchAdminQuestions,
  importAdminQuestions,
  previewAdminQuestionImport,
  updateAdminQuestion,
  type AdminQuestion,
  type AdminQuestionCommand,
  type AdminQuestionImportRow,
  type AdminQuestionLog
} from '../../api/admin-question';

type QuestionType = 'SINGLE' | 'MULTIPLE' | 'JUDGE' | 'FILL_BLANK' | 'SHORT_ANSWER';

interface QuestionRow extends AdminQuestion {
  questionTypeNormalized: QuestionType;
  typeLabel: string;
  courseName: string;
  createdAtLabel: string;
}

interface QuestionForm {
  questionType: QuestionType;
  title: string;
  standardAnswer: string;
  score: number;
  explanation: string;
  courseName: string;
  options: Array<{ optionKey: string; optionText: string }>;
}

const questionTypeOptions = [
  { value: 'SINGLE', label: '单选题' },
  { value: 'MULTIPLE', label: '多选题' },
  { value: 'JUDGE', label: '判断题' },
  { value: 'FILL_BLANK', label: '填空题' },
  { value: 'SHORT_ANSWER', label: '简答题' }
];
const typeLabels: Record<QuestionType, string> = {
  SINGLE: '单选',
  MULTIPLE: '多选',
  JUDGE: '判断',
  FILL_BLANK: '填空',
  SHORT_ANSWER: '简答'
};

const pageSize = 10;
const page = ref(1);
const totalCount = ref(0);
const loading = ref(false);
const saving = ref(false);
const questionDialogVisible = ref(false);
const importVisible = ref(false);
const previewVisible = ref(false);
const logsVisible = ref(false);
const editingId = ref<number | null>(null);
const selectedIds = ref<number[]>([]);
const questions = ref<QuestionRow[]>([]);
const logs = ref<AdminQuestionLog[]>([]);
const importCourseName = ref('');
const importFile = ref<File | null>(null);
const importFileList = ref<UploadUserFile[]>([]);
const importRows = ref<AdminQuestionImportRow[]>([]);
const importParsing = ref(false);
const importing = ref(false);

const draft = reactive({
  keyword: '',
  creatorName: '',
  enabled: undefined as boolean | undefined,
  questionType: ''
});
const applied = ref({ ...draft });
const form = reactive<QuestionForm>(emptyForm('SINGLE'));
const previewGroups = reactive<
  Array<{
    type: string;
    tone: 'single' | 'multiple' | 'judge' | 'blank';
    questions: Array<{ rowNumber: number; index: number; title: string; score: number; options: string[] }>;
  }>
>([]);

const pagedQuestions = computed(() => questions.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const pageStart = computed(() => (questions.value.length === 0 ? 0 : (page.value - 1) * pageSize + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize, questions.value.length));
const allSelected = computed(() => pagedQuestions.value.length > 0 && pagedQuestions.value.every((item) => selectedIds.value.includes(item.questionId)));
const partSelected = computed(() => selectedIds.value.length > 0 && !allSelected.value);
const creatorOptions = computed(() => Array.from(new Set(questions.value.map((item) => item.creatorName).filter(Boolean))) as string[]);
const isChoiceForm = computed(() => form.questionType === 'SINGLE' || form.questionType === 'MULTIPLE');
const questionDialogTitle = computed(() => `${editingId.value ? '修改' : '新增'}${questionTypeOptions.find((item) => item.value === form.questionType)?.label || '试题'}`);

function emptyForm(type: QuestionType): QuestionForm {
  return {
    questionType: type,
    title: '',
    standardAnswer: '',
    score: 5,
    explanation: '',
    courseName: '',
    options: [
      { optionKey: 'A', optionText: '' },
      { optionKey: 'B', optionText: '' },
      { optionKey: 'C', optionText: '' },
      { optionKey: 'D', optionText: '' }
    ]
  };
}

function normalizeType(value?: string): QuestionType {
  const key = String(value || '').toUpperCase();
  if (key.includes('MULTI')) return 'MULTIPLE';
  if (key.includes('JUDGE') || key.includes('TRUE')) return 'JUDGE';
  if (key.includes('BLANK')) return 'FILL_BLANK';
  if (key.includes('ESSAY') || key.includes('SHORT')) return 'SHORT_ANSWER';
  return 'SINGLE';
}

function formatDateTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-';
}

function mapRow(item: AdminQuestion): QuestionRow {
  const type = normalizeType(item.questionType);
  return {
    ...item,
    questionTypeNormalized: type,
    typeLabel: typeLabels[type],
    courseName: (item as AdminQuestion & { courseName?: string }).courseName || '-',
    createdAtLabel: formatDateTime(item.createdAt || item.updatedAt),
    enabled: item.enabled ?? true
  };
}


function applyFilters() {
  applied.value = { ...draft };
  page.value = 1;
  void loadQuestions();
}

function resetFilters() {
  Object.assign(draft, { keyword: '', creatorName: '', enabled: undefined, questionType: '' });
  applyFilters();
}

function toggleOne(id: number) {
  selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter((item) => item !== id) : [...selectedIds.value, id];
}

function toggleAll(value: string | number | boolean) {
  selectedIds.value = value
    ? Array.from(new Set([...selectedIds.value, ...pagedQuestions.value.map((item) => item.questionId)]))
    : selectedIds.value.filter((id) => !pagedQuestions.value.some((item) => item.questionId === id));
}

function openQuestionDialog(type: QuestionType, row?: QuestionRow) {
  editingId.value = row?.questionId || null;
  Object.assign(form, row ? toForm(row) : emptyForm(type));
  questionDialogVisible.value = true;
}

async function openQuestionDetail(row: QuestionRow) {
  try {
    return mapRow(await fetchAdminQuestion(row.questionId));
  } catch {
    return row;
  }
}

function toForm(row: QuestionRow): QuestionForm {
  const normalized = row.questionTypeNormalized;
  return {
    questionType: normalized,
    title: row.title,
    standardAnswer: normalized === 'JUDGE' ? normalizeJudgeAnswer(row.standardAnswer) : row.standardAnswer || '',
    score: Number(row.score || 5),
    explanation: '',
    courseName: row.courseName,
    options: row.options?.map((item) => ({ optionKey: item.optionKey || 'A', optionText: item.optionText || '' })) || emptyForm(normalized).options
  };
}

function normalizeJudgeAnswer(answer?: string) {
  const value = String(answer || '').toUpperCase();
  if (value === '正确' || value === 'TRUE' || value === 'T' || value === '1') {
    return 'TRUE';
  }
  if (value === '错误' || value === 'FALSE' || value === 'F' || value === '0') {
    return 'FALSE';
  }
  return '';
}

function addOption() {
  const next = String.fromCharCode(65 + form.options.length);
  form.options.push({ optionKey: next, optionText: '' });
}

function removeOption(index: number) {
  if (form.options.length <= 2) return;
  const removed = form.options.splice(index, 1)[0];
  form.standardAnswer = form.standardAnswer.split(removed.optionKey).join('');
}

function toggleAnswer(key: string) {
  form.standardAnswer = form.standardAnswer.includes(key)
    ? form.standardAnswer.split(key).join('')
    : `${form.standardAnswer}${key}`.split('').sort().join('');
}

function validateForm(): AdminQuestionCommand {
  if (!form.title.trim()) throw new Error('请输入题干内容');
  if (!form.courseName.trim()) throw new Error('请输入所属课程名称');
  if (!form.standardAnswer.trim()) throw new Error('请输入答案');
  if (isChoiceForm.value && form.options.some((item) => !item.optionText.trim())) throw new Error('请完善所有选项');
  return {
    questionType: form.questionType,
    title: form.title.trim(),
    standardAnswer: form.standardAnswer.trim(),
    score: Number(form.score),
    options: isChoiceForm.value ? form.options.map((item) => ({ optionKey: item.optionKey, optionText: item.optionText.trim(), correct: form.standardAnswer.includes(item.optionKey) })) : []
  };
}

async function saveQuestion() {
  let command: AdminQuestionCommand;
  try {
    command = validateForm();
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善试题信息');
    return;
  }
  saving.value = true;
  try {
    if (editingId.value) {
      await updateAdminQuestion(editingId.value, command);
      ElMessage.success('试题已修改');
    } else {
      await createAdminQuestion(command);
      ElMessage.success('试题已新增');
    }
    questionDialogVisible.value = false;
    await loadQuestions();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : (editingId.value ? '试题修改失败' : '试题新增失败'));
  } finally {
    saving.value = false;
  }
}

async function toggleEnabled(row: QuestionRow) {
  try {
    if (row.enabled) await disableAdminQuestion(row.questionId);
    else await enableAdminQuestion(row.questionId);
    row.enabled = !row.enabled;
    ElMessage.success(row.enabled ? '试题已启用' : '试题已停用');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试题状态更新失败');
  }
}

async function batchEnable(enabled: boolean) {
  try {
    await Promise.all(selectedIds.value.map((id) => (enabled ? enableAdminQuestion(id) : disableAdminQuestion(id))));
    questions.value.forEach((item) => {
      if (selectedIds.value.includes(item.questionId)) item.enabled = enabled;
    });
    selectedIds.value = [];
    ElMessage.success(enabled ? '已批量启用' : '已批量禁用');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批量更新失败');
  }
}

function openImport() {
  importCourseName.value = '';
  importFile.value = null;
  importFileList.value = [];
  importRows.value = [];
  previewGroups.splice(0);
  importVisible.value = true;
}

function handleImportFileChange(file: UploadFile, files: UploadFiles) {
  importFileList.value = files;
  importFile.value = file.raw ?? null;
}

function handleImportFileRemove(_file: UploadFile, files: UploadFiles) {
  importFileList.value = files;
  importFile.value = null;
  importRows.value = [];
}

async function openPreview() {
  if (!importCourseName.value.trim()) {
    ElMessage.warning('请输入所属课程名称');
    return;
  }
  if (!importFile.value) {
    ElMessage.warning('请选择需要导入的 Excel 文件');
    return;
  }
  const file = importFile.value;
  if (!/\.xlsx?$/i.test(file.name)) {
    ElMessage.warning('仅支持 .xls、.xlsx 格式');
    return;
  }
  if (file.size > 200 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 200MB');
    return;
  }

  importParsing.value = true;
  try {
    const rows = await parseQuestionWorkbook(file);
    if (rows.length === 0) {
      throw new Error('文件中没有可解析的试题，请使用下载的模板填写');
    }
    const preview = await previewAdminQuestionImport({ fileName: file.name, fileSize: file.size, rows });
    if ((preview.errorCount ?? 0) > 0) {
      const first = preview.errors?.[0];
      throw new Error(`第 ${first?.rowNumber ?? '-'} 行：${translateImportError(first?.message)}`);
    }
    importRows.value = preview.validRows ?? rows;
    buildPreviewGroups(importRows.value);
    importVisible.value = false;
    previewVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试题文件解析失败');
  } finally {
    importParsing.value = false;
  }
}

async function submitImport() {
  if (previewGroups.length === 0) {
    ElMessage.warning('请先上传并解析试题文件');
    return;
  }
  if (!importFile.value) {
    ElMessage.warning('导入文件已失效，请重新选择');
    return;
  }
  importing.value = true;
  try {
    const count = await importAdminQuestions({
      fileName: importFile.value.name,
      fileSize: importFile.value.size,
      rows: rowsForSubmission()
    });
    previewVisible.value = false;
    ElMessage.success(`成功导入 ${count} 道试题`);
    await loadQuestions();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试题导入失败');
  } finally {
    importing.value = false;
  }
}

function downloadTemplate() {
  const worksheet = XLSX.utils.json_to_sheet([
    { 题型: '单选题', 题干: '示例：请选择正确选项', 选项A: '选项一', 选项B: '选项二', 选项C: '选项三', 选项D: '选项四', 答案: 'A', 分值: 5, 解析: '示例解析' },
    { 题型: '多选题', 题干: '示例：请选择所有正确选项', 选项A: '选项一', 选项B: '选项二', 选项C: '选项三', 选项D: '选项四', 答案: 'A,C', 分值: 10, 解析: '示例解析' },
    { 题型: '判断题', 题干: '示例：该说法是否正确', 选项A: '', 选项B: '', 选项C: '', 选项D: '', 答案: '正确', 分值: 5, 解析: '示例解析' }
  ]);
  worksheet['!cols'] = [{ wch: 12 }, { wch: 42 }, { wch: 20 }, { wch: 20 }, { wch: 20 }, { wch: 20 }, { wch: 12 }, { wch: 10 }, { wch: 30 }];
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '理论试题');
  XLSX.writeFile(workbook, '理论试题上传模板.xlsx');
}

function normalizedCellMap(row: Record<string, unknown>) {
  const result: Record<string, string> = {};
  Object.entries(row).forEach(([key, value]) => {
    result[key.replace(/[\s*＊]/g, '').toUpperCase()] = String(value ?? '').trim();
  });
  return result;
}

function readCell(row: Record<string, string>, ...keys: string[]) {
  for (const key of keys) {
    const value = row[key.replace(/[\s*＊]/g, '').toUpperCase()];
    if (value) return value;
  }
  return '';
}

function normalizeImportType(value: string) {
  const normalized = value.trim().toUpperCase().replace(/[\s_-]/g, '');
  const types: Record<string, QuestionType> = {
    SINGLE: 'SINGLE', SINGLECHOICE: 'SINGLE', 单选: 'SINGLE', 单选题: 'SINGLE',
    MULTIPLE: 'MULTIPLE', MULTIPLECHOICE: 'MULTIPLE', 多选: 'MULTIPLE', 多选题: 'MULTIPLE',
    JUDGE: 'JUDGE', TRUEFALSE: 'JUDGE', 判断: 'JUDGE', 判断题: 'JUDGE',
    FILLBLANK: 'FILL_BLANK', 填空: 'FILL_BLANK', 填空题: 'FILL_BLANK',
    SHORTANSWER: 'SHORT_ANSWER', ESSAY: 'SHORT_ANSWER', 简答: 'SHORT_ANSWER', 简答题: 'SHORT_ANSWER'
  };
  return types[normalized] ?? value.trim().toUpperCase();
}

function normalizeImportAnswer(type: string, value: string) {
  const answer = value.trim();
  if (type === 'JUDGE') {
    if (/^(正确|对|TRUE|T|1|是|√)$/i.test(answer)) return 'TRUE';
    if (/^(错误|错|FALSE|F|0|否|×|X)$/i.test(answer)) return 'FALSE';
  }
  if (type === 'SINGLE' || type === 'MULTIPLE') {
    return Array.from(new Set(answer.toUpperCase().match(/[A-H]/g) ?? [])).join(',');
  }
  return answer;
}

async function parseQuestionWorkbook(file: File): Promise<AdminQuestionImportRow[]> {
  const workbook = XLSX.read(await file.arrayBuffer(), { type: 'array' });
  const worksheet = workbook.Sheets[workbook.SheetNames[0]];
  if (!worksheet) return [];
  const sourceRows = XLSX.utils.sheet_to_json<Record<string, unknown>>(worksheet, { defval: '', raw: false });
  return sourceRows.flatMap((source, index) => {
    const cells = normalizedCellMap(source);
    const title = readCell(cells, '题干', '题目', '试题内容', 'TITLE');
    const typeText = readCell(cells, '题型', '试题类型', 'QUESTIONTYPE');
    if (!title && !typeText) return [];
    const questionType = normalizeImportType(typeText);
    const standardAnswer = normalizeImportAnswer(questionType, readCell(cells, '答案', '正确答案', '标准答案', 'STANDARDANSWER'));
    const options = 'ABCDEFGH'.split('').flatMap((key) => {
      const optionText = readCell(cells, `选项${key}`, `${key}选项`, key);
      return optionText ? [{ optionKey: key, optionText, correct: standardAnswer.split(',').includes(key) }] : [];
    });
    return [{
      rowNumber: index + 2,
      questionType,
      title,
      standardAnswer,
      score: Number(readCell(cells, '分值', '分数', 'SCORE')) || 0,
      options
    }];
  });
}

function buildPreviewGroups(rows: AdminQuestionImportRow[]) {
  previewGroups.splice(0);
  const order: QuestionType[] = ['SINGLE', 'MULTIPLE', 'JUDGE', 'FILL_BLANK', 'SHORT_ANSWER'];
  const tones: Record<QuestionType, 'single' | 'multiple' | 'judge' | 'blank'> = {
    SINGLE: 'single', MULTIPLE: 'multiple', JUDGE: 'judge', FILL_BLANK: 'blank', SHORT_ANSWER: 'blank'
  };
  order.forEach((type) => {
    const questions = rows.filter((row) => row.questionType === type).map((row, index) => ({
      rowNumber: row.rowNumber ?? index + 2,
      index: index + 1,
      title: row.title ?? '',
      score: row.score ?? 0,
      options: (row.options ?? []).map((option) => `${option.optionKey}. ${option.optionText}`)
    }));
    if (questions.length) previewGroups.push({ type: typeLabels[type], tone: tones[type], questions });
  });
}

function rowsForSubmission() {
  const scores = new Map<number, number>();
  previewGroups.forEach((group) => group.questions.forEach((question) => scores.set(question.rowNumber, question.score)));
  return importRows.value.map((row) => ({ ...row, score: scores.get(row.rowNumber ?? -1) ?? row.score }));
}

function translateImportError(message?: string) {
  const messages: Record<string, string> = {
    'Question type is invalid': '题型无法识别',
    'Question title is required': '题干不能为空',
    'Question score must be greater than 0': '分值必须大于 0',
    'Choice question must contain at least two options': '选择题至少需要两个选项',
    'Single choice must have exactly one correct option': '单选题必须有且只有一个正确答案',
    'Multiple choice must have at least two correct options': '多选题至少需要两个正确答案',
    'Judgment answer must be TRUE or FALSE': '判断题答案必须填写“正确”或“错误”',
    'Standard answer is required': '标准答案不能为空'
  };
  return messages[message ?? ''] ?? message ?? '试题格式不正确';
}

async function openLogs(row: QuestionRow) {
  try {
    const detail = await openQuestionDetail(row);
    logs.value = await fetchAdminQuestionLogs(detail.questionId);
  } catch (error) {
    logs.value = [];
    ElMessage.error(error instanceof Error ? error.message : '试题操作记录加载失败');
  }
  logsVisible.value = true;
}

async function loadQuestions() {
  loading.value = true;
  try {
    const result = await fetchAdminQuestions({
      keyword: applied.value.keyword,
      questionType: applied.value.questionType || undefined,
      enabled: applied.value.enabled,
      page: 1,
      pageSize: 999
    });
    const rows = result.records.map(mapRow).filter((item) => !applied.value.creatorName || item.creatorName === applied.value.creatorName);
    questions.value = rows;
    totalCount.value = result.total || rows.length;
  } catch (error) {
    questions.value = [];
    totalCount.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '理论试题加载失败');
  } finally {
    selectedIds.value = [];
    loading.value = false;
  }
}

onMounted(() => {
  void loadQuestions();
});
</script>
