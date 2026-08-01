<template>
  <AdminShell activeKey="theory-paper">
    <section class="admin-theory-paper-page">
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
          <el-button class="admin-theory-paper-primary" @click="openPaperDialog()">新增</el-button>
          <el-button class="admin-theory-paper-primary" @click="openImport">导入试卷</el-button>
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
                <th class="check-col">
                  <el-checkbox :model-value="allSelected" :indeterminate="partSelected" @change="toggleAll" />
                </th>
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
                <td class="check-col">
                  <el-checkbox :model-value="selectedIds.includes(row.paperId)" @change="toggleOne(row.paperId)" />
                </td>
                <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                <td><strong>{{ row.paperName }}</strong></td>
                <td>{{ row.courseName }}</td>
                <td>{{ row.questionCount }}</td>
                <td>{{ row.totalScore }}</td>
                <td>{{ row.creatorName }}</td>
                <td>{{ row.createdAt }}</td>
                <td>
                  <span class="admin-theory-paper-status" :class="row.enabled ? 'enabled' : 'disabled'">
                    <i></i>{{ row.enabled ? '启用' : '禁用' }}
                  </span>
                </td>
                <td>
                  <div class="admin-theory-paper-row-actions">
                    <el-button text @click="openPaperDialog(row)">修改</el-button>
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

    <el-dialog v-model="paperDialogVisible" class="admin-theory-paper-dialog" width="720px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-theory-paper-dialog-head">
          <strong>{{ editingPaper ? '修改试卷' : '新增试卷' }}</strong>
          <el-button text circle :icon="Close" @click="paperDialogVisible = false" />
        </div>
      </template>
      <div class="admin-theory-paper-form">
        <label class="admin-theory-paper-field">
          <span>试卷名称</span>
          <el-input v-model="form.paperName" placeholder="请输入试卷名称" />
        </label>
        <label class="admin-theory-paper-field">
          <span>所属课程</span>
          <el-input v-model="form.courseName" placeholder="请输入所属课程" />
        </label>
        <div class="admin-theory-paper-form-grid">
          <label class="admin-theory-paper-field">
            <span>试题数量</span>
            <el-input-number v-model="form.questionCount" :min="1" :max="200" controls-position="right" />
          </label>
          <label class="admin-theory-paper-field">
            <span>试卷总分</span>
            <el-input-number v-model="form.totalScore" :min="1" :max="200" controls-position="right" />
          </label>
        </div>
      </div>
      <template #footer>
        <div class="admin-theory-paper-dialog-footer">
          <el-button @click="paperDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePaper">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" class="admin-theory-paper-dialog" width="680px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-theory-paper-dialog-head">
          <strong>试卷上传</strong>
          <el-button text circle :icon="Close" @click="importVisible = false" />
        </div>
      </template>
      <div class="admin-theory-paper-upload">
        <p>上传试卷文件后可先预览确认，确认后完成上传。</p>
        <el-upload drag action="#" :auto-upload="false">
          <el-icon><UploadFilled /></el-icon>
          <div class="el-upload__text">拖拽文件到此处，或点击上传</div>
        </el-upload>
      </div>
      <template #footer>
        <div class="admin-theory-paper-dialog-footer">
          <el-button @click="importVisible = false">取消</el-button>
          <el-button type="primary" @click="openPreview">预览确认</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" class="admin-theory-paper-preview-modal" fullscreen :show-close="false" append-to-body>
      <section class="admin-theory-paper-preview-page">
        <header class="admin-theory-paper-preview-head">
          <div>
            <h2>预览试卷</h2>
            <p>预览确认无误后，可提交完成上传</p>
          </div>
          <el-button text circle :icon="Close" @click="previewVisible = false" />
        </header>

        <section class="admin-theory-paper-preview-meta">
          <p><span>试卷名称：</span><strong>{{ previewPaper.paperName }}</strong></p>
          <p><span>所属课程：</span><strong>{{ previewPaper.courseName }}</strong></p>
          <el-button class="admin-theory-paper-primary" @click="submitImport">提交</el-button>
        </section>

        <section v-for="group in previewGroups" :key="group.type" class="admin-theory-paper-preview-card" :class="group.tone">
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

    <el-drawer v-model="logsVisible" class="admin-theory-paper-log-drawer" direction="rtl" size="520px" :with-header="false">
      <div class="admin-theory-paper-dialog-head">
        <strong>操作日志</strong>
        <el-button text circle :icon="Close" @click="logsVisible = false" />
      </div>
      <article v-for="item in logRows" :key="item.time" class="admin-theory-paper-log-row">
        <header><strong>{{ item.action }}</strong><span>{{ item.time }}</span></header>
        <p>{{ item.content }}</p>
      </article>
    </el-drawer>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, UploadFilled } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

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

const pageSize = 12;
const page = ref(1);
const jumpPage = ref(1);
const selectedIds = ref<number[]>([]);
const paperDialogVisible = ref(false);
const importVisible = ref(false);
const previewVisible = ref(false);
const logsVisible = ref(false);
const editingPaper = ref<TheoryPaper | null>(null);

const draft = reactive({
  keyword: '',
  courseName: '',
  creator: '',
  enabled: undefined as boolean | undefined
});
const applied = ref({ ...draft });
const form = reactive({
  paperName: '',
  courseName: '',
  questionCount: 45,
  totalScore: 100
});

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

const previewPaper = reactive({
  paperName: '2025-2026学年期中考试',
  courseName: '铁道概论'
});
const previewGroups = reactive([
  {
    type: '单选题',
    tone: 'single',
    questions: [
      { index: 1, title: '城市轨道交通中，CBTC系统的全称是什么？', score: 2, options: ['A. Communication-Based Train Control', 'B. Centralized Block Traffic Control', 'C. Computer-Based Train Communication', 'D. Continuous Braking Train Control'] },
      { index: 2, title: '列车自动防护子系统（ATP）的主要功能是什么？', score: 2, options: ['A. 列车超速防护和间隔控制', 'B. 列车自动驾驶', 'C. 列车自动监控', 'D. 列车自动调度'] }
    ]
  },
  {
    type: '多选题',
    tone: 'multiple',
    questions: [
      { index: 3, title: '以下哪些属于城市轨道交通信号系统的组成部分？（多选）', score: 3, options: ['A. ATP列车自动防护', 'B. ATO列车自动驾驶', 'C. ATS列车自动监控', 'D. ATC列车自动控制'] }
    ]
  },
  {
    type: '判断题',
    tone: 'judge',
    questions: [{ index: 4, title: 'CBTC系统可以实现列车精确定位和实时追踪。', score: 1, options: [] }]
  },
  {
    type: '填空题',
    tone: 'blank',
    questions: [{ index: 5, title: '城市轨道交通信号机一般设置在______和______位置。', score: 2, options: [] }]
  }
]);

const creatorOptions = computed(() => Array.from(new Set(papers.value.map((item) => item.creatorName))));
const filteredPapers = computed(() =>
  papers.value.filter((item) => {
    const keyword = applied.value.keyword.trim();
    const courseName = applied.value.courseName.trim();
    return (!keyword || item.paperName.includes(keyword)) &&
      (!courseName || item.courseName.includes(courseName)) &&
      (!applied.value.creator || item.creatorName === applied.value.creator) &&
      (applied.value.enabled === undefined || item.enabled === applied.value.enabled);
  })
);
const maxPage = computed(() => Math.max(1, Math.ceil(filteredPapers.value.length / pageSize)));
const pagedPapers = computed(() => filteredPapers.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const allSelected = computed(() => pagedPapers.value.length > 0 && pagedPapers.value.every((item) => selectedIds.value.includes(item.paperId)));
const partSelected = computed(() => selectedIds.value.length > 0 && !allSelected.value);
const pageStart = computed(() => (filteredPapers.value.length === 0 ? 0 : (page.value - 1) * pageSize + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize, filteredPapers.value.length));
const logRows = computed(() => [
  { action: '修改试卷', time: '2025-01-15 14:30:22', content: `${editingPaper.value?.paperName || '试卷'} 信息更新` },
  { action: '启用状态变更', time: '2025-01-14 09:15:08', content: '管理员调整启用状态' }
]);

watch(page, (value) => {
  jumpPage.value = value;
});

function applyFilters() {
  applied.value = { ...draft };
  page.value = 1;
  selectedIds.value = [];
}

function resetFilters() {
  Object.assign(draft, { keyword: '', courseName: '', creator: '', enabled: undefined });
  applyFilters();
}

function toggleOne(id: number) {
  selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter((item) => item !== id) : [...selectedIds.value, id];
}

function toggleAll(value: string | number | boolean) {
  selectedIds.value = value
    ? Array.from(new Set([...selectedIds.value, ...pagedPapers.value.map((item) => item.paperId)]))
    : selectedIds.value.filter((id) => !pagedPapers.value.some((item) => item.paperId === id));
}

function openPaperDialog(row?: TheoryPaper) {
  editingPaper.value = row || null;
  Object.assign(form, row ? row : { paperName: '', courseName: '', questionCount: 45, totalScore: 100 });
  paperDialogVisible.value = true;
}

function savePaper() {
  if (!form.paperName.trim() || !form.courseName.trim()) {
    ElMessage.warning('请完善试卷名称和所属课程');
    return;
  }
  if (editingPaper.value) {
    Object.assign(editingPaper.value, form);
    ElMessage.success('试卷已修改');
  } else {
    papers.value.unshift({
      paperId: Date.now(),
      paperName: form.paperName,
      courseName: form.courseName,
      questionCount: Number(form.questionCount),
      totalScore: Number(form.totalScore),
      creatorName: '张建国',
      createdAt: '2025-01-15 14:30:22',
      enabled: true
    });
    ElMessage.success('试卷已新增');
  }
  paperDialogVisible.value = false;
}

function setEnabled(row: TheoryPaper) {
  row.enabled = !row.enabled;
}

function batchSetEnabled(enabled: boolean) {
  papers.value.forEach((item) => {
    if (selectedIds.value.includes(item.paperId)) {
      item.enabled = enabled;
    }
  });
  selectedIds.value = [];
  ElMessage.success(enabled ? '已批量启用' : '已批量禁用');
}

function openImport() {
  importVisible.value = true;
}

function openPreview() {
  importVisible.value = false;
  previewVisible.value = true;
}

function submitImport() {
  previewVisible.value = false;
  ElMessage.success('试卷已提交');
}

function openLogs(row: TheoryPaper) {
  editingPaper.value = row;
  logsVisible.value = true;
}

function jumpToPage(value?: number) {
  page.value = Math.min(maxPage.value, Math.max(1, Number(value || 1)));
}
</script>
