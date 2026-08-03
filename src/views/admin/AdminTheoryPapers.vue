<template>
  <AdminShell activeKey="theory-paper">
    <section v-if="viewMode === 'list'" class="admin-theory-paper-page">
      <el-breadcrumb class="admin-theory-paper-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>理论试卷</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-theory-paper-filter-card">
        <div class="admin-theory-paper-filter-row">
          <label class="admin-theory-paper-field is-name">
            <span>试卷名称</span>
            <el-input v-model="draft.keyword" placeholder="请输入试卷名称" clearable @keyup.enter="applyFilters" />
          </label>
          <label class="admin-theory-paper-field">
            <span>所属课程</span>
            <el-input v-model="draft.courseName" placeholder="请输入所属课程" clearable @keyup.enter="applyFilters" />
          </label>
          <label class="admin-theory-paper-field">
            <span>添加人</span>
            <el-select v-model="draft.creatorId" placeholder="请选择添加人" clearable filterable>
              <el-option v-for="item in creatorOptions" :key="item.creatorId" :label="item.creatorName" :value="item.creatorId" />
            </el-select>
          </label>
          <label class="admin-theory-paper-field">
            <span>启用状态</span>
            <el-select v-model="draft.enabled" placeholder="请选择状态" clearable>
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
          </label>
          <el-button class="admin-theory-paper-query-button" @click="applyFilters">查询</el-button>
          <el-button class="admin-theory-paper-reset-button" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <section class="admin-theory-paper-actions">
        <div>
          <el-button class="admin-theory-paper-primary" :icon="Plus" @click="openCreate">新增</el-button>
          <el-button class="admin-theory-paper-primary" :icon="UploadFilled" @click="openImport">导入试卷</el-button>
          <el-button class="admin-theory-paper-lite" :disabled="selectedIds.length === 0" @click="batchSetEnabled(true)">批量启用</el-button>
          <el-button class="admin-theory-paper-lite" :disabled="selectedIds.length === 0" @click="batchSetEnabled(false)">批量禁用</el-button>
        </div>
        <p>共 <b>{{ totalCount }}</b> 条记录</p>
      </section>

      <section class="admin-theory-paper-board" v-loading="loading">
        <div class="admin-theory-paper-table-scroll">
          <table class="admin-theory-paper-table">
            <thead>
              <tr>
                <th class="check-col"><el-checkbox :model-value="allSelected" :indeterminate="partSelected" @change="toggleAll" /></th>
                <th>序号</th>
                <th>试卷名称</th>
                <th>所属课程</th>
                <th>试题数量</th>
                <th>试卷总分</th>
                <th>添加人</th>
                <th>添加时间</th>
                <th>启用状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in papers" :key="row.paperId">
                <td class="check-col"><el-checkbox :model-value="selectedIds.includes(row.paperId)" @change="toggleOne(row.paperId)" /></td>
                <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                <td><strong>{{ row.paperName }}</strong></td>
                <td>{{ row.courseName }}</td>
                <td>{{ row.questionCount }}</td>
                <td>{{ row.totalScore }}</td>
                <td>{{ row.creatorName }}</td>
                <td>{{ row.createdAt }}</td>
                <td><span class="admin-theory-paper-status" :class="row.enabled ? 'enabled' : 'disabled'"><i></i>{{ row.enabled ? '启用' : '禁用' }}</span></td>
                <td>
                  <div class="admin-theory-paper-row-actions">
                    <el-button text @click="openManage(row)">修改</el-button>
                    <el-button text :class="row.enabled ? 'warn' : 'success'" @click="setEnabled(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
                    <el-button text @click="openLogs(row)">操作日志</el-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="admin-theory-paper-footer">
          <p>显示 <b>{{ pageStart }}</b> 到 <b>{{ pageEnd }}</b> 条，共 <b>{{ totalCount }}</b> 条记录</p>
          <div class="admin-theory-paper-pager">
            <el-pagination v-model:current-page="page" :page-size="pageSize" :total="totalCount" layout="prev, pager, next" background @current-change="loadPapers" />
            <span>跳至</span>
            <el-input-number v-model="jumpPage" :min="1" :max="maxPage" controls-position="right" @change="jumpToPage" />
            <span>页</span>
          </div>
        </footer>
      </section>
    </section>

    <section v-else-if="viewMode === 'auto'" class="admin-theory-paper-builder-page">
      <BuilderHeader title="新增试卷" subtitle="自动组卷" @back="backToList" />
      <section class="admin-theory-paper-builder-card">
        <header><strong>基础信息</strong></header>
        <div class="admin-theory-paper-create-basic">
          <label class="admin-theory-paper-field"><span>试卷名称 <b>*</b></span><el-input v-model="builder.paperName" placeholder="请输入试卷名称" /></label>
          <label class="admin-theory-paper-field is-mode">
            <span>组卷方式 <b>*</b></span>
            <el-radio-group :model-value="viewMode" @change="switchCreateMode">
              <el-radio label="auto">自动组卷</el-radio>
              <el-radio label="manual">手动组卷</el-radio>
            </el-radio-group>
          </label>
        </div>
      </section>
      <section class="admin-theory-paper-builder-card">
        <header><strong>选题设置</strong></header>
        <table class="admin-theory-paper-rule-table is-auto">
          <thead><tr><th>题型</th><th>选题数量</th></tr></thead>
          <tbody>
            <tr v-for="rule in builder.rules" :key="rule.type">
              <td><el-checkbox v-model="rule.selected">{{ rule.type }}</el-checkbox></td>
              <td><el-input-number v-model="rule.count" :min="0" :max="100" :disabled="!rule.selected" controls-position="right" /></td>
            </tr>
          </tbody>
        </table>
        <footer class="admin-theory-paper-rule-summary">选题数量合计：<b>{{ autoQuestionTotal }}</b>题</footer>
      </section>
      <footer class="admin-theory-paper-builder-footer is-center">
        <button type="button" class="ghost" @click="backToList">取消</button>
        <button type="button" class="primary" @click="openPreview('auto')">下一步</button>
      </footer>
    </section>

    <section v-else-if="viewMode === 'manual'" class="admin-theory-paper-builder-page">
      <BuilderHeader title="新增试卷" subtitle="手动组卷" @back="backToList" />
      <section class="admin-theory-paper-builder-card">
        <header><strong>基础信息</strong></header>
        <div class="admin-theory-paper-create-basic">
          <label class="admin-theory-paper-field"><span>试卷名称 <b>*</b></span><el-input v-model="builder.paperName" placeholder="请输入试卷名称" /></label>
          <label class="admin-theory-paper-field is-mode">
            <span>组卷方式 <b>*</b></span>
            <el-radio-group :model-value="viewMode" @change="switchCreateMode">
              <el-radio label="auto">自动组卷</el-radio>
              <el-radio label="manual">手动组卷</el-radio>
            </el-radio-group>
          </label>
        </div>
      </section>
      <footer class="admin-theory-paper-builder-footer is-center">
        <button type="button" class="ghost" @click="backToList">取消</button>
        <button type="button" class="primary" @click="viewMode = 'manual-select'">下一步</button>
      </footer>
    </section>

    <section v-else-if="viewMode === 'manual-select'" class="admin-theory-paper-builder-page">
      <BuilderHeader title="新增试卷" subtitle="手动组卷" @back="() => { viewMode = 'manual'; }" />
      <section class="admin-theory-paper-manual-layout">
        <section class="admin-theory-paper-builder-card">
          <header><strong>题库选择</strong></header>
          <div class="admin-theory-paper-question-filter"><el-input v-model="questionKeyword" placeholder="搜索题干" clearable /><el-select v-model="questionType" placeholder="题型" clearable><el-option v-for="item in questionTypeOptions" :key="item" :label="item" :value="item" /></el-select></div>
          <table class="admin-theory-paper-question-table">
            <thead><tr><th>题干</th><th>题型</th><th>分值</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="item in filteredQuestionBank" :key="item.id">
                <td>{{ item.title }}</td><td>{{ item.type }}</td><td>{{ item.score }}</td>
                <td><el-button text @click="addQuestion(item)">添加</el-button></td>
              </tr>
            </tbody>
          </table>
        </section>
        <section class="admin-theory-paper-builder-card">
          <header><strong>已选试题</strong><span>{{ selectedQuestions.length }}题 / {{ selectedScore }}分</span></header>
          <article v-for="item in selectedQuestions" :key="item.id" class="admin-theory-paper-selected-question">
            <div><strong>{{ item.title }}</strong><span>{{ item.type }}</span></div>
            <label>分值 <el-input-number v-model="item.score" :min="1" :max="20" controls-position="right" /></label>
            <el-button text class="warn" @click="removeQuestion(item.id)">移除</el-button>
          </article>
        </section>
      </section>
      <BuilderFooter @cancel="backToList" @preview="openPreview('manual')" @save="saveBuilder" />
    </section>

    <section v-else-if="viewMode === 'manage'" class="admin-theory-paper-builder-page">
      <BuilderHeader title="管理试题" :subtitle="activePaper?.paperName || '理论试卷'" @back="backToList" />
      <section class="admin-theory-paper-manage-filter">
        <label class="admin-theory-paper-field">
          <span>题型</span>
          <el-select v-model="questionType" placeholder="全部题型" clearable>
            <el-option v-for="item in questionTypeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </label>
        <label class="admin-theory-paper-field is-question-search">
          <span>题干搜索</span>
          <el-input v-model="questionKeyword" placeholder="请输入题干关键词搜索" clearable />
        </label>
        <label class="admin-theory-paper-field">
          <span>添加人</span>
          <el-input v-model="manageCreator" placeholder="请输入添加人" clearable />
        </label>
        <label class="admin-theory-paper-field">
          <span>所属课程</span>
          <el-input v-model="manageCourse" placeholder="请输入所属课程" clearable />
        </label>
        <el-button class="admin-theory-paper-query-button">查询</el-button>
        <el-button class="admin-theory-paper-reset-button" @click="resetManageFilters">重置</el-button>
      </section>

      <section class="admin-theory-paper-manage-actions">
        <el-button class="admin-theory-paper-primary" :icon="Plus" @click="addFilteredQuestions">加入试题篮</el-button>
        <p>已加入试题篮：<b>{{ selectedQuestions.length }}</b> 题</p>
        <span v-for="item in questionStats" :key="item.type">{{ item.short }} <b>{{ item.count }}</b></span>
      </section>

      <section class="admin-theory-paper-builder-card is-manage-table">
        <table class="admin-theory-paper-question-table is-manage">
          <thead>
            <tr>
              <th class="check-col"><el-checkbox :model-value="allQuestionSelected" :indeterminate="partQuestionSelected" @change="toggleAllQuestions" /></th>
              <th>序号</th>
              <th>题型</th>
              <th>题干</th>
              <th>所属课程</th>
              <th>启用状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in filteredQuestionBank" :key="item.id">
              <td class="check-col"><el-checkbox :model-value="selectedQuestionIds.includes(item.id)" @change="toggleQuestion(item.id)" /></td>
              <td>{{ index + 1 }}</td>
              <td><span class="admin-theory-paper-type-pill" :class="typeTone(item.type)">{{ item.type }}</span></td>
              <td>{{ item.title }}</td>
              <td>{{ item.courseName }}</td>
              <td><span class="admin-theory-paper-status enabled"><i></i>已启用</span></td>
            </tr>
          </tbody>
        </table>
      </section>

      <footer class="admin-theory-paper-manage-bottom">
        <p>共 <b>{{ filteredQuestionBank.length }}</b> 条记录</p>
        <el-pagination v-model:current-page="managePage" :page-size="6" :total="filteredQuestionBank.length" layout="prev, pager, next" background />
      </footer>

      <footer class="admin-theory-paper-builder-footer is-center">
        <button type="button" class="primary" @click="openPreview('manage')">预览试卷</button>
        <button type="button" class="ghost" @click="backToList">取消</button>
      </footer>
    </section>

    <section v-else-if="viewMode === 'manage-edit'" class="admin-theory-paper-builder-page">
      <BuilderHeader title="试卷信息" :subtitle="activePaper?.paperName || '理论试卷'" @back="backToList" />
      <section class="admin-theory-paper-builder-card">
        <header><strong>试卷信息</strong><el-button class="admin-theory-paper-primary" @click="openPreview('manage')">组卷预览</el-button></header>
        <div class="admin-theory-paper-builder-grid">
          <label class="admin-theory-paper-field"><span>试卷名称</span><el-input v-model="manageForm.paperName" /></label>
          <label class="admin-theory-paper-field"><span>所属课程</span><el-input v-model="manageForm.courseName" /></label>
          <label class="admin-theory-paper-field"><span>试题数量</span><el-input-number v-model="selectedQuestions.length" disabled controls-position="right" /></label>
          <label class="admin-theory-paper-field"><span>试卷总分</span><el-input-number v-model="selectedScore" disabled controls-position="right" /></label>
        </div>
      </section>
      <section class="admin-theory-paper-builder-card">
        <header><strong>试题列表</strong><el-button class="admin-theory-paper-lite" @click="viewMode = 'manual-select'">添加试题</el-button></header>
        <table class="admin-theory-paper-rule-table">
          <thead><tr><th>序号</th><th>题干</th><th>题型</th><th>分值</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="(item, index) in selectedQuestions" :key="item.id">
              <td>{{ index + 1 }}</td><td>{{ item.title }}</td><td>{{ item.type }}</td>
              <td><el-input-number v-model="item.score" :min="1" :max="20" controls-position="right" /></td>
              <td><el-button text class="warn" @click="removeQuestion(item.id)">删除</el-button></td>
            </tr>
          </tbody>
        </table>
      </section>
      <BuilderFooter save-text="保存修改" @cancel="backToList" @preview="openPreview('manage')" @save="saveManage" />
    </section>

    <el-dialog v-model="importVisible" class="admin-theory-paper-upload-dialog" width="600px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-theory-paper-upload-head">
          <span><el-icon><UploadFilled /></el-icon></span>
          <div><strong>上传试卷</strong><p>请填写试卷信息并上传文件</p></div>
          <el-button text circle :icon="Close" @click="importVisible = false" />
        </div>
      </template>
      <div class="admin-theory-paper-upload-body">
        <label><span>试卷名称 <b>*</b></span><el-input v-model="previewPaper.paperName" maxlength="30" show-word-limit placeholder="请输入试卷名称" /></label>
        <label><span>试卷模板</span><el-button class="admin-theory-paper-template-button">点击下载试卷上传模板</el-button></label>
        <label><span>试卷内容 <b>*</b></span><el-upload drag action="#" :auto-upload="false"><el-icon><UploadFilled /></el-icon><div class="el-upload__text">点击或拖拽上传资源文件</div><template #tip><p>仅支持.excel 格式，大小不超过 200MB</p></template></el-upload></label>
        <label><span>所属课程 <b>*</b></span><el-input v-model="previewPaper.courseName" maxlength="30" show-word-limit placeholder="请输入所属课程名称" /></label>
      </div>
      <template #footer><div class="admin-theory-paper-dialog-footer"><el-button @click="importVisible = false">取消</el-button><el-button type="primary" :icon="UploadFilled" @click="openPreview('upload')">确认上传</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="previewVisible" class="admin-theory-paper-preview-modal" fullscreen :show-close="false" append-to-body>
      <section class="admin-theory-paper-preview-page">
        <header class="admin-theory-paper-preview-head">
          <div><h2>预览试卷</h2><p>预览确认无误后，可提交完成上传</p></div>
          <el-button text circle :icon="Close" @click="previewVisible = false" />
        </header>
        <section class="admin-theory-paper-preview-meta">
          <p><span>试卷名称：</span><strong>{{ previewPaper.paperName }}</strong></p>
          <i></i>
          <p><span>总分：</span><strong>100</strong><span>分</span></p>
          <div>
            <el-button @click="previewVisible = false">返回</el-button>
            <el-button class="admin-theory-paper-primary" @click="submitImport">保存</el-button>
          </div>
        </section>
        <main class="admin-theory-paper-preview-layout">
          <template v-if="previewGroups.length > 0">
            <aside class="admin-theory-paper-answer-card">
              <header><strong>答题卡</strong></header>
              <section v-for="item in answerCardGroups" :key="item.type" :class="item.tone">
                <p><span>{{ item.short }}</span>{{ item.count }}题 · {{ item.score }}分</p>
                <div>
                  <button v-for="num in item.numbers" :key="num" :class="{ active: num === 1 }">{{ num }}</button>
                </div>
              </section>
            </aside>
            <div class="admin-theory-paper-preview-stack">
              <section v-for="group in previewGroups" :key="group.type" class="admin-theory-paper-preview-card" :class="group.tone">
                <header><strong>{{ group.title }}</strong><span>{{ group.meta }}</span><el-button text>批量修改得分</el-button></header>
                <article v-for="question in group.questions" :key="question.index">
                  <div><h3>{{ question.index }}、{{ question.title }}</h3><ol v-if="question.options.length"><li v-for="option in question.options" :key="option">{{ option }}</li></ol></div>
                  <label><span>得分</span><el-input-number v-model="question.score" :min="1" :max="20" controls-position="right" /></label>
                </article>
              </section>
            </div>
          </template>
          <el-empty v-else description="暂无试卷内容，请先加入试题" />
        </main>
      </section>
    </el-dialog>

    <el-drawer v-model="logsVisible" class="admin-theory-paper-log-drawer" direction="rtl" size="520px" :with-header="false">
      <div class="admin-theory-paper-dialog-head"><strong>操作日志</strong><el-button text circle :icon="Close" @click="logsVisible = false" /></div>
      <article v-for="item in logRows" :key="item.time" class="admin-theory-paper-log-row"><header><strong>{{ item.action }}</strong><span>{{ item.time }}</span></header><p>{{ item.content }}</p></article>
    </el-drawer>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Close, Plus, UploadFilled } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  cancelPublishAdminPaper,
  createAdminPaper,
  fetchAdminPaper,
  fetchAdminPaperLogs,
  fetchAdminPapers,
  publishAdminPaper,
  updateAdminPaper,
  type AdminPaper,
  type AdminPaperCommand,
  type AdminPaperLog,
  type AdminPaperQuestion
} from '../../api/admin-paper';
import { fetchAdminQuestions, type AdminQuestion } from '../../api/admin-question';

type ViewMode = 'list' | 'auto' | 'manual' | 'manual-select' | 'manage' | 'manage-edit';

interface TheoryPaper {
  paperId: number;
  paperName: string;
  courseName: string;
  questionCount: number;
  totalScore: number;
  creatorName: string;
  creatorId?: number;
  createdAt: string;
  enabled: boolean;
  publishStatus?: string;
}

interface QuestionItem {
  id: number;
  title: string;
  type: string;
  score: number;
  courseName: string;
  options?: string[];
}

const BuilderHeader = defineComponent({
  props: { title: { type: String, required: true }, subtitle: { type: String, required: true } },
  emits: ['back'],
  setup(props, { emit }) {
    return () => h('header', { class: 'admin-theory-paper-builder-head' }, [
      h('button', { type: 'button', class: 'admin-theory-paper-back', onClick: () => emit('back') }, [h(ArrowLeft), '返回']),
      h('div', [h('h2', props.title), h('p', props.subtitle)])
    ]);
  }
});

const BuilderFooter = defineComponent({
  props: { saveText: { type: String, default: '保存试卷' } },
  emits: ['cancel', 'preview', 'save'],
  setup(props, { emit }) {
    return () => h('footer', { class: 'admin-theory-paper-builder-footer' }, [
      h('button', { type: 'button', class: 'ghost', onClick: () => emit('cancel') }, '取消'),
      h('button', { type: 'button', class: 'lite', onClick: () => emit('preview') }, '预览'),
      h('button', { type: 'button', class: 'primary', onClick: () => emit('save') }, props.saveText)
    ]);
  }
});

const pageSize = 12;
const questionTypeOptions = ['单选题', '多选题', '判断题', '填空题', '简答题'];
const viewMode = ref<ViewMode>('list');
const page = ref(1);
const totalCount = ref(0);
const loading = ref(false);
const saving = ref(false);
const jumpPage = ref(1);
const selectedIds = ref<number[]>([]);
const importVisible = ref(false);
const previewVisible = ref(false);
const logsVisible = ref(false);
const activePaper = ref<TheoryPaper | null>(null);
const questionKeyword = ref('');
const questionType = ref('');
const manageCreator = ref('');
const manageCourse = ref('');
const managePage = ref(1);
const selectedQuestionIds = ref<number[]>([]);
const paperLogs = ref<AdminPaperLog[]>([]);

const draft = reactive({ keyword: '', courseName: '', creatorId: undefined as number | undefined, enabled: undefined as boolean | undefined });
const applied = ref({ ...draft });
const builder = reactive({
  paperName: '',
  courseName: '',
  totalScore: 100,
  passScore: 60,
  rules: [
    { type: '单选题', count: 0, score: 0, difficulty: '全部', selected: false },
    { type: '多选题', count: 0, score: 0, difficulty: '全部', selected: false },
    { type: '判断题', count: 0, score: 0, difficulty: '基础', selected: false },
    { type: '填空题', count: 0, score: 0, difficulty: '全部', selected: false },
    { type: '简答题', count: 0, score: 0, difficulty: '全部', selected: false }
  ]
});
const manageForm = reactive({ paperName: '', courseName: '' });
const previewPaper = reactive({ paperName: '', courseName: '' });

const papers = ref<TheoryPaper[]>([]);
const questionBank = ref<QuestionItem[]>([]);
const selectedQuestions = ref<QuestionItem[]>([]);
const previewGroups = computed(() => {
  const groupMap = new Map<string, QuestionItem[]>();
  selectedQuestions.value.forEach((item) => {
    const key = item.type || '未分类';
    if (!groupMap.has(key)) {
      groupMap.set(key, []);
    }
    groupMap.get(key)!.push(item);
  });

  return questionTypeOptions
    .map((type, typeIndex) => {
      const questions = groupMap.get(type) ?? [];
      if (!questions.length) {
        return null;
      }

      return {
        type,
        title: `${'一二三四五'[typeIndex]}、${type}`,
        meta: `${questions.length}题 · 共${questions.reduce((sum, item) => sum + Number(item.score || 0), 0)}分`,
        tone: typeTone(type),
        questions: questions.map((question, index) => ({
          index: index + 1,
          title: question.title,
          score: Number(question.score || 0),
          options: question.options ?? []
        }))
      };
    })
    .filter(Boolean) as Array<{ type: string; title: string; meta: string; tone: string; questions: Array<{ index: number; title: string; score: number; options: string[] }> }>;
});

const creatorOptions = computed(() => {
  const map = new Map<number, string>();
  papers.value.forEach((item) => {
    if (item.creatorId && item.creatorName !== '-') {
      map.set(item.creatorId, item.creatorName);
    }
  });
  return Array.from(map.entries()).map(([creatorId, creatorName]) => ({ creatorId, creatorName }));
});
const maxPage = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)));
const allSelected = computed(() => papers.value.length > 0 && papers.value.every((item) => selectedIds.value.includes(item.paperId)));
const partSelected = computed(() => selectedIds.value.length > 0 && !allSelected.value);
const pageStart = computed(() => (totalCount.value === 0 ? 0 : (page.value - 1) * pageSize + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize, totalCount.value));
const selectedScore = computed(() => selectedQuestions.value.reduce((sum, item) => sum + Number(item.score || 0), 0));
const autoQuestionTotal = computed(() => builder.rules.reduce((sum, rule) => sum + (rule.selected ? Number(rule.count || 0) : 0), 0));
const filteredQuestionBank = computed(() => questionBank.value.filter((item) => (!questionKeyword.value || item.title.includes(questionKeyword.value)) && (!questionType.value || item.type === questionType.value) && (!manageCourse.value || item.courseName.includes(manageCourse.value))));
const allQuestionSelected = computed(() => filteredQuestionBank.value.length > 0 && filteredQuestionBank.value.every((item) => selectedQuestionIds.value.includes(item.id)));
const partQuestionSelected = computed(() => selectedQuestionIds.value.length > 0 && !allQuestionSelected.value);
const questionStats = computed(() => questionTypeOptions.map((type) => ({
  type,
  short: type.replace('题', ''),
  count: selectedQuestions.value.filter((item) => item.type === type).length
})));
const answerCardGroups = computed(() => {
  const groupMap = new Map<string, { short: string; tone: string; count: number; score: number; numbers: number[] }>();
  selectedQuestions.value.forEach((item, index) => {
    const type = item.type || '未分类';
    if (!groupMap.has(type)) {
      groupMap.set(type, { short: type.replace('题', ''), tone: typeTone(type), count: 0, score: 0, numbers: [] });
    }
    const group = groupMap.get(type)!;
    group.count += 1;
    group.score += Number(item.score || 0);
    group.numbers.push(index + 1);
  });

  return questionTypeOptions
    .map((type) => {
      const group = groupMap.get(type);
      return group ? { type, ...group } : null;
    })
    .filter(Boolean) as Array<{ type: string; short: string; tone: string; count: number; score: number; numbers: number[] }>;
});
const logRows = computed(() => [
  ...paperLogs.value.map((item) => ({
    action: item.action || '操作',
    time: formatDateTime(item.createdAt),
    content: item.content || '-'
  }))
]);

watch(page, (value) => { jumpPage.value = value; });

function formatDateTime(value?: string) { return value ? value.replace('T', ' ').slice(0, 19) : '-'; }
function publishStatus(enabled?: boolean) { return enabled === undefined ? undefined : enabled ? 'PUBLISHED' : 'OFFLINE'; }
function typeCode(label: string) {
  if (label.includes('多')) return 'MULTIPLE';
  if (label.includes('判断')) return 'JUDGE';
  if (label.includes('填空')) return 'FILL_BLANK';
  if (label.includes('简答')) return 'SHORT_ANSWER';
  return 'SINGLE';
}
function typeLabel(code?: string) {
  const normalized = String(code || '').toUpperCase();
  if (normalized.includes('MULTIPLE')) return '多选题';
  if (normalized.includes('JUDGE')) return '判断题';
  if (normalized.includes('FILL') || normalized.includes('BLANK')) return '填空题';
  if (normalized.includes('SHORT') || normalized.includes('ESSAY')) return '简答题';
  return '单选题';
}
function mapPaper(item: AdminPaper): TheoryPaper {
  const status = String(item.publishStatus || '').toUpperCase();
  return {
    paperId: item.paperId,
    paperName: item.paperName,
    courseName: (item as AdminPaper & { courseName?: string }).courseName || '-',
    questionCount: item.questionCount || item.questions?.length || 0,
    totalScore: item.totalScore || 0,
    creatorId: item.creatorId,
    creatorName: item.creatorName || '-',
    createdAt: formatDateTime(item.createdAt),
    enabled: status === 'PUBLISHED',
    publishStatus: item.publishStatus
  };
}
function mapQuestion(item: AdminQuestion | AdminPaperQuestion): QuestionItem {
  const questionId = 'questionId' in item ? item.questionId : 0;
  return {
    id: questionId,
    title: item.title || '-',
    type: typeLabel(item.questionType),
    score: Number(item.score || 1),
    courseName: (item as AdminQuestion & { courseName?: string }).courseName || '-'
  };
}
function paperCommand(mode: 'auto' | 'manual' | 'manage'): AdminPaperCommand {
  const paperName = mode === 'manage' ? manageForm.paperName.trim() : builder.paperName.trim();
  if (!paperName) {
    throw new Error('请输入试卷名称');
  }
  if (mode === 'auto') {
    const autoRules = builder.rules
      .filter((rule) => rule.selected && Number(rule.count) > 0)
      .map((rule) => ({ questionType: typeCode(rule.type), questionCount: Number(rule.count), scorePerQuestion: Number(rule.score || 1) }));
    if (autoRules.length === 0) {
      throw new Error('请至少设置一种题型');
    }
    return { paperName, composeMode: 'AUTO', autoRules };
  }

  const questions = selectedQuestions.value.map((item) => ({ questionId: item.id, score: Number(item.score || 1) }));
  if (questions.length === 0) {
    throw new Error('请至少加入一道试题');
  }
  return { paperName, composeMode: 'MANUAL', questions };
}
async function loadPapers() {
  loading.value = true;
  try {
    const result = await fetchAdminPapers({
      keyword: [applied.value.keyword, applied.value.courseName].map((item) => item.trim()).filter(Boolean).join(' ') || undefined,
      publishStatus: publishStatus(applied.value.enabled),
      creatorId: applied.value.creatorId,
      page: page.value,
      pageSize
    });
    papers.value = result.records.map(mapPaper);
    totalCount.value = result.total;
  } catch (error) {
    papers.value = [];
    totalCount.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '理论试卷加载失败');
  } finally {
    loading.value = false;
  }
}
async function loadQuestionBank() {
  try {
    const result = await fetchAdminQuestions({ enabled: true, page: 1, pageSize: 100 });
    questionBank.value = result.records.map(mapQuestion).filter((item) => item.id > 0);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '理论试题加载失败');
  }
}
function applyFilters() { applied.value = { ...draft }; page.value = 1; selectedIds.value = []; void loadPapers(); }
function resetFilters() { Object.assign(draft, { keyword: '', courseName: '', creatorId: undefined, enabled: undefined }); applyFilters(); }
function toggleOne(id: number) { selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter((item) => item !== id) : [...selectedIds.value, id]; }
function toggleAll(value: string | number | boolean) { selectedIds.value = value ? Array.from(new Set([...selectedIds.value, ...papers.value.map((item) => item.paperId)])) : selectedIds.value.filter((id) => !papers.value.some((item) => item.paperId === id)); }
function openCreate() { resetBuilder(); void loadQuestionBank(); viewMode.value = 'auto'; }
function switchCreateMode(value: string | number | boolean) { viewMode.value = value === 'manual' ? 'manual' : 'auto'; }
async function openManage(row: TheoryPaper) {
  activePaper.value = row;
  Object.assign(manageForm, { paperName: row.paperName, courseName: row.courseName });
  previewPaper.paperName = row.paperName;
  previewPaper.courseName = row.courseName;
  await loadQuestionBank();
  try {
    const detail = await fetchAdminPaper(row.paperId);
    selectedQuestions.value = (detail.questions || []).map(mapQuestion).filter((item) => item.id > 0);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试卷详情加载失败');
  }
  viewMode.value = 'manage';
}
function backToList() { viewMode.value = 'list'; }
function resetBuilder() { Object.assign(builder, { paperName: '', courseName: '', totalScore: 100, passScore: 60 }); }
function addQuestion(item: QuestionItem) { if (!selectedQuestions.value.some((question) => question.id === item.id)) selectedQuestions.value.push({ ...item }); }
function removeQuestion(id: number) { selectedQuestions.value = selectedQuestions.value.filter((item) => item.id !== id); }
function toggleQuestion(id: number) { selectedQuestionIds.value = selectedQuestionIds.value.includes(id) ? selectedQuestionIds.value.filter((item) => item !== id) : [...selectedQuestionIds.value, id]; }
function toggleAllQuestions(value: string | number | boolean) { selectedQuestionIds.value = value ? Array.from(new Set([...selectedQuestionIds.value, ...filteredQuestionBank.value.map((item) => item.id)])) : selectedQuestionIds.value.filter((id) => !filteredQuestionBank.value.some((item) => item.id === id)); }
function addFilteredQuestions() { filteredQuestionBank.value.filter((item) => selectedQuestionIds.value.includes(item.id)).forEach(addQuestion); }
function resetManageFilters() { questionKeyword.value = ''; questionType.value = ''; manageCreator.value = ''; manageCourse.value = ''; selectedQuestionIds.value = []; }
function typeTone(type: string) { if (type.includes('多')) return 'multiple'; if (type.includes('判断')) return 'judge'; if (type.includes('填空')) return 'blank'; if (type.includes('简答')) return 'essay'; return 'single'; }
async function saveBuilder() {
  saving.value = true;
  try {
    await createAdminPaper(paperCommand('manual'));
    ElMessage.success('试卷已保存');
    backToList();
    await loadPapers();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试卷保存失败');
  } finally {
    saving.value = false;
  }
}
async function saveManage() {
  if (!activePaper.value) return;
  saving.value = true;
  try {
    await updateAdminPaper(activePaper.value.paperId, paperCommand('manage'));
    ElMessage.success('试卷已修改');
    backToList();
    await loadPapers();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试卷修改失败');
  } finally {
    saving.value = false;
  }
}
function openImport() { importVisible.value = true; }
function openPreview(source: 'auto' | 'manual' | 'manage' | 'upload') { if (source !== 'upload') { previewPaper.paperName = source === 'manage' ? manageForm.paperName : builder.paperName || '-'; previewPaper.courseName = source === 'manage' ? manageForm.courseName : builder.courseName || '-'; } importVisible.value = false; previewVisible.value = true; }
async function submitImport() {
  previewVisible.value = false;
  try {
    await createAdminPaper(paperCommand(viewMode.value === 'auto' ? 'auto' : 'manual'));
    ElMessage.success('试卷已提交');
    backToList();
    await loadPapers();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试卷提交失败');
  }
}
async function setEnabled(row: TheoryPaper) {
  try {
    if (row.enabled) {
      await cancelPublishAdminPaper(row.paperId);
    } else {
      await publishAdminPaper(row.paperId);
    }
    ElMessage.success(row.enabled ? '试卷已禁用' : '试卷已启用');
    await loadPapers();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}
async function batchSetEnabled(enabled: boolean) {
  try {
    await Promise.all(selectedIds.value.map((id) => enabled ? publishAdminPaper(id) : cancelPublishAdminPaper(id)));
    selectedIds.value = [];
    ElMessage.success(enabled ? '已批量启用' : '已批量禁用');
    await loadPapers();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批量状态更新失败');
  }
}
async function openLogs(row: TheoryPaper) {
  activePaper.value = row;
  logsVisible.value = true;
  try {
    paperLogs.value = await fetchAdminPaperLogs(row.paperId);
  } catch (error) {
    paperLogs.value = [];
    ElMessage.error(error instanceof Error ? error.message : '操作日志加载失败');
  }
}
function jumpToPage(value?: number) { page.value = Math.min(maxPage.value, Math.max(1, Number(value || 1))); }

onMounted(() => {
  void loadPapers();
});
</script>
