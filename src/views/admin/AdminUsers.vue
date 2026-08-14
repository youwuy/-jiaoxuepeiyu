<template>
  <AdminShell activeKey="users">
    <section v-if="teacherFormPageVisible" class="admin-user-teacher-form-page">
      <el-breadcrumb class="admin-user-teacher-breadcrumb" separator="/">
        <el-breadcrumb-item>系统管理</el-breadcrumb-item>
        <el-breadcrumb-item>用户管理</el-breadcrumb-item>
        <el-breadcrumb-item>{{ formMode === 'create' ? '新增教师' : '编辑教师' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-user-teacher-card">
        <h3><i></i>基本信息</h3>
        <div class="admin-user-teacher-grid basic">
          <label>
            <span>姓名 <b>*</b></span>
            <el-input v-model="form.realName" maxlength="30" placeholder="请输入姓名" />
            <small>最多输入30个字</small>
          </label>
          <label>
            <span>工号 <b>*</b></span>
            <el-input v-model="form.accountNo" :disabled="formMode === 'edit'" maxlength="30" placeholder="请输入工号" />
            <small>最多输入30个字</small>
          </label>
          <label>
            <span>手机号 <b>*</b></span>
            <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" />
          </label>
          <label>
            <span>身份证号</span>
            <el-input v-model="form.idCard" maxlength="18" placeholder="请输入18位身份证号" />
          </label>
          <label>
            <span>岗位</span>
            <el-input v-model="form.jobTitle" maxlength="10" placeholder="请输入岗位" />
            <small>最多输入10个字</small>
          </label>
        </div>
      </section>

      <section class="admin-user-teacher-card compact">
        <h3><i></i>组织信息</h3>
        <div class="admin-user-teacher-grid org">
          <label>
            <span>所属组织 <b>*</b></span>
            <el-select v-model="form.orgId" placeholder="请选择所属组织" filterable>
              <el-option v-for="org in orgOptions" :key="org.orgId" :label="org.label" :value="org.orgId" />
            </el-select>
          </label>
          <label>
            <span>管理组织 <em title="该教师可管理的数据组织范围">i</em></span>
            <el-select v-model="form.managedOrgIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择管理组织" filterable>
              <el-option v-for="org in orgOptions" :key="org.orgId" :label="org.label" :value="org.orgId" />
            </el-select>
          </label>
          <label>
            <span>授课班级 <em title="该教师负责授课的班级，可多选">i</em></span>
            <el-select v-model="form.teachingClassIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择该教师的授课班级，可多选" filterable>
              <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
            </el-select>
          </label>
        </div>
      </section>

      <section class="admin-user-teacher-card bio">
        <h3><i></i>生物信息录入</h3>
        <div class="admin-user-teacher-bio-row">
          <div class="admin-user-bio-item">
            <span>人脸信息录入</span>
            <button type="button" class="admin-user-bio-box" :class="{ recorded: bioFaceRecorded }" :disabled="uploadingBio !== null || !can(formMode === 'create' ? 'create' : 'update')" @click="triggerBioUpload('face')">
              <el-icon><Camera /></el-icon>
              <strong>{{ uploadingBio === 'face' ? '人脸信息上传中...' : bioFaceRecorded ? '已录入人脸信息' : '点击拍照或上传照片' }}</strong>
            </button>
            <input ref="faceFileInput" type="file" accept="image/jpeg,image/png,image/bmp" capture="user" hidden @change="handleBioFileChange('face', $event)" />
            <p>状态：<b :class="{ recorded: bioFaceRecorded }">{{ bioFaceRecorded ? '已录入' : '未录入' }}</b></p>
          </div>
          <div class="admin-user-bio-item">
            <span>指纹信息录入</span>
            <button type="button" class="admin-user-bio-box" :class="{ recorded: bioFingerprintRecorded }" :disabled="uploadingBio !== null || !can(formMode === 'create' ? 'create' : 'update')" @click="triggerBioUpload('fingerprint')">
              <el-icon><Pointer /></el-icon>
              <strong>{{ uploadingBio === 'fingerprint' ? '指纹信息上传中...' : bioFingerprintRecorded ? '已录入指纹信息' : '点击录入指纹或上传指纹' }}</strong>
            </button>
            <input ref="fingerprintFileInput" type="file" accept="image/jpeg,image/png,image/bmp" hidden @change="handleBioFileChange('fingerprint', $event)" />
            <p>状态：<b :class="{ recorded: bioFingerprintRecorded }">{{ bioFingerprintRecorded ? '已录入' : '未录入' }}</b></p>
          </div>
        </div>
      </section>

      <footer class="admin-user-teacher-actions">
        <el-button class="admin-user-teacher-cancel" @click="cancelTeacherForm">取消</el-button>
        <el-button class="admin-user-teacher-confirm" type="primary" :disabled="!can(formMode === 'create' ? 'create' : 'update')" :loading="saving" @click="saveAccount">确定</el-button>
      </footer>
    </section>

    <section v-else-if="studentFormPageVisible" class="admin-user-student-form-page">
      <nav class="admin-user-student-topbar">
        <el-breadcrumb class="admin-user-student-breadcrumb" separator="/">
          <el-breadcrumb-item>系统管理</el-breadcrumb-item>
          <el-breadcrumb-item>用户管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ formMode === 'create' ? '新增学员' : '编辑学员' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </nav>

      <main class="admin-user-student-content">
        <section class="admin-user-student-card">
          <h3><i></i>基本信息</h3>
          <div class="admin-user-student-grid first">
            <label>
              <span>姓名 <b>*</b></span>
              <el-input v-model="form.realName" maxlength="30" placeholder="请输入姓名" />
              <small>最多输入30个字</small>
            </label>
            <label>
              <span>学号 <b>*</b></span>
              <el-input v-model="form.accountNo" :disabled="formMode === 'edit'" maxlength="30" placeholder="请输入学号" />
              <small>最多输入30个字</small>
            </label>
            <label>
              <span>手机号 <b>*</b></span>
              <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" />
            </label>
          </div>
          <div class="admin-user-student-grid second">
            <label>
              <span>身份证号</span>
              <el-input v-model="form.idCard" maxlength="18" placeholder="请输入18位身份证号" />
            </label>
            <label>
              <span>所在班级 <b>*</b></span>
              <el-select v-model="form.classId" placeholder="请选择所在班级" filterable>
                <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
              </el-select>
            </label>
          </div>
        </section>

        <section class="admin-user-student-card bio">
          <h3><i></i>生物信息录入</h3>
          <div class="admin-user-student-bio-row">
            <div class="admin-user-student-bio-item">
              <span>人脸信息录入</span>
              <button type="button" class="admin-user-student-bio-box" :class="{ recorded: bioFaceRecorded }" :disabled="uploadingBio !== null || !can(formMode === 'create' ? 'create' : 'update')" @click="triggerBioUpload('face')">
                <el-icon><Camera /></el-icon>
                <strong>{{ uploadingBio === 'face' ? '人脸信息上传中...' : bioFaceRecorded ? '已录入人脸信息' : '点击拍照或上传照片' }}</strong>
              </button>
              <input ref="faceFileInput" type="file" accept="image/jpeg,image/png,image/bmp" capture="user" hidden @change="handleBioFileChange('face', $event)" />
              <p>状态：<b :class="{ recorded: bioFaceRecorded }">{{ bioFaceRecorded ? '已录入' : '未录入' }}</b></p>
            </div>
            <div class="admin-user-student-bio-item">
              <span>指纹信息录入</span>
              <button type="button" class="admin-user-student-bio-box" :class="{ recorded: bioFingerprintRecorded }" :disabled="uploadingBio !== null || !can(formMode === 'create' ? 'create' : 'update')" @click="triggerBioUpload('fingerprint')">
                <el-icon><Pointer /></el-icon>
                <strong>{{ uploadingBio === 'fingerprint' ? '指纹信息上传中...' : bioFingerprintRecorded ? '已录入指纹信息' : '点击录入指纹或上传指纹' }}</strong>
              </button>
              <input ref="fingerprintFileInput" type="file" accept="image/jpeg,image/png,image/bmp" hidden @change="handleBioFileChange('fingerprint', $event)" />
              <p>状态：<b :class="{ recorded: bioFingerprintRecorded }">{{ bioFingerprintRecorded ? '已录入' : '未录入' }}</b></p>
            </div>
          </div>
        </section>

        <footer class="admin-user-student-actions">
          <el-button class="admin-user-student-cancel" @click="cancelStudentForm">取消</el-button>
          <el-button class="admin-user-student-confirm" type="primary" :disabled="!can(formMode === 'create' ? 'create' : 'update')" :loading="saving" @click="saveAccount">确定</el-button>
        </footer>
      </main>
    </section>

    <section v-else-if="detailPageVisible" class="admin-user-detail-page">
      <nav class="admin-user-detail-topbar">
        <el-breadcrumb class="admin-user-detail-breadcrumb" separator="/">
          <el-breadcrumb-item>系统管理</el-breadcrumb-item>
          <el-breadcrumb-item>用户管理</el-breadcrumb-item>
          <el-breadcrumb-item>信息查看</el-breadcrumb-item>
        </el-breadcrumb>
      </nav>

      <main v-if="detailAccount" class="admin-user-detail-content">
        <section class="admin-user-detail-card">
          <h3><i></i>基本信息</h3>
          <dl class="admin-user-detail-grid">
            <div><dt>姓名</dt><dd>{{ detailAccount.realName }}</dd></div>
            <div><dt>{{ detailAccount.userType === 'student' ? '学号' : '工号/学号' }}</dt><dd>{{ detailAccount.accountNo }}</dd></div>
            <div><dt>手机号</dt><dd>{{ detailAccount.maskedPhone || detailAccount.phone || '-' }}</dd></div>
            <div><dt>身份证号</dt><dd>{{ detailAccount.maskedIdCard || '-' }}</dd></div>
            <div v-if="detailAccount.userType === 'teacher'"><dt>岗位</dt><dd>{{ detailAccount.jobTitle || '-' }}</dd></div>
            <div v-else><dt>所在班级</dt><dd>{{ detailAccount.className || '-' }}</dd></div>
          </dl>
        </section>

        <section v-if="detailAccount.userType === 'teacher'" class="admin-user-detail-card">
          <h3><i></i>组织信息</h3>
          <dl class="admin-user-detail-grid">
            <div><dt>所属组织</dt><dd>{{ detailAccount.orgName || '-' }}</dd></div>
            <div><dt>管理组织</dt><dd>{{ orgNames(detailAccount.managedOrgIds) }}</dd></div>
            <div><dt>授课班级</dt><dd>{{ classNames(detailAccount.teachingClassIds) }}</dd></div>
          </dl>
        </section>

        <section class="admin-user-detail-card bio">
          <h3><i></i>生物信息</h3>
          <div class="admin-user-detail-bio-row">
            <div class="admin-user-detail-bio-item">
              <span>人脸数据</span>
              <div class="admin-user-detail-face" :class="{ empty: !detailAccount.faceFileUrl }">
                <img v-if="detailAccount.faceFileUrl" :src="detailAccount.faceFileUrl" alt="人脸数据" />
                <el-icon v-else><Camera /></el-icon>
              </div>
              <p :class="{ recorded: detailAccount.faceRecorded }"><i></i>{{ detailAccount.faceRecorded ? '已录入' : '未录入' }}</p>
            </div>
            <div class="admin-user-detail-bio-item">
              <span>指纹数据</span>
              <div class="admin-user-detail-fingerprint">
                <el-icon><Pointer /></el-icon>
              </div>
              <p :class="{ recorded: detailAccount.fingerprintRecorded }"><i></i>{{ detailAccount.fingerprintRecorded ? '已录入' : '未录入' }}</p>
            </div>
          </div>
        </section>

        <footer class="admin-user-detail-actions">
          <el-button @click="closeDetailPage">返回</el-button>
        </footer>
      </main>
    </section>

    <section v-else class="admin-users-page">
      <el-breadcrumb class="admin-users-breadcrumb" separator="/">
        <el-breadcrumb-item>系统基础设置</el-breadcrumb-item>
        <el-breadcrumb-item>用户管理</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-users-workspace">
        <aside class="admin-users-org-panel">
          <h2>所属组织</h2>
          <el-tree
            class="admin-users-org-tree"
            :data="orgTreeWithAll"
            node-key="orgId"
            default-expand-all
            highlight-current
            :current-node-key="selectedOrgId"
            :props="{ label: 'orgName', children: 'children', disabled: 'disabled' }"
            @node-click="selectOrg"
          />
        </aside>

        <section class="admin-users-main-panel">
          <section class="admin-users-tabs">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              type="button"
              :class="{ active: activeKind === tab.key }"
              @click="switchKind(tab.key)"
            >
              <span>{{ tab.label.replace('管理', '') }}</span>
            </button>
          </section>

          <section class="admin-users-toolbar">
            <div class="admin-users-filter-grid">
              <el-input v-model="draft.realName" :prefix-icon="Search" placeholder="姓名搜索" clearable @keyup.enter="applySearch" />
              <el-input v-model="draft.accountNo" :prefix-icon="Search" :placeholder="activeKind === 'teacher' ? '工号搜索' : '学号搜索'" clearable @keyup.enter="applySearch" />
              <el-input v-model="draft.phone" :prefix-icon="Search" placeholder="手机号搜索" clearable @keyup.enter="applySearch" />
              <el-input v-if="activeKind === 'teacher'" v-model="draft.jobTitle" :prefix-icon="Search" placeholder="岗位搜索" clearable @keyup.enter="applySearch" />
              <el-select v-if="activeKind === 'student'" v-model="draft.classId" placeholder="所在班级" clearable filterable>
                <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
              </el-select>
              <el-select v-model="draft.enabled" placeholder="账号状态" clearable>
                <el-option label="启用" :value="true" />
                <el-option label="禁用" :value="false" />
              </el-select>
              <div class="admin-users-filter-actions">
                <el-button class="admin-users-ghost-button" @click="applySearch">查询</el-button>
                <el-button class="admin-users-ghost-button" @click="resetSearch">重置</el-button>
              </div>
            </div>
            <div class="admin-users-head-actions">
              <el-button class="admin-users-primary-button" type="primary" :disabled="!can('create')" @click="openCreate">
                <el-icon><Plus /></el-icon>
                新增{{ activeKindLabel }}
              </el-button>
              <el-button class="admin-users-lite-button" :disabled="!can('create')" @click="openImport">批量导入</el-button>
              <el-button class="admin-users-lite-button" @click="exportRows">批量导出</el-button>
              <el-button class="admin-users-lite-button" :disabled="selectedIds.length === 0 || !can('update')" @click="openBatchReset">批量重置密码</el-button>
              <el-button v-if="activeKind === 'teacher'" class="admin-users-lite-button" :disabled="selectedIds.length === 0 || !can('update')" @click="openBatchOrg">批量设置所属组织</el-button>
              <el-button v-if="activeKind === 'teacher'" class="admin-users-lite-button" :disabled="selectedIds.length === 0 || !can('update')" @click="openBatchRole">批量修改角色</el-button>
            </div>
          </section>

          <section class="admin-users-table-card">
            <div v-if="loading" class="admin-course-empty">用户加载中...</div>
            <template v-else>
              <div class="admin-users-table-scroll">
                <table class="admin-users-table">
                  <thead>
                    <tr>
                      <th class="check-col">
                        <el-checkbox :model-value="allCurrentSelected" :indeterminate="partSelected" @change="toggleAll" />
                      </th>
                      <th>姓名</th>
                      <th>{{ activeKind === 'teacher' ? '工号' : '学号' }}</th>
                      <th>手机号</th>
                      <th v-if="activeKind === 'teacher'">岗位</th>
                      <th v-if="activeKind === 'teacher'">所属组织</th>
                      <th v-if="activeKind === 'teacher'">管理组织</th>
                      <th>{{ activeKind === 'teacher' ? '授课班级' : '班级' }}</th>
                      <th v-if="activeKind === 'teacher'">角色</th>
                      <th>状态</th>
                      <th>创建时间</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="row in visibleAccounts" :key="row.userId" :class="{ disabled: !row.enabled }">
                      <td class="check-col">
                        <el-checkbox :model-value="selectedIds.includes(row.userId)" @change="toggleOne(row.userId)" />
                      </td>
                      <td class="strong-cell">{{ row.realName }}</td>
                      <td>{{ row.accountNo }}</td>
                      <td>{{ row.maskedPhone || row.phone || '-' }}</td>
                      <td v-if="activeKind === 'teacher'">{{ row.jobTitle || '-' }}</td>
                      <td v-if="activeKind === 'teacher'" class="wrap-cell">{{ row.orgName || '-' }}</td>
                      <td v-if="activeKind === 'teacher'" class="wrap-cell">{{ orgNames(row.managedOrgIds) }}</td>
                      <td class="wrap-cell">{{ activeKind === 'teacher' ? classNames(row.teachingClassIds) : row.className || '-' }}</td>
                      <td v-if="activeKind === 'teacher'" class="wrap-cell">{{ compactList(row.roleNames) }}</td>
                      <td>
                        <span class="admin-users-status" :class="row.enabled ? 'enabled' : 'disabled'">
                          <i></i>
                          {{ row.enabled ? '启用' : '禁用' }}
                        </span>
                      </td>
                      <td>{{ formatAccountTime(row.createdAt) }}</td>
                      <td>
                        <div class="admin-users-row-actions">
                          <el-button class="edit" :disabled="!can('update')" @click="openEdit(row)">编辑</el-button>
                          <el-button v-if="activeKind === 'teacher'" class="edit" :disabled="!can('update')" @click="openRole(row)">设置角色</el-button>
                          <el-button :class="row.enabled ? 'warn' : 'enable'" :disabled="!can(row.enabled ? 'disable' : 'enable')" :loading="busyId === row.userId" @click="toggleEnabled(row)">
                            {{ row.enabled ? '禁用' : '启用' }}
                          </el-button>
                          <el-button class="plain" :disabled="!can('update')" @click="openReset(row)">重置密码</el-button>
                          <el-button class="plain" @click="openDetail(row)">查看</el-button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <footer class="admin-users-footer">
                <p>共 <strong>{{ page.total }}</strong> 条记录，每页</p>
                <el-select v-model="page.pageSize" class="admin-users-page-size" @change="loadAccounts">
                  <el-option label="10" :value="10" />
                  <el-option label="20" :value="20" />
                  <el-option label="50" :value="50" />
                </el-select>
                <p>条</p>
                <el-pagination
                  v-model:current-page="page.page"
                  :page-size="page.pageSize"
                  :total="page.total"
                  layout="prev, pager, next"
                  background
                  @current-change="loadAccounts"
                />
              </footer>
            </template>
          </section>
        </section>
      </section>
    </section>

    <el-dialog v-model="resetVisible" class="admin-users-mini-dialog" width="480px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-users-dialog-head">
          <strong>重置密码</strong>
          <el-button text circle :icon="Close" @click="resetVisible = false" />
        </div>
      </template>
      <p class="admin-users-reset-confirm">
        确认将所选 {{ resetIds.length }} 个账号的密码重置为系统初始密码吗？
      </p>
      <template #footer>
        <div class="admin-users-dialog-footer">
          <el-button @click="resetVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!can('update')" :loading="saving" @click="saveResetPassword">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" class="admin-users-mini-dialog" width="480px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-users-dialog-head">
          <strong>{{ roleMode === 'batch' ? '批量修改角色' : '设置角色' }}</strong>
          <el-button text circle :icon="Close" @click="roleVisible = false" />
        </div>
      </template>
      <el-select v-model="roleFormIds" class="admin-users-full-select" placeholder="请选择角色" multiple collapse-tags collapse-tags-tooltip>
        <el-option v-for="role in roleOptions" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
      </el-select>
      <template #footer>
        <div class="admin-users-dialog-footer">
          <el-button @click="roleVisible = false">关闭</el-button>
          <el-button type="primary" :loading="saving" @click="saveRole">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="batchOrgVisible" class="admin-users-mini-dialog" width="480px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-users-dialog-head">
          <strong>批量设置所属组织</strong>
          <el-button text circle :icon="Close" @click="batchOrgVisible = false" />
        </div>
      </template>
      <el-select v-model="batchOrgId" class="admin-users-full-select" placeholder="请选择所属组织" filterable>
        <el-option v-for="org in orgOptions" :key="org.orgId" :label="org.label" :value="org.orgId" />
      </el-select>
      <template #footer>
        <div class="admin-users-dialog-footer">
          <el-button @click="batchOrgVisible = false">关闭</el-button>
          <el-button type="primary" :loading="saving" @click="saveBatchOrg">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" class="admin-users-import-dialog" width="520px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-users-dialog-head">
          <strong>导入{{ activeKindLabel }}</strong>
          <el-button text circle :icon="Close" @click="importVisible = false" />
        </div>
      </template>
      <div class="admin-users-import-tip">
        <strong>操作说明</strong>
        <span>请先下载导入模板，按模板格式填写{{ activeKindLabel }}信息后再上传文件</span>
      </div>
      <button type="button" class="admin-users-template-button" @click="downloadImportTemplate">下载导入模板</button>
      <div class="admin-users-upload-divider"><span>上传文件</span></div>
      <label class="admin-users-upload-box">
        <input ref="importFileInput" type="file" accept=".xlsx,.csv,.txt" @change="handleImportFile" />
        <el-icon><UploadFilled /></el-icon>
        <strong>{{ importFileName || '点击上传或拖拽文件到此处' }}</strong>
        <span>支持 xlsx/csv/txt，文件大小不超过10MB</span>
      </label>
      <div v-if="importPreview" class="admin-users-import-preview">
        <div v-for="row in importPreview.rows.slice(0, 5)" :key="row.rowNo" :class="{ error: row.valid === false }">
          第 {{ row.rowNo }} 行：{{ row.accountNo }} / {{ row.realName }}
          <span>{{ row.errors?.join('；') || '校验通过' }}</span>
        </div>
      </div>
      <template #footer>
        <div class="admin-users-dialog-footer">
          <el-button @click="importVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitImportRows">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Camera, Close, Plus, Pointer, Search, UploadFilled } from '@element-plus/icons-vue';
import * as XLSX from 'xlsx';
import AdminShell from '../../components/admin/AdminShell.vue';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';
import {
  createAdminAccount,
  disableAdminAccount,
  enableAdminAccount,
  exportAdminAccounts,
  fetchAdminAccountDetail,
  fetchAdminAccounts,
  fetchAdminClasses,
  fetchAdminRoles,
  importAdminAccounts,
  previewAdminAccountImport,
  resetAdminAccountPasswords,
  updateAdminAccount,
  updateAdminAccountOrg,
  updateAdminTeacherRoles,
  uploadAdminBiometricFile,
  type AdminAccount,
  type AdminAccountCommand,
  type AdminAccountImportPreview,
  type AdminAccountImportRow,
  type AdminAccountKind,
  type AdminAccountQuery,
  type AdminClassOption,
  type AdminRoleOption
} from '../../api/admin-account';
import { fetchAdminOrgTree } from '../../api/admin-org';
import type { AdminOrgNode } from '../../api/admin-org';
import {
  accountKindLabel,
  compactList,
  flattenOrgOptions,
  formatAccountTime,
  normalizeRoleOptions
} from '../../features/admin/accounts';

type FormMode = 'create' | 'edit';

const activeKind = ref<AdminAccountKind>('teacher');
const { can } = useAdminPermissions('system:user');
const loading = ref(false);
const saving = ref(false);
const busyId = ref<number | null>(null);
const accounts = ref<AdminAccount[]>([]);
const selectedIds = ref<number[]>([]);
const tabs = ref([
  { key: 'teacher' as AdminAccountKind, label: '教师管理', count: 0 },
  { key: 'student' as AdminAccountKind, label: '学员管理', count: 0 }
]);
const page = reactive({ page: 1, pageSize: 20, total: 0 });
const query = reactive<AdminAccountQuery>({});
const draft = reactive<AdminAccountQuery>({ enabled: null });
const selectedOrgId = ref(0);
const orgTree = ref<AdminOrgNode[]>([]);
const classOptions = ref<AdminClassOption[]>([]);
const roleOptions = ref<AdminRoleOption[]>([]);
const teacherFormPageVisible = ref(false);
const studentFormPageVisible = ref(false);
const detailPageVisible = ref(false);
const bioFaceRecorded = ref(false);
const bioFingerprintRecorded = ref(false);
const uploadingBio = ref<'face' | 'fingerprint' | null>(null);
const faceFileInput = ref<HTMLInputElement | null>(null);
const fingerprintFileInput = ref<HTMLInputElement | null>(null);
const formMode = ref<FormMode>('create');
const editingId = ref<number | null>(null);
const detailAccount = ref<AdminAccount | null>(null);
const roleVisible = ref(false);
const roleAccount = ref<AdminAccount | null>(null);
const roleMode = ref<'single' | 'batch'>('single');
const roleFormIds = ref<number[]>([]);
const batchOrgVisible = ref(false);
const batchOrgId = ref<number | null>(null);
const resetVisible = ref(false);
const resetIds = ref<number[]>([]);
const importVisible = ref(false);
const importText = ref('');
const importFileName = ref('');
const importPreview = ref<AdminAccountImportPreview | null>(null);
const importFileInput = ref<HTMLInputElement | null>(null);

const emptyForm = (): AdminAccountCommand => ({
  realName: '',
  accountNo: '',
  phone: '',
  idCard: '',
  jobTitle: '',
  orgId: null,
  classId: null,
  faceFileId: null,
  fingerprintFileId: null,
  initialPassword: undefined,
  roleIds: [],
  managedOrgIds: [],
  teachingClassIds: []
});

const form = reactive<AdminAccountCommand>(emptyForm());
const activeKindLabel = computed(() => accountKindLabel(activeKind.value));
const orgOptions = computed(() => flattenOrgOptions(orgTree.value));
function mapOrgTree(nodes: AdminOrgNode[]): Array<AdminOrgNode & { disabled?: boolean }> {
  return nodes.map((node) => ({
    ...node,
    orgName: node.enabled ? node.orgName : `${node.orgName} (已禁用)`,
    disabled: !node.enabled,
    children: mapOrgTree(node.children ?? [])
  }));
}

const orgTreeWithAll = computed(() => [
  {
    orgId: 0,
    orgName: '全部',
    parentId: null,
    sortOrder: 0,
    enabled: true,
    children: mapOrgTree(orgTree.value)
  }
]);
const visibleAccounts = computed(() => accounts.value);
const allCurrentSelected = computed(() => visibleAccounts.value.length > 0 && visibleAccounts.value.every((row) => selectedIds.value.includes(row.userId)));
const partSelected = computed(() => selectedIds.value.length > 0 && !allCurrentSelected.value);

const accountState = {
  teacher: { query: {} as AdminAccountQuery, draft: { enabled: null } as AdminAccountQuery, page: 1, pageSize: 20, selectedIds: [] as number[], orgId: 0 },
  student: { query: {} as AdminAccountQuery, draft: { enabled: null } as AdminAccountQuery, page: 1, pageSize: 20, selectedIds: [] as number[], orgId: 0 }
};

function saveAccountState(kind: AdminAccountKind) {
  accountState[kind].query = { ...query };
  accountState[kind].draft = { ...draft };
  accountState[kind].page = page.page;
  accountState[kind].pageSize = page.pageSize;
  accountState[kind].selectedIds = [...selectedIds.value];
  accountState[kind].orgId = selectedOrgId.value;
}

function restoreAccountState(kind: AdminAccountKind) {
  Object.assign(query, accountState[kind].query);
  Object.assign(draft, { enabled: null }, accountState[kind].draft);
  page.page = accountState[kind].page;
  page.pageSize = accountState[kind].pageSize;
  selectedIds.value = [...accountState[kind].selectedIds];
  selectedOrgId.value = accountState[kind].orgId;
}

function applyForm(next: AdminAccountCommand) {
  Object.assign(form, emptyForm(), next);
}

function resetBioState(faceRecorded = false, fingerprintRecorded = false) {
  bioFaceRecorded.value = faceRecorded;
  bioFingerprintRecorded.value = fingerprintRecorded;
}

function currentQuery() {
  return {
    ...query,
    page: page.page,
    pageSize: page.pageSize
  };
}

async function loadOptions() {
  try {
    orgTree.value = await fetchAdminOrgTree();
  } catch (error) {
    orgTree.value = [];
    ElMessage.error(error instanceof Error ? error.message : '组织选项加载失败');
  }

  try {
    classOptions.value = await fetchAdminClasses();
  } catch (error) {
    classOptions.value = [];
    ElMessage.error(error instanceof Error ? error.message : '班级选项加载失败');
  }

  try {
    roleOptions.value = normalizeRoleOptions(await fetchAdminRoles()).filter(
      (role) => role.enabled !== false && role.roleCode !== 'super_admin' && role.roleName !== '超级管理员'
    );
  } catch (error) {
    roleOptions.value = [];
    ElMessage.error(error instanceof Error ? error.message : '角色选项加载失败');
  }
}

async function loadAccounts(clearSelection = true) {
  loading.value = true;
  if (clearSelection) selectedIds.value = [];
  try {
    const result = await fetchAdminAccounts(activeKind.value, currentQuery());
    accounts.value = result.records;
    page.total = result.total;
    page.page = result.page;
    page.pageSize = result.pageSize;
    updateTabCount(result.total);
    saveAccountState(activeKind.value);
  } catch (error) {
    accounts.value = [];
    page.total = 0;
    updateTabCount(0);
    ElMessage.error(error instanceof Error ? error.message : '用户列表加载失败');
  } finally {
    loading.value = false;
  }
}

async function refreshTabCounts() {
  try {
    const [teacherResult, studentResult] = await Promise.all([
      fetchAdminAccounts('teacher', { page: 1, pageSize: 1 }),
      fetchAdminAccounts('student', { page: 1, pageSize: 1 })
    ]);
    tabs.value = tabs.value.map((tab) => ({
      ...tab,
      count: tab.key === 'teacher' ? teacherResult.total : studentResult.total
    }));
  } catch {
    // keep current counts if the aggregate request fails
  }
}

function updateTabCount(total: number) {
  tabs.value = tabs.value.map((tab) => (tab.key === activeKind.value ? { ...tab, count: total } : tab));
}

function switchKind(kind: AdminAccountKind) {
  if (activeKind.value === kind) return;
  saveAccountState(activeKind.value);
  teacherFormPageVisible.value = false;
  studentFormPageVisible.value = false;
  detailPageVisible.value = false;
  activeKind.value = kind;
  restoreAccountState(kind);
  loadAccounts(false);
}

function applySearch() {
  Object.assign(query, {
    realName: draft.realName?.trim() || undefined,
    accountNo: draft.accountNo?.trim() || undefined,
    phone: draft.phone?.trim() || undefined,
    jobTitle: activeKind.value === 'teacher' ? draft.jobTitle?.trim() || undefined : undefined,
    orgId: draft.orgId || undefined,
    classId: activeKind.value === 'student' ? draft.classId || undefined : undefined,
    enabled: draft.enabled
  });
  page.page = 1;
  selectedIds.value = [];
  loadAccounts();
}

function resetSearch() {
  selectedOrgId.value = 0;
  Object.assign(draft, { realName: '', accountNo: '', phone: '', jobTitle: '', orgId: null, classId: null, enabled: null });
  Object.assign(query, { realName: undefined, accountNo: undefined, phone: undefined, jobTitle: undefined, orgId: undefined, classId: undefined, enabled: undefined });
  page.page = 1;
  selectedIds.value = [];
  loadAccounts();
}

function toggleAll(value: string | number | boolean) {
  selectedIds.value = value ? visibleAccounts.value.map((row) => row.userId) : [];
}

function toggleOne(userId: number) {
  selectedIds.value = selectedIds.value.includes(userId)
    ? selectedIds.value.filter((id) => id !== userId)
    : [...selectedIds.value, userId];
}

function openCreate() {
  formMode.value = 'create';
  editingId.value = null;
  applyForm(emptyForm());
  form.orgId = activeKind.value === 'teacher' ? selectedOrgId.value || null : null;
  resetBioState();
  if (activeKind.value === 'teacher') {
    teacherFormPageVisible.value = true;
    return;
  }
  studentFormPageVisible.value = true;
}

function accountToForm(account: AdminAccount): AdminAccountCommand {
  return {
    realName: account.realName,
    accountNo: account.accountNo,
    phone: account.phone || '',
    idCard: account.idCard || '',
    jobTitle: account.jobTitle || '',
    orgId: account.orgId ?? null,
    classId: account.classId ?? null,
    faceFileId: account.faceFileId ?? null,
    fingerprintFileId: account.fingerprintFileId ?? null,
    roleIds: account.roleIds ?? [],
    managedOrgIds: account.managedOrgIds ?? [],
    teachingClassIds: account.teachingClassIds ?? []
  };
}

async function openEdit(row: AdminAccount) {
  formMode.value = 'edit';
  editingId.value = row.userId;
  let account = row;
  try {
    account = await fetchAdminAccountDetail(row.userId);
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '用户详情加载失败，已使用列表数据回填');
  }
  applyForm(accountToForm(account));
  resetBioState(Boolean(account.faceRecorded), Boolean(account.fingerprintRecorded));
  if (activeKind.value === 'teacher') {
    teacherFormPageVisible.value = true;
    return;
  }
  studentFormPageVisible.value = true;
}

function validateForm() {
  if (!form.realName.trim()) {
    throw new Error('请输入姓名');
  }
  if (!form.accountNo?.trim()) {
    throw new Error(activeKind.value === 'teacher' ? '请输入工号' : '请输入学号');
  }
  if (activeKind.value === 'teacher' && !form.orgId) {
    throw new Error('请选择所属组织');
  }
  if (!form.phone?.trim()) {
    throw new Error('请输入11位手机号');
  }
  if (!/^1\d{10}$/.test(form.phone)) {
    throw new Error('请输入11位手机号');
  }
  if (activeKind.value === 'student' && !form.classId) {
    throw new Error('请选择班级');
  }
}

function cancelTeacherForm() {
  teacherFormPageVisible.value = false;
  editingId.value = null;
  applyForm(emptyForm());
  resetBioState();
}

function cancelStudentForm() {
  studentFormPageVisible.value = false;
  editingId.value = null;
  applyForm(emptyForm());
  resetBioState();
}

function triggerBioUpload(type: 'face' | 'fingerprint') {
  const input = type === 'face' ? faceFileInput.value : fingerprintFileInput.value;
  input?.click();
}

async function handleBioFileChange(type: 'face' | 'fingerprint', event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file) return;
  if (!['image/jpeg', 'image/png', 'image/bmp'].includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG、BMP 图片格式');
    return;
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('生物信息图片大小不能超过10MB');
    return;
  }

  uploadingBio.value = type;
  try {
    const uploaded = await uploadAdminBiometricFile(file, type);
    if (!uploaded.fileId) {
      throw new Error('上传接口未返回文件ID');
    }
    if (type === 'face') {
      form.faceFileId = uploaded.fileId;
      bioFaceRecorded.value = true;
    } else {
      form.fingerprintFileId = uploaded.fileId;
      bioFingerprintRecorded.value = true;
    }
    ElMessage.success(type === 'face' ? '人脸信息上传成功' : '指纹信息上传成功');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '生物信息上传失败');
  } finally {
    uploadingBio.value = null;
  }
}

async function saveAccount() {
  try {
    validateForm();
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善用户信息');
    return;
  }

  saving.value = true;
  try {
    if (formMode.value === 'create') {
      const command = activeKind.value === 'student'
        ? { ...form, orgId: null, initialPassword: undefined }
        : { ...form, initialPassword: undefined };
      await createAdminAccount(activeKind.value, command);
      ElMessage.success(`新增${activeKindLabel.value}成功`);
    } else if (editingId.value) {
      await updateAdminAccount(activeKind.value, editingId.value, form);
      ElMessage.success(`编辑${activeKindLabel.value}成功`);
    }
    teacherFormPageVisible.value = false;
    studentFormPageVisible.value = false;
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function toggleEnabled(row: AdminAccount) {
  busyId.value = row.userId;
  try {
    if (row.enabled) {
      await disableAdminAccount(row.userId);
      ElMessage.success('账号已禁用');
    } else {
      await enableAdminAccount(row.userId);
      ElMessage.success('账号已启用');
    }
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败');
  } finally {
    busyId.value = null;
  }
}

async function openDetail(row: AdminAccount) {
  detailAccount.value = row;
  detailPageVisible.value = true;
  try {
    detailAccount.value = await fetchAdminAccountDetail(row.userId);
  } catch {
    detailAccount.value = row;
  }
}

function closeDetailPage() {
  detailPageVisible.value = false;
  detailAccount.value = null;
}

function openRole(row: AdminAccount) {
  roleMode.value = 'single';
  roleAccount.value = row;
  roleFormIds.value = [...(row.roleIds ?? [])];
  roleVisible.value = true;
}

function openBatchRole() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择教师');
    return;
  }
  roleMode.value = 'batch';
  roleAccount.value = null;
  roleFormIds.value = [];
  roleVisible.value = true;
}

async function saveRole() {
  if (roleMode.value === 'single' && !roleAccount.value) {
    return;
  }
  if (!roleFormIds.value.length) {
    ElMessage.warning('请选择角色');
    return;
  }
  saving.value = true;
  try {
    if (roleMode.value === 'batch') {
      await Promise.all(selectedIds.value.map((userId) => updateAdminTeacherRoles(userId, roleFormIds.value)));
    } else if (roleAccount.value) {
      await updateAdminTeacherRoles(roleAccount.value.userId, roleFormIds.value);
    }
    ElMessage.success('角色设置成功');
    roleVisible.value = false;
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '角色设置失败');
  } finally {
    saving.value = false;
  }
}

async function openBatchReset() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择用户');
    return;
  }
  resetIds.value = [...selectedIds.value];
  resetVisible.value = true;
}

function selectOrg(node: AdminOrgNode & { orgId: number }) {
  selectedOrgId.value = node.orgId;
  draft.orgId = node.orgId === 0 ? null : node.orgId;
  query.orgId = node.orgId === 0 ? undefined : node.orgId;
  page.page = 1;
  selectedIds.value = [];
  loadAccounts();
}

function orgNames(ids?: number[]) {
  if (!ids || ids.length === 0) {
    return '-';
  }
  const labels = ids.map((id) => orgOptions.value.find((item) => item.orgId === id)?.orgName).filter(Boolean);
  return labels.length > 0 ? labels.join('、') : '-';
}

function classNames(ids?: number[]) {
  if (!ids || ids.length === 0) {
    return '-';
  }
  const labels = ids.map((id) => classOptions.value.find((item) => item.classId === id)?.className).filter(Boolean);
  return labels.length > 0 ? labels.join('、') : '-';
}

function openReset(row: AdminAccount) {
  resetIds.value = [row.userId];
  resetVisible.value = true;
}

async function saveResetPassword() {
  if (resetIds.value.length === 0) {
    ElMessage.warning('请先选择用户');
    return;
  }
  saving.value = true;
  try {
    await resetAdminAccountPasswords(resetIds.value);
    ElMessage.success(resetIds.value.length > 1 ? '密码已批量重置' : '密码已重置');
    resetVisible.value = false;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '重置密码失败');
  } finally {
    saving.value = false;
  }
}

function openBatchOrg() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择用户');
    return;
  }
  batchOrgId.value = null;
  batchOrgVisible.value = true;
}

async function saveBatchOrg() {
  if (!batchOrgId.value) {
    ElMessage.warning('请选择所属组织');
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认将选中 ${selectedIds.value.length} 条数据批量变更至所选组织，操作不可回退`,
      '批量设置所属组织',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    );
  } catch {
    return;
  }
  saving.value = true;
  try {
    await updateAdminAccountOrg(selectedIds.value, batchOrgId.value);
    ElMessage.success('所属组织已更新');
    batchOrgVisible.value = false;
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批量设置失败');
  } finally {
    saving.value = false;
  }
}

function openImport() {
  importText.value = '';
  importFileName.value = '';
  importPreview.value = null;
  if (importFileInput.value) {
    importFileInput.value.value = '';
  }
  importVisible.value = true;
}

function downloadImportTemplate() {
  if (activeKind.value === 'teacher') {
    const url = `/templates/${encodeURIComponent('教师导入表格.xlsx')}`;
    const link = document.createElement('a');
    link.href = url;
    link.download = '教师导入表格.xlsx';
    link.click();
    return;
  }
  const worksheet = XLSX.utils.aoa_to_sheet([['学号', '姓名', '手机号', '所在班级']]);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '学员信息');
  XLSX.writeFile(workbook, '学员导入表格.xlsx');
}

async function handleImportFile(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) {
    return;
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过10MB');
    return;
  }
  importFileName.value = file.name;
  if (activeKind.value === 'teacher' && /\.xlsx?$/i.test(file.name)) {
    try {
      importText.value = JSON.stringify(parseTeacherWorkbook(await file.arrayBuffer()));
      await previewImportRows();
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '教师模板读取失败');
    }
    return;
  }
  if (activeKind.value === 'student' && /\.xlsx?$/i.test(file.name)) {
    try {
      importText.value = JSON.stringify(parseStudentWorkbook(await file.arrayBuffer()));
      await previewImportRows();
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '学员模板读取失败');
    }
    return;
  }
  const reader = new FileReader();
  reader.onload = () => {
    importText.value = String(reader.result || '');
    previewImportRows();
  };
  reader.onerror = () => ElMessage.error('文件读取失败');
  reader.readAsText(file);
}

function parseTeacherWorkbook(buffer: ArrayBuffer): AdminAccountImportRow[] {
  const workbook = XLSX.read(buffer, { type: 'array' });
  const sheet = workbook.Sheets[workbook.SheetNames[0]];
  if (!sheet) return [];
  const matrix = XLSX.utils.sheet_to_json<unknown[]>(sheet, { header: 1, defval: '', raw: false });
  const headerIndex = matrix.findIndex((row) => {
    const headers = row.map((cell) => String(cell ?? '').replace(/\s+/g, ''));
    return headers.includes('姓名') && headers.includes('工号') && headers.includes('所属组织');
  });
  if (headerIndex < 0) throw new Error('未找到“姓名、工号、所属组织”表头，请使用教师导入模板');
  const headers = matrix[headerIndex].map((cell) => String(cell ?? '').replace(/[\s*＊]/g, ''));
  return matrix.slice(headerIndex + 1).map((values, index) => {
    const row = Object.fromEntries(headers.map((header, column) => [header, String(values[column] ?? '').trim()]));
    const read = (...names: string[]) => names.map((name) => row[name.replace(/[\s*＊]/g, '')]).find(Boolean) || '';
    const managedOrgNames = read('管理组织').split('、').map((name) => name.trim()).filter(Boolean);
    const classNames = read('授课班级').split('、').map((name) => name.trim()).filter(Boolean);
    return {
      rowNo: headerIndex + index + 2,
      accountNo: read('工号'),
      realName: read('姓名'),
      phone: read('手机号'),
      idCard: read('身份证号'),
      jobTitle: read('岗位'),
      orgId: resolveOrgId(read('所属组织')),
      managedOrgIds: managedOrgNames.map(resolveOrgId).filter((id): id is number => id !== undefined),
      teachingClassIds: classNames.map(resolveClassId).filter((id): id is number => id !== undefined)
    };
  }).filter((row) => row.accountNo || row.realName || row.phone);
}

function parseStudentWorkbook(buffer: ArrayBuffer): AdminAccountImportRow[] {
  const workbook = XLSX.read(buffer, { type: 'array' });
  const sheet = workbook.Sheets[workbook.SheetNames[0]];
  if (!sheet) return [];
  const matrix = XLSX.utils.sheet_to_json<unknown[]>(sheet, { header: 1, defval: '', raw: false });
  const headerIndex = matrix.findIndex((row) => {
    const headers = row.map((cell) => String(cell ?? '').replace(/[\s*＊]/g, ''));
    return headers.includes('学号') && headers.includes('姓名') && headers.includes('所在班级');
  });
  if (headerIndex < 0) throw new Error('未找到“学号、姓名、所在班级”表头，请使用学员导入模板');
  const headers = matrix[headerIndex].map((cell) => String(cell ?? '').replace(/[\s*＊]/g, ''));
  return matrix.slice(headerIndex + 1).map((values, index) => {
    const row = Object.fromEntries(headers.map((header, column) => [header, String(values[column] ?? '').trim()]));
    const read = (...names: string[]) => names.map((name) => row[name.replace(/[\s*＊]/g, '')]).find(Boolean) || '';
    return {
      rowNo: headerIndex + index + 2,
      accountNo: read('学号'),
      realName: read('姓名'),
      phone: read('手机号'),
      classId: resolveClassId(read('所在班级'))
    };
  }).filter((row) => row.accountNo || row.realName || row.phone);
}

function resolveOrgId(path: string): number | undefined {
  if (!path) return undefined;
  const names = path.split('/').map((item) => item.trim()).filter(Boolean);
  const name = names[names.length - 1];
  return orgOptions.value.find((item) => item.orgName === name)?.orgId;
}

function resolveClassId(name: string): number | undefined {
  return classOptions.value.find((item) => item.className === name)?.classId;
}

function parseImportRows(): AdminAccountImportRow[] {
  if (importText.value.trim().startsWith('[')) {
    try {
      return JSON.parse(importText.value) as AdminAccountImportRow[];
    } catch {
      return [];
    }
  }
  return importText.value
    .split(/\n+/)
    .map((line) => line.trim())
    .filter(Boolean)
    .filter((line, index) => index > 0 || !/^(工号|学号),姓名,手机号/.test(line))
    .map((line, index) => {
      const [accountNo = '', realName = '', phone = '', className = ''] = line.split(',').map((item) => item.trim());
      return { rowNo: index + 1, accountNo, realName, phone, classId: resolveClassId(className) };
    })
    .filter((row) => row.accountNo || row.realName || row.phone);
}

async function previewImportRows() {
  const rows = parseImportRows();
  if (rows.length === 0) {
    ElMessage.warning('请输入导入内容');
    return;
  }
  try {
    importPreview.value = await previewAdminAccountImport(activeKind.value, rows);
  } catch (error) {
    importPreview.value = null;
    ElMessage.error(error instanceof Error ? error.message : '导入预览失败，请检查接口后重试');
  }
}

async function submitImportRows() {
  if (!importPreview.value) {
    await previewImportRows();
  }
  if (!importPreview.value) {
    return;
  }
  if (importPreview.value.errorCount > 0) {
    ElMessage.warning('导入文件存在异常，请修改后重新上传');
    return;
  }
  saving.value = true;
  try {
    await importAdminAccounts(activeKind.value, importPreview.value.rows);
    ElMessage.success('导入成功');
    importVisible.value = false;
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '导入失败');
  } finally {
    saving.value = false;
  }
}

async function exportRows() {
  try {
    await exportAdminAccounts(activeKind.value, currentQuery());
    ElMessage.success('导出数据已生成');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '导出失败');
  }
}

onMounted(async () => {
  await loadOptions();
  await refreshTabCounts();
  await loadAccounts();
});
</script>
