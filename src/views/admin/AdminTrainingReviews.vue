<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-course-reviews-page admin-training-reviews-page">
      <header class="admin-course-reviews-topbar">
        <div class="admin-course-reviews-left">
          <el-button class="admin-course-reviews-back" :icon="ArrowLeft" @click="goBack" />
          <el-breadcrumb class="admin-course-reviews-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>实训组课</el-breadcrumb-item>
            <el-breadcrumb-item>阅卷</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ trainingTitle }} - 阅卷</h1>
        <span></span>
      </header>

      <section class="admin-course-reviews-filter-card">
        <div class="admin-course-reviews-filter-row">
          <label class="admin-course-reviews-field">
            <span>学员姓名</span>
            <el-input v-model="filters.studentName" placeholder="请输入学员姓名" clearable />
          </label>
          <label class="admin-course-reviews-field">
            <span>学员学号</span>
            <el-input v-model="filters.studentNo" placeholder="请输入学员学号" clearable />
          </label>
          <label class="admin-course-reviews-field">
            <span>所属班级</span>
            <el-input v-model="filters.className" placeholder="请输入所属班级" clearable />
          </label>
          <label class="admin-course-reviews-field assignment">
            <span>实训任务</span>
            <el-input v-model="filters.taskName" placeholder="请输入实训任务名称" clearable />
          </label>
          <div class="admin-course-reviews-buttons">
            <el-button type="primary" class="admin-course-reviews-query" @click="applyFilters">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button class="admin-course-reviews-reset" @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </section>

      <section class="admin-course-reviews-table-card">
        <header class="admin-course-reviews-table-head">
          <div>
            <el-icon><Tickets /></el-icon>
            <strong>实训课批阅</strong>
          </div>
        </header>

        <div class="admin-course-reviews-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="{ active: activeTab === tab.key }"
            @click="setTab(tab.key)"
          >
            <span>{{ tab.label }}</span>
            <b :class="tab.tone">{{ tab.count }}</b>
          </button>
        </div>

        <div v-if="loading" class="admin-course-empty">阅卷列表加载中...</div>
        <div v-else-if="pagedRows.length === 0" class="admin-course-empty">
          <el-empty description="暂无阅卷记录" />
        </div>
        <div v-else class="admin-course-reviews-table-scroll">
          <table class="admin-course-reviews-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>学员姓名</th>
                <th>学号</th>
                <th>所属班级</th>
                <th>实训任务</th>
                <th>是否提交</th>
                <th>最后一次提交时间</th>
                <th>是否批阅</th>
                <th>实训得分</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in pagedRows" :key="item.id">
                <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                <td class="admin-course-reviews-name">{{ item.studentName }}</td>
                <td>{{ item.studentNo }}</td>
                <td>{{ item.className }}</td>
                <td>{{ item.taskName }}</td>
                <td>
                  <span class="admin-course-reviews-tag" :class="item.submitted ? 'submitted' : 'not-submitted'">
                    {{ item.submitted ? '已提交' : '未提交' }}
                  </span>
                </td>
                <td>{{ item.submittedAt || '-' }}</td>
                <td>
                  <span v-if="item.submitted" class="admin-course-reviews-tag" :class="item.reviewed ? 'reviewed' : 'pending'">
                    {{ item.reviewed ? '已批阅' : '未批阅' }}
                  </span>
                  <span v-else>-</span>
                </td>
                <td>{{ item.score ?? '-' }}</td>
                <td>
                  <span v-if="!item.submitted" class="admin-course-reviews-none">-</span>
                  <el-button
                    v-else-if="item.reviewed"
                    class="admin-course-reviews-action view"
                    @click="openReview(item)"
                  >
                    <el-icon><View /></el-icon>
                    查看批阅
                  </el-button>
                  <el-button v-else class="admin-course-reviews-action edit" @click="openReview(item)">
                    <el-icon><EditPen /></el-icon>
                    批阅
                  </el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <footer class="admin-course-reviews-pagination">
          <p>显示 {{ pageStart }} 到 {{ pageEnd }} 条，共 {{ total }} 条记录</p>
          <div class="admin-course-reviews-pager">
            <el-button :icon="DArrowLeft" :disabled="page === 1" @click="goToPage(1)" />
            <el-button :icon="ArrowLeft" :disabled="page === 1" @click="goToPage(Math.max(1, page - 1))" />
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="pager"
              background
              @current-change="goToPage"
            />
            <el-button :icon="ArrowRight" :disabled="page === pageCount" @click="goToPage(Math.min(pageCount, page + 1))" />
            <el-button :icon="DArrowRight" :disabled="page === pageCount" @click="goToPage(pageCount)" />
            <span>每页</span>
            <el-select v-model="pageSize" class="admin-course-reviews-size">
              <el-option :label="10" :value="10" />
              <el-option :label="20" :value="20" />
            </el-select>
            <span>条</span>
          </div>
        </footer>
      </section>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowLeft,
  ArrowRight,
  DArrowLeft,
  DArrowRight,
  EditPen,
  Refresh,
  Search,
  Tickets,
  View
} from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { fetchAdminTraining } from '../../api/admin-training';

type ReviewTabKey = 'all' | 'pending' | 'reviewed' | 'notSubmitted';

interface TrainingReviewRow {
  id: number;
  studentName: string;
  studentNo: string;
  className: string;
  taskName: string;
  submitted: boolean;
  submittedAt?: string;
  reviewed: boolean;
  score?: number;
}

const route = useRoute();
const router = useRouter();
const trainingId = computed(() => Number(route.params.id));
const trainingTitle = ref(String(route.query.title || '实训组课'));
const page = ref(1);
const pageSize = ref(10);
const activeTab = ref<ReviewTabKey>('all');
const loading = ref(false);
const filters = reactive({
  studentName: '',
  studentNo: '',
  className: '',
  taskName: ''
});
const rows = ref<TrainingReviewRow[]>([]);

const matchedRows = computed(() =>
  rows.value.filter((item) => {
    const keywordMatched =
      (!filters.studentName || item.studentName.includes(filters.studentName.trim())) &&
      (!filters.studentNo || item.studentNo.includes(filters.studentNo.trim())) &&
      (!filters.className || item.className.includes(filters.className.trim())) &&
      (!filters.taskName || item.taskName.includes(filters.taskName.trim()));
    if (!keywordMatched) {
      return false;
    }
    if (activeTab.value === 'pending') {
      return item.submitted && !item.reviewed;
    }
    if (activeTab.value === 'reviewed') {
      return item.submitted && item.reviewed;
    }
    if (activeTab.value === 'notSubmitted') {
      return !item.submitted;
    }
    return true;
  })
);

const filterBaseRows = computed(() =>
  rows.value.filter((item) => {
    return (
      (!filters.studentName || item.studentName.includes(filters.studentName.trim())) &&
      (!filters.studentNo || item.studentNo.includes(filters.studentNo.trim())) &&
      (!filters.className || item.className.includes(filters.className.trim())) &&
      (!filters.taskName || item.taskName.includes(filters.taskName.trim()))
    );
  })
);

const tabs = computed(() => [
  { key: 'all' as const, label: '全部', count: filterBaseRows.value.length, tone: 'all' },
  {
    key: 'pending' as const,
    label: '待批阅',
    count: filterBaseRows.value.filter((item) => item.submitted && !item.reviewed).length,
    tone: 'pending'
  },
  {
    key: 'reviewed' as const,
    label: '已批阅',
    count: filterBaseRows.value.filter((item) => item.submitted && item.reviewed).length,
    tone: 'reviewed'
  },
  {
    key: 'notSubmitted' as const,
    label: '未提交',
    count: filterBaseRows.value.filter((item) => !item.submitted).length,
    tone: 'notSubmitted'
  }
]);

const total = computed(() => matchedRows.value.length);
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));
const pagedRows = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return matchedRows.value.slice(start, start + pageSize.value);
});
const pageStart = computed(() => (total.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize.value, total.value));

function goBack() {
  router.push('/admin/training');
}

function setTab(tab: ReviewTabKey) {
  activeTab.value = tab;
  page.value = 1;
}

function applyFilters() {
  page.value = 1;
}

function resetFilters() {
  filters.studentName = '';
  filters.studentNo = '';
  filters.className = '';
  filters.taskName = '';
  page.value = 1;
}

function goToPage(nextPage: number) {
  page.value = Math.min(Math.max(1, nextPage), pageCount.value);
}

function openReview(row: TrainingReviewRow) {
  ElMessage.info(`正在打开${row.studentName}的实训批阅记录`);
}

async function loadTrainingTitle() {
  if (!trainingId.value) {
    return;
  }
  try {
    const detail = await fetchAdminTraining(trainingId.value);
    trainingTitle.value = detail.trainingName || trainingTitle.value;
  } catch {
    return;
  }
}

onMounted(() => {
  loading.value = false;
  void loadTrainingTitle();
});
</script>

<style scoped>
.admin-training-reviews-page {
  min-width: 0;
}

.admin-training-reviews-page .admin-course-reviews-table-card {
  overflow: hidden;
}

@media (max-width: 980px) {
  .admin-training-reviews-page .admin-course-reviews-topbar {
    grid-template-columns: 1fr;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 0;
  }

  .admin-training-reviews-page .admin-course-reviews-topbar h1 {
    order: -1;
    text-align: left;
  }

  .admin-training-reviews-page .admin-course-reviews-filter-card {
    padding-top: 20px;
  }

  .admin-training-reviews-page .admin-course-reviews-field {
    max-width: none;
  }

  .admin-training-reviews-page .admin-course-reviews-buttons {
    width: 100%;
  }
}
</style>
