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
          <el-select v-model="filters.type" class="admin-training-select" placeholder="实训类型" clearable>
            <el-option label="考试" value="考试" />
            <el-option label="练习" value="练习" />
          </el-select>
          <el-select v-model="filters.time" class="admin-training-select" placeholder="实训时间" clearable>
            <el-option label="本周" value="week" />
            <el-option label="本月" value="month" />
            <el-option label="本学期" value="term" />
          </el-select>
          <el-select v-model="filters.status" class="admin-training-select" placeholder="发布状态" clearable>
            <el-option label="已发布" value="已发布" />
            <el-option label="未发布" value="未发布" />
          </el-select>
          <el-button class="admin-training-ghost" @click="refreshCourses">查询</el-button>
          <el-button class="admin-training-ghost" @click="resetFilters">重置</el-button>
        </div>
        <div class="admin-training-action-row">
          <el-button class="admin-training-primary" type="primary" :icon="Plus" @click="openCreate">新增实训课</el-button>
          <el-button class="admin-training-ghost" :icon="Upload" @click="openImport">导入实训课</el-button>
        </div>
      </div>

      <div class="admin-training-table-card">
        <div class="admin-training-table-scroll">
          <table class="admin-training-table">
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
                <th>操作</th>
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
                <td>
                  <div class="admin-row-actions">
                    <template v-if="course.status === '未发布'">
                      <el-button link type="primary" @click="openEdit(course)">编辑</el-button>
                      <el-button link type="danger" @click="confirmDelete(course)">删除</el-button>
                      <el-button class="publish-action" link @click="openPublish(course)">发布</el-button>
                      <el-button class="log-action" link @click="openLogs(course)">操作日志</el-button>
                    </template>
                    <template v-else-if="course.exam">
                      <el-button class="primary-action" link @click="openMonitor(course)">开始考试</el-button>
                      <el-button link type="primary" @click="openEdit(course)">编辑</el-button>
                      <el-button link type="danger" @click="confirmDelete(course)">删除</el-button>
                      <el-dropdown trigger="click">
                        <el-button class="more-action" link>更多 <el-icon><ArrowDown /></el-icon></el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item @click="openPreview(course)">详情预览</el-dropdown-item>
                            <el-dropdown-item @click="copyCourse(course)">复制组课</el-dropdown-item>
                            <el-dropdown-item @click="openLogs(course)">操作日志</el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </template>
                    <template v-else>
                      <el-button class="primary-action" link @click="openMonitor(course)">监考</el-button>
                      <el-button class="primary-action" link @click="openMarking(course)">阅卷</el-button>
                      <el-button class="primary-action" link @click="openStats(course)">成绩统计</el-button>
                      <el-button class="log-action" link @click="openLogs(course)">操作日志</el-button>
                      <el-dropdown trigger="click">
                        <el-button class="more-action" link>更多 <el-icon><ArrowDown /></el-icon></el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item @click="openPreview(course)">详情预览</el-dropdown-item>
                            <el-dropdown-item @click="copyCourse(course)">复制组课</el-dropdown-item>
                            <el-dropdown-item @click="withdrawCourse(course)">撤回发布</el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
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
                  <el-option v-for="semester in semesters" :key="semester" :label="semester" :value="semester" />
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
        <template #header><div class="admin-training-dialog-head"><strong>导入实训课</strong><el-button text circle :icon="Close" @click="importVisible = false" /></div></template>
        <div class="admin-training-import">
          <div class="admin-training-upload-box">
            <el-icon><UploadFilled /></el-icon>
            <strong>上传实训组课模板</strong>
            <span>支持 .xlsx，导入前会先进入预览校验</span>
            <el-button type="primary" plain @click="importChecked = true">选择文件</el-button>
          </div>
          <div class="admin-training-import-result" :class="{ active: importChecked }">
            <strong>{{ importChecked ? '校验通过 6 条，需确认 1 条' : '等待上传文件' }}</strong>
            <p>{{ importChecked ? '第 3 行监考教师重名，请在导入后进入编辑页确认。' : '上传后会展示数据行、错误原因和可导入数量。' }}</p>
          </div>
        </div>
        <template #footer><div class="admin-training-dialog-footer"><el-button @click="importVisible = false">取消</el-button><el-button type="primary" :disabled="!importChecked" @click="confirmImport">确认导入</el-button></div></template>
      </el-dialog>

      <el-drawer v-model="monitorVisible" size="72%" direction="rtl" class="monitor-drawer">
        <template #header>
          <div class="monitor-title">
            <h2>{{ selectedCourse?.name || '实时监考' }}</h2>
            <p>{{ selectedCourse?.time }} / {{ selectedCourse?.room }}</p>
          </div>
        </template>
        <div class="monitor-grid">
          <article v-for="camera in cameras" :key="camera.name" class="camera-card">
            <div class="camera-screen"><span class="live-dot">直播</span><strong>{{ camera.name }}</strong><p>RTSP 可配置接入</p></div>
            <footer><span>{{ camera.location }}</span><el-tag size="small" type="success">在线</el-tag></footer>
          </article>
        </div>
        <div class="monitor-student-panel">
          <div class="panel-heading"><h3>学员监控</h3><el-button :icon="Monitor" type="primary" plain>查看学员桌面</el-button></div>
          <el-table :data="students">
            <el-table-column prop="name" label="学员姓名" min-width="100" />
            <el-table-column prop="studentNo" label="学号" min-width="120" />
            <el-table-column prop="topic" label="当前实训题" min-width="160" />
            <el-table-column prop="mode" label="模式" width="92" />
            <el-table-column prop="room" label="所在房间" width="120" />
            <el-table-column prop="ip" label="IP" width="140" />
            <el-table-column label="在线状态" width="104"><template #default="{ row }"><el-tag :type="row.online ? 'success' : 'info'" size="small">{{ row.online ? '在线' : '离线' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
      </el-drawer>

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
      </el-drawer>

      <el-drawer v-model="markingVisible" class="admin-training-work-drawer" direction="rtl" size="760px" :with-header="false">
        <div class="admin-training-drawer-head compact"><div><span>阅卷</span><h3>{{ selectedCourse?.name || '阅卷' }}</h3></div><el-button text circle :icon="Close" @click="markingVisible = false" /></div>
        <el-table :data="markingRows">
          <el-table-column prop="student" label="学员" width="120" />
          <el-table-column prop="className" label="班级" min-width="160" />
          <el-table-column prop="submitAt" label="提交时间" width="160" />
          <el-table-column prop="score" label="得分" width="100" />
          <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === '已阅' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="120"><template #default><el-button link type="primary">进入阅卷</el-button></template></el-table-column>
        </el-table>
      </el-drawer>

      <el-drawer v-model="statsVisible" class="admin-training-work-drawer" direction="rtl" size="760px" :with-header="false">
        <div class="admin-training-drawer-head compact"><div><span>成绩统计</span><h3>{{ selectedCourse?.name || '成绩统计' }}</h3></div><el-button text circle :icon="Close" @click="statsVisible = false" /></div>
        <div class="admin-training-stats-grid">
          <article><span>应参加</span><strong>{{ statsSummary.participantCount || 0 }}</strong></article>
          <article><span>已完成</span><strong>{{ statsSummary.submittedAttemptCount || 0 }}</strong></article>
          <article><span>平均分</span><strong>{{ formatNumber(statsSummary.averageScore) }}</strong></article>
          <article><span>通过率</span><strong>{{ passRate(statsSummary) }}</strong></article>
        </div>
        <el-table :data="statsRows">
          <el-table-column prop="className" label="班级" min-width="160" />
          <el-table-column prop="total" label="人数" width="90" />
          <el-table-column prop="finished" label="完成人数" width="110" />
          <el-table-column prop="avg" label="平均分" width="100" />
          <el-table-column prop="passRate" label="通过率" width="100" />
        </el-table>
      </el-drawer>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, Close, Document, FolderOpened, Monitor, OfficeBuilding, Plus, Search, Tickets, Upload, UploadFilled, User, UserFilled, View } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  cancelPublishAdminTraining,
  createAdminTraining,
  deleteAdminTraining,
  fetchAdminTraining,
  fetchAdminTrainingLogs,
  fetchAdminTrainingMonitor,
  fetchAdminTrainings,
  fetchAdminTrainingStatistics,
  publishAdminTraining,
  updateAdminTraining,
  type AdminTraining,
  type AdminTrainingCameraState,
  type AdminTrainingLog,
  type AdminTrainingStatistics,
  type AdminTrainingStudentState
} from '../../api/admin-training';

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
  exam?: boolean;
}

interface SelectableItem {
  id: number;
  name: string;
  meta: string;
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

const filters = reactive({ keyword: '', type: '', time: '', status: '' });
const loading = ref(false);
const page = ref(1);
const pageSize = 8;
const total = ref(0);
const formVisible = ref(false);
const formMode = ref<'create' | 'edit'>('create');
const activeStep = ref<StepKey>('base');
const selectedCourse = ref<CourseRow>();
const previewCourse = ref<CourseRow>();
const monitorVisible = ref(false);
const logVisible = ref(false);
const markingVisible = ref(false);
const statsVisible = ref(false);
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
const importChecked = ref(false);

const form = reactive({
  id: 0,
  name: '',
  type: '考试' as '考试' | '练习',
  mode: '协同实训' as '单人实训' | '协同实训',
  semester: '2024-2025学年 第二学期',
  range: [] as string[],
  description: '',
  roles: [
    { name: '值班员', capacity: 1, duty: '负责接收调度指令并完成票据确认' },
    { name: '信号员', capacity: 1, duty: '负责设备状态确认和联锁操作' }
  ] as TrainingRole[],
  flow: [
    { name: '任务接收', rule: '确认任务、角色和安全注意事项', score: 10 },
    { name: '设备检查', rule: '按流程完成设备状态核验', score: 30 },
    { name: '故障处置', rule: '完成协同操作并记录结果', score: 40 },
    { name: '复盘提交', rule: '提交实训报告和过程附件', score: 20 }
  ] as TrainingFlowNode[]
});

const roleForm = reactive({ name: '', capacity: 1, duty: '' });

const steps = [
  { key: 'base' as StepKey, index: 1, label: '基础信息' },
  { key: 'resource' as StepKey, index: 2, label: '实训内容' },
  { key: 'target' as StepKey, index: 3, label: '对象场地' },
  { key: 'rule' as StepKey, index: 4, label: '角色规则' }
];

const semesters = ['2024-2025学年 第二学期', '2024-2025学年 第一学期', '2023-2024学年 第二学期'];

const courses = ref<CourseRow[]>([]);

const topicOptions: SelectableItem[] = [
  { id: 1, name: '信号机故障应急处置', meta: '信号 / 45 分钟 / 30 分', category: '信号', duration: 45, score: 30 },
  { id: 2, name: '道岔失表排查流程', meta: '信号 / 35 分钟 / 25 分', category: '信号', duration: 35, score: 25 },
  { id: 3, name: '车站客流突发处置', meta: '站务 / 40 分钟 / 25 分', category: '站务', duration: 40, score: 25 },
  { id: 4, name: '调度命令闭环演练', meta: '调度 / 30 分钟 / 20 分', category: '调度', duration: 30, score: 20 }
];

const resourceOptions: SelectableItem[] = [
  { id: 11, name: '信号系统实训指导书', meta: '文档 / 12.6MB', type: '文档', size: '12.6MB' },
  { id: 12, name: '道岔设备三维演示课件', meta: '视频 / 86.2MB', type: '视频', size: '86.2MB' },
  { id: 13, name: '站务应急处置评分表', meta: '表格 / 2.1MB', type: '表格', size: '2.1MB' }
];

const paperOptions: SelectableItem[] = [
  { id: 21, name: '信号基础理论试卷 A', meta: '20 题 / 100 分' },
  { id: 22, name: '站务应急理论测评', meta: '15 题 / 100 分' }
];

const classOptions: SelectableItem[] = [
  { id: 31, name: '城轨信号2401班', meta: '48 人 / 交通运输学院' },
  { id: 32, name: '城轨车辆2401班', meta: '42 人 / 车辆工程学院' },
  { id: 33, name: '张明亮、孙志强、王欣欣', meta: '指定学生 / 3 人' }
];

const teacherOptions: SelectableItem[] = [
  { id: 41, name: '李明峰', meta: '实训教师 / 交通运输学院' },
  { id: 42, name: '王志强', meta: '监考教师 / 交通运输学院' },
  { id: 43, name: '陈志远', meta: '实训教师 / 调度教研室' }
];

const roomOptions: SelectableItem[] = [
  { id: 51, name: '实训室A-301', meta: '48 座 / 摄像头 4 路 / 在线' },
  { id: 52, name: '驾驶模拟室B-101', meta: '36 座 / 摄像头 3 路 / 在线' },
  { id: 53, name: '调度实训室D-401', meta: '40 座 / 摄像头 4 路 / 在线' }
];

const selectedTopicIds = ref([1, 2]);
const selectedResourceIds = ref([11, 12]);
const selectedPaperId = ref(21);
const selectedClassIds = ref([31]);
const selectedTeacherIds = ref([41, 42]);
const selectedRoomId = ref(51);

const cameras = ref<Array<{ name: string; location: string; online: boolean; streamUrl?: string }>>([]);

const students = ref<Array<{ name: string; studentNo: string; topic: string; mode: string; room: string; ip: string; online: boolean }>>([]);

const logs = ref<Array<{ time: string; operator: string; action: string; content: string }>>([]);

const markingRows = [
  { student: '李明', className: '城轨信号2401班', submitAt: '2025-03-20 10:01', score: 92, status: '已阅' },
  { student: '周雨', className: '城轨信号2401班', submitAt: '2025-03-20 10:03', score: 88, status: '已阅' },
  { student: '陈晓', className: '城轨信号2401班', submitAt: '2025-03-20 10:05', score: '-', status: '待阅' }
];

const statsSummary = ref<AdminTrainingStatistics>({});
const statsRows = ref<Array<{ className: string; total: number; finished: number; avg: string; passRate: string }>>([]);

const selectedTopics = computed(() => topicOptions.filter((item) => selectedTopicIds.value.includes(item.id)));
const selectedResources = computed(() => resourceOptions.filter((item) => selectedResourceIds.value.includes(item.id)));
const selectedPaper = computed(() => paperOptions.find((item) => item.id === selectedPaperId.value));
const selectedClasses = computed(() => classOptions.filter((item) => selectedClassIds.value.includes(item.id)));
const selectedTeachers = computed(() => teacherOptions.filter((item) => selectedTeacherIds.value.includes(item.id)));
const selectedRoom = computed(() => roomOptions.find((item) => item.id === selectedRoomId.value));
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
    topic: topicOptions,
    resource: resourceOptions,
    paper: paperOptions,
    class: classOptions,
    teacher: teacherOptions,
    room: roomOptions
  }[selectorKind.value];

  return source.filter((item) => {
    const keywordMatched = !selectorKeyword.value || item.name.includes(selectorKeyword.value) || item.meta.includes(selectorKeyword.value);
    const typeMatched = !selectorType.value || item.category === selectorType.value || item.name.includes(selectorType.value);
    return keywordMatched && typeMatched;
  });
});

function resetFilters() {
  filters.keyword = '';
  filters.type = '';
  filters.time = '';
  filters.status = '';
  page.value = 1;
  void loadCourses();
}

function refreshCourses() {
  page.value = 1;
  void loadCourses();
}

function resetForm() {
  form.id = 0;
  form.name = '';
  form.type = '考试';
  form.mode = '协同实训';
  form.semester = semesters[0];
  form.range = [];
  form.description = '';
  selectedTopicIds.value = [1, 2];
  selectedResourceIds.value = [11, 12];
  selectedPaperId.value = 21;
  selectedClassIds.value = [31];
  selectedTeacherIds.value = [41, 42];
  selectedRoomId.value = 51;
  activeStep.value = 'base';
}

function openCreate() {
  resetForm();
  formMode.value = 'create';
  formVisible.value = true;
}

function openEdit(course: CourseRow) {
  formMode.value = 'edit';
  form.id = course.id;
  form.name = course.name;
  form.type = course.type;
  form.mode = course.mode;
  form.range = course.time.split('\n至 ');
  selectedCourse.value = course;
  activeStep.value = 'base';
  formVisible.value = true;
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

function openImport() {
  importChecked.value = false;
  importVisible.value = true;
}

function confirmImport() {
  importVisible.value = false;
  ElMessage.warning('实训课导入接口暂未提供，当前不能生成真实草稿');
}

async function openMonitor(row: CourseRow) {
  selectedCourse.value = row;
  try {
    const snapshot = await fetchAdminTrainingMonitor(row.id);
    cameras.value = (snapshot.cameras || []).map(mapCamera);
    students.value = (snapshot.students || []).map(mapStudent);
    statsSummary.value = snapshot.statistics || {};
  } catch (error) {
    cameras.value = [];
    students.value = [];
    ElMessage.error(error instanceof Error ? error.message : '实训监控加载失败');
  }
  monitorVisible.value = true;
}

function openMarking(row: CourseRow) {
  selectedCourse.value = row;
  markingVisible.value = true;
}

async function openStats(row: CourseRow) {
  selectedCourse.value = row;
  try {
    statsSummary.value = await fetchAdminTrainingStatistics(row.id);
    statsRows.value = [{
      className: row.target || '-',
      total: Number(statsSummary.value.participantCount || 0),
      finished: Number(statsSummary.value.submittedAttemptCount || 0),
      avg: formatNumber(statsSummary.value.averageScore),
      passRate: passRate(statsSummary.value)
    }];
  } catch (error) {
    statsSummary.value = {};
    statsRows.value = [];
    ElMessage.error(error instanceof Error ? error.message : '成绩统计加载失败');
  }
  statsVisible.value = true;
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
  await ElMessageBox.confirm(`确认删除实训课「${course.name}」？`, '删除实训课', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' });
  try {
    await deleteAdminTraining(course.id);
    ElMessage.success('实训课已删除');
    await loadCourses();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  }
}

async function copyCourse(course: CourseRow) {
  try {
    const detail = await fetchAdminTraining(course.id);
    await createAdminTraining({
      trainingName: `${detail.trainingName || course.name} 副本`,
      academicYearId: detail.academicYearId,
      semesterId: detail.semesterId,
      majorId: detail.majorId,
      coverUrl: detail.coverUrl,
      trainingType: detail.trainingType,
      trainingMode: detail.trainingMode,
      paperMode: detail.paperMode,
      paperId: detail.paperId,
      openStartTime: detail.openStartTime,
      openEndTime: detail.openEndTime,
      teamSize: detail.teamSize,
      appRequired: detail.appRequired,
      classIds: detail.classIds || [],
      roles: detail.roles || [],
      publishStatus: 'DRAFT'
    });
    ElMessage.success('已复制为草稿');
    await loadCourses();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '复制失败');
  }
}

async function withdrawCourse(course: CourseRow) {
  try {
    await cancelPublishAdminTraining(course.id);
    ElMessage.success('已撤回发布');
    await loadCourses();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '撤回失败');
  }
}

async function loadCourses() {
  loading.value = true;
  try {
    const result = await fetchAdminTrainings({
      keyword: filters.keyword.trim() || undefined,
      trainingType: trainingTypeToApi(filters.type),
      publishStatus: statusToApi(filters.status),
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

function mapCourse(item: AdminTraining): CourseRow {
  const type = apiTrainingTypeToText(item.trainingType);
  return {
    id: item.trainingId,
    name: item.trainingName || '-',
    type,
    mode: apiTrainingModeToText(item.trainingMode),
    time: `${formatDateTime(item.openStartTime)}\n至 ${formatDateTime(item.openEndTime)}`,
    target: item.classNames || '-',
    teacher: item.creatorName || '-',
    room: `${item.roomCount || 0} 间`,
    status: apiStatusToText(item.publishStatus),
    createdAt: formatDateTime(item.createdAt),
    topicCount: Math.max(1, item.roles?.length || 1),
    exam: type === '考试'
  };
}

function buildTrainingCommand(publishStatus: string) {
  return {
    trainingName: form.name.trim(),
    semesterId: semesterIdFromLabel(form.semester),
    trainingType: trainingTypeToApi(form.type),
    trainingMode: trainingModeToApi(form.mode),
    paperMode: selectedPaperId.value ? 'THEORY_PAPER' : 'NONE',
    paperId: selectedPaperId.value || undefined,
    openStartTime: form.range[0],
    openEndTime: form.range[1],
    teamSize: form.roles.reduce((sum, role) => sum + Number(role.capacity || 0), 0) || 1,
    appRequired: true,
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

function mapCamera(item: AdminTrainingCameraState) {
  return {
    name: item.cameraName || `摄像头${item.cameraId || ''}`,
    location: item.classroomName || '-',
    online: item.cameraStatus !== 'OFFLINE',
    streamUrl: item.streamUrl
  };
}

function mapStudent(item: AdminTrainingStudentState) {
  return {
    name: item.studentName || '-',
    studentNo: item.studentNo || '-',
    topic: item.roleName || '-',
    mode: apiTrainingModeToText(item.roomStatus),
    room: item.roomId ? `房间 ${item.roomId}` : '-',
    ip: item.deskStatus || '-',
    online: item.progressStatus !== 'OFFLINE'
  };
}

function mapLog(item: AdminTrainingLog) {
  return {
    time: formatDateTime(item.createdAt),
    operator: item.operatorName || '-',
    action: item.action || '-',
    content: item.content || '-'
  };
}

function apiStatusToText(status?: string): CourseStatus {
  return status === 'PUBLISHED' || status === 'published' ? '已发布' : '未发布';
}

function statusToApi(status?: string) {
  if (status === '已发布') return 'PUBLISHED';
  if (status === '未发布') return 'DRAFT';
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
  if (mode === '协同实训') return 'COLLABORATIVE';
  return undefined;
}

function apiTrainingModeToText(mode?: string): '单人实训' | '协同实训' {
  return mode === 'SINGLE' ? '单人实训' : '协同实训';
}

function semesterIdFromLabel(label: string) {
  const index = semesters.indexOf(label);
  return index >= 0 ? index + 1 : undefined;
}

function formatDateTime(value?: string) {
  if (!value) return '-';
  return value.replace('T', ' ').slice(0, 16);
}

function formatNumber(value?: number) {
  if (value === undefined || value === null || Number.isNaN(Number(value))) return '-';
  return Number(value).toFixed(1).replace(/\.0$/, '');
}

function passRate(stats: AdminTrainingStatistics) {
  const totalCount = Number(stats.participantCount || 0);
  const finishedCount = Number(stats.submittedAttemptCount || 0);
  if (!totalCount) return '0%';
  return `${Math.round((finishedCount / totalCount) * 100)}%`;
}

onMounted(() => {
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
  min-width: 1400px;
  border-collapse: collapse;
  table-layout: fixed;
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
