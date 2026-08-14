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
          <el-button v-if="can('create')" class="admin-theory-paper-primary" :icon="Plus" @click="openCreate">新增</el-button>
          <el-button v-if="can('create')" class="admin-theory-paper-primary" :icon="UploadFilled" @click="openImport">导入试卷</el-button>
          <el-button v-if="can('enable')" class="admin-theory-paper-lite" :disabled="selectedIds.length === 0" @click="batchSetEnabled(true)">批量启用</el-button>
          <el-button v-if="can('disable')" class="admin-theory-paper-lite" :disabled="selectedIds.length === 0" @click="batchSetEnabled(false)">批量禁用</el-button>
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
                    <el-button v-if="can('update')" text @click="openManage(row)">修改</el-button>
                    <el-button v-if="can(row.enabled ? 'disable' : 'enable')" text :class="row.enabled ? 'warn' : 'success'" @click="setEnabled(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
                    <el-button text @click="openLogs(row)">操作日志</el-button>
                  </div>
                </td>
              </tr>
              <tr v-if="papers.length === 0 && !loading">
                <td colspan="10"><el-empty description="暂无试卷" /></td>
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

    <section v-else-if="viewMode === 'auto'" class="admin-theory-paper-builder-page is-create">
      <header class="admin-theory-paper-create-nav">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>试题管理</el-breadcrumb-item>
          <el-breadcrumb-item>试卷管理</el-breadcrumb-item>
          <el-breadcrumb-item>新增试卷</el-breadcrumb-item>
        </el-breadcrumb>
      </header>
      <main class="admin-theory-paper-create-main">
        <section class="admin-theory-paper-builder-card is-create-card">
          <header><strong>基本信息</strong></header>
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
        <section class="admin-theory-paper-builder-card is-create-card">
          <header><strong>选题设置</strong></header>
          <table class="admin-theory-paper-rule-table is-auto">
            <thead><tr><th>题型</th><th>选题数量</th></tr></thead>
            <tbody>
              <tr v-for="rule in builder.rules" :key="rule.type">
                <td><el-checkbox v-model="rule.selected" @change="toggleAutoRule(rule)">{{ rule.type }}</el-checkbox></td>
                <td><el-input-number v-model="rule.count" :min="0" :max="100" :disabled="!rule.selected" :controls="false" /></td>
              </tr>
            </tbody>
          </table>
          <footer class="admin-theory-paper-rule-summary">选题数量合计：<b>{{ autoQuestionTotal }}题</b></footer>
        </section>
        <footer class="admin-theory-paper-builder-footer is-center is-create-actions">
          <button type="button" class="ghost" @click="cancelCreate">取消</button>
          <button type="button" class="primary" @click="openPreview('auto')">下一步</button>
        </footer>
      </main>
    </section>

    <section v-else-if="viewMode === 'manual'" class="admin-theory-paper-builder-page is-create">
      <header class="admin-theory-paper-create-nav">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>试题管理</el-breadcrumb-item>
          <el-breadcrumb-item>试卷管理</el-breadcrumb-item>
          <el-breadcrumb-item>新增试卷</el-breadcrumb-item>
        </el-breadcrumb>
      </header>
      <main class="admin-theory-paper-create-main">
        <section class="admin-theory-paper-builder-card is-create-card">
          <header><strong>基本信息</strong></header>
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
        <footer class="admin-theory-paper-builder-footer is-center is-create-actions">
          <button type="button" class="ghost" @click="cancelCreate">取消</button>
          <button type="button" class="primary" @click="enterManualSelection">下一步</button>
        </footer>
      </main>
    </section>

    <section v-else-if="viewMode === 'manual-select'" class="admin-theory-paper-builder-page is-manage-prototype">
      <header class="admin-theory-paper-create-nav">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>试题管理</el-breadcrumb-item>
          <el-breadcrumb-item>试卷管理</el-breadcrumb-item>
          <el-breadcrumb-item>管理试题</el-breadcrumb-item>
        </el-breadcrumb>
      </header>
      <main class="admin-theory-paper-manage-main">
        <section class="admin-theory-paper-manage-filter">
          <label class="admin-theory-paper-field is-type">
            <span>题型</span>
            <el-select v-model="questionType" placeholder="全部题型" clearable>
              <el-option v-for="item in questionTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>
          <label class="admin-theory-paper-field is-question-search">
            <span>题干搜索</span>
            <el-input v-model="questionKeyword" :prefix-icon="Search" placeholder="请输入题干关键词搜索" clearable />
          </label>
          <label class="admin-theory-paper-field is-creator">
            <span>添加人</span>
            <el-input v-model="manageCreator" :prefix-icon="Search" placeholder="请输入添加人" clearable />
          </label>
          <label class="admin-theory-paper-field is-course">
            <span>所属课程</span>
            <el-input v-model="manageCourse" :prefix-icon="Search" placeholder="请输入所属课程" clearable />
          </label>
          <div class="admin-theory-paper-manage-filter-buttons">
            <el-button class="admin-theory-paper-query-button" @click="applyManageFilters">查询</el-button>
            <el-button class="admin-theory-paper-reset-button" @click="resetManageFilters">重置</el-button>
          </div>
        </section>

        <section class="admin-theory-paper-manage-actions">
          <el-button class="admin-theory-paper-primary" :icon="Plus" @click="addFilteredQuestions">加入试题篮</el-button>
          <p><span>已加入试题篮：</span><b>{{ selectedQuestions.length }}</b><span>题</span></p>
          <div class="admin-theory-paper-manage-stat">
            <span v-for="item in questionStats" :key="item.type">{{ item.type }} <b>{{ item.count }}</b></span>
          </div>
        </section>

        <section class="admin-theory-paper-builder-card is-manage-table">
          <table class="admin-theory-paper-question-table is-manage">
            <thead>
              <tr>
                <th class="check-col"><el-checkbox :model-value="allQuestionSelected" :indeterminate="partQuestionSelected" @change="toggleAllQuestions" /></th>
                <th class="seq-col">序号</th>
                <th class="type-col">题型</th>
                <th>题干</th>
                <th class="course-col">所属课程</th>
                <th class="status-col">启用状态</th>
                <th class="action-col">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in pagedManageQuestions" :key="item.id">
                <td class="check-col"><el-checkbox :model-value="selectedQuestionIds.includes(item.id)" :disabled="!item.enabled" @change="toggleQuestion(item.id)" /></td>
                <td class="seq-col">{{ (managePage - 1) * managePageSize + index + 1 }}</td>
                <td class="type-col"><span class="admin-theory-paper-type-pill" :class="typeTone(item.type)">{{ item.type }}</span></td>
                <td>{{ item.title }}</td>
                <td class="course-col">{{ item.courseName }}</td>
                <td class="status-col"><span class="admin-theory-paper-status" :class="item.enabled ? 'enabled' : 'disabled'"><i></i>{{ item.enabled ? '已启用' : '已禁用' }}</span></td>
                <td class="action-col">
                  <el-button v-if="isQuestionInPaper(item.id)" text class="warn" @click="removeQuestion(item.id)">删除</el-button>
                  <el-button v-else text :disabled="!item.enabled" @click="addQuestion(item)">加入</el-button>
                </td>
              </tr>
            </tbody>
          </table>
          <el-empty v-if="pagedManageQuestions.length === 0" :description="questionBank.length ? '暂无匹配试题，请更换筛选条件' : '题库暂无试题，请前往试题管理页面录入试题'" />
        </section>

        <footer class="admin-theory-paper-manage-bottom">
          <p>共 <b>{{ filteredQuestionBank.length }}</b> 条记录</p>
          <el-pagination v-model:current-page="managePage" :page-size="managePageSize" :total="filteredQuestionBank.length" layout="prev, pager, next" background />
        </footer>

        <footer class="admin-theory-paper-builder-footer is-center is-manage-actions">
          <button type="button" class="ghost" @click="cancelManualSelection">取消</button>
          <button type="button" class="primary" @click="openPreview('manual')">预览试卷</button>
        </footer>
      </main>
    </section>

    <section v-else-if="viewMode === 'manage'" class="admin-theory-paper-builder-page is-manage-prototype">
      <header class="admin-theory-paper-create-nav">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>试题管理</el-breadcrumb-item>
          <el-breadcrumb-item>试卷管理</el-breadcrumb-item>
          <el-breadcrumb-item>管理试题</el-breadcrumb-item>
        </el-breadcrumb>
      </header>
      <main class="admin-theory-paper-manage-main">
        <section class="admin-theory-paper-manage-filter">
          <label class="admin-theory-paper-field is-type">
            <span>题型</span>
            <el-select v-model="questionType" placeholder="全部题型" clearable>
              <el-option v-for="item in questionTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>
          <label class="admin-theory-paper-field is-question-search">
            <span>题干搜索</span>
            <el-input v-model="questionKeyword" :prefix-icon="Search" placeholder="请输入题干关键词搜索" clearable />
          </label>
          <label class="admin-theory-paper-field is-creator">
            <span>添加人</span>
            <el-input v-model="manageCreator" :prefix-icon="Search" placeholder="请输入添加人" clearable />
          </label>
          <label class="admin-theory-paper-field is-course">
            <span>所属课程</span>
            <el-input v-model="manageCourse" :prefix-icon="Search" placeholder="请输入所属课程" clearable />
          </label>
          <div class="admin-theory-paper-manage-filter-buttons">
            <el-button class="admin-theory-paper-query-button" @click="applyManageFilters">查询</el-button>
            <el-button class="admin-theory-paper-reset-button" @click="resetManageFilters">重置</el-button>
          </div>
        </section>

        <section class="admin-theory-paper-manage-actions">
          <el-button class="admin-theory-paper-primary" :icon="Plus" @click="addFilteredQuestions">加入试题篮</el-button>
          <p><span>已加入试题篮：</span><b>{{ selectedQuestions.length }}</b><span>题</span></p>
          <div class="admin-theory-paper-manage-stat">
            <span v-for="item in questionStats" :key="item.type">{{ item.type }} <b>{{ item.count }}</b></span>
          </div>
        </section>

        <section class="admin-theory-paper-builder-card is-manage-table">
          <table class="admin-theory-paper-question-table is-manage">
            <thead>
              <tr>
                <th class="check-col"><el-checkbox :model-value="allQuestionSelected" :indeterminate="partQuestionSelected" @change="toggleAllQuestions" /></th>
                <th class="seq-col">序号</th>
                <th class="type-col">题型</th>
                <th>题干</th>
                <th class="course-col">所属课程</th>
                <th class="status-col">启用状态</th>
                <th class="action-col">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in pagedManageQuestions" :key="item.id">
                <td class="check-col"><el-checkbox :model-value="selectedQuestionIds.includes(item.id)" :disabled="!item.enabled" @change="toggleQuestion(item.id)" /></td>
                <td class="seq-col">{{ (managePage - 1) * managePageSize + index + 1 }}</td>
                <td class="type-col"><span class="admin-theory-paper-type-pill" :class="typeTone(item.type)">{{ item.type }}</span></td>
                <td>{{ item.title }}</td>
                <td class="course-col">{{ item.courseName }}</td>
                <td class="status-col"><span class="admin-theory-paper-status" :class="item.enabled ? 'enabled' : 'disabled'"><i></i>{{ item.enabled ? '已启用' : '已禁用' }}</span></td>
                <td class="action-col">
                  <el-button v-if="isQuestionInPaper(item.id)" text class="warn" @click="removeQuestion(item.id)">删除</el-button>
                  <el-button v-else text :disabled="!item.enabled" @click="addQuestion(item)">加入</el-button>
                </td>
              </tr>
            </tbody>
          </table>
          <el-empty v-if="pagedManageQuestions.length === 0" :description="questionBank.length ? '暂无匹配试题，请更换筛选条件' : '题库暂无试题，请前往试题管理页面录入试题'" />
        </section>

        <footer class="admin-theory-paper-manage-bottom">
          <p>共 <b>{{ filteredQuestionBank.length }}</b> 条记录</p>
          <el-pagination v-model:current-page="managePage" :page-size="managePageSize" :total="filteredQuestionBank.length" layout="prev, pager, next" background />
        </footer>

        <footer class="admin-theory-paper-builder-footer is-center is-manage-actions">
          <button type="button" class="primary" @click="openPreview('manage')">预览试卷</button>
          <button type="button" class="ghost" @click="cancelManage">取消</button>
        </footer>
      </main>
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
              <td><el-input-number v-model="item.score" :min="1" :max="100" :controls="false" /></td>
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
        <label><span>试卷模板</span><el-button class="admin-theory-paper-template-button" :loading="templateDownloading" @click="downloadPaperTemplate">点击下载试卷上传模板</el-button></label>
        <label>
          <span>试卷内容 <b>*</b></span>
          <el-upload
            v-model:file-list="paperImportFileList"
            drag
            action="#"
            accept=".xls,.xlsx,.excel"
            :auto-upload="false"
            :limit="1"
            :on-change="handlePaperFileChange"
            :on-remove="handlePaperFileRemove"
          >
            <el-icon><UploadFilled /></el-icon>
            <div class="el-upload__text">点击或拖拽上传资源文件</div>
            <template #tip><p>仅支持 .xls、.xlsx、.excel 格式，大小不超过 200MB</p></template>
          </el-upload>
          <el-progress v-if="importProgress > 0" :percentage="importProgress" :status="importProgress === 100 ? 'success' : undefined" />
          <el-button v-if="paperImportErrors.length" text type="danger" @click="downloadPaperImportErrors">下载错误明细</el-button>
        </label>
        <label><span>所属课程 <b>*</b></span><el-input v-model="previewPaper.courseName" maxlength="30" show-word-limit placeholder="请输入所属课程名称" /></label>
      </div>
      <template #footer><div class="admin-theory-paper-dialog-footer"><el-button @click="importVisible = false">取消</el-button><el-button type="primary" :icon="UploadFilled" :loading="importParsing" @click="openPreview('upload')">确认上传</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="previewVisible" class="admin-theory-paper-preview-modal" fullscreen :show-close="false" :close-on-click-modal="false" append-to-body>
      <section class="admin-theory-paper-preview-page">
        <header class="admin-theory-paper-preview-head">
          <div><h2>预览试卷</h2><p>预览确认无误后，可提交完成上传</p></div>
          <el-button text circle :icon="Close" @click="closePreview" />
        </header>
        <section class="admin-theory-paper-preview-meta">
          <p><span>试卷名称：</span><strong>{{ previewPaper.paperName }}</strong></p>
          <i></i>
          <p v-if="uploadPreviewActive"><span>所属课程：</span><strong>{{ previewPaper.courseName }}</strong></p>
          <p v-else><span>总分：</span><strong>{{ selectedScore }}</strong><span>分</span></p>
          <div>
            <el-button v-if="!uploadPreviewActive" @click="closePreview">返回</el-button>
            <el-button class="admin-theory-paper-primary" :loading="saving" @click="submitImport">{{ uploadPreviewActive ? '提交' : '保存' }}</el-button>
          </div>
        </section>
        <main class="admin-theory-paper-preview-layout">
          <template v-if="previewGroups.length > 0">
            <aside class="admin-theory-paper-answer-card">
              <header><strong>答题卡</strong></header>
              <section v-for="item in answerCardGroups" :key="item.type" :class="item.tone">
                <p><span>{{ item.short }}</span>{{ item.count }}题 · {{ item.score }}分</p>
                <div>
                  <button
                    v-for="num in item.numbers"
                    :key="num"
                    type="button"
                    :class="{ active: num === activePreviewQuestionNumber }"
                    @click="focusPreviewQuestion(num)"
                  >
                    {{ num }}
                  </button>
                </div>
              </section>
            </aside>
            <div class="admin-theory-paper-preview-stack">
              <section v-for="group in previewGroups" :key="group.type" class="admin-theory-paper-preview-card" :class="group.tone">
                <header><strong>{{ group.title }}</strong><span>{{ group.meta }}</span><el-button text @click="openBatchScore(group.type)">批量修改得分</el-button></header>
                <article v-for="question in group.questions" :key="question.id" :ref="(element) => setPreviewQuestionRef(question.index, element)">
                  <div>
                    <h3>{{ question.index }}、{{ question.title }}<small v-if="group.type === '多选题'">（多选）</small></h3>
                    <ul v-if="question.options.length"><li v-for="option in question.options" :key="option">{{ option }}</li></ul>
                  </div>
                  <label><span>得分</span><el-input-number v-model="question.score" :min="1" :max="100" :controls="false" @change="updatePreviewScore(question.id, $event)" /></label>
                </article>
              </section>
            </div>
          </template>
          <el-empty v-else description="暂无试卷内容，请先加入试题" />
        </main>
      </section>
    </el-dialog>

    <el-dialog v-model="logsVisible" class="admin-theory-paper-log-dialog" width="760px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-theory-paper-dialog-head"><strong>操作日志</strong><el-button text circle :icon="Close" @click="logsVisible = false" /></div>
      </template>
      <el-table :data="logRows" max-height="460">
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="operator" label="操作人" min-width="120" />
        <el-table-column prop="time" label="操作时间" min-width="170" />
        <el-table-column prop="action" label="操作内容" min-width="140" />
      </el-table>
      <el-empty v-if="logRows.length === 0" description="暂无操作日志" />
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { UploadFile, UploadFiles, UploadUserFile } from 'element-plus';
import { ArrowLeft, Close, Plus, Search, UploadFilled } from '@element-plus/icons-vue';
import * as XLSX from 'xlsx';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  cancelPublishAdminPaper,
  createAdminPaper,
  fetchAdminPaper,
  fetchAdminPaperLogs,
  fetchAdminPapers,
  importAdminPaperQuestions,
  previewAdminPaper,
  publishAdminPaper,
  updateAdminPaper,
  type AdminPaper,
  type AdminPaperCommand,
  type AdminPaperLog,
  type AdminPaperQuestion
} from '../../api/admin-paper';
import { fetchAdminQuestions, previewAdminQuestionImport, type AdminQuestion, type AdminQuestionImportRow } from '../../api/admin-question';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

type ViewMode = 'list' | 'auto' | 'manual' | 'manual-select' | 'manage' | 'manage-edit';

async function downloadPaperTemplate() {
  const url = `/templates/${encodeURIComponent('试卷导入表格.xlsx')}`;
  templateDownloading.value = true;
  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error('试卷模板下载失败');
    const objectUrl = URL.createObjectURL(await response.blob());
    const link = document.createElement('a');
    link.href = objectUrl;
    link.download = '试卷导入表格.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(objectUrl);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试卷模板下载失败');
  } finally {
    templateDownloading.value = false;
  }
}

function normalizedPaperCellMap(row: Record<string, unknown>) {
  const result: Record<string, string> = {};
  Object.entries(row).forEach(([key, value]) => {
    result[key.replace(/[\s*＊]/g, '').toUpperCase()] = String(value ?? '').trim();
  });
  return result;
}

function readPaperCell(row: Record<string, string>, ...keys: string[]) {
  for (const key of keys) {
    const value = row[key.replace(/[\s*＊]/g, '').toUpperCase()];
    if (value) return value;
  }
  return '';
}

function normalizePaperImportType(value: string) {
  const normalized = value.trim().toUpperCase().replace(/[\s_-]/g, '');
  const types: Record<string, string> = {
    SINGLE: 'SINGLE', SINGLECHOICE: 'SINGLE', 单选: 'SINGLE', 单选题: 'SINGLE',
    MULTIPLE: 'MULTIPLE', MULTIPLECHOICE: 'MULTIPLE', 多选: 'MULTIPLE', 多选题: 'MULTIPLE',
    JUDGE: 'JUDGE', TRUEFALSE: 'JUDGE', 判断: 'JUDGE', 判断题: 'JUDGE',
    FILLBLANK: 'FILL_BLANK', 填空: 'FILL_BLANK', 填空题: 'FILL_BLANK',
    SHORTANSWER: 'SHORT_ANSWER', ESSAY: 'SHORT_ANSWER', 简答: 'SHORT_ANSWER', 简答题: 'SHORT_ANSWER'
  };
  return types[normalized] ?? value.trim().toUpperCase();
}

function normalizePaperImportAnswer(type: string, value: string) {
  const answer = value.trim();
  if (type === 'JUDGE') {
    if (/^(正确|对|TRUE|T|1|是|√)$/i.test(answer)) return 'TRUE';
    if (/^(错误|错|FALSE|F|0|否|×|X)$/i.test(answer)) return 'FALSE';
  }
  if (type === 'SINGLE' || type === 'MULTIPLE') {
    return Array.from(new Set(answer.toUpperCase().match(/[A-H]/g) ?? [])).join(',');
  }
  return answer;
}

async function parsePaperWorkbook(file: File): Promise<AdminQuestionImportRow[]> {
  const workbook = XLSX.read(await file.arrayBuffer(), { type: 'array' });
  const worksheet = workbook.Sheets[workbook.SheetNames[0]];
  if (!worksheet) return [];
  const matrix = XLSX.utils.sheet_to_json<unknown[]>(worksheet, { header: 1, defval: '', raw: false });
  const headerIndex = matrix.findIndex((row) => {
    const headers = row.map((cell) => String(cell ?? '').replace(/[\s*＊]/g, ''));
    return headers.includes('题型') && headers.includes('题干');
  });
  if (headerIndex < 0) throw new Error('未找到“题型、题干”表头，请使用试卷导入模板');
  const headers = matrix[headerIndex].map((cell) => String(cell ?? ''));
  const sourceRows = matrix.slice(headerIndex + 1).map((values) => {
    const source: Record<string, unknown> = {};
    headers.forEach((header, index) => {
      if (header) source[header] = values[index] ?? '';
    });
    return source;
  });
  return sourceRows.flatMap((source, index) => {
    const cells = normalizedPaperCellMap(source);
    const title = readPaperCell(cells, '题干', '题目', '试题内容', 'TITLE');
    const typeText = readPaperCell(cells, '题型', '试题类型', 'QUESTIONTYPE');
    if (!title && !typeText) return [];
    const questionType = normalizePaperImportType(typeText);
    const standardAnswer = normalizePaperImportAnswer(questionType, readPaperCell(cells, '答案', '正确答案', '标准答案', 'STANDARDANSWER'));
    const options = 'ABCDEFGH'.split('').flatMap((key) => {
      const optionText = readPaperCell(cells, `选项${key}`, `${key}选项`, key);
      return optionText ? [{ optionKey: key, optionText, correct: standardAnswer.split(',').includes(key) }] : [];
    });
    const scoreText = readPaperCell(cells, '分值', '分数', 'SCORE');
    return [{
      rowNumber: headerIndex + index + 2,
      questionType,
      title,
      standardAnswer,
      explanation: readPaperCell(cells, '题目解析', '解析', '答案解析', 'EXPLANATION'),
      score: scoreText ? Number(scoreText) || 0 : 5,
      options
    }];
  });
}

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
  creatorName: string;
  enabled: boolean;
  options?: string[];
  importRowNumber?: number;
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
const PAPER_DRAFT_KEY = 'admin-theory-paper-create-draft';
const viewMode = ref<ViewMode>('list');
const page = ref(1);
const totalCount = ref(0);
const loading = ref(false);
const saving = ref(false);
const importParsing = ref(false);
const templateDownloading = ref(false);
const importProgress = ref(0);
const jumpPage = ref(1);
const selectedIds = ref<number[]>([]);
const { can } = useAdminPermissions('resource:theory-paper');
const importVisible = ref(false);
const previewVisible = ref(false);
const activePreviewQuestionNumber = ref(1);
const previewQuestionRefs = new Map<number, HTMLElement>();
const logsVisible = ref(false);
const activePaper = ref<TheoryPaper | null>(null);
const previewSource = ref<'auto' | 'manual' | 'manage' | 'upload'>('manual');
const questionKeyword = ref('');
const questionType = ref('');
const manageCreator = ref('');
const manageCourse = ref('');
const managePage = ref(1);
const managePageSize = 6;
const selectedQuestionIds = ref<number[]>([]);
const paperLogs = ref<AdminPaperLog[]>([]);
const creatorOptions = ref<Array<{ creatorId: number; creatorName: string }>>([]);

const draft = reactive({ keyword: '', courseName: '', creatorId: undefined as number | undefined, enabled: undefined as boolean | undefined });
const applied = ref({ ...draft });
const builder = reactive({
  paperName: '',
  courseName: '',
  totalScore: 100,
  passScore: 60,
  rules: createDefaultRules()
});
const manageForm = reactive({ paperName: '', courseName: '' });
const previewPaper = reactive({ paperName: '', courseName: '' });
const paperImportFile = ref<File | null>(null);
const paperImportFileList = ref<UploadUserFile[]>([]);
const paperImportRows = ref<AdminQuestionImportRow[]>([]);
const paperImportErrors = ref<Array<{ rowNumber?: number; message?: string }>>([]);
const uploadPreviewActive = ref(false);

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
          questions: questions.map((question) => ({
            id: question.id,
            index: selectedQuestions.value.findIndex((item) => item.id === question.id) + 1,
          title: question.title,
          score: Number(question.score || 0),
          options: question.options ?? []
        }))
      };
    })
    .filter(Boolean) as Array<{ type: string; title: string; meta: string; tone: string; questions: Array<{ id: number; index: number; title: string; score: number; options: string[] }> }>;
});

const maxPage = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)));
const allSelected = computed(() => papers.value.length > 0 && papers.value.every((item) => selectedIds.value.includes(item.paperId)));
const partSelected = computed(() => selectedIds.value.length > 0 && !allSelected.value);
const pageStart = computed(() => (totalCount.value === 0 ? 0 : (page.value - 1) * pageSize + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize, totalCount.value));
const selectedScore = computed(() => selectedQuestions.value.reduce((sum, item) => sum + Number(item.score || 0), 0));
const autoQuestionTotal = computed(() => builder.rules.reduce((sum, rule) => sum + (rule.selected ? Number(rule.count || 0) : 0), 0));
const appliedManageFilters = ref({ keyword: '', type: '', creator: '', course: '' });
const filteredQuestionBank = computed(() => questionBank.value.filter((item) => {
  const filters = appliedManageFilters.value;
  return (!filters.keyword || item.title.includes(filters.keyword))
    && (!filters.type || item.type === filters.type)
    && (!filters.creator || item.creatorName.includes(filters.creator))
    && (!filters.course || item.courseName.includes(filters.course));
}));
const pagedManageQuestions = computed(() => filteredQuestionBank.value.slice((managePage.value - 1) * managePageSize, managePage.value * managePageSize));
const allQuestionSelected = computed(() => {
  const enabledQuestions = pagedManageQuestions.value.filter((item) => item.enabled);
  return enabledQuestions.length > 0 && enabledQuestions.every((item) => selectedQuestionIds.value.includes(item.id));
});
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
    operator: item.operatorName || '-',
    action: paperLogActionLabel(item.action),
    time: formatDateTime(item.createdAt),
    content: item.content || '-'
  }))
]);

watch(page, (value) => {
  jumpPage.value = value;
  selectedIds.value = [];
});

watch(managePage, () => {
  selectedQuestionIds.value = [];
});

watch([builder, selectedQuestions, viewMode], () => {
  if (viewMode.value === 'auto' || viewMode.value === 'manual' || viewMode.value === 'manual-select') {
    sessionStorage.setItem(PAPER_DRAFT_KEY, JSON.stringify({
      viewMode: viewMode.value,
      builder: {
        paperName: builder.paperName,
        courseName: builder.courseName,
        rules: builder.rules
      },
      selectedQuestions: selectedQuestions.value
    }));
  }
}, { deep: true });

function clearCreateDraft() {
  sessionStorage.removeItem(PAPER_DRAFT_KEY);
}

function restoreCreateDraft() {
  const raw = sessionStorage.getItem(PAPER_DRAFT_KEY);
  if (!raw) return false;
  try {
    const draft = JSON.parse(raw) as {
      viewMode?: ViewMode;
      builder?: { paperName?: string; courseName?: string; rules?: ReturnType<typeof createDefaultRules> };
      selectedQuestions?: QuestionItem[];
    };
    if (!['auto', 'manual', 'manual-select'].includes(String(draft.viewMode))) return false;
    Object.assign(builder, {
      paperName: draft.builder?.paperName || '',
      courseName: draft.builder?.courseName || '',
      rules: Array.isArray(draft.builder?.rules) ? draft.builder?.rules : createDefaultRules()
    });
    selectedQuestions.value = Array.isArray(draft.selectedQuestions) ? draft.selectedQuestions : [];
    viewMode.value = draft.viewMode as ViewMode;
    return true;
  } catch {
    clearCreateDraft();
    return false;
  }
}

function formatDateTime(value?: string) { return value ? value.replace('T', ' ').slice(0, 19) : '-'; }
function paperLogActionLabel(action?: string) {
  const labels: Record<string, string> = {
    CREATE: '新增', UPDATE: '编辑', PUBLISH: '发布', CANCEL_PUBLISH: '取消发布', ENABLE: '启用', DISABLE: '禁用'
  };
  return labels[String(action || '').toUpperCase()] || action || '操作';
}
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
    courseName: item.courseName || '-',
    questionCount: item.questionCount || item.questions?.length || 0,
    totalScore: item.totalScore || 0,
    creatorId: item.creatorId,
    creatorName: item.creatorName || '-',
    createdAt: item.createdAt ? item.createdAt.replace('T', ' ').slice(0, 16) : '-',
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
    courseName: (item as AdminQuestion & { courseName?: string }).courseName || '-',
    creatorName: (item as AdminQuestion & { creatorName?: string }).creatorName || '-',
    enabled: (item as AdminQuestion & { enabled?: boolean }).enabled !== false,
    options: item.options?.map((option) => `${option.optionKey || ''}. ${option.optionText || ''}`)
  };
}
function paperCommand(mode: 'auto' | 'manual' | 'manage'): AdminPaperCommand {
  const paperName = mode === 'manage' ? manageForm.paperName.trim() : builder.paperName.trim();
  const courseName = mode === 'manage' ? manageForm.courseName.trim() : builder.courseName.trim();
  if (!paperName) {
    throw new Error('请输入试卷名称');
  }
  if (paperName.length > 30) {
    throw new Error('试卷名称不能超过 30 个字符');
  }
  if (courseName && courseName.length > 30) {
    throw new Error('所属课程不能超过 30 个字符');
  }
  if (mode === 'auto') {
    const invalidRule = builder.rules.find((rule) => rule.selected && (!Number.isInteger(Number(rule.count)) || Number(rule.count) <= 0));
    if (invalidRule) {
      throw new Error(`${invalidRule.type}选题数量必须为正整数`);
    }
    const autoRules = builder.rules
      .filter((rule) => rule.selected && Number(rule.count) > 0)
      .map((rule) => ({ questionType: typeCode(rule.type), questionCount: Number(rule.count), scorePerQuestion: Number(rule.score || 1) }));
    if (autoRules.length === 0) {
      throw new Error('至少选择一类题型并设置抽题数量');
    }
    const questions = selectedQuestions.value.length
      ? selectedQuestions.value.map((item) => ({ questionId: item.id, score: Number(item.score || 1) }))
      : undefined;
    return { paperName, courseName, composeMode: 'AUTO', autoRules, questions };
  }

  const questions = selectedQuestions.value.map((item) => ({ questionId: item.id, score: Number(item.score || 1) }));
  if (questions.length === 0) {
    throw new Error('试题篮暂无试题，请先勾选试题加入试题篮');
  }
  if (questions.some((item) => !Number.isInteger(item.score) || item.score <= 0)) {
    throw new Error('所有试题分值必须为正整数');
  }
  return { paperName, courseName, composeMode: 'MANUAL', questions };
}
async function loadPapers() {
  loading.value = true;
  try {
    const result = await fetchAdminPapers({
      keyword: applied.value.keyword.trim() || undefined,
      courseName: applied.value.courseName.trim() || undefined,
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
    const allQuestions: AdminQuestion[] = [];
    let currentPage = 1;
    let total = 0;
    do {
      const previousLength = allQuestions.length;
      const result = await fetchAdminQuestions({ page: currentPage, pageSize: 100 });
      allQuestions.push(...result.records);
      total = result.total;
      if (allQuestions.length === previousLength) break;
      currentPage += 1;
    } while (allQuestions.length < total);
    questionBank.value = allQuestions.map(mapQuestion).filter((item) => item.id > 0);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '理论试题加载失败');
  }
}
async function loadPaperCreators() {
  try {
    const records: AdminPaper[] = [];
    let currentPage = 1;
    let total = 0;
    do {
      const previousLength = records.length;
      const result = await fetchAdminPapers({ page: currentPage, pageSize: 100 });
      records.push(...result.records);
      total = result.total;
      if (records.length === previousLength) break;
      currentPage++;
    } while (records.length < total);
    const creators = new Map<number, string>();
    records.forEach((item) => {
      if (item.creatorId && item.creatorName) creators.set(item.creatorId, item.creatorName);
    });
    creatorOptions.value = Array.from(creators, ([creatorId, creatorName]) => ({ creatorId, creatorName }));
  } catch {
    creatorOptions.value = [];
  }
}
function applyFilters() { applied.value = { ...draft }; page.value = 1; selectedIds.value = []; void loadPapers(); }
function resetFilters() { Object.assign(draft, { keyword: '', courseName: '', creatorId: undefined, enabled: undefined }); applyFilters(); }
function toggleOne(id: number) { selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter((item) => item !== id) : [...selectedIds.value, id]; }
function toggleAll(value: string | number | boolean) { selectedIds.value = value ? Array.from(new Set([...selectedIds.value, ...papers.value.map((item) => item.paperId)])) : selectedIds.value.filter((id) => !papers.value.some((item) => item.paperId === id)); }
function openCreate() { clearCreateDraft(); resetBuilder(); resetManageFilters(); activePaper.value = null; selectedQuestions.value = []; selectedQuestionIds.value = []; void loadQuestionBank(); viewMode.value = 'manual'; }
function switchCreateMode(value: string | number | boolean) { viewMode.value = value === 'manual' ? 'manual' : 'auto'; }
function cancelCreate() {
  clearCreateDraft();
  resetBuilder();
  selectedQuestions.value = [];
  selectedQuestionIds.value = [];
  backToList();
}
function enterManualSelection() {
  if (!builder.paperName.trim()) {
    ElMessage.warning('请填写试卷名称');
    return;
  }
  viewMode.value = 'manual-select';
}
async function openManage(row: TheoryPaper) {
  activePaper.value = row;
  Object.assign(manageForm, { paperName: row.paperName, courseName: row.courseName });
  previewPaper.paperName = row.paperName;
  previewPaper.courseName = row.courseName;
  resetManageFilters();
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
async function cancelManualSelection() {
  try {
    await ElMessageBox.confirm('确认退出手动组卷？试题篮内所有题目将清空，已填写的试卷名称保留', '退出手动组卷', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }
  selectedQuestions.value = [];
  selectedQuestionIds.value = [];
  viewMode.value = 'manual';
}
function cancelManage() {
  selectedQuestionIds.value = [];
  backToList();
}
function createDefaultRules() {
  return [
    { type: '单选题', count: 20, previousCount: 20, score: 1, difficulty: '全部', selected: true },
    { type: '多选题', count: 10, previousCount: 10, score: 1, difficulty: '全部', selected: true },
    { type: '判断题', count: 10, previousCount: 10, score: 1, difficulty: '基础', selected: true },
    { type: '填空题', count: 5, previousCount: 5, score: 1, difficulty: '全部', selected: true },
    { type: '简答题', count: 0, previousCount: 1, score: 1, difficulty: '全部', selected: false }
  ];
}
function toggleAutoRule(rule: ReturnType<typeof createDefaultRules>[number]) {
  if (rule.selected) {
    rule.count = rule.previousCount > 0 ? rule.previousCount : 1;
    return;
  }
  if (rule.count > 0) rule.previousCount = rule.count;
  rule.count = 0;
}
function resetBuilder() { Object.assign(builder, { paperName: '', courseName: '', totalScore: 100, passScore: 60, rules: createDefaultRules() }); }
function isQuestionInPaper(id: number) { return selectedQuestions.value.some((item) => item.id === id); }
function addQuestion(item: QuestionItem) { if (item.enabled && !selectedQuestions.value.some((question) => question.id === item.id)) selectedQuestions.value.push({ ...item }); }
function removeQuestion(id: number) { selectedQuestions.value = selectedQuestions.value.filter((item) => item.id !== id); }
function toggleQuestion(id: number) {
  const question = questionBank.value.find((item) => item.id === id);
  if (!question?.enabled) return;
  selectedQuestionIds.value = selectedQuestionIds.value.includes(id) ? selectedQuestionIds.value.filter((item) => item !== id) : [...selectedQuestionIds.value, id];
}
function toggleAllQuestions(value: string | number | boolean) {
  const enabledPageQuestions = pagedManageQuestions.value.filter((item) => item.enabled);
  selectedQuestionIds.value = value
    ? Array.from(new Set([...selectedQuestionIds.value, ...enabledPageQuestions.map((item) => item.id)]))
    : selectedQuestionIds.value.filter((id) => !enabledPageQuestions.some((item) => item.id === id));
}
function addFilteredQuestions() {
  const selected = questionBank.value.filter((item) => item.enabled && selectedQuestionIds.value.includes(item.id));
  if (!selected.length) {
    ElMessage.warning('请先勾选需要加入试题篮的试题');
    return;
  }
  selected.forEach(addQuestion);
  selectedQuestionIds.value = [];
}
function applyManageFilters() {
  appliedManageFilters.value = {
    keyword: questionKeyword.value.trim(),
    type: questionType.value,
    creator: manageCreator.value.trim(),
    course: manageCourse.value.trim()
  };
  managePage.value = 1;
  selectedQuestionIds.value = [];
}
function resetManageFilters() {
  questionKeyword.value = '';
  questionType.value = '';
  manageCreator.value = '';
  manageCourse.value = '';
  appliedManageFilters.value = { keyword: '', type: '', creator: '', course: '' };
  managePage.value = 1;
  selectedQuestionIds.value = [];
}
function typeTone(type: string) { if (type.includes('多')) return 'multiple'; if (type.includes('判断')) return 'judge'; if (type.includes('填空')) return 'blank'; if (type.includes('简答')) return 'essay'; return 'single'; }
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
function openImport() {
  Object.assign(previewPaper, { paperName: '', courseName: '' });
  paperImportFile.value = null;
  paperImportFileList.value = [];
  paperImportRows.value = [];
  paperImportErrors.value = [];
  importProgress.value = 0;
  uploadPreviewActive.value = false;
  importVisible.value = true;
}
function handlePaperFileChange(file: UploadFile, files: UploadFiles) {
  const raw = file.raw;
  paperImportErrors.value = [];
  importProgress.value = 0;
  if (!raw) {
    paperImportFile.value = null;
    paperImportFileList.value = [];
    return;
  }
  if (!/\.(xls|xlsx|excel)$/i.test(raw.name)) {
    ElMessage.warning('仅支持 Excel 文件');
    paperImportFile.value = null;
    paperImportFileList.value = [];
    return;
  }
  if (raw.size > 200 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 200MB');
    paperImportFile.value = null;
    paperImportFileList.value = [];
    return;
  }
  paperImportFile.value = raw;
  paperImportFileList.value = files.slice(-1);
}
function handlePaperFileRemove(_file: UploadFile, files: UploadFiles) {
  paperImportFileList.value = files;
  paperImportFile.value = null;
  paperImportRows.value = [];
  paperImportErrors.value = [];
  importProgress.value = 0;
}
function downloadPaperImportErrors() {
  const worksheet = XLSX.utils.json_to_sheet(paperImportErrors.value.map((item) => ({
    行号: item.rowNumber ?? '-',
    错误原因: item.message || '试题格式不正确'
  })));
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '错误明细');
  XLSX.writeFile(workbook, '试卷导入错误明细.xlsx');
}
async function openPreview(source: 'auto' | 'manual' | 'manage' | 'upload') {
  if (source === 'upload') {
    void prepareUploadPreview();
    return;
  }
  uploadPreviewActive.value = false;
  previewSource.value = source;
  try {
    const mode = source === 'auto' ? 'auto' : source === 'manage' ? 'manage' : 'manual';
    const command = paperCommand(mode);
    if (source === 'auto') {
      const preview = await previewAdminPaper({ ...command, questions: undefined });
      selectedQuestions.value = preview.questions.map(mapQuestion).filter((item) => item.id > 0);
    }
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善试卷信息');
    return;
  }
  previewPaper.paperName = source === 'manage' ? manageForm.paperName : builder.paperName || '-';
  previewPaper.courseName = source === 'manage' ? manageForm.courseName : builder.courseName || '-';
  importVisible.value = false;
  previewVisible.value = true;
}
async function prepareUploadPreview() {
  if (!previewPaper.paperName.trim()) {
    ElMessage.warning('请输入试卷名称');
    return;
  }
  const file = paperImportFile.value;
  if (!file) {
    ElMessage.warning('请上传试卷 Excel 文件');
    return;
  }
  if (!/\.(xls|xlsx|excel)$/i.test(file.name)) {
    ElMessage.warning('仅支持 Excel 文件');
    return;
  }
  if (file.size > 200 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 200MB');
    return;
  }
  if (!previewPaper.courseName.trim()) {
    ElMessage.warning('请输入所属课程名称');
    return;
  }
  importParsing.value = true;
  importProgress.value = 20;
  paperImportErrors.value = [];
  try {
    const rows = await parsePaperWorkbook(file);
    importProgress.value = 60;
    if (!rows.length) throw new Error('Excel 内未读取到有效试题');
    const result = await previewAdminQuestionImport({
      fileName: file.name,
      fileSize: file.size,
      courseName: previewPaper.courseName.trim(),
      rows
    });
    if ((result.errorCount ?? 0) > 0) {
      paperImportErrors.value = result.errors ?? [];
      const first = result.errors?.[0];
      throw new Error(`第 ${first?.rowNumber ?? '-'} 行：${first?.message || '试题格式不正确'}`);
    }
    paperImportRows.value = result.validRows ?? rows;
    selectedQuestions.value = paperImportRows.value.map((row, index) => ({
      id: -(row.rowNumber ?? index + 1),
      importRowNumber: row.rowNumber,
      title: row.title || '-',
      type: typeLabel(row.questionType),
      score: Number(row.score || 5),
      courseName: previewPaper.courseName,
      creatorName: '-',
      enabled: true,
      options: (row.options ?? []).map((option) => `${option.optionKey}. ${option.optionText}`)
    }));
    uploadPreviewActive.value = true;
    previewSource.value = 'upload';
    importProgress.value = 100;
    importVisible.value = false;
    previewVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试卷文件解析失败');
  } finally {
    importParsing.value = false;
  }
}
function closePreview() {
  previewVisible.value = false;
  if (uploadPreviewActive.value) {
    importVisible.value = true;
  }
}
function updatePreviewScore(questionId: number, value: number | undefined) {
  const question = selectedQuestions.value.find((item) => item.id === questionId);
  if (question && value !== undefined) question.score = Number(value);
}

function setPreviewQuestionRef(index: number, element: unknown) {
  if (element instanceof HTMLElement) {
    previewQuestionRefs.set(index, element);
  } else {
    previewQuestionRefs.delete(index);
  }
}

function focusPreviewQuestion(index: number) {
  activePreviewQuestionNumber.value = index;
  previewQuestionRefs.get(index)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function openBatchScore(type: string) {
  const questions = selectedQuestions.value.filter((item) => item.type === type);
  if (!questions.length) {
    return;
  }
  try {
    const result = await ElMessageBox.prompt(`请输入${type}统一得分（1-100的正整数）`, '批量修改得分', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: String(questions[0].score || 1),
      inputPattern: /^[1-9]\\d*$/,
      inputErrorMessage: '请输入1-100的正整数'
    });
    const score = Number(result.value);
    if (!Number.isInteger(score) || score < 1 || score > 100) {
      ElMessage.warning('得分必须是1-100的正整数');
      return;
    }
    questions.forEach((question) => { question.score = score; });
  } catch {
    // 用户取消批量修改时保持当前预览内容。
  }
}
function paperRowsForSubmission() {
  const scores = new Map(selectedQuestions.value.map((item) => [item.importRowNumber, item.score]));
  return paperImportRows.value.map((row) => ({ ...row, score: scores.get(row.rowNumber) ?? row.score }));
}
async function submitImport() {
  if (selectedQuestions.value.some((item) => !Number.isInteger(Number(item.score)) || Number(item.score) < 1 || Number(item.score) > 100)) {
    ElMessage.warning('所有试题分值必须为 1-100 的正整数');
    return;
  }
  if (uploadPreviewActive.value) {
    try {
      await ElMessageBox.confirm('确认提交该试卷至题库？提交后将生成正式试卷，可在试卷管理列表查看。', '提交试卷', {
        type: 'warning'
      });
    } catch {
      return;
    }
  }
  saving.value = true;
  try {
    if (uploadPreviewActive.value) {
      const file = paperImportFile.value;
      if (!file) throw new Error('导入文件已失效，请重新选择');
      await importAdminPaperQuestions({
        paperName: previewPaper.paperName.trim(),
        courseName: previewPaper.courseName.trim(),
        fileName: file.name,
        fileSize: file.size,
        rows: paperRowsForSubmission()
      });
    } else if (previewSource.value === 'manage' && activePaper.value) {
      await updateAdminPaper(activePaper.value.paperId, paperCommand('manage'));
    } else {
      await createAdminPaper(paperCommand(viewMode.value === 'auto' ? 'auto' : 'manual'));
    }
    previewVisible.value = false;
    ElMessage.success(uploadPreviewActive.value ? '试卷上传提交成功' : previewSource.value === 'manage' ? '试卷已修改' : '试卷已保存');
    clearCreateDraft();
    backToList();
    await loadPapers();
    await loadPaperCreators();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '试卷提交失败');
  } finally {
    saving.value = false;
  }
}
async function setEnabled(row: TheoryPaper) {
  try {
    await ElMessageBox.confirm(
      `确定要${row.enabled ? '禁用' : '启用'}【${row.paperName}】吗？`,
      row.enabled ? '禁用试卷' : '启用试卷',
      { type: 'warning' }
    );
  } catch {
    return;
  }
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
    await ElMessageBox.confirm(
      `确定要批量${enabled ? '启用' : '禁用'}选中的 ${selectedIds.value.length} 套试卷吗？`,
      enabled ? '批量启用' : '批量禁用',
      { type: 'warning' }
    );
  } catch {
    return;
  }
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
  if (restoreCreateDraft()) void loadQuestionBank();
  void loadPapers();
  void loadPaperCreators();
});
</script>
