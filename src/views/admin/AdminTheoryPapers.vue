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
            <el-select v-model="draft.creator" placeholder="请选择添加人" clearable filterable>
              <el-option v-for="item in creatorOptions" :key="item" :label="item" :value="item" />
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
          <el-dropdown trigger="click" @command="openCreate">
            <el-button class="admin-theory-paper-primary" :icon="Plus">新增</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="auto">自动组卷</el-dropdown-item>
                <el-dropdown-item command="manual">手动组卷</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button class="admin-theory-paper-primary" :icon="UploadFilled" @click="openImport">导入试卷</el-button>
          <el-button class="admin-theory-paper-lite" :disabled="selectedIds.length === 0" @click="batchSetEnabled(true)">批量启用</el-button>
          <el-button class="admin-theory-paper-lite" :disabled="selectedIds.length === 0" @click="batchSetEnabled(false)">批量禁用</el-button>
        </div>
        <p>共 <b>{{ filteredPapers.length }}</b> 条记录</p>
      </section>

      <section class="admin-theory-paper-board">
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
              <tr v-for="(row, index) in pagedPapers" :key="row.paperId">
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
          <p>显示 <b>{{ pageStart }}</b> 到 <b>{{ pageEnd }}</b> 条，共 <b>{{ filteredPapers.length }}</b> 条记录</p>
          <div class="admin-theory-paper-pager">
            <el-pagination v-model:current-page="page" :page-size="pageSize" :total="filteredPapers.length" layout="prev, pager, next" background />
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
        <div class="admin-theory-paper-builder-grid">
          <label class="admin-theory-paper-field"><span>试卷名称 <b>*</b></span><el-input v-model="builder.paperName" placeholder="请输入试卷名称" /></label>
          <label class="admin-theory-paper-field"><span>所属课程 <b>*</b></span><el-input v-model="builder.courseName" placeholder="请输入所属课程" /></label>
          <label class="admin-theory-paper-field"><span>试卷总分 <b>*</b></span><el-input-number v-model="builder.totalScore" :min="1" :max="200" controls-position="right" /></label>
          <label class="admin-theory-paper-field"><span>及格分 <b>*</b></span><el-input-number v-model="builder.passScore" :min="1" :max="200" controls-position="right" /></label>
        </div>
      </section>
      <section class="admin-theory-paper-builder-card">
        <header><strong>题型配置</strong><el-button class="admin-theory-paper-lite" @click="addRule">新增题型</el-button></header>
        <table class="admin-theory-paper-rule-table">
          <thead><tr><th>题型</th><th>试题数量</th><th>每题分值</th><th>难度</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="(rule, index) in builder.rules" :key="rule.type">
              <td><el-select v-model="rule.type"><el-option v-for="item in questionTypeOptions" :key="item" :label="item" :value="item" /></el-select></td>
              <td><el-input-number v-model="rule.count" :min="1" :max="100" controls-position="right" /></td>
              <td><el-input-number v-model="rule.score" :min="1" :max="20" controls-position="right" /></td>
              <td><el-select v-model="rule.difficulty"><el-option label="全部" value="全部" /><el-option label="基础" value="基础" /><el-option label="提高" value="提高" /></el-select></td>
              <td><el-button text class="warn" :disabled="builder.rules.length <= 1" @click="removeRule(index)">删除</el-button></td>
            </tr>
          </tbody>
        </table>
      </section>
      <BuilderFooter @cancel="backToList" @preview="openPreview('auto')" @save="saveBuilder" />
    </section>

    <section v-else-if="viewMode === 'manual'" class="admin-theory-paper-builder-page">
      <BuilderHeader title="新增试卷" subtitle="手动组卷" @back="backToList" />
      <section class="admin-theory-paper-builder-card">
        <header><strong>基础信息</strong></header>
        <div class="admin-theory-paper-builder-grid">
          <label class="admin-theory-paper-field"><span>试卷名称 <b>*</b></span><el-input v-model="builder.paperName" placeholder="请输入试卷名称" /></label>
          <label class="admin-theory-paper-field"><span>所属课程 <b>*</b></span><el-input v-model="builder.courseName" placeholder="请输入所属课程" /></label>
          <label class="admin-theory-paper-field"><span>试卷总分</span><el-input-number v-model="selectedScore" disabled controls-position="right" /></label>
          <label class="admin-theory-paper-field"><span>试题数量</span><el-input-number v-model="selectedQuestions.length" disabled controls-position="right" /></label>
        </div>
      </section>
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
        <header><strong>试题列表</strong><el-button class="admin-theory-paper-lite" @click="viewMode = 'manual'">添加试题</el-button></header>
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
          <div><strong>上传试卷</strong><p>请填写试卷信息并上传EXCEL文件</p></div>
          <el-button text circle :icon="Close" @click="importVisible = false" />
        </div>
      </template>
      <div class="admin-theory-paper-upload-body">
        <label><span>试卷模板</span><el-button class="admin-theory-paper-template-button">点击下载试卷上传模板</el-button></label>
        <label><span>试卷内容 <b>*</b></span><el-upload drag action="#" :auto-upload="false"><el-icon><UploadFilled /></el-icon><div class="el-upload__text">点击或拖拽上传资源文件</div><template #tip><p>仅支持.excel 格式，大小不超过 200MB</p></template></el-upload></label>
        <label><span>试卷名称 <b>*</b></span><el-input v-model="previewPaper.paperName" maxlength="30" show-word-limit placeholder="请输入试卷名称" /></label>
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
          <p><span>所属课程：</span><strong>{{ previewPaper.courseName }}</strong></p>
          <el-button class="admin-theory-paper-primary" @click="submitImport">提交</el-button>
        </section>
        <section v-for="group in previewGroups" :key="group.type" class="admin-theory-paper-preview-card" :class="group.tone">
          <header><strong>{{ group.type }}</strong><span>{{ group.questions.length }}题</span><el-button text>批量修改分值</el-button></header>
          <article v-for="question in group.questions" :key="question.index">
            <div><h3>{{ question.index }}、{{ question.title }}</h3><ol v-if="question.options.length"><li v-for="option in question.options" :key="option">{{ option }}</li></ol></div>
            <label><span>分值</span><el-input-number v-model="question.score" :min="1" :max="20" controls-position="right" /></label>
          </article>
        </section>
      </section>
    </el-dialog>

    <el-drawer v-model="logsVisible" class="admin-theory-paper-log-drawer" direction="rtl" size="520px" :with-header="false">
      <div class="admin-theory-paper-dialog-head"><strong>操作日志</strong><el-button text circle :icon="Close" @click="logsVisible = false" /></div>
      <article v-for="item in logRows" :key="item.time" class="admin-theory-paper-log-row"><header><strong>{{ item.action }}</strong><span>{{ item.time }}</span></header><p>{{ item.content }}</p></article>
    </el-drawer>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Close, Plus, UploadFilled } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

type ViewMode = 'list' | 'auto' | 'manual' | 'manage';
type CreateMode = 'auto' | 'manual';

interface TheoryPaper {
  paperId: number;
  paperName: string;
  courseName: string;
  questionCount: number;
  totalScore: number;
  creatorName: string;
  createdAt: string;
  enabled: boolean;
}

interface QuestionItem {
  id: number;
  title: string;
  type: string;
  score: number;
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
const jumpPage = ref(1);
const selectedIds = ref<number[]>([]);
const importVisible = ref(false);
const previewVisible = ref(false);
const logsVisible = ref(false);
const activePaper = ref<TheoryPaper | null>(null);
const questionKeyword = ref('');
const questionType = ref('');

const draft = reactive({ keyword: '', courseName: '', creator: '', enabled: undefined as boolean | undefined });
const applied = ref({ ...draft });
const builder = reactive({
  paperName: '',
  courseName: '铁道概论',
  totalScore: 100,
  passScore: 60,
  rules: [
    { type: '单选题', count: 20, score: 2, difficulty: '全部' },
    { type: '多选题', count: 10, score: 3, difficulty: '全部' },
    { type: '判断题', count: 10, score: 1, difficulty: '基础' }
  ]
});
const manageForm = reactive({ paperName: '', courseName: '' });
const previewPaper = reactive({ paperName: '2025-2026学年期中考试', courseName: '铁道概论' });

const papers = ref<TheoryPaper[]>([
  { paperId: 1, paperName: '城市轨道交通行车组织基础理论考核', courseName: '城轨信号系统', questionCount: 50, totalScore: 95, creatorName: '张建国', createdAt: '2025-01-15 14:30:22', enabled: true },
  { paperId: 2, paperName: '城市轨道交通信号系统原理与故障处理', courseName: '城轨信号系统', questionCount: 45, totalScore: 90, creatorName: '李明辉', createdAt: '2025-01-14 09:15:08', enabled: true },
  { paperId: 3, paperName: '城轨车辆构造与检修技术综合测试', courseName: '城轨信号系统', questionCount: 60, totalScore: 100, creatorName: '王思远', createdAt: '2025-01-13 16:42:35', enabled: false },
  { paperId: 4, paperName: '城市轨道交通运营安全与应急管理考核', courseName: '城轨信号系统', questionCount: 40, totalScore: 80, creatorName: '赵志强', createdAt: '2025-01-12 10:20:47', enabled: true },
  { paperId: 5, paperName: '城轨通信系统维护与故障诊断专项测试', courseName: '城轨信号系统', questionCount: 35, totalScore: 75, creatorName: '张建国', createdAt: '2025-01-11 08:55:13', enabled: true },
  { paperId: 6, paperName: '城市轨道交通客运服务规范与礼仪考核', courseName: '城轨信号系统', questionCount: 55, totalScore: 100, creatorName: '李明辉', createdAt: '2025-01-10 14:12:30', enabled: false },
  { paperId: 7, paperName: '城轨供电系统运行与维护综合测评', courseName: '城轨信号系统', questionCount: 48, totalScore: 92, creatorName: '王思远', createdAt: '2025-01-09 11:38:55', enabled: true },
  { paperId: 8, paperName: '城市轨道交通线路与站场设计基础知识', courseName: '城轨信号系统', questionCount: 42, totalScore: 85, creatorName: '赵志强', createdAt: '2025-01-08 15:05:42', enabled: true },
  { paperId: 9, paperName: '城轨自动售检票系统（AFC）操作与维护', courseName: '城轨信号系统', questionCount: 38, totalScore: 78, creatorName: '张建国', createdAt: '2025-01-07 09:22:18', enabled: true },
  { paperId: 10, paperName: '城市轨道交通调度指挥与应急处置综合卷', courseName: '城轨信号系统', questionCount: 52, totalScore: 98, creatorName: '李明辉', createdAt: '2025-01-06 13:48:09', enabled: false },
  { paperId: 11, paperName: '城轨环境控制系统与通风空调技术考核', courseName: '城轨信号系统', questionCount: 30, totalScore: 70, creatorName: '王思远', createdAt: '2025-01-05 10:15:33', enabled: true },
  { paperId: 12, paperName: '城市轨道交通法规与标准知识测试', courseName: '城轨信号系统', questionCount: 46, totalScore: 88, creatorName: '赵志强', createdAt: '2025-01-04 16:30:51', enabled: true }
]);
const questionBank = ref<QuestionItem[]>([
  { id: 1, type: '单选题', score: 2, title: '城市轨道交通中，CBTC系统的全称是什么？' },
  { id: 2, type: '单选题', score: 2, title: '列车自动防护子系统（ATP）的主要功能是什么？' },
  { id: 3, type: '多选题', score: 3, title: '以下哪些属于城市轨道交通信号系统的组成部分？' },
  { id: 4, type: '判断题', score: 1, title: 'CBTC系统可以实现列车精确定位和实时追踪。' },
  { id: 5, type: '填空题', score: 2, title: '城市轨道交通信号机一般设置在______和______位置。' }
]);
const selectedQuestions = ref<QuestionItem[]>(questionBank.value.slice(0, 5).map((item) => ({ ...item })));
const previewGroups = reactive([
  { type: '单选题', tone: 'single', questions: [
    { index: 1, title: '城市轨道交通中，CBTC系统的全称是什么？', score: 2, options: ['A. Communication-Based Train Control', 'B. Centralized Block Traffic Control', 'C. Computer-Based Train Communication', 'D. Continuous Braking Train Control'] },
    { index: 2, title: '列车自动防护子系统（ATP）的主要功能是什么？', score: 2, options: ['A. 列车超速防护和间隔控制', 'B. 列车自动驾驶', 'C. 列车自动监控', 'D. 列车自动调度'] }
  ] },
  { type: '多选题', tone: 'multiple', questions: [{ index: 3, title: '以下哪些属于城市轨道交通信号系统的组成部分？（多选）', score: 3, options: ['A. ATP列车自动防护', 'B. ATO列车自动驾驶', 'C. ATS列车自动监控', 'D. ATC列车自动控制'] }] },
  { type: '判断题', tone: 'judge', questions: [{ index: 4, title: 'CBTC系统可以实现列车精确定位和实时追踪。', score: 1, options: [] }] },
  { type: '填空题', tone: 'blank', questions: [{ index: 5, title: '城市轨道交通信号机一般设置在______和______位置。', score: 2, options: [] }] }
]);

const creatorOptions = computed(() => Array.from(new Set(papers.value.map((item) => item.creatorName))));
const filteredPapers = computed(() => papers.value.filter((item) => (!applied.value.keyword || item.paperName.includes(applied.value.keyword.trim())) && (!applied.value.courseName || item.courseName.includes(applied.value.courseName.trim())) && (!applied.value.creator || item.creatorName === applied.value.creator) && (applied.value.enabled === undefined || item.enabled === applied.value.enabled)));
const maxPage = computed(() => Math.max(1, Math.ceil(filteredPapers.value.length / pageSize)));
const pagedPapers = computed(() => filteredPapers.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const allSelected = computed(() => pagedPapers.value.length > 0 && pagedPapers.value.every((item) => selectedIds.value.includes(item.paperId)));
const partSelected = computed(() => selectedIds.value.length > 0 && !allSelected.value);
const pageStart = computed(() => (filteredPapers.value.length === 0 ? 0 : (page.value - 1) * pageSize + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize, filteredPapers.value.length));
const selectedScore = computed(() => selectedQuestions.value.reduce((sum, item) => sum + Number(item.score || 0), 0));
const filteredQuestionBank = computed(() => questionBank.value.filter((item) => (!questionKeyword.value || item.title.includes(questionKeyword.value)) && (!questionType.value || item.type === questionType.value)));
const logRows = computed(() => [
  { action: '修改试卷', time: '2025-01-15 14:30:22', content: `${activePaper.value?.paperName || '试卷'} 信息更新` },
  { action: '启用状态变更', time: '2025-01-14 09:15:08', content: '管理员调整启用状态' }
]);

watch(page, (value) => { jumpPage.value = value; });

function applyFilters() { applied.value = { ...draft }; page.value = 1; selectedIds.value = []; }
function resetFilters() { Object.assign(draft, { keyword: '', courseName: '', creator: '', enabled: undefined }); applyFilters(); }
function toggleOne(id: number) { selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter((item) => item !== id) : [...selectedIds.value, id]; }
function toggleAll(value: string | number | boolean) { selectedIds.value = value ? Array.from(new Set([...selectedIds.value, ...pagedPapers.value.map((item) => item.paperId)])) : selectedIds.value.filter((id) => !pagedPapers.value.some((item) => item.paperId === id)); }
function openCreate(command: CreateMode) { resetBuilder(); viewMode.value = command; }
function openManage(row: TheoryPaper) { activePaper.value = row; Object.assign(manageForm, { paperName: row.paperName, courseName: row.courseName }); previewPaper.paperName = row.paperName; previewPaper.courseName = row.courseName; selectedQuestions.value = questionBank.value.slice(0, 5).map((item) => ({ ...item })); viewMode.value = 'manage'; }
function backToList() { viewMode.value = 'list'; }
function resetBuilder() { Object.assign(builder, { paperName: '', courseName: '铁道概论', totalScore: 100, passScore: 60 }); }
function addRule() { builder.rules.push({ type: '简答题', count: 1, score: 10, difficulty: '全部' }); }
function removeRule(index: number) { builder.rules.splice(index, 1); }
function addQuestion(item: QuestionItem) { if (!selectedQuestions.value.some((question) => question.id === item.id)) selectedQuestions.value.push({ ...item }); }
function removeQuestion(id: number) { selectedQuestions.value = selectedQuestions.value.filter((item) => item.id !== id); }
function saveBuilder() { ElMessage.success('试卷已保存'); backToList(); }
function saveManage() { if (activePaper.value) Object.assign(activePaper.value, { paperName: manageForm.paperName, courseName: manageForm.courseName, questionCount: selectedQuestions.value.length, totalScore: selectedScore.value }); ElMessage.success('试卷已修改'); backToList(); }
function openImport() { importVisible.value = true; }
function openPreview(source: 'auto' | 'manual' | 'manage' | 'upload') { if (source !== 'upload') { previewPaper.paperName = source === 'manage' ? manageForm.paperName : builder.paperName || '2025-2026学年期中考试'; previewPaper.courseName = source === 'manage' ? manageForm.courseName : builder.courseName || '铁道概论'; } importVisible.value = false; previewVisible.value = true; }
function submitImport() { previewVisible.value = false; ElMessage.success('试卷已提交'); }
function setEnabled(row: TheoryPaper) { row.enabled = !row.enabled; }
function batchSetEnabled(enabled: boolean) { papers.value.forEach((item) => { if (selectedIds.value.includes(item.paperId)) item.enabled = enabled; }); selectedIds.value = []; ElMessage.success(enabled ? '已批量启用' : '已批量禁用'); }
function openLogs(row: TheoryPaper) { activePaper.value = row; logsVisible.value = true; }
function jumpToPage(value?: number) { page.value = Math.min(maxPage.value, Math.max(1, Number(value || 1))); }
</script>
