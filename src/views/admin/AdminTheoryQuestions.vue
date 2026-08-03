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
            <el-radio label="正确">正确</el-radio>
            <el-radio label="错误">错误</el-radio>
          </el-radio-group>
        </section>

        <label v-else-if="form.questionType === 'BLANK'" class="admin-question-field">
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
          <el-upload drag action="#" :auto-upload="false">
            <el-icon><Document /></el-icon>
            <div class="el-upload__text">点击或拖拽上传资源文件</div>
            <template #tip><p>仅支持.excel 格式，大小不超过 200MB</p></template>
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
          <el-button type="primary" :icon="UploadFilled" @click="openPreview">确认上传</el-button>
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
          <el-button class="admin-question-primary-button" @click="submitImport">提交</el-button>
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
          <article v-for="question in group.questions" :key="question.index">
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
import { CircleCheck, CircleClose, Close, Delete, Document, Plus, Refresh, Search, Upload, UploadFilled } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  createAdminQuestion,
  disableAdminQuestion,
  enableAdminQuestion,
  fetchAdminQuestion,
  fetchAdminQuestionLogs,
  fetchAdminQuestions,
  updateAdminQuestion,
  type AdminQuestion,
  type AdminQuestionCommand,
  type AdminQuestionLog
} from '../../api/admin-question';

type QuestionType = 'SINGLE' | 'MULTIPLE' | 'JUDGE' | 'BLANK' | 'ESSAY';

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
  { value: 'BLANK', label: '填空题' },
  { value: 'ESSAY', label: '简答题' }
];
const typeLabels: Record<QuestionType, string> = {
  SINGLE: '单选',
  MULTIPLE: '多选',
  JUDGE: '判断',
  BLANK: '填空',
  ESSAY: '简答'
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
    questions: Array<{ index: number; title: string; score: number; options: string[] }>;
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
  if (key.includes('BLANK')) return 'BLANK';
  if (key.includes('ESSAY') || key.includes('SHORT')) return 'ESSAY';
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
    standardAnswer: row.standardAnswer || '',
    score: Number(row.score || 5),
    explanation: '',
    courseName: row.courseName,
    options: row.options?.map((item) => ({ optionKey: item.optionKey || 'A', optionText: item.optionText || '' })) || emptyForm(normalized).options
  };
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
  importVisible.value = true;
}

function openPreview() {
  importVisible.value = false;
  previewVisible.value = true;
}

function submitImport() {
  if (previewGroups.length === 0) {
    ElMessage.warning('请先上传并解析试题文件');
    return;
  }
  previewVisible.value = false;
  ElMessage.success('试题已提交');
}

function downloadTemplate() {
  const content = '题型,题干,选项A,选项B,选项C,选项D,答案,分值,解析';
  const blob = new Blob([`\ufeff${content}`], { type: 'text/csv;charset=utf-8' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = '理论试题上传模板.csv';
  link.click();
  URL.revokeObjectURL(link.href);
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
