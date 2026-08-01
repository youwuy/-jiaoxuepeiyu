<template>
  <AdminShell activeKey="theory-question">
    <section class="admin-question-page">
      <el-breadcrumb class="admin-question-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>理论试题</el-breadcrumb-item>
      </el-breadcrumb>

      <header class="admin-question-head">
        <div>
          <h1>理论试题</h1>
          <p>维护理论题库内容、答案、分值和启用状态</p>
        </div>
        <div class="admin-question-head-actions">
          <el-button class="admin-question-quiet-button" :icon="Download" @click="downloadTemplate">下载模板</el-button>
          <el-button class="admin-question-quiet-button" :icon="Upload" @click="openImport">批量导入</el-button>
          <el-button type="primary" class="admin-question-primary-button" :icon="Plus" @click="openCreate">新增试题</el-button>
        </div>
      </header>

      <section class="admin-question-filter-card">
        <el-input v-model="draft.keyword" class="admin-question-search" :prefix-icon="Search" clearable placeholder="搜索题目内容" @keyup.enter="applyFilters" />
        <el-select v-model="draft.questionType" class="admin-question-select" placeholder="题型" clearable>
          <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="draft.enabled" class="admin-question-select" placeholder="状态" clearable>
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-button type="primary" class="admin-question-query-button" @click="applyFilters">查询</el-button>
        <el-button class="admin-question-reset-button" @click="resetFilters">重置</el-button>
      </section>

      <section class="admin-question-layout" :class="{ 'has-panel': panelVisible }">
        <section class="admin-question-board">
          <header class="admin-question-board-head">
            <div>
              <strong>题库列表</strong>
              <p>共 {{ totalCount }} 道试题，已选 {{ selectedIds.length }} 道</p>
            </div>
            <div class="admin-question-board-actions">
              <el-button class="admin-question-batch-button" :disabled="selectedIds.length === 0" @click="batchEnable(true)">批量启用</el-button>
              <el-button class="admin-question-batch-button danger" :disabled="selectedIds.length === 0" @click="batchEnable(false)">批量停用</el-button>
              <el-button class="admin-question-batch-button" :icon="Refresh" @click="loadQuestions">刷新</el-button>
            </div>
          </header>

          <div v-if="loading" class="admin-question-empty">试题加载中...</div>
          <div v-else-if="pagedQuestions.length === 0" class="admin-question-empty"><el-empty description="暂无试题" /></div>
          <div v-else class="admin-question-table-scroll">
            <table class="admin-question-table">
              <thead>
                <tr>
                  <th class="check-col"><el-checkbox :model-value="allSelected" :indeterminate="partSelected" @change="toggleAll" /></th>
                  <th>题目内容</th>
                  <th>题型</th>
                  <th>分值</th>
                  <th>标准答案</th>
                  <th>创建人</th>
                  <th>状态</th>
                  <th>更新时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in pagedQuestions" :key="row.questionId" :class="{ selected: selectedIds.includes(row.questionId) }">
                  <td class="check-col"><el-checkbox :model-value="selectedIds.includes(row.questionId)" @change="toggleOne(row.questionId)" /></td>
                  <td class="question-title-cell"><strong>{{ row.title }}</strong><span>ID {{ row.questionId }}</span></td>
                  <td><span class="admin-question-type-pill" :class="row.typeTone">{{ row.typeLabel }}</span></td>
                  <td><strong>{{ row.score }} 分</strong></td>
                  <td class="answer-cell">{{ row.standardAnswer || '-' }}</td>
                  <td>{{ row.creatorName || '-' }}</td>
                  <td><span class="admin-question-status" :class="row.enabled ? 'enabled' : 'disabled'"><i></i>{{ row.enabled ? '启用' : '停用' }}</span></td>
                  <td>{{ row.updatedAtLabel }}</td>
                  <td><div class="admin-question-row-actions"><el-button class="plain" @click="openPreview(row)">查看</el-button><el-button class="edit" @click="openEdit(row)">编辑</el-button><el-button class="plain" @click="openLogs(row)">记录</el-button></div></td>
                </tr>
              </tbody>
            </table>
          </div>
          <footer v-if="!loading && pagedQuestions.length" class="admin-question-footer"><span>共 {{ totalCount }} 条记录</span><el-pagination v-model:current-page="page" :page-size="pageSize" :total="totalCount" layout="prev, pager, next" background /></footer>
        </section>

        <aside v-if="panelVisible" class="admin-question-panel">
          <header class="admin-question-panel-head"><div><strong>{{ panelMode === 'create' ? '新增试题' : '编辑试题' }}</strong><p>填写题干、答案和题目选项</p></div><el-button text circle :icon="Close" @click="panelVisible = false" /></header>
          <div class="admin-question-form">
            <label class="admin-question-field wide"><span>题目内容 <b>*</b></span><el-input v-model="form.title" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入题目内容" /></label>
            <div class="admin-question-form-grid">
              <label class="admin-question-field"><span>题型 <b>*</b></span><el-select v-model="form.questionType"><el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></label>
              <label class="admin-question-field"><span>分值 <b>*</b></span><el-input-number v-model="form.score" :min="1" :max="100" controls-position="right" /></label>
            </div>
            <div v-if="isChoice" class="admin-question-options-field"><div class="admin-question-field-label"><span>选项与答案 <b>*</b></span><el-button text class="admin-question-add-option" :icon="Plus" @click="addOption">添加选项</el-button></div><div v-for="(option, index) in form.options" :key="option.optionKey" class="admin-question-option-row"><el-radio v-model="form.standardAnswer" :label="option.optionKey">{{ option.optionKey }}</el-radio><el-input v-model="option.optionText" placeholder="请输入选项内容" /><el-button text :icon="Delete" aria-label="删除选项" :disabled="form.options.length <= 2" @click="removeOption(index)" /></div></div>
            <label v-else class="admin-question-field wide"><span>标准答案 <b>*</b></span><el-input v-model="form.standardAnswer" :placeholder="form.questionType === 'JUDGE' ? '请输入正确或错误' : '请输入标准答案'" /></label>
            <label class="admin-question-field wide"><span>解析说明</span><el-input v-model="form.explanation" type="textarea" :rows="3" maxlength="300" show-word-limit placeholder="可填写答案解析，便于查看时复核" /></label>
          </div>
          <footer class="admin-question-panel-footer"><el-button @click="panelVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveQuestion">保存试题</el-button></footer>
        </aside>
      </section>
    </section>

    <el-dialog v-model="previewVisible" class="admin-question-dialog" width="720px" :show-close="false" append-to-body>
      <template #header><div class="admin-question-dialog-head"><strong>试题预览</strong><el-button text circle :icon="Close" @click="previewVisible = false" /></div></template>
      <div v-if="previewTarget" class="admin-question-preview"><div class="admin-question-preview-meta"><span class="admin-question-type-pill" :class="previewTarget.typeTone">{{ previewTarget.typeLabel }}</span><span>{{ previewTarget.score }} 分</span><span>ID {{ previewTarget.questionId }}</span></div><h2>{{ previewTarget.title }}</h2><ol v-if="previewTarget.options?.length" class="admin-question-preview-options"><li v-for="option in previewTarget.options" :key="option.optionKey"><span>{{ option.optionKey }}</span>{{ option.optionText }}</li></ol><div class="admin-question-preview-answer"><span>标准答案</span><strong>{{ previewTarget.standardAnswer || '-' }}</strong></div></div>
      <template #footer><div class="admin-question-dialog-footer"><el-button @click="previewVisible = false">关闭</el-button><el-button type="primary" @click="openEdit(previewTarget); previewVisible = false">编辑试题</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="importVisible" class="admin-question-dialog" width="760px" :show-close="false" append-to-body>
      <template #header><div class="admin-question-dialog-head"><strong>批量导入试题</strong><el-button text circle :icon="Close" @click="importVisible = false" /></div></template>
      <div class="admin-question-import"><div class="admin-question-import-tip"><el-icon><Document /></el-icon><div><strong>按模板整理后导入</strong><p>支持单选题、多选题、判断题和填空题。先下载模板，再粘贴题干、选项、答案和分值。</p></div><el-button text @click="downloadTemplate">下载模板</el-button></div><el-input v-model="importText" type="textarea" :rows="8" placeholder="粘贴导入内容，或填写示例题目后点击预览" /><div v-if="importPreview" class="admin-question-import-result"><strong>校验结果</strong><span class="success">有效 {{ importPreview.validCount ?? 0 }} 条</span><span class="error">错误 {{ importPreview.invalidCount ?? 0 }} 条</span><p v-for="item in importPreview.errors || []" :key="`${item.rowNumber}-${item.message}`">第 {{ item.rowNumber }} 行：{{ item.message }}</p></div></div>
      <template #footer><div class="admin-question-dialog-footer"><el-button @click="importVisible = false">取消</el-button><el-button type="primary" :loading="importing" @click="previewImport">预览校验</el-button></div></template>
    </el-dialog>

    <el-drawer v-model="logsVisible" class="admin-question-log-drawer" direction="rtl" size="520px" :with-header="false"><div class="admin-question-drawer-head"><div><span>操作记录</span><h3>{{ logTarget?.title || '试题记录' }}</h3></div><el-button text :icon="Close" @click="logsVisible = false" /></div><div v-if="logsLoading" class="admin-question-empty">记录加载中...</div><template v-else><article v-for="item in logs" :key="item.logId" class="admin-question-log-row"><header><strong>{{ item.action || '操作' }}</strong><span>{{ formatDateTime(item.createdAt) }}</span></header><p>{{ item.content || '-' }}</p><small>{{ item.operatorName || '-' }}</small></article><el-empty v-if="logs.length === 0" description="暂无记录" /></template></el-drawer>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Delete, Document, Download, Plus, Refresh, Search, Upload } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { createAdminQuestion, disableAdminQuestion, enableAdminQuestion, fetchAdminQuestion, fetchAdminQuestionLogs, fetchAdminQuestions, previewAdminQuestionImport, updateAdminQuestion, type AdminQuestion, type AdminQuestionCommand, type AdminQuestionImportPreview, type AdminQuestionLog } from '../../api/admin-question';

type QuestionType = 'SINGLE' | 'MULTIPLE' | 'JUDGE' | 'BLANK';
interface QuestionRow extends AdminQuestion { typeLabel: string; typeTone: string; updatedAtLabel: string; }
interface QuestionForm { questionType: QuestionType; title: string; standardAnswer: string; score: number; explanation: string; options: Array<{ optionKey: string; optionText: string }> }

const questionTypeOptions = [{ value: 'SINGLE', label: '单选题' }, { value: 'MULTIPLE', label: '多选题' }, { value: 'JUDGE', label: '判断题' }, { value: 'BLANK', label: '填空题' }];
const typeLabels: Record<string, string> = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', BLANK: '填空题', CHOICE: '单选题', MULTI_CHOICE: '多选题', TRUE_FALSE: '判断题' };
const draft = reactive<{ keyword: string; questionType: string; enabled: boolean | undefined }>({ keyword: '', questionType: '', enabled: undefined });
const applied = ref({ keyword: '', questionType: '', enabled: undefined as boolean | undefined });
const questions = ref<QuestionRow[]>([]); const loading = ref(false); const page = ref(1); const pageSize = 10; const totalCount = ref(0); const selectedIds = ref<number[]>([]);
const panelVisible = ref(false); const panelMode = ref<'create' | 'edit'>('create'); const editingId = ref<number | null>(null); const saving = ref(false); const previewVisible = ref(false); const previewTarget = ref<QuestionRow | null>(null); const importVisible = ref(false); const importing = ref(false); const importText = ref(''); const importPreview = ref<AdminQuestionImportPreview | null>(null); const logsVisible = ref(false); const logsLoading = ref(false); const logTarget = ref<QuestionRow | null>(null); const logs = ref<AdminQuestionLog[]>([]);
const form = reactive<QuestionForm>(emptyForm());
const pagedQuestions = computed(() => questions.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const allSelected = computed(() => pagedQuestions.value.length > 0 && pagedQuestions.value.every((item) => selectedIds.value.includes(item.questionId)));
const partSelected = computed(() => selectedIds.value.length > 0 && !allSelected.value);
const isChoice = computed(() => form.questionType === 'SINGLE' || form.questionType === 'MULTIPLE');

function emptyForm(): QuestionForm { return { questionType: 'SINGLE', title: '', standardAnswer: '', score: 5, explanation: '', options: [{ optionKey: 'A', optionText: '' }, { optionKey: 'B', optionText: '' }, { optionKey: 'C', optionText: '' }, { optionKey: 'D', optionText: '' }] }; }
function mapRow(item: AdminQuestion): QuestionRow { const key = String(item.questionType || 'SINGLE').toUpperCase(); return { ...item, typeLabel: typeLabels[key] || item.questionType || '试题', typeTone: key.toLowerCase(), updatedAtLabel: formatDateTime(item.updatedAt || item.createdAt) }; }
function mockQuestions(): AdminQuestion[] { return [{ questionId: 2101, questionType: 'SINGLE', title: '城市轨道交通信号系统中，联锁设备的核心功能是什么？', standardAnswer: 'B', score: 5, enabled: true, creatorName: '李教师', updatedAt: '2025-04-12 10:20', options: [{ optionKey: 'A', optionText: '控制列车运行速度' }, { optionKey: 'B', optionText: '保证列车运行安全，防止冲突' }, { optionKey: 'C', optionText: '管理车站票务流量' }, { optionKey: 'D', optionText: '调度车辆维修计划' }] }, { questionId: 2102, questionType: 'MULTIPLE', title: '以下哪些属于城市轨道交通信号系统的组成部分？', standardAnswer: 'ABD', score: 8, enabled: true, creatorName: '王教师', updatedAt: '2025-04-11 15:02', options: [{ optionKey: 'A', optionText: '联锁设备' }, { optionKey: 'B', optionText: '闭塞设备' }, { optionKey: 'C', optionText: '售票系统' }, { optionKey: 'D', optionText: 'ATS系统' }] }, { questionId: 2103, questionType: 'JUDGE', title: 'CBTC系统是基于通信的列车控制系统。', standardAnswer: '正确', score: 3, enabled: true, creatorName: '李教师', updatedAt: '2025-04-10 09:18' }, { questionId: 2104, questionType: 'BLANK', title: '城市轨道交通信号系统的核心目标是保障列车运行______和提高运输效率。', standardAnswer: '安全', score: 4, enabled: false, creatorName: '赵教师', updatedAt: '2025-04-08 17:40' }, { questionId: 2105, questionType: 'SINGLE', title: 'ATP系统的主要作用是对列车运行进行什么防护？', standardAnswer: 'A', score: 5, enabled: true, creatorName: '李教师', updatedAt: '2025-04-07 14:12', options: [{ optionKey: 'A', optionText: '安全速度防护' }, { optionKey: 'B', optionText: '票务计费' }, { optionKey: 'C', optionText: '客流统计' }, { optionKey: 'D', optionText: '设备采购' }] }]; }
function formatDateTime(value?: string) { return value ? value.replace('T', ' ').slice(0, 16) : '-'; }
function applyFilters() { applied.value = { ...draft }; page.value = 1; void loadQuestions(); }
function resetFilters() { draft.keyword = ''; draft.questionType = ''; draft.enabled = undefined; applyFilters(); }
function toggleOne(id: number) { selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter((item) => item !== id) : [...selectedIds.value, id]; }
function toggleAll(value: string | number | boolean) { selectedIds.value = value ? Array.from(new Set([...selectedIds.value, ...pagedQuestions.value.map((item) => item.questionId)])) : selectedIds.value.filter((id) => !pagedQuestions.value.some((item) => item.questionId === id)); }
function openCreate() { Object.assign(form, emptyForm()); editingId.value = null; panelMode.value = 'create'; panelVisible.value = true; }
async function openEdit(row: QuestionRow | null) { if (!row) return; panelMode.value = 'edit'; editingId.value = row.questionId; panelVisible.value = true; try { Object.assign(form, toForm(await fetchAdminQuestion(row.questionId))); } catch { Object.assign(form, toForm(row)); } }
function toForm(row: AdminQuestion): QuestionForm { return { questionType: normalizeType(row.questionType), title: row.title, standardAnswer: row.standardAnswer || '', score: Number(row.score || 5), explanation: '', options: row.options?.map((item) => ({ optionKey: item.optionKey || 'A', optionText: item.optionText || '' })) || emptyForm().options }; }
function normalizeType(value?: string): QuestionType { const key = String(value || '').toUpperCase(); if (key.includes('MULTI')) return 'MULTIPLE'; if (key.includes('JUDGE') || key.includes('TRUE')) return 'JUDGE'; if (key.includes('BLANK')) return 'BLANK'; return 'SINGLE'; }
function addOption() { const next = String.fromCharCode(65 + form.options.length); form.options.push({ optionKey: next, optionText: '' }); }
function removeOption(index: number) { if (form.options.length > 2) { const removed = form.options.splice(index, 1)[0]; if (form.standardAnswer.includes(removed.optionKey)) form.standardAnswer = form.standardAnswer.replace(removed.optionKey, ''); } }
function openPreview(row: QuestionRow) { previewTarget.value = row; previewVisible.value = true; }
async function openLogs(row: QuestionRow) { logTarget.value = row; logsVisible.value = true; logsLoading.value = true; try { logs.value = await fetchAdminQuestionLogs(row.questionId); } catch { logs.value = []; } finally { logsLoading.value = false; } }
function validateForm(): AdminQuestionCommand { if (!form.title.trim()) throw new Error('请输入题目内容'); if (!form.standardAnswer.trim()) throw new Error('请输入标准答案'); if (isChoice.value && form.options.some((item) => !item.optionText.trim())) throw new Error('请完善所有选项'); return { questionType: form.questionType, title: form.title.trim(), standardAnswer: form.standardAnswer.trim(), score: Number(form.score), options: isChoice.value ? form.options.map((item) => ({ optionKey: item.optionKey, optionText: item.optionText.trim(), correct: form.standardAnswer.includes(item.optionKey) })) : [] }; }
async function saveQuestion() { let command: AdminQuestionCommand; try { command = validateForm(); } catch (error) { ElMessage.warning(error instanceof Error ? error.message : '请完善试题信息'); return; } saving.value = true; try { if (editingId.value) await updateAdminQuestion(editingId.value, command); else await createAdminQuestion(command); ElMessage.success(editingId.value ? '试题已更新' : '试题已新增'); panelVisible.value = false; await loadQuestions(); } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存失败'); } finally { saving.value = false; } }
async function batchEnable(enabled: boolean) { try { await ElMessageBox.confirm(`确认${enabled ? '启用' : '停用'}已选 ${selectedIds.value.length} 道试题？`, enabled ? '批量启用' : '批量停用', { confirmButtonText: enabled ? '启用' : '停用', cancelButtonText: '取消', type: enabled ? 'success' : 'warning' }); } catch { return; } await Promise.all(selectedIds.value.map((id) => enabled ? enableAdminQuestion(id) : disableAdminQuestion(id))); ElMessage.success(`已${enabled ? '启用' : '停用'}所选试题`); selectedIds.value = []; await loadQuestions(); }
function downloadTemplate() { const content = '题型,题目内容,选项A,选项B,选项C,选项D,标准答案,分值\n单选题,示例题目,选项A,选项B,选项C,选项D,B,5'; const blob = new Blob([`\ufeff${content}`], { type: 'text/csv;charset=utf-8' }); const link = document.createElement('a'); link.href = URL.createObjectURL(blob); link.download = '理论试题导入模板.csv'; link.click(); URL.revokeObjectURL(link.href); }
function openImport() { importText.value = ''; importPreview.value = null; importVisible.value = true; }
function parseImportRows() { return importText.value.split(/\r?\n/).map((line) => line.trim()).filter(Boolean).slice(1).map((line, index) => { const cells = line.split(',').map((item) => item.trim()); return { rowNumber: index + 2, questionType: cells[0], title: cells[1], standardAnswer: cells[6], score: Number(cells[7] || 5), options: ['A', 'B', 'C', 'D'].map((key, optionIndex) => ({ optionKey: key, optionText: cells[optionIndex + 2] })).filter((item) => item.optionText) }; }); }
async function previewImport() { const rows = parseImportRows(); if (!rows.length) { ElMessage.warning('请先粘贴导入内容'); return; } importing.value = true; try { importPreview.value = await previewAdminQuestionImport(rows); } catch { importPreview.value = { validCount: rows.filter((item) => item.title && item.standardAnswer).length, invalidCount: rows.filter((item) => !item.title || !item.standardAnswer).length, errors: rows.filter((item) => !item.title || !item.standardAnswer).map((item) => ({ rowNumber: item.rowNumber, message: '题目内容和标准答案不能为空' })) }; } finally { importing.value = false; } }
async function loadQuestions() { loading.value = true; try { const result = await fetchAdminQuestions({ keyword: applied.value.keyword, questionType: applied.value.questionType || undefined, enabled: applied.value.enabled, page: 1, pageSize: 999 }); questions.value = result.records.map(mapRow); totalCount.value = result.total || questions.value.length; } catch { const rows = mockQuestions().filter((item) => (!applied.value.keyword || item.title.includes(applied.value.keyword)) && (!applied.value.questionType || normalizeType(item.questionType) === applied.value.questionType) && (applied.value.enabled === undefined || item.enabled === applied.value.enabled)); questions.value = rows.map(mapRow); totalCount.value = questions.value.length; } finally { selectedIds.value = []; loading.value = false; } }
onMounted(() => { void loadQuestions(); });
</script>
