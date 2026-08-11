<template>
  <AdminShell activeKey="public-application">
    <section v-if="detailPageVisible && selectedApplication" class="admin-public-review-page">
      <header class="admin-public-review-top">
        <button type="button" class="admin-public-review-back" @click="closeDetailPage">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>资源管理</el-breadcrumb-item>
          <el-breadcrumb-item>审核列表</el-breadcrumb-item>
          <el-breadcrumb-item>{{ detailMode === 'review' ? '审核详情' : '查看审核详情' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </header>

      <section class="admin-public-review-info-card">
        <div class="admin-public-review-card-title">
          <span><el-icon><Document /></el-icon></span>
          <strong>资源审核详情</strong>
        </div>
        <dl>
          <div><dt>资源名称</dt><dd>{{ selectedApplication.resourceName }}</dd></div>
          <div><dt>资源类型</dt><dd><span class="admin-public-type-pill" :class="selectedApplication.typeTone">{{ selectedApplication.typeLabel }}</span></dd></div>
          <div><dt>所属专业</dt><dd>{{ selectedApplication.majorLabel }}</dd></div>
          <div><dt>所属课程</dt><dd>{{ selectedApplication.courseName || '-' }}</dd></div>
          <div><dt>申请人</dt><dd>{{ selectedApplication.applicantName || '-' }}</dd></div>
          <div><dt>申请时间</dt><dd>{{ selectedApplication.appliedAtLabel }}</dd></div>
        </dl>
      </section>

      <div class="admin-public-review-grid">
        <section class="admin-public-review-preview-card">
          <div class="admin-public-review-card-title">
            <span><el-icon><Document /></el-icon></span>
            <strong>资源内容预览</strong>
          </div>
          <div class="admin-public-review-doc">
            <template v-if="selectedApplication && resourcePreviewSource(selectedApplication)">
              <img
                v-if="resourcePreviewKind(selectedApplication) === 'image'"
                class="admin-resource-preview-media image"
                :src="resourcePreviewSource(selectedApplication)"
                :alt="selectedApplication.resourceName"
              />
              <video
                v-else-if="resourcePreviewKind(selectedApplication) === 'video'"
                class="admin-resource-preview-media"
                :src="resourcePreviewSource(selectedApplication)"
                controls
              />
              <audio
                v-else-if="resourcePreviewKind(selectedApplication) === 'audio'"
                class="admin-resource-preview-audio"
                :src="resourcePreviewSource(selectedApplication)"
                controls
              />
              <iframe
                v-else-if="resourcePreviewKind(selectedApplication) === 'frame'"
                class="admin-resource-preview-frame"
                :src="resourcePreviewSource(selectedApplication)"
                :title="selectedApplication.resourceName"
              />
              <div v-else class="admin-resource-preview-unsupported">
                <span class="admin-resource-file-icon">
                  <el-icon><Document /></el-icon>
                </span>
                <strong>{{ selectedApplication.fileName || selectedApplication.resourceName }}</strong>
                <p>当前文件类型不支持在线预览，请下载后查看。</p>
              </div>
            </template>
            <el-empty v-else description="暂无可预览内容" />
          </div>
        </section>

        <aside class="admin-public-review-progress-card">
          <div class="admin-public-review-card-title">
            <span><el-icon><Refresh /></el-icon></span>
            <strong>审核进度</strong>
          </div>
          <div class="admin-public-review-timeline">
            <article v-for="item in reviewTimeline" :key="item.title">
              <i></i>
              <div>
                <header>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.date }}</span>
                </header>
                <p>操作人： <b>{{ item.operator }}</b></p>
                <p>操作时间： <b>{{ item.time }}</b></p>
                <em v-if="item.status">{{ item.status }}</em>
                <small v-if="item.comment">{{ item.comment }}</small>
              </div>
            </article>
          </div>
        </aside>
      </div>

      <section v-if="detailMode === 'review'" class="admin-public-review-action-card">
        <div class="admin-public-review-card-title">
          <span><el-icon><Refresh /></el-icon></span>
          <strong>审核操作</strong>
        </div>
        <label>
          <span>审核意见</span>
          <el-input
            v-model="reviewComment"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="请输入审核意见，包括审核结论、修改建议等..."
          />
          <small>若驳回，则必须要填写审核意见</small>
        </label>
        <footer>
          <el-button class="admin-public-review-reject" :loading="saving" @click="submitDetailReview('REJECTED')">驳回</el-button>
          <el-button class="admin-public-review-approve" type="primary" :loading="saving" @click="submitDetailReview('APPROVED')">审核通过</el-button>
        </footer>
      </section>
    </section>

    <section v-else class="admin-public-page">
      <el-breadcrumb class="admin-public-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>资源公开申请</el-breadcrumb-item>
      </el-breadcrumb>

      <header class="admin-public-head">
        <div>
          <h1>资源公开申请</h1>
          <p>统一查看资源申请、审核意见和公开状态，支持快速通过或驳回。</p>
        </div>

        <div class="admin-public-summary">
          <div class="admin-public-summary-card pending">
            <span>待审核</span>
            <strong>{{ pendingCount }}</strong>
          </div>
          <div class="admin-public-summary-card approved">
            <span>已通过</span>
            <strong>{{ approvedCount }}</strong>
          </div>
          <div class="admin-public-summary-card rejected">
            <span>已驳回</span>
            <strong>{{ rejectedCount }}</strong>
          </div>
          <div class="admin-public-summary-card total">
            <span>总申请</span>
            <strong>{{ totalCount }}</strong>
          </div>
        </div>
      </header>

      <section class="admin-public-filter-card">
        <div class="admin-public-filter-row">
          <el-input
            v-model="draft.keyword"
            class="admin-public-search"
            :prefix-icon="Search"
            placeholder="搜索资源名称"
            clearable
            @keyup.enter="applyFilters"
          />
          <el-select v-model="draft.publicStatus" class="admin-public-select" placeholder="审核状态：全部" clearable>
            <el-option v-for="item in publicStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button class="admin-public-query-button" @click="applyFilters">查询</el-button>
          <el-button class="admin-public-reset-button" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <div class="admin-public-workspace" :class="{ 'has-detail': Boolean(selectedApplication) }">
        <section class="admin-public-board">
          <header class="admin-public-board-head">
            <div>
              <strong>申请列表</strong>
              <p>共 {{ totalCount }} 条申请，当前待审 {{ pendingCount }} 条，已选 {{ selectedIds.length }} 条</p>
            </div>
            <div class="admin-public-board-actions">
              <el-button class="admin-public-lite-button" :disabled="selectedIds.length === 0" @click="openBatchReview('APPROVED')">
                批量通过
              </el-button>
              <el-button class="admin-public-lite-button" :disabled="selectedIds.length === 0" @click="openBatchReview('REJECTED')">
                批量驳回
              </el-button>
              <el-button class="admin-public-lite-button" @click="refreshList">刷新</el-button>
            </div>
          </header>

          <div v-if="loading" class="admin-public-empty">申请加载中...</div>
          <div v-else-if="pagedApplications.length === 0" class="admin-public-empty">
            <el-empty description="暂无匹配申请" />
          </div>
          <template v-else>
            <div class="admin-public-table-scroll">
              <table class="admin-public-table">
                <thead>
                  <tr>
                    <th class="check-col">
                      <el-checkbox :model-value="allCurrentSelected" :indeterminate="partCurrentSelected" @change="toggleAllCurrent" />
                    </th>
                    <th>资源名称</th>
                    <th>资源类型</th>
                    <th>所属课程</th>
                    <th>所属专业</th>
                    <th>申请人</th>
                    <th>申请时间</th>
                    <th>审核状态</th>
                    <th>审核人</th>
                    <th>审核时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="row in pagedApplications"
                    :key="row.applicationId"
                    :class="{ selected: selectedApplication?.applicationId === row.applicationId }"
                    @click="selectApplication(row)"
                  >
                    <td class="check-col">
                      <el-checkbox :model-value="selectedIds.includes(row.applicationId)" @change="toggleOne(row.applicationId)" />
                    </td>
                    <td>
                      <div class="admin-public-name-cell">
                        <img :src="row.coverResolved" :alt="row.resourceName" />
                        <div>
                          <strong>{{ row.resourceName }}</strong>
                          <span>{{ row.fileName || row.fileUrl || '-' }}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span class="admin-public-type-pill" :class="row.typeTone">{{ row.typeLabel }}</span>
                    </td>
                    <td class="wrap-cell">{{ row.courseName || '-' }}</td>
                    <td class="wrap-cell">{{ row.majorLabel }}</td>
                    <td>{{ row.applicantName || '-' }}</td>
                    <td>{{ row.appliedAtLabel }}</td>
                    <td>
                      <span class="admin-public-status" :class="row.statusTone">
                        <i />
                        {{ row.statusLabel }}
                      </span>
                    </td>
                    <td>{{ row.reviewerName || '-' }}</td>
                    <td>{{ row.reviewedAtLabel }}</td>
                    <td>
                      <div class="admin-public-row-actions">
                        <el-button v-if="row.statusTone === 'pending'" class="approve" @click.stop="openReviewDetail(row)">审核</el-button>
                        <el-button v-else class="detail-button" @click.stop="openReadonlyDetail(row)">查看审核详情</el-button>
                        <el-button v-if="row.statusTone === 'rejected'" class="warn" @click.stop="openEditRestatement(row)">编辑重申</el-button>
                        <el-button class="plain" @click.stop="openPreview(row)">预览</el-button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <footer class="admin-public-footer">
              <p>共 {{ totalCount }} 条记录</p>
              <el-pagination
                v-model:current-page="page"
                :page-size="pageSize"
                :total="totalCount"
                layout="prev, pager, next"
                background
              />
            </footer>
          </template>
        </section>

        <aside v-if="selectedApplication" class="admin-public-panel">
          <div class="admin-public-panel-head">
            <div>
              <strong>申请详情</strong>
              <p>{{ selectedApplication.resourceName }}</p>
            </div>
            <el-button text circle :icon="Close" @click="selectedApplication = null" />
          </div>

          <section class="admin-public-status-card">
            <div>
              <span>审核状态</span>
              <strong :class="selectedApplication.statusTone">{{ selectedApplication.statusLabel }}</strong>
            </div>
            <div>
              <span>申请人</span>
              <strong>{{ selectedApplication.applicantName || '-' }}</strong>
            </div>
            <div>
              <span>申请时间</span>
              <strong>{{ selectedApplication.appliedAtLabel }}</strong>
            </div>
            <div>
              <span>审核人</span>
              <strong>{{ selectedApplication.reviewerName || '-' }}</strong>
            </div>
          </section>

          <section class="admin-public-preview">
            <img :src="selectedApplication.coverResolved" :alt="selectedApplication.resourceName" />
            <div>
              <strong>{{ selectedApplication.resourceName }}</strong>
              <p>{{ selectedApplication.fileName || '-' }}</p>
              <div class="admin-public-preview-actions">
                <el-button class="plain" @click="openPreview(selectedApplication)">打开预览</el-button>
                <el-button class="plain" @click="copyFileLink(selectedApplication)">复制链接</el-button>
              </div>
            </div>
          </section>

          <section class="admin-public-detail-grid">
            <div class="admin-public-detail-card">
              <p>资源快照</p>
              <dl>
                <div><dt>资源类型</dt><dd>{{ selectedApplication.typeLabel }}</dd></div>
                <div><dt>所属专业</dt><dd>{{ selectedApplication.majorLabel }}</dd></div>
                <div><dt>课程名称</dt><dd>{{ selectedApplication.courseName || '-' }}</dd></div>
                <div><dt>文件大小</dt><dd>{{ selectedApplication.fileSizeLabel }}</dd></div>
                <div><dt>申请版本</dt><dd>{{ formatVersion(selectedApplication.resourceVersion) }}</dd></div>
                <div><dt>公开版本</dt><dd>{{ formatVersion(selectedApplication.publicVersion ?? selectedApplication.resourceVersion) }}</dd></div>
              </dl>
            </div>

            <div class="admin-public-detail-card">
              <p>审核信息</p>
              <dl>
                <div><dt>审核状态</dt><dd>{{ selectedApplication.statusLabel }}</dd></div>
                <div><dt>审核人</dt><dd>{{ selectedApplication.reviewerName || '-' }}</dd></div>
                <div class="wide"><dt>审核意见</dt><dd>{{ selectedApplication.reviewComment || '-' }}</dd></div>
              </dl>
            </div>
          </section>

          <section class="admin-public-log-card">
            <p>处理动作</p>
                      <div class="admin-public-panel-actions">
                        <el-button
                          v-if="selectedApplication.statusTone === 'pending'"
                          class="admin-public-panel-approve"
                          :loading="busyId === selectedApplication.applicationId"
                @click="openReview(selectedApplication, 'APPROVED')"
              >
                通过公开
              </el-button>
              <el-button
                v-if="selectedApplication.statusTone === 'pending'"
                class="admin-public-panel-reject"
                :loading="busyId === selectedApplication.applicationId"
                @click="openReview(selectedApplication, 'REJECTED')"
              >
                驳回申请
              </el-button>
              <el-button class="admin-public-panel-ghost" @click="selectApplication(selectedApplication)">保持选中</el-button>
            </div>
          </section>

          <section class="admin-public-timeline-card">
            <p>处理时间线</p>
            <article class="admin-public-timeline-item">
              <header>
                <strong>提交申请</strong>
                <span>{{ selectedApplication.appliedAtLabel }}</span>
              </header>
              <p>申请人 {{ selectedApplication.applicantName || '-' }} 提交了资源公开申请。</p>
            </article>
            <article class="admin-public-timeline-item">
              <header>
                <strong>当前状态</strong>
                <span>{{ selectedApplication.reviewedAtLabel || '-' }}</span>
              </header>
              <p>
                {{
                  selectedApplication.statusTone === 'pending'
                    ? '当前还在等待审核。'
                    : selectedApplication.statusTone === 'approved'
                      ? '申请已通过，资源已进入公开库。'
                      : '申请已驳回，可重新整理后再次提交。'
                }}
              </p>
            </article>
          </section>
        </aside>
      </div>
    </section>

    <el-dialog v-model="reviewVisible" class="admin-public-dialog" width="560px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-dialog-head">
          <strong>{{ reviewMode === 'APPROVED' ? '通过公开申请' : '驳回公开申请' }}</strong>
          <el-button text circle :icon="Close" @click="reviewVisible = false" />
        </div>
      </template>

      <div v-if="reviewTarget" class="admin-public-dialog-body">
        <div class="admin-public-dialog-summary">
          <strong>{{ reviewTarget.resourceName }}</strong>
          <span>{{ reviewTarget.applicantName || '-' }} · {{ reviewTarget.appliedAtLabel }}</span>
        </div>

        <label class="admin-public-dialog-field">
          <span>{{ reviewMode === 'APPROVED' ? '通过说明' : '驳回原因' }}</span>
          <el-input
            v-model="reviewComment"
            type="textarea"
            :rows="5"
            maxlength="120"
            show-word-limit
            :placeholder="reviewMode === 'APPROVED' ? '可填写通过说明' : '请填写驳回原因'"
          />
        </label>
      </div>

      <template #footer>
        <div class="admin-public-dialog-footer">
          <el-button @click="reviewVisible = false">取消</el-button>
          <el-button :type="reviewMode === 'APPROVED' ? 'primary' : 'danger'" :loading="saving" @click="submitReview">
            {{ reviewMode === 'APPROVED' ? '确认通过' : '确认驳回' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-drawer v-model="logDrawerVisible" class="admin-public-log-drawer" direction="rtl" size="560px" :with-header="false">
      <div class="admin-public-resource-drawer-head">
        <div>
          <p>申请记录</p>
          <h3>{{ logTarget?.resourceName || '资源申请记录' }}</h3>
        </div>
        <el-button text :icon="Close" @click="logDrawerVisible = false" />
      </div>

      <div v-if="logsLoading" class="admin-public-resource-empty drawer-state">记录加载中...</div>
      <template v-else>
        <article v-for="item in selectedLogs" :key="item.logId" class="admin-public-resource-log-row">
          <header>
            <strong>{{ item.action }}</strong>
            <span>{{ formatDateTime(item.createdAt) }}</span>
          </header>
          <p>{{ item.content }}</p>
          <small>{{ item.operatorName }}</small>
        </article>
        <div v-if="selectedLogs.length === 0" class="admin-public-resource-log-empty">暂无记录</div>
      </template>
    </el-drawer>

    <el-dialog v-model="previewVisible" class="admin-public-preview-modal" width="820px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-dialog-head">
          <strong>{{ previewTarget?.resourceName || '资源预览' }}</strong>
          <el-button text circle :icon="Close" @click="previewVisible = false" />
        </div>
      </template>

      <div v-if="previewTarget" class="admin-public-preview-doc">
        <template v-if="resourcePreviewSource(previewTarget)">
          <img
            v-if="resourcePreviewKind(previewTarget) === 'image'"
            class="admin-resource-preview-media image"
            :src="resourcePreviewSource(previewTarget)"
            :alt="previewTarget.resourceName"
          />
          <video
            v-else-if="resourcePreviewKind(previewTarget) === 'video'"
            class="admin-resource-preview-media"
            :src="resourcePreviewSource(previewTarget)"
            controls
          />
          <audio
            v-else-if="resourcePreviewKind(previewTarget) === 'audio'"
            class="admin-resource-preview-audio"
            :src="resourcePreviewSource(previewTarget)"
            controls
          />
          <iframe
            v-else-if="resourcePreviewKind(previewTarget) === 'frame'"
            class="admin-resource-preview-frame"
            :src="resourcePreviewSource(previewTarget)"
            :title="previewTarget.resourceName"
          />
          <div v-else class="admin-resource-preview-unsupported">
            <span class="admin-resource-file-icon">
              <el-icon><Document /></el-icon>
            </span>
            <strong>{{ previewTarget.fileName || previewTarget.resourceName }}</strong>
            <p>当前文件类型不支持在线预览，请下载后查看。</p>
          </div>
        </template>
        <el-empty v-else description="暂无可预览内容" />
      </div>
    </el-dialog>

    <el-dialog v-model="editRestatementVisible" class="admin-public-edit-dialog" width="600px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-edit-head">
          <strong>编辑重申</strong>
          <el-button text circle :icon="Close" @click="editRestatementVisible = false" />
        </div>
      </template>

      <div v-if="editTarget" class="admin-public-edit-form">
        <label>
          <span>资源名称 <b>*</b></span>
          <el-input v-model="editForm.resourceName" maxlength="20" show-word-limit />
        </label>
        <label>
          <span>封面图 <b>*</b></span>
          <div class="admin-public-edit-file cover">
            <img :src="editForm.coverUrl" alt="封面图" />
            <div>
              <strong>{{ editForm.coverName || '-' }}</strong>
              <p>{{ editForm.coverSize || '-' }}</p>
            </div>
            <el-button text circle :icon="Close" />
          </div>
        </label>
        <label>
          <span>资源内容 <b>*</b></span>
          <div class="admin-public-edit-file">
            <span class="admin-public-edit-file-icon"><el-icon><Document /></el-icon></span>
            <div>
              <strong>{{ editForm.fileName }}</strong>
              <p>{{ editForm.fileSizeLabel }} <i></i> 上传完成 <em>✓</em></p>
            </div>
            <el-button text circle :icon="Close" />
          </div>
        </label>
        <label>
          <span>所属专业 <b>*</b></span>
          <el-select v-model="editForm.majorId" placeholder="请选择所属专业" filterable>
            <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </label>
        <label>
          <span>所属课程</span>
          <el-input v-model="editForm.courseName" maxlength="30" show-word-limit />
        </label>
      </div>

      <template #footer>
        <div class="admin-public-edit-footer">
          <el-button @click="editRestatementVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditRestatement">编辑重申</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="batchReviewVisible" class="admin-public-dialog" width="560px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-dialog-head">
          <strong>{{ batchReviewMode === 'APPROVED' ? '批量通过' : '批量驳回' }}</strong>
          <el-button text circle :icon="Close" @click="batchReviewVisible = false" />
        </div>
      </template>

      <div class="admin-public-dialog-summary">
        <strong>已选 {{ selectedIds.length }} 条申请</strong>
        <span>{{ batchReviewMode === 'APPROVED' ? '将统一通过并进入公开流程' : '将统一驳回并保留历史记录' }}</span>
      </div>

      <label class="admin-public-dialog-field">
        <span>{{ batchReviewMode === 'APPROVED' ? '通过说明' : '驳回原因' }}</span>
        <el-input
          v-model="batchReviewComment"
          type="textarea"
          :rows="5"
          maxlength="120"
          show-word-limit
          :placeholder="batchReviewMode === 'APPROVED' ? '可填写统一通过说明' : '请填写统一驳回原因'"
        />
      </label>

      <template #footer>
        <div class="admin-public-dialog-footer">
          <el-button @click="batchReviewVisible = false">取消</el-button>
          <el-button :type="batchReviewMode === 'APPROVED' ? 'primary' : 'danger'" :loading="saving" @click="submitBatchReview">
            {{ batchReviewMode === 'APPROVED' ? '确认通过' : '确认驳回' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Close, Document, Refresh, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  approveAdminPublicApplication,
  fetchAdminPublicApplication,
  fetchAdminPublicApplications,
  rejectAdminPublicApplication,
  type AdminPublicApplication,
  type AdminResourceLog,
  type AdminResourceQuery
} from '../../api/admin-resource';
import { resolvePublicUrl } from '../../api/http';
import { resourcePreviewKind, resourcePreviewSource } from '../../features/admin/resource-preview';
import { coverForResourceType } from '../../features/student/resources';

type ReviewMode = 'APPROVED' | 'REJECTED';
type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

interface ResourceOption {
  label: string;
  value: string;
}

interface MajorOption {
  label: string;
  value: number;
}

interface PublicApplicationRow extends AdminPublicApplication {
  coverResolved: string;
  typeLabel: string;
  typeTone: string;
  statusLabel: string;
  statusTone: 'pending' | 'approved' | 'rejected';
  majorLabel: string;
  fileSizeLabel: string;
  appliedAtLabel: string;
  reviewedAtLabel: string;
}

const pageSize = 8;
const loading = ref(false);
const saving = ref(false);
const busyId = ref<number | null>(null);
const page = ref(1);
const applications = ref<PublicApplicationRow[]>([]);
const selectedApplication = ref<PublicApplicationRow | null>(null);
const reviewVisible = ref(false);
const reviewMode = ref<ReviewMode>('APPROVED');
const reviewTarget = ref<PublicApplicationRow | null>(null);
const reviewComment = ref('');
const logDrawerVisible = ref(false);
const logTarget = ref<PublicApplicationRow | null>(null);
const logsLoading = ref(false);
const selectedLogs = ref<AdminResourceLog[]>([]);
const previewVisible = ref(false);
const previewTarget = ref<PublicApplicationRow | null>(null);
const selectedIds = ref<number[]>([]);
const batchReviewVisible = ref(false);
const batchReviewMode = ref<ReviewMode>('APPROVED');
const batchReviewComment = ref('');
const detailPageVisible = ref(false);
const detailMode = ref<'review' | 'readonly'>('review');
const editRestatementVisible = ref(false);
const editTarget = ref<PublicApplicationRow | null>(null);
const editForm = reactive({
  resourceName: '',
  coverUrl: '',
  coverName: '',
  coverSize: '',
  fileName: '',
  fileSizeLabel: '',
  majorId: null as number | null,
  courseName: ''
});
const reviewTimeline = computed(() => {
  const row = selectedApplication.value;
  if (!row) {
    return [];
  }

  return [
    {
      title: '提交申请',
      date: row.appliedAtLabel.slice(0, 10),
      operator: row.applicantName || '-',
      time: row.appliedAtLabel.slice(11) || '-',
      status: '',
      comment: ''
    },
    ...(row.reviewedAtLabel === '-'
      ? []
      : [
          {
            title: '审核',
            date: row.reviewedAtLabel.slice(0, 10),
            operator: row.reviewerName || '-',
            time: row.reviewedAtLabel.slice(11) || '-',
            status: row.statusTone === 'approved' ? '审核通过' : row.statusTone === 'rejected' ? '审核驳回' : '',
            comment: row.reviewComment || '-'
          }
        ])
  ];
});

const draft = reactive({
  keyword: '',
  publicStatus: '' as '' | ApplicationStatus
});

const appliedFilters = ref({ ...draft });

const majorOptions: MajorOption[] = [
  { label: '城市轨道交通运营管理', value: 1 },
  { label: '城市轨道交通车辆技术', value: 2 },
  { label: '城市轨道交通机电技术', value: 3 },
  { label: '城市轨道交通通信信号技术', value: 4 }
];

const publicStatusOptions: ResourceOption[] = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
];

const filteredApplications = computed(() =>
  applications.value.filter((item) => {
    const keyword = appliedFilters.value.keyword.trim().toLowerCase();
    const matchesKeyword = !keyword || item.resourceName.toLowerCase().includes(keyword);
    const matchesStatus = !appliedFilters.value.publicStatus || item.publicStatus === appliedFilters.value.publicStatus;
    return matchesKeyword && matchesStatus;
  })
);

const totalCount = computed(() => filteredApplications.value.length);
const pendingCount = computed(() => filteredApplications.value.filter((item) => item.statusTone === 'pending').length);
const approvedCount = computed(() => filteredApplications.value.filter((item) => item.statusTone === 'approved').length);
const rejectedCount = computed(() => filteredApplications.value.filter((item) => item.statusTone === 'rejected').length);
const pagedApplications = computed(() => filteredApplications.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const allCurrentSelected = computed(() => pagedApplications.value.length > 0 && pagedApplications.value.every((item) => selectedIds.value.includes(item.applicationId)));
const partCurrentSelected = computed(() => selectedIds.value.length > 0 && !allCurrentSelected.value);

function formatVersion(value?: number) {
  const version = Number(value);
  return Number.isFinite(version) && version > 0 ? `V${version}` : '-';
}

function normalizeStatus(value?: string): ApplicationStatus {
  const upper = String(value || '').toUpperCase();
  if (upper === 'APPROVED' || upper === 'PUBLIC') {
    return 'APPROVED';
  }
  if (upper === 'REJECTED') {
    return 'REJECTED';
  }
  return 'PENDING';
}

function statusTone(status: ApplicationStatus): 'pending' | 'approved' | 'rejected' {
  if (status === 'APPROVED') {
    return 'approved';
  }
  if (status === 'REJECTED') {
    return 'rejected';
  }
  return 'pending';
}

function statusLabel(status: ApplicationStatus) {
  if (status === 'APPROVED') {
    return '审核通过';
  }
  if (status === 'REJECTED') {
    return '审核驳回';
  }
  return '审核中';
}

function normalizeResourceType(type?: string) {
  const raw = String(type || '').trim();
  if (!raw) {
    return '资源';
  }
  const upper = raw.toUpperCase();
  const labels: Record<string, string> = {
    DOCUMENT: '文本文档',
    PRESENTATION: '演示文稿',
    IMAGE: '图片',
    AUDIO: '音频',
    VIDEO: '视频'
  };
  return labels[upper] || raw;
}

function typeTone(type: string) {
  if (type === '视频') {
    return 'video';
  }
  if (type === '图片') {
    return 'image';
  }
  if (type === '音频') {
    return 'audio';
  }
  if (type === '演示文稿') {
    return 'presentation';
  }
  if (type === '实训试题') {
    return 'exam';
  }
  return 'document';
}

function formatFileSize(bytes?: number) {
  if (!bytes || bytes <= 0) {
    return '-';
  }
  if (bytes < 1024) {
    return `${bytes} KB`;
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} MB`;
  }
  return `${(bytes / 1024 / 1024).toFixed(1)} GB`;
}

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }
  return value.includes('T') ? value.replace('T', ' ').slice(0, 16) : value.slice(0, 16);
}

function findMajorLabel(majorId?: number | null) {
  return majorOptions.find((item) => item.value === majorId)?.label || '-';
}

function mapApplicationRow(application: AdminPublicApplication): PublicApplicationRow {
  const typeLabel = normalizeResourceType(application.resourceType);
  const status = normalizeStatus(application.publicStatus);
  const majorLabel = application.majorName || findMajorLabel(application.majorId);
  const resolvedCoverUrl = resolvePublicUrl(application.coverUrl);
  return {
    ...application,
    resourceType: typeLabel,
    coverUrl: resolvedCoverUrl || coverForResourceType(typeLabel),
    coverResolved: resolvedCoverUrl || coverForResourceType(typeLabel),
    fileUrl: resolvePublicUrl(application.fileUrl),
    previewUrl: resolvePublicUrl(application.previewUrl),
    typeLabel,
    typeTone: typeTone(typeLabel),
    statusLabel: statusLabel(status),
    statusTone: statusTone(status),
    majorLabel,
    fileSizeLabel: formatFileSize(application.fileSize),
    appliedAtLabel: formatDateTime(application.appliedAt),
    reviewedAtLabel: formatDateTime(application.reviewedAt),
    publicStatus: status
  };
}

function mergeSelectedIds(ids: number[]) {
  selectedIds.value = Array.from(new Set([...selectedIds.value, ...ids]));
}

function createEmptyFilters() {
  return {
    keyword: '',
    publicStatus: '' as '' | ApplicationStatus
  };
}

function selectApplication(application: PublicApplicationRow) {
  selectedApplication.value = application;
  void loadApplicationDetail(application);
}

function openReviewDetail(application: PublicApplicationRow) {
  selectedApplication.value = application;
  detailMode.value = 'review';
  reviewComment.value = '';
  detailPageVisible.value = true;
  void loadApplicationDetail(application);
}

function openReadonlyDetail(application: PublicApplicationRow) {
  selectedApplication.value = application;
  detailMode.value = 'readonly';
  detailPageVisible.value = true;
  void loadApplicationDetail(application);
}

function closeDetailPage() {
  detailPageVisible.value = false;
}

async function submitDetailReview(mode: ReviewMode) {
  if (!selectedApplication.value) {
    return;
  }

  reviewTarget.value = selectedApplication.value;
  reviewMode.value = mode;
  await submitReview();
  detailPageVisible.value = false;
}

function openReview(application: PublicApplicationRow, mode: ReviewMode) {
  reviewTarget.value = application;
  reviewMode.value = mode;
  reviewComment.value = '';
  reviewVisible.value = true;
}

function openBatchReview(mode: ReviewMode) {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择申请');
    return;
  }
  batchReviewMode.value = mode;
  batchReviewComment.value = '';
  batchReviewVisible.value = true;
}

function toggleOne(applicationId: number) {
  selectedIds.value = selectedIds.value.includes(applicationId)
    ? selectedIds.value.filter((id) => id !== applicationId)
    : [...selectedIds.value, applicationId];
}

function toggleAllCurrent(value: string | number | boolean) {
  if (!value) {
    selectedIds.value = selectedIds.value.filter((id) => !pagedApplications.value.some((item) => item.applicationId === id));
    return;
  }
  mergeSelectedIds(pagedApplications.value.map((item) => item.applicationId));
}

function applyFilters() {
  appliedFilters.value = {
    keyword: draft.keyword,
    publicStatus: draft.publicStatus
  };
  page.value = 1;
  void loadApplications();
}

function resetFilters() {
  Object.assign(draft, createEmptyFilters());
  appliedFilters.value = createEmptyFilters();
  page.value = 1;
  void loadApplications();
}

function buildQuery(): AdminResourceQuery {
  return {
    keyword: appliedFilters.value.keyword.trim() || undefined,
    publicStatus: appliedFilters.value.publicStatus || undefined,
    page: 1,
    pageSize: 999
  };
}

async function loadApplications() {
  loading.value = true;
  try {
    const result = await fetchAdminPublicApplications(buildQuery());
    const mapped = result.records.map(mapApplicationRow);
    applications.value = mapped;
    if (!selectedApplication.value || !applications.value.some((item) => item.applicationId === selectedApplication.value?.applicationId)) {
      selectedApplication.value = applications.value[0] ?? null;
    }
    selectedIds.value = selectedIds.value.filter((id) => applications.value.some((item) => item.applicationId === id));
    if (selectedApplication.value) {
      void loadApplicationDetail(selectedApplication.value);
    }
  } catch (error) {
    applications.value = [];
    selectedApplication.value = null;
    selectedIds.value = [];
    ElMessage.error(error instanceof Error ? error.message : '资源公开申请加载失败');
  } finally {
    loading.value = false;
  }
}

async function refreshList() {
  await loadApplications();
}

async function loadApplicationDetail(application: PublicApplicationRow) {
  try {
    const detail = await fetchAdminPublicApplication(application.applicationId);
    selectedApplication.value = mapApplicationRow(detail);
  } catch {
    selectedApplication.value = application;
  }
}

function openPreview(application: PublicApplicationRow) {
  previewTarget.value = application;
  previewVisible.value = true;
}

function openEditRestatement(application: PublicApplicationRow) {
  editTarget.value = application;
  Object.assign(editForm, {
    resourceName: application.resourceName,
    coverUrl: application.coverResolved,
    coverName: '',
    coverSize: '',
    fileName: application.fileName || '',
    fileSizeLabel: application.fileSizeLabel === '-' ? '-' : application.fileSizeLabel,
    majorId: application.majorId ?? null,
    courseName: application.courseName || ''
  });
  editRestatementVisible.value = true;
}

function submitEditRestatement() {
  if (!editTarget.value) {
    return;
  }

  editRestatementVisible.value = false;
  ElMessage.warning('重申接口暂未提供，当前仅关闭弹窗');
}

function copyFileLink(application: PublicApplicationRow) {
  const url = application.fileUrl || application.previewUrl;
  if (!url) {
    ElMessage.warning('暂无可复制链接');
    return;
  }
  void navigator.clipboard?.writeText(url);
  ElMessage.success('链接已复制');
}

async function submitBatchReview() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择申请');
    return;
  }

  if (batchReviewMode.value === 'REJECTED' && !batchReviewComment.value.trim()) {
    ElMessage.warning('请填写驳回原因');
    return;
  }

  try {
    await ElMessageBox.confirm(
      batchReviewMode.value === 'APPROVED'
        ? `确认批量通过 ${selectedIds.value.length} 条公开申请？`
        : `确认批量驳回 ${selectedIds.value.length} 条公开申请？`,
      batchReviewMode.value === 'APPROVED' ? '批量通过' : '批量驳回',
      {
        confirmButtonText: batchReviewMode.value === 'APPROVED' ? '通过' : '驳回',
        cancelButtonText: '取消',
        type: batchReviewMode.value === 'APPROVED' ? 'success' : 'warning'
      }
    );
  } catch {
    return;
  }

  saving.value = true;
  try {
    const targets = applications.value.filter((item) => selectedIds.value.includes(item.applicationId));
    await Promise.all(
      targets.map((item) =>
        batchReviewMode.value === 'APPROVED'
          ? approveAdminPublicApplication(item.applicationId, {
              reviewComment: batchReviewComment.value.trim() || undefined
            })
          : rejectAdminPublicApplication(item.applicationId, {
              reviewComment: batchReviewComment.value.trim()
            })
      )
    );
    ElMessage.success(batchReviewMode.value === 'APPROVED' ? '批量通过成功' : '批量驳回成功');
    batchReviewVisible.value = false;
    selectedIds.value = [];
    await loadApplications();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批量审核失败');
  } finally {
    saving.value = false;
  }
}

async function submitReview() {
  if (!reviewTarget.value) {
    return;
  }

  const application = reviewTarget.value;
  if (reviewMode.value === 'REJECTED' && !reviewComment.value.trim()) {
    ElMessage.warning('请填写驳回原因');
    return;
  }

  try {
    await ElMessageBox.confirm(
      reviewMode.value === 'APPROVED'
        ? `确认通过资源「${application.resourceName}」的公开申请？`
        : `确认驳回资源「${application.resourceName}」的公开申请？`,
      reviewMode.value === 'APPROVED' ? '通过申请' : '驳回申请',
      {
        confirmButtonText: reviewMode.value === 'APPROVED' ? '通过' : '驳回',
        cancelButtonText: '取消',
        type: reviewMode.value === 'APPROVED' ? 'success' : 'warning'
      }
    );
  } catch {
    return;
  }

  busyId.value = application.applicationId;
  saving.value = true;
  try {
    if (reviewMode.value === 'APPROVED') {
      await approveAdminPublicApplication(application.applicationId, {
        reviewComment: reviewComment.value.trim() || undefined
      });
      ElMessage.success('公开申请已通过');
    } else {
      await rejectAdminPublicApplication(application.applicationId, {
        reviewComment: reviewComment.value.trim()
      });
      ElMessage.success('公开申请已驳回');
    }
    reviewVisible.value = false;
    await loadApplications();
    const nextStatus = reviewMode.value;
    selectedApplication.value = {
      ...application,
      publicStatus: nextStatus,
      statusTone: statusTone(nextStatus),
      statusLabel: statusLabel(nextStatus),
      reviewerName: application.reviewerName || '-',
      reviewedAtLabel: formatDateTime(new Date().toISOString()),
      reviewComment: reviewComment.value.trim() || application.reviewComment || ''
    };
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '审核失败');
  } finally {
    busyId.value = null;
    saving.value = false;
  }
}

onMounted(() => {
  void loadApplications();
});
</script>
