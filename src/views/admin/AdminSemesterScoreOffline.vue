<template>
  <AdminShell activeKey="semester-score">
    <section class="admin-semester-score-offline-page">
      <el-breadcrumb class="admin-semester-score-breadcrumb" separator="/">
        <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
        <el-breadcrumb-item>
          <button type="button" @click="backToList">综合成绩</button>
        </el-breadcrumb-item>
        <el-breadcrumb-item>线下成绩管理</el-breadcrumb-item>
      </el-breadcrumb>

      <el-button class="admin-semester-score-upload" :loading="importing" @click="openImportDialog">
        <el-icon><UploadFilled /></el-icon>
        上传线下考试成绩
      </el-button>

      <section class="admin-semester-score-offline-board">
        <table class="admin-semester-score-offline-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>考试名称</th>
              <th>考试起止时间</th>
              <th>所属学年学期</th>
              <th>人数</th>
              <th>上传人</th>
              <th>上传时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-if="visibleExams.length">
            <tr v-for="(item, index) in visibleExams" :key="item.id">
              <td>{{ (page - 1) * pageSize + index + 1 }}</td>
              <td><strong>{{ item.name }}</strong></td>
              <td>{{ item.timeRange }}</td>
              <td>{{ item.term }}</td>
              <td>{{ item.count }}</td>
              <td>{{ item.uploader }}</td>
              <td>{{ item.uploadedAt }}</td>
              <td>
                <el-button class="admin-semester-score-edit-link" @click="openEditDialog(item)">
                  编辑成绩
                </el-button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!visibleExams.length" class="admin-semester-score-offline-empty">
          <el-empty description="暂无线下成绩记录，请先上传考试成绩" />
        </div>
      </section>

      <footer class="admin-semester-score-offline-footer">
        <p>共 <b>{{ totalCount }}</b> 条记录</p>
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="totalCount"
          layout="prev, pager, next"
          background
        />
      </footer>
    </section>

    <el-dialog
      v-model="importVisible"
      class="admin-semester-score-import-dialog"
      width="560px"
      :show-close="false"
      append-to-body
    >
      <template #header>
        <div class="admin-semester-score-dialog-head">
          <strong>导入线下考试成绩</strong>
          <button type="button" aria-label="关闭" @click="closeImportDialog">
            <el-icon><Close /></el-icon>
          </button>
        </div>
      </template>

      <section class="admin-semester-score-import-form">
        <label>
          <span>考试名称 <b>*</b></span>
          <el-input v-model="importForm.name" placeholder="请输入考试名称" maxlength="20" />
          <em>最多输入20个字</em>
        </label>
        <label>
          <span>考试起止时间 <b>*</b></span>
          <div class="admin-semester-score-date-row">
            <el-date-picker
              v-model="importForm.startTime"
              type="datetime"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              placeholder="开始时间"
              :editable="false"
              clearable
            />
            <i>至</i>
            <el-date-picker
              v-model="importForm.endTime"
              type="datetime"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              placeholder="结束时间"
              :editable="false"
              clearable
            />
          </div>
        </label>
        <label>
          <span>所属学年学期 <b>*</b></span>
          <el-select v-model="importForm.semesterId" placeholder="请选择学年学期">
            <el-option
              v-for="item in termOptions"
              :key="item.semesterId"
              :label="item.label"
              :value="item.semesterId"
            />
          </el-select>
        </label>

        <button type="button" class="admin-semester-score-upload-box" @click="fileInput?.click()">
          <input
            ref="fileInput"
            type="file"
            accept=".xlsx,.xls,.csv"
            hidden
            @change="handleFileChange"
          />
          <el-icon><UploadFilled /></el-icon>
          <strong>{{ selectedFileName || '点击上传或拖拽文件到此处' }}</strong>
          <span>仅支持 .xlsx 格式，文件大小不超过10MB</span>
        </button>

        <div v-if="parsedRows.length" class="admin-semester-score-import-preview">
          已读取 <b>{{ parsedRows.length }}</b> 条成绩记录
          <span v-if="previewResult">，校验通过 {{ previewResult.validCount }} 条，错误 {{ previewResult.errorCount }} 条</span>
        </div>

        <div class="admin-semester-score-template-row">
          <button type="button" @click="downloadTemplate">下载导入模板</button>
          <span>请按模板格式填写成绩数据后上传</span>
        </div>
      </section>

      <template #footer>
        <div class="admin-semester-score-dialog-footer">
          <el-button @click="closeImportDialog">取消</el-button>
          <el-button type="primary" :loading="importing" @click="confirmImport">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editVisible"
      class="admin-semester-score-edit-dialog"
      width="724px"
      :show-close="false"
      append-to-body
    >
      <template #header>
        <div class="admin-semester-score-dialog-head">
          <strong>{{ editingExam?.name || '编辑成绩' }}-编辑成绩</strong>
          <button type="button" aria-label="关闭" @click="editVisible = false">
            <el-icon><Close /></el-icon>
          </button>
        </div>
      </template>

      <section class="admin-semester-score-edit-body">
        <div class="admin-semester-score-tip">
          <el-icon><InfoFilled /></el-icon>
          在成绩文本框中直接修改分数，修改完成后点击“确定”保存。
        </div>
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>学号</th>
              <th>姓名</th>
              <th>成绩</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in editablePageRows" :key="row.studentNo">
              <td>{{ (editPage - 1) * editPageSize + index + 1 }}</td>
              <td>{{ row.studentNo }}</td>
              <td><strong>{{ row.studentName }}</strong></td>
              <td><el-input v-model="row.score" /></td>
            </tr>
          </tbody>
        </table>
        <footer>
          <p>共 <b>{{ editableScores.length }}</b> 条记录</p>
          <el-pagination
            v-model:current-page="editPage"
            :page-size="editPageSize"
            :total="editableScores.length"
            layout="prev, pager, next"
            background
          />
        </footer>
      </section>

      <template #footer>
        <div class="admin-semester-score-dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingEdit" @click="saveEditScores">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, InfoFilled, UploadFilled } from '@element-plus/icons-vue';
import * as XLSX from 'xlsx';
import { useRouter } from 'vue-router';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  importAdminSemesterScores,
  previewAdminSemesterScoreImport,
  type AdminSemesterScoreImportPreview,
  type AdminSemesterScoreImportRow
} from '../../api/admin-semester-score';
import {
  fetchAdminAcademicYears,
  fetchAdminScoreWeights,
  type AdminAcademicYear,
  type AdminScoreWeight
} from '../../api/admin-settings';

interface TermOption {
  semesterId: number;
  label: string;
  current: boolean;
}

interface LocalImportRow {
  rowNo: number;
  studentNo: string;
  studentName: string;
  semesterId: number;
  coursewareLearningScore: number;
  trainingPracticeScore: number;
  courseAssignmentScore: number;
  examScore: number;
  coursewareWeight: number;
  trainingPracticeWeight: number;
  assignmentWeight: number;
  examWeight: number;
}

interface OfflineExam {
  id: string;
  semesterId: number;
  name: string;
  timeRange: string;
  term: string;
  count: number;
  uploader: string;
  uploadedAt: string;
  rows: LocalImportRow[];
}

interface EditableScore {
  studentNo: string;
  studentName: string;
  score: string;
}

const router = useRouter();
const fileInput = ref<HTMLInputElement | null>(null);
const termOptions = ref<TermOption[]>([]);
const scoreWeights = ref<AdminScoreWeight[]>([]);
const offlineExams = ref<OfflineExam[]>([]);
const page = ref(1);
const pageSize = 6;
const editPage = ref(1);
const editPageSize = 8;
const importing = ref(false);
const savingEdit = ref(false);
const importVisible = ref(false);
const editVisible = ref(false);
const selectedFileName = ref('');
const parsedRows = ref<LocalImportRow[]>([]);
const previewResult = ref<AdminSemesterScoreImportPreview | null>(null);
const editingExam = ref<OfflineExam | null>(null);
const editableScores = ref<EditableScore[]>([]);
const importForm = reactive({
  name: '',
  startTime: '',
  endTime: '',
  semesterId: null as number | null
});

const totalCount = computed(() => offlineExams.value.length);
const visibleExams = computed(() => offlineExams.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const editablePageRows = computed(() => editableScores.value.slice((editPage.value - 1) * editPageSize, editPage.value * editPageSize));

function backToList() {
  router.push('/admin/semester-score');
}

async function loadOptions() {
  try {
    const [years, weights] = await Promise.all([fetchAdminAcademicYears(), fetchAdminScoreWeights()]);
    scoreWeights.value = weights;
    termOptions.value = flattenTerms(years);
    const current = termOptions.value.find((item) => item.current);
    importForm.semesterId = current?.semesterId ?? termOptions.value[0]?.semesterId ?? null;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '学年学期加载失败');
  }
}

function flattenTerms(years: AdminAcademicYear[]): TermOption[] {
  return years.flatMap((year) =>
    year.semesters.map((semester) => ({
      semesterId: semester.semesterId,
      label: `${year.yearName} ${semester.semesterName}`,
      current: semester.current
    }))
  );
}

function openImportDialog() {
  importVisible.value = true;
  if (!importForm.semesterId) {
    importForm.semesterId = termOptions.value[0]?.semesterId ?? null;
  }
}

function closeImportDialog() {
  importVisible.value = false;
  selectedFileName.value = '';
  parsedRows.value = [];
  previewResult.value = null;
  importForm.name = '';
  importForm.startTime = '';
  importForm.endTime = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
}

async function handleFileChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) {
    return;
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('成绩文件大小不能超过10MB');
    return;
  }

  selectedFileName.value = file.name;
  try {
    parsedRows.value = await parseSpreadsheet(file);
    previewResult.value = null;
    if (!parsedRows.value.length) {
      ElMessage.warning('文件中没有可导入的成绩记录');
    }
  } catch (error) {
    parsedRows.value = [];
    ElMessage.error(error instanceof Error ? error.message : '成绩文件读取失败');
  }
}

async function parseSpreadsheet(file: File): Promise<LocalImportRow[]> {
  const workbook = XLSX.read(await file.arrayBuffer(), { type: 'array', cellDates: true });
  const sheetName = workbook.SheetNames[0];
  if (!sheetName) {
    return [];
  }
  const sheet = workbook.Sheets[sheetName];
  const matrix = XLSX.utils.sheet_to_json<unknown[]>(sheet, { header: 1, defval: '', raw: false });
  const headerIndex = matrix.findIndex((row) => {
    const headers = row.map((cell) => normalizeHeader(String(cell ?? '')));
    return headers.includes('姓名')
      && headers.includes('学号')
      && headers.some((header) => ['成绩', '考试', '考试成绩', 'examscore'].includes(header));
  });
  if (headerIndex < 0) {
    throw new Error('未找到“姓名、学号、成绩”表头，请使用线下考试成绩导入模板');
  }
  const headers = matrix[headerIndex].map((cell) => String(cell ?? ''));
  const rows = matrix.slice(headerIndex + 1).map((values) => Object.fromEntries(headers.map((header, index) => [header, values[index] ?? ''])));
  const weights = selectedWeights(importForm.semesterId);

  return rows
    .map((row, index) => {
      const studentNo = textValue(readCell(row, ['学号', 'studentNo', 'student number']));
      const rawStudentName = textValue(readCell(row, ['姓名', '学员姓名', 'studentName']));
      if (!studentNo && !rawStudentName) {
        return null;
      }
      const studentName = rawStudentName || '-';
      return {
        rowNo: headerIndex + index + 2,
        studentNo,
        studentName,
        semesterId: importForm.semesterId || 0,
        coursewareLearningScore: numberValue(readCell(row, ['课件学习', '课件学习成绩', 'coursewareLearningScore'])),
        trainingPracticeScore: numberValue(readCell(row, ['实训练习', '实训练习成绩', 'trainingPracticeScore'])),
        courseAssignmentScore: numberValue(readCell(row, ['课程作业', '课程作业成绩', 'courseAssignmentScore'])),
        examScore: numberValue(readCell(row, ['成绩', '考试', '考试成绩', 'examScore'])),
        coursewareWeight: weights.coursewareWeight,
        trainingPracticeWeight: weights.trainingPracticeWeight,
        assignmentWeight: weights.assignmentWeight,
        examWeight: weights.examWeight
      };
    })
    .filter((row): row is LocalImportRow => row !== null);
}

function readCell(row: Record<string, unknown>, aliases: string[]) {
  const aliasSet = new Set(aliases.map(normalizeHeader));
  const entry = Object.entries(row).find(([key]) => aliasSet.has(normalizeHeader(key)));
  return entry?.[1];
}

function normalizeHeader(value: string) {
  return value.replace(/[\s*＊]/g, '').replace(/_/g, '').toLowerCase();
}

function textValue(value: unknown) {
  return String(value ?? '').trim();
}

function numberValue(value: unknown) {
  const parsed = Number(String(value ?? '').replace(/[%\s]/g, ''));
  return Number.isFinite(parsed) ? parsed : 0;
}

function selectedWeights(semesterId: number | null) {
  const rows = scoreWeights.value.filter((item) => item.semesterId === semesterId);
  const row = [...rows, ...scoreWeights.value].sort((left, right) => Number(right.weightId || 0) - Number(left.weightId || 0))[0];
  return {
    coursewareWeight: row?.coursewareWeight ?? 30,
    trainingPracticeWeight: row?.trainingPracticeWeight ?? 30,
    assignmentWeight: row?.assignmentWeight ?? 30,
    examWeight: row?.examWeight ?? 10
  };
}

async function confirmImport() {
  if (!importForm.name.trim() || !importForm.startTime.trim() || !importForm.endTime.trim() || !importForm.semesterId) {
    ElMessage.warning('请完整填写考试名称、考试时间和所属学年学期');
    return;
  }
  if (importForm.endTime < importForm.startTime) {
    ElMessage.warning('考试结束时间不能早于开始时间');
    return;
  }
  if (!parsedRows.value.length) {
    ElMessage.warning('请先上传成绩文件');
    return;
  }

  importing.value = true;
  try {
    const preview = await previewAdminSemesterScoreImport(toApiRows(parsedRows.value));
    previewResult.value = preview;
    if (preview.errorCount > 0) {
      ElMessage.warning(`成绩文件存在 ${preview.errorCount} 条错误，请修正后再导入`);
      return;
    }

    await importAdminSemesterScores(toApiRows(parsedRows.value));
    offlineExams.value.unshift({
      id: `${importForm.semesterId}-${Date.now()}`,
      semesterId: importForm.semesterId,
      name: importForm.name.trim(),
      timeRange: `${importForm.startTime.trim()} ~ ${importForm.endTime.trim()}`,
      term: termOptions.value.find((item) => item.semesterId === importForm.semesterId)?.label || '-',
      count: parsedRows.value.length,
      uploader: currentUploaderName(),
      uploadedAt: formatDateTime(new Date())
        ,
      rows: parsedRows.value.map((row) => ({ ...row }))
    });
    page.value = 1;
    closeImportDialog();
    ElMessage.success('线下考试成绩已导入');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '线下成绩导入失败');
  } finally {
    importing.value = false;
  }
}

function toApiRows(rows: LocalImportRow[]): AdminSemesterScoreImportRow[] {
  return rows.map(({ studentName: _studentName, ...row }) => row);
}

function openEditDialog(item: OfflineExam) {
  if (!item.rows.length) {
    ElMessage.info('该批次暂无可编辑的成绩数据');
    return;
  }
  editingExam.value = item;
  editableScores.value = item.rows.map((row) => ({
    studentNo: row.studentNo,
    studentName: row.studentName,
    score: String(row.examScore)
  }));
  editPage.value = 1;
  editVisible.value = true;
}

async function saveEditScores() {
  const item = editingExam.value;
  if (!item) {
    return;
  }
  const scoreMap = new Map(editableScores.value.map((row) => [row.studentNo, row.score]));
  const rows = item.rows.map((row) => {
    const score = Number(scoreMap.get(row.studentNo));
    return { ...row, examScore: score };
  });
  if (rows.some((row) => !Number.isFinite(row.examScore) || row.examScore < 0 || row.examScore > 100)) {
    ElMessage.warning('成绩必须填写0到100之间的数字');
    return;
  }

  savingEdit.value = true;
  try {
    await importAdminSemesterScores(toApiRows(rows));
    item.rows = rows;
    item.uploadedAt = formatDateTime(new Date());
    editVisible.value = false;
    ElMessage.success('成绩已保存');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '成绩保存失败');
  } finally {
    savingEdit.value = false;
  }
}

function downloadTemplate() {
  const url = `/templates/${encodeURIComponent('线下考试成绩导入表格.xlsx')}`;
  const link = document.createElement('a');
  link.href = url;
  link.download = '线下考试成绩导入表格.xlsx';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function currentUploaderName() {
  const storedUser = localStorage.getItem('jiaoxuepeiyu_admin_user');
  if (!storedUser) {
    return '当前用户';
  }
  try {
    const user = JSON.parse(storedUser) as { realName?: string; name?: string; username?: string };
    return user.realName || user.name || user.username || '当前用户';
  } catch {
    return '当前用户';
  }
}

function formatDateTime(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

onMounted(loadOptions);
</script>
