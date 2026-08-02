<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-page">
      <template v-if="viewMode === 'list'">
      <el-breadcrumb class="admin-course-breadcrumb" separator="/">
        <el-breadcrumb-item>教学实训</el-breadcrumb-item>
        <el-breadcrumb-item>实训课</el-breadcrumb-item>
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
              <tr v-for="course in filteredCourses" :key="course.id">
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
          <span>共 {{ filteredCourses.length }} 条记录</span>
          <el-pagination layout="prev, pager, next" :total="filteredCourses.length" :page-size="8" />
        </div>
      </div>
      </template>

      <template v-else-if="viewMode === 'form'">
        <header class="admin-training-subpage-top">
          <button type="button" class="admin-training-back" @click="backToCourses"><el-icon><ArrowLeft /></el-icon></button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>实训课</el-breadcrumb-item>
            <el-breadcrumb-item>{{ formMode === 'create' ? '添加实训课程' : '编辑实训课程' }}</el-breadcrumb-item>
          </el-breadcrumb>
          <strong>{{ formMode === 'create' ? '添加实训课程' : '编辑实训课程' }}</strong>
        </header>

        <section class="admin-training-create-card">
          <header><b>基础信息</b><span>填写实训课程的基本信息，带 * 为必填项</span></header>
          <div class="admin-training-create-grid">
            <label><span><i>*</i> 实训课程名</span><el-input v-model="form.name" placeholder="请输入实训课程名称" /></label>
            <label><span><i>*</i> 类型</span><el-radio-group v-model="form.type"><el-radio label="练习" /><el-radio label="考试" /></el-radio-group></label>
            <label class="range"><span><i>*</i> 起止时间</span><div><el-input placeholder="开始时间" /><em>至</em><el-input placeholder="结束时间" /></div></label>
            <label><span><i>*</i> 所属学年学期</span><el-select v-model="form.semester"><el-option v-for="semester in semesters" :key="semester" :label="semester" :value="semester" /></el-select></label>
            <label><span><i>*</i> 参训班级/学员</span><el-select placeholder="请选择参训班级/学员（可多选）"><el-option label="城轨信号2401班" value="1" /></el-select></label>
            <label><span><i>*</i> 监考教师</span><el-select placeholder="请选择监考教师（可多选）"><el-option label="李明峰" value="1" /></el-select></label>
            <label><span><i>*</i> 教室</span><el-select placeholder="请选择教室"><el-option label="实训室A-301" value="1" /></el-select></label>
            <label><span><i>*</i> 学生实训时是否自动录屏</span><el-radio-group v-model="autoRecord"><el-radio label="否" /><el-radio label="是" /></el-radio-group></label>
            <label><span><i>*</i> 最终成绩取值依据</span><el-radio-group v-model="scoreRule"><el-radio label="最高成绩" /><el-radio label="最后一次提交的成绩" /></el-radio-group></label>
          </div>
        </section>

        <section class="admin-training-create-card">
          <header class="with-action"><div><b>实训题列表</b><span>选择本次实训包含的题目，可拖拽排序</span></div><el-button type="primary" :icon="Plus" @click="openSelector('topic')">添加实训题</el-button></header>
          <div class="admin-training-create-table-scroll">
            <table class="admin-training-create-table">
              <thead><tr><th>排序</th><th>序号</th><th>实训题名称</th><th>实训模式</th><th>训练角色</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="(item, index) in selectedTopics" :key="item.id">
                  <td><span class="drag-dot">⠿</span></td><td>{{ index + 1 }}</td><td><strong>{{ item.name }}</strong></td><td>{{ index % 2 ? '多人实训' : '单人实训' }}</td><td>{{ index % 2 ? '司机  调度员  信号员' : '-' }}</td>
                  <td><el-button link type="danger" @click="removeSelected('topic', item.id)">删除</el-button></td>
                </tr>
              </tbody>
            </table>
          </div>
          <p class="admin-training-picked">已选择 <b>{{ selectedTopics.length }}</b> 道实训题</p>
        </section>

        <footer class="admin-training-sticky-footer"><el-button @click="backToCourses">取消</el-button><el-button type="primary" @click="saveDraft">保存</el-button></footer>
      </template>

      <template v-else-if="viewMode === 'monitor'">
        <header class="admin-training-monitor-top">
          <button type="button" class="admin-training-back" @click="backToCourses"><el-icon><ArrowLeft /></el-icon></button>
          <div><h2>{{ selectedCourse?.name || '信号故障处理综合实训' }} - 监考详情</h2><p>2025-03-20 08:00 至 2025-03-20 10:00　|　实训室A-301　|　城轨信号2401班</p></div>
          <span>实训中</span>
        </header>
        <section class="admin-training-live-title"><b>教室全景监控</b><i>LIVE</i></section>
        <div class="admin-training-camera-grid">
          <button v-for="camera in cameras" :key="camera.name" class="admin-training-camera" @click="monitorDesktopVisible = true"><span>在线</span><strong>{{ camera.name }}</strong><em>{{ camera.location }}</em><small>2025-03-20 09:32:15</small></button>
        </div>
        <section class="admin-training-monitor-table-card">
          <h3>学员实况</h3>
          <div class="admin-training-table-scroll">
            <table class="admin-training-monitor-table">
              <thead><tr><th>学员姓名</th><th>学号</th><th>当前实训题</th><th>模式</th><th>个人成绩</th><th>实训进度</th><th>当前角色</th><th>当前所在房间</th><th>队员姓名</th><th>整队成绩</th><th>操作</th></tr></thead>
              <tbody><tr v-for="student in monitorStudents" :key="student.studentNo"><td><strong>{{ student.name }}</strong></td><td>{{ student.studentNo }}</td><td>城市轨道交通行车组织实训</td><td>多人实训</td><td>{{ student.score }}</td><td>9/10</td><td>{{ student.role }}</td><td>张明远创建的房间</td><td>张明远、李思雨、王浩然</td><td>325.2</td><td><el-button class="primary-action" link @click="monitorDesktopVisible = true">查看监控</el-button><el-button link type="danger">解散房间</el-button></td></tr></tbody>
            </table>
          </div>
        </section>
      </template>

      <template v-else-if="viewMode === 'marking'">
        <header class="admin-training-subpage-top is-dark"><button type="button" class="admin-training-back" @click="backToCourses"><el-icon><ArrowLeft /></el-icon></button><el-breadcrumb separator="/"><el-breadcrumb-item>教学实训</el-breadcrumb-item><el-breadcrumb-item>实训课</el-breadcrumb-item><el-breadcrumb-item>阅卷</el-breadcrumb-item></el-breadcrumb><strong>{{ selectedCourse?.name || '城市轨道交通信号系统实训' }}</strong></header>
        <div class="admin-training-marking-layout">
          <aside class="admin-training-marking-side"><b>实训列表</b><article class="active"><strong>轨道电路分路不良故障模拟与排查实训</strong><span>多人实训</span></article><article>信号系统整体架构认知实训<span>单人实训</span></article></aside>
          <main class="admin-training-marking-main">
            <section class="admin-training-marking-filter"><label>学员姓名<el-input placeholder="请输入学员姓名" /></label><label>学员学号<el-input placeholder="请输入学员学号" /></label><label>所属班级<el-select placeholder="请选择所属班级" /></label><el-button type="primary" :icon="Search">查询</el-button><el-button>重置</el-button></section>
            <section class="admin-training-marking-tabs"><button class="active">全部 <b>48</b></button><button>待批阅 <b>12</b></button><button>已批阅 <b>28</b></button><button>未提交 <b>8</b></button><el-button>导出数据</el-button></section>
            <div class="admin-training-table-scroll"><table class="admin-training-marking-table"><thead><tr><th>序号</th><th>学员姓名</th><th>学号</th><th>所属班级</th><th>是否提交</th><th>提交次数</th><th>最后一次提交时间</th><th>是否批阅</th><th>个人得分</th><th>同组队员成绩</th><th>操作</th></tr></thead><tbody><tr v-for="(row,index) in reviewRows" :key="row.no"><td>{{ index + 1 }}</td><td><strong>{{ row.name }}</strong></td><td>{{ row.no }}</td><td>信号1班</td><td><span class="green">已提交</span></td><td>{{ row.count }}</td><td>2025-04-10 14:32</td><td><span :class="row.reviewed ? 'green' : 'orange'">{{ row.reviewed ? '已批阅' : '未批阅' }}</span></td><td>{{ row.score }}</td><td>李浩然（88）, 王志强（85）</td><td><el-button class="primary-action" link @click="openGrade(row.reviewed)"> {{ row.reviewed ? '查看批阅' : '批阅' }} </el-button></td></tr></tbody></table></div>
          </main>
        </div>
      </template>

      <template v-else-if="viewMode === 'stats'">
        <header class="admin-training-subpage-top"><button type="button" class="admin-training-back" @click="backToCourses"><el-icon><ArrowLeft /></el-icon></button><el-breadcrumb separator="/"><el-breadcrumb-item>教学实训</el-breadcrumb-item><el-breadcrumb-item>实训课</el-breadcrumb-item><el-breadcrumb-item>成绩统计</el-breadcrumb-item></el-breadcrumb></header>
        <section class="admin-training-stat-metrics"><article v-for="metric in statMetrics" :key="metric.label"><span>{{ metric.label }}</span><strong>{{ metric.value }}</strong></article></section>
        <section class="admin-training-class-filter"><b>班级筛选</b><button class="active">全部班级</button><button>城轨运营2501班</button><button>城轨机电2502班</button><button>城轨车辆2501班</button></section>
        <section class="admin-training-stat-grid"><article class="wide"><h3>各班级参训人数统计</h3><div class="fake-bars"><i v-for="n in 5" :key="n" :style="{ height: `${120 + n * 18}px` }"></i></div></article><article><h3>成绩等级占比</h3><div class="fake-donut">182<span>总人数</span></div></article><article class="wide"><h3>成绩区间分布</h3><div class="fake-stack"><i v-for="n in 5" :key="n"></i></div></article><article><h3>班级平均分对比分析</h3><p v-for="item in avgBars" :key="item.name"><span>{{ item.name }}</span><b :style="{ width: `${item.value}%` }"></b><em>{{ item.value }}</em></p></article></section>
      </template>

      <el-dialog v-model="monitorDesktopVisible" class="admin-training-monitor-dialog" width="800px" :show-close="false" append-to-body>
        <template #header><div class="admin-training-dialog-head"><strong>学员桌面监控</strong><el-button text circle :icon="Close" @click="monitorDesktopVisible = false" /></div></template>
        <div class="admin-training-desktop-preview"></div>
        <section class="admin-training-desktop-info"><article><span>学员姓名</span><strong>张明远</strong></article><article><span>学号</span><strong>20240301001</strong></article><article><span>所在班级</span><strong>城轨运营专业2...</strong></article><article><span>当前进度</span><strong>8/10</strong></article></section>
        <template #footer><div class="admin-training-dialog-footer"><el-button>上一个</el-button><el-button>下一个</el-button><el-button type="primary" @click="monitorDesktopVisible = false">关闭</el-button></div></template>
      </el-dialog>

      <el-dialog v-model="reviewConfirmVisible" class="admin-training-review-dialog" width="420px" :show-close="false" append-to-body>
        <template #header><div class="admin-training-dialog-head"><strong>{{ gradeReadOnly ? '查看批阅' : '批阅确认' }}</strong><el-button text circle :icon="Close" @click="reviewConfirmVisible = false" /></div></template>
        <p>已进入{{ gradeReadOnly ? '查看批阅' : '批阅' }}页面，包含实训步骤详情、操作视频和底部批阅栏。</p>
        <template #footer><div class="admin-training-dialog-footer"><el-button @click="reviewConfirmVisible = false">关闭</el-button><el-button type="primary" @click="viewMode = 'grade'; reviewConfirmVisible = false">查看详情页</el-button></div></template>
      </el-dialog>

      <template v-if="viewMode === 'grade'">
        <section class="admin-training-grade-page">
          <header class="admin-training-subpage-top"><button type="button" class="admin-training-back" @click="viewMode = 'marking'"><el-icon><ArrowLeft /></el-icon></button><el-breadcrumb separator="/"><el-breadcrumb-item>教学实训</el-breadcrumb-item><el-breadcrumb-item>实训课</el-breadcrumb-item><el-breadcrumb-item>阅卷</el-breadcrumb-item><el-breadcrumb-item>{{ gradeReadOnly ? '查看批阅' : '批阅' }}</el-breadcrumb-item></el-breadcrumb></header>
          <section class="admin-training-grade-info">学生姓名：<b>张明远</b>　学生学号：<b>2024CGXH001</b>　所属班级：<b>2024CGXH001</b>　提交时间：<b>2025-04-10 14:32</b></section>
          <article v-for="block in [1,2]" :key="block" class="admin-training-grade-block"><header><i>{{ block }}</i><div><strong>信号机检修标准化作业实训{{ block === 2 ? '（扮演司机角色）' : '' }}</strong><span>{{ block === 1 ? '单人实训' : '多人实训' }}</span></div><b>整队成绩：29分</b></header><div class="admin-training-grade-layout"><section class="admin-training-grade-table"><h3>实训步骤详情</h3><table><thead><tr><th>序号</th><th>步骤名称</th><th>正确结果</th><th>实际操作</th><th>得分</th><th>用时(秒)</th></tr></thead><tbody><tr v-for="step in archiveSteps" :key="step.name + block"><td>{{ step.id }}</td><td>{{ step.name }}</td><td><span>按十字对角顺序初步拧入所有</span></td><td><span :class="step.score ? 'green' : 'red'">按十字对角顺序初步拧入所有</span></td><td>{{ step.score }}</td><td>{{ step.seconds }}</td></tr></tbody></table></section><aside><h3>实训操作视频</h3><div class="admin-training-video-box"><span></span></div></aside></div></article>
          <footer class="admin-training-grade-footer"><label>实训评语<el-input placeholder="请输入本次实训作业整体评语（选填，最多500字）" /></label><el-button type="primary">保存批阅结果</el-button><span>最终总分：<b>6</b> / 9</span></footer>
        </section>
      </template>

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
          <article><span>应参加</span><strong>48</strong></article>
          <article><span>已完成</span><strong>43</strong></article>
          <article><span>平均分</span><strong>86.5</strong></article>
          <article><span>通过率</span><strong>91%</strong></article>
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
import { computed, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, ArrowLeft, Close, Document, FolderOpened, Monitor, OfficeBuilding, Plus, Search, Tickets, Upload, UploadFilled, User, UserFilled, View } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

type CourseStatus = '已发布' | '未发布';
type SelectorKind = 'topic' | 'resource' | 'paper' | 'class' | 'teacher' | 'room';
type StepKey = 'base' | 'resource' | 'target' | 'rule';
type TrainingViewMode = 'list' | 'form' | 'monitor' | 'marking' | 'grade' | 'stats';

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
const viewMode = ref<TrainingViewMode>('list');
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
const monitorDesktopVisible = ref(false);
const reviewConfirmVisible = ref(false);
const gradeReadOnly = ref(false);
const autoRecord = ref('否');
const scoreRule = ref('最高成绩');

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

const courses = ref<CourseRow[]>([
  { id: 1, name: '期末考试', type: '考试', mode: '单人实训', time: '2025-03-20 08:00\n至 2025-03-20 10:00', target: '城轨信号2401班', teacher: '李明峰、王志强', room: '实训室A-301', status: '已发布', createdAt: '2025-03-15 10:00', topicCount: 3, exam: true },
  { id: 2, name: '信号故障处理综合实训', type: '考试', mode: '协同实训', time: '2025-03-20 08:00\n至 2025-03-20 10:00', target: '城轨信号2401班', teacher: '李明峰、王志强', room: '实训室A-301', status: '已发布', createdAt: '2025-03-15 10:00', topicCount: 4 },
  { id: 3, name: '列车驾驶模拟实训考核', type: '考试', mode: '单人实训', time: '2025-03-12 09:00\n至 2025-03-13 11:00', target: '城轨车辆2401班', teacher: '赵建国', room: '驾驶模拟室B-101', status: '未发布', createdAt: '2025-03-18 14:30', topicCount: 2 },
  { id: 4, name: '站务应急处置实训', type: '练习', mode: '协同实训', time: '2025-03-10 14:00\n至 2025-03-10 16:00', target: '张明亮、孙志强、王欣欣', teacher: '陈志远、李明峰', room: '实训室C-201', status: '已发布', createdAt: '2025-03-05 09:00', topicCount: 5 },
  { id: 5, name: '调度指挥综合实训', type: '练习', mode: '协同实训', time: '2025-03-08 08:30\n至 2025-03-08 11:30', target: '城轨运营2401班', teacher: '陈志远', room: '调度实训室D-401', status: '已发布', createdAt: '2025-03-01 10:00', topicCount: 4 }
]);

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

const cameras = [
  { name: '教室全景摄像头 01', location: '第一实训室 / 前方' },
  { name: '操作区摄像头 02', location: '第一实训室 / 操作台' },
  { name: '走廊入口摄像头 03', location: '第一实训室 / 入口' },
  { name: '教师端摄像头 04', location: '第一实训室 / 讲台' }
];

const students = [
  { name: '李明', studentNo: '202601001', topic: '设备停送电流程', mode: '协同', room: 'A-01', ip: '192.168.1.21', online: true },
  { name: '周雨', studentNo: '202601002', topic: '安全隔离确认', mode: '协同', room: 'A-01', ip: '192.168.1.22', online: true },
  { name: '陈晓', studentNo: '202601003', topic: '故障票填写', mode: '单人', room: 'B-03', ip: '192.168.1.47', online: false }
];

const logs = [
  { time: '2025-03-15 10:00', operator: '张建国', action: '创建实训课', content: '新增信号故障处理综合实训，保存为草稿。' },
  { time: '2025-03-16 09:20', operator: '李明峰', action: '发布实训课', content: '发布到城轨信号2401班，并通知监考教师。' },
  { time: '2025-03-20 08:02', operator: '系统', action: '开始实训', content: '实训教室在线状态正常，已允许学生进入。' }
];

const markingRows = [
  { student: '李明', className: '城轨信号2401班', submitAt: '2025-03-20 10:01', score: 92, status: '已阅' },
  { student: '周雨', className: '城轨信号2401班', submitAt: '2025-03-20 10:03', score: 88, status: '已阅' },
  { student: '陈晓', className: '城轨信号2401班', submitAt: '2025-03-20 10:05', score: '-', status: '待阅' }
];

const statsRows = [
  { className: '城轨信号2401班', total: 48, finished: 43, avg: 86.5, passRate: '91%' },
  { className: '城轨车辆2401班', total: 42, finished: 39, avg: 83.2, passRate: '88%' }
];

const monitorStudents = [
  { name: '张明远', studentNo: '20240301001', score: 89.8, role: '乘客' },
  { name: '李思雨', studentNo: '20240301002', score: 75.9, role: '站台员' },
  { name: '王浩然', studentNo: '20240301003', score: 85.6, role: '司机' },
  { name: '赵晓琳', studentNo: '20240301004', score: 73.9, role: '运营人员' },
  { name: '陈志强', studentNo: '20240301005', score: 91.2, role: '-' },
  { name: '刘雨桐', studentNo: '20240301006', score: '-', role: '-' }
];

const reviewRows = [
  { name: '张明远', no: '2024CGXH001', count: 3, reviewed: true, score: 92 },
  { name: '李晓婷', no: '2024CGXH002', count: 2, reviewed: false, score: 90 },
  { name: '王志强', no: '2024CGXH003', count: 0, reviewed: false, score: '-' },
  { name: '赵雨涵', no: '2024CGXH004', count: 2, reviewed: true, score: 88 },
  { name: '陈浩然', no: '2024CGXH005', count: 4, reviewed: false, score: 88 },
  { name: '刘思琪', no: '2024CGXH006', count: 0, reviewed: false, score: '-' }
];

const archiveSteps = [
  { id: 1, name: '穿戴安全防护用品', score: 1, seconds: 45 },
  { id: 2, name: '检查工具准备情况', score: 1, seconds: 32 },
  { id: 3, name: '确认信号机断电状态', score: 0, seconds: 18 },
  { id: 4, name: '拆卸信号机外壳', score: 1, seconds: 56 },
  { id: 5, name: '检查内部接线端子', score: 1, seconds: 78 },
  { id: 6, name: '清洁透镜组表面', score: 0, seconds: 22 },
  { id: 7, name: '检测灯泡工作状态', score: 1, seconds: 41 },
  { id: 8, name: '测量电路电压参数', score: 1, seconds: 63 },
  { id: 9, name: '调整灯丝转换继电器', score: 0, seconds: 15 }
];

const statMetrics = [
  { label: '实训课名称', value: '2025年春季城轨综合实训期末考' },
  { label: '实训课起止时间', value: '2025-06-20 09:00 - 11:30' },
  { label: '应参训人数', value: '186 人' },
  { label: '实际参训人数', value: '182 人' },
  { label: '未参训人数', value: '4 人' },
  { label: '平均分', value: '78.5 /100' }
];

const avgBars = [
  { name: '城轨信号2501班', value: 84.2 },
  { name: '城轨机电2502班', value: 80.5 },
  { name: '城轨运营2501班', value: 77.8 },
  { name: '城轨供电2501班', value: 75.3 },
  { name: '城轨车辆2501班', value: 72.1 }
];

const filteredCourses = computed(() => courses.value.filter((course) => {
  const keywordMatched = !filters.keyword || course.name.includes(filters.keyword) || course.target.includes(filters.keyword);
  const typeMatched = !filters.type || course.type === filters.type;
  const statusMatched = !filters.status || course.status === filters.status;
  return keywordMatched && typeMatched && statusMatched;
}));

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
}

function refreshCourses() {
  ElMessage.success('查询条件已应用');
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
  viewMode.value = 'form';
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
  viewMode.value = 'form';
}

function saveDraft() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入实训课名称');
    return;
  }

  if (formMode.value === 'create') {
    courses.value.unshift({
      id: Date.now(),
      name: form.name,
      type: form.type,
      mode: form.mode,
      time: formatRange.value,
      target: selectedClasses.value.map((item) => item.name).join('、') || '-',
      teacher: selectedTeachers.value.map((item) => item.name).join('、') || '-',
      room: selectedRoom.value?.name || '-',
      status: '未发布',
      createdAt: '2026-08-02 10:00',
      topicCount: selectedTopics.value.length,
      exam: form.type === '考试'
    });
  }

  ElMessage.success('草稿已保存');
  viewMode.value = 'list';
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

function confirmPublish() {
  if (publishTarget.value) {
    publishTarget.value.status = '已发布';
  } else {
    saveDraft();
  }
  publishVisible.value = false;
  ElMessage.success(publishNotify.value ? '已发布并发送通知' : '已发布');
}

function openImport() {
  importChecked.value = false;
  importVisible.value = true;
}

function confirmImport() {
  importVisible.value = false;
  ElMessage.success('导入预览已确认，已生成草稿');
}

function openMonitor(row: CourseRow) {
  selectedCourse.value = row;
  viewMode.value = 'monitor';
}

function openMarking(row: CourseRow) {
  selectedCourse.value = row;
  viewMode.value = 'marking';
}

function openStats(row: CourseRow) {
  selectedCourse.value = row;
  viewMode.value = 'stats';
}

/** 返回实训课列表页。 */
function backToCourses() {
  viewMode.value = 'list';
}

/** 打开批阅或查看批阅入口弹窗。 */
function openGrade(readOnly: boolean) {
  gradeReadOnly.value = readOnly;
  reviewConfirmVisible.value = true;
}

function openLogs(row: CourseRow) {
  selectedCourse.value = row;
  logVisible.value = true;
}

async function confirmDelete(course: CourseRow) {
  await ElMessageBox.confirm(`确认删除实训课「${course.name}」？`, '删除实训课', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' });
  courses.value = courses.value.filter((item) => item.id !== course.id);
  ElMessage.success('实训课已删除');
}

function copyCourse(course: CourseRow) {
  courses.value.unshift({ ...course, id: Date.now(), name: `${course.name} 副本`, status: '未发布', createdAt: '2026-08-02 10:00' });
  ElMessage.success('已复制为草稿');
}

function withdrawCourse(course: CourseRow) {
  course.status = '未发布';
  ElMessage.success('已撤回发布');
}
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

.admin-training-table th:nth-child(1),
.admin-training-table td:nth-child(1) { width: 190px; }
.admin-training-table th:nth-child(2),
.admin-training-table td:nth-child(2) { width: 80px; }
.admin-training-table th:nth-child(3),
.admin-training-table td:nth-child(3) { width: 170px; }
.admin-training-table th:nth-child(4),
.admin-training-table td:nth-child(4) { width: 150px; }
.admin-training-table th:nth-child(5),
.admin-training-table td:nth-child(5) { width: 130px; }
.admin-training-table th:nth-child(6),
.admin-training-table td:nth-child(6) { width: 110px; }
.admin-training-table th:nth-child(7),
.admin-training-table td:nth-child(7) { width: 110px; }
.admin-training-table th:nth-child(8),
.admin-training-table td:nth-child(8) { width: 150px; }
.admin-training-table th:nth-child(9),
.admin-training-table td:nth-child(9) {
  position: sticky;
  right: 0;
  z-index: 2;
  width: 300px;
  background: #ffffff;
  box-shadow: -10px 0 18px rgba(15, 23, 42, 0.06);
}

.admin-training-table th:nth-child(9) {
  z-index: 3;
  background: #f8fafc;
}

.admin-row-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
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

.admin-training-subpage-top,
.admin-training-monitor-top {
  min-height: 64px;
  display: grid;
  grid-template-columns: 40px 1fr auto;
  align-items: center;
  gap: 14px;
}

.admin-training-subpage-top > strong {
  justify-self: center;
  color: #152238;
  font-size: 18px;
}

.admin-training-subpage-top.is-dark {
  margin: 0 -24px;
  padding: 0 24px;
  background: #ffffff;
}

.admin-training-back {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  background: #f8fafc;
  color: #52657d;
  cursor: pointer;
}

.admin-training-create-card,
.admin-training-monitor-table-card,
.admin-training-marking-side,
.admin-training-marking-main,
.admin-training-stat-metrics article,
.admin-training-class-filter,
.admin-training-stat-grid article,
.admin-training-grade-info,
.admin-training-grade-block {
  border: 1px solid #e3e8f0;
  border-radius: 12px;
  background: #ffffff;
}

.admin-training-create-card {
  margin: 20px 0;
  padding: 24px;
}

.admin-training-create-card header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 22px;
}

.admin-training-create-card header.with-action {
  justify-content: space-between;
}

.admin-training-create-card b {
  color: #152238;
  font-size: 16px;
}

.admin-training-create-card header span,
.admin-training-picked {
  color: #8aa0bd;
  font-size: 13px;
}

.admin-training-create-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px 24px;
}

.admin-training-create-grid label {
  min-width: 0;
  display: grid;
  gap: 8px;
}

.admin-training-create-grid label > span {
  color: #334155;
  font-size: 14px;
  font-weight: 800;
}

.admin-training-create-grid i {
  color: #ef4444;
  font-style: normal;
}

.admin-training-create-grid .range div {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 28px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
}

.admin-training-create-grid em {
  color: #8aa0bd;
  font-style: normal;
  text-align: center;
}

.admin-training-create-table-scroll,
.admin-training-marking-main .admin-training-table-scroll,
.admin-training-monitor-table-card .admin-training-table-scroll {
  overflow-x: auto;
}

.admin-training-create-table,
.admin-training-monitor-table,
.admin-training-marking-table,
.admin-training-grade-table table {
  width: 100%;
  min-width: 960px;
  border-collapse: collapse;
  table-layout: fixed;
}

.admin-training-create-table th,
.admin-training-create-table td,
.admin-training-monitor-table th,
.admin-training-monitor-table td,
.admin-training-marking-table th,
.admin-training-marking-table td,
.admin-training-grade-table th,
.admin-training-grade-table td {
  height: 48px;
  padding: 0 14px;
  color: #334155;
  font-size: 13px;
  text-align: left;
  white-space: nowrap;
}

.admin-training-create-table th,
.admin-training-monitor-table th,
.admin-training-marking-table th,
.admin-training-grade-table th {
  background: #f8fafc;
  font-weight: 900;
}

.drag-dot {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #eef2f7;
  color: #94a3b8;
}

.admin-training-sticky-footer,
.admin-training-grade-footer {
  position: sticky;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding: 16px 24px;
  border: 1px solid #e3e8f0;
  border-radius: 12px;
  background: #ffffff;
}

.admin-training-monitor-top h2 {
  margin: 0;
  color: #152238;
  font-size: 18px;
}

.admin-training-monitor-top p {
  margin: 6px 0 0;
  color: #8aa0bd;
  font-size: 13px;
}

.admin-training-monitor-top > span {
  padding: 8px 18px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2f76ff;
  font-weight: 900;
}

.admin-training-live-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 22px 0;
}

.admin-training-live-title b {
  color: #152238;
}

.admin-training-live-title i {
  padding: 2px 8px;
  border-radius: 4px;
  background: #fff1f2;
  color: #ef4444;
  font-style: normal;
  font-size: 12px;
  font-weight: 900;
}

.admin-training-camera-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
}

.admin-training-camera,
.admin-training-desktop-preview,
.admin-training-video-box {
  position: relative;
  min-height: 420px;
  overflow: hidden;
  border: 0;
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255,255,255,.05), rgba(0,0,0,.35)),
    url("https://images.unsplash.com/photo-1519452575417-564c1401ecc0?auto=format&fit=crop&w=1200&q=80") center / cover;
  color: #ffffff;
  cursor: pointer;
}

.admin-training-camera span,
.admin-training-camera strong,
.admin-training-camera em,
.admin-training-camera small {
  position: absolute;
  z-index: 1;
}

.admin-training-camera span { top: 26px; left: 30px; color: #10b981; font-weight: 900; }
.admin-training-camera strong { top: 26px; right: 18px; padding: 4px 10px; border-radius: 8px; background: rgba(0,0,0,.55); }
.admin-training-camera em { left: 26px; bottom: 20px; font-style: normal; }
.admin-training-camera small { right: 26px; bottom: 20px; }

.admin-training-monitor-table-card {
  margin-top: 24px;
  padding: 18px 0 0;
}

.admin-training-monitor-table-card h3 {
  margin: 0 24px 16px;
  color: #152238;
}

.admin-training-desktop-preview {
  min-height: 420px;
}

.admin-training-desktop-info {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.admin-training-desktop-info article {
  min-height: 58px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f8fafc;
}

.admin-training-desktop-info span {
  color: #8aa0bd;
  font-size: 12px;
}

.admin-training-desktop-info strong {
  display: block;
  margin-top: 4px;
  color: #152238;
}

.admin-training-marking-layout {
  display: grid;
  grid-template-columns: 282px minmax(0, 1fr);
  gap: 16px;
  margin: 24px -24px 0;
  padding: 24px;
  background: #000000;
}

.admin-training-marking-side {
  min-height: 760px;
  padding: 20px 16px;
}

.admin-training-marking-side b {
  display: block;
  margin-bottom: 24px;
  color: #152238;
}

.admin-training-marking-side article {
  display: grid;
  gap: 12px;
  padding: 18px 14px;
  border-radius: 8px;
  color: #334155;
}

.admin-training-marking-side article.active {
  background: #eef5ff;
}

.admin-training-marking-side span {
  width: fit-content;
  padding: 4px 10px;
  border-radius: 4px;
  background: #3b82f6;
  color: #ffffff;
  font-size: 12px;
}

.admin-training-marking-main {
  min-width: 0;
  overflow: hidden;
}

.admin-training-marking-filter {
  display: flex;
  align-items: end;
  flex-wrap: wrap;
  gap: 14px;
  padding: 28px 24px 20px;
  border-bottom: 14px solid #000000;
}

.admin-training-marking-filter label {
  display: grid;
  gap: 8px;
  min-width: 200px;
  color: #334155;
  font-size: 13px;
  font-weight: 800;
}

.admin-training-marking-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 24px;
}

.admin-training-marking-tabs button {
  min-width: 92px;
  height: 32px;
  border: 0;
  border-radius: 8px;
  background: #eef2f7;
  color: #52657d;
  font-weight: 900;
}

.admin-training-marking-tabs button.active {
  background: #3b82f6;
  color: #ffffff;
}

.admin-training-marking-tabs .el-button {
  margin-left: auto;
}

.admin-training-marking-table {
  min-width: 1480px;
}

.admin-training-marking-table .green,
.admin-training-grade-table .green {
  padding: 4px 10px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #059669;
  font-weight: 900;
}

.admin-training-marking-table .orange,
.admin-training-grade-table .red {
  padding: 4px 10px;
  border-radius: 999px;
  background: #fff7ed;
  color: #f97316;
  font-weight: 900;
}

.admin-training-grade-page {
  margin: -24px;
  padding: 0 24px 110px;
  background: #f5f7fb;
}

.admin-training-grade-info {
  min-height: 50px;
  display: flex;
  align-items: center;
  gap: 22px;
  margin: 20px 0;
  padding: 0 24px;
  color: #8aa0bd;
  font-weight: 900;
}

.admin-training-grade-info b {
  color: #334155;
}

.admin-training-grade-block {
  margin-bottom: 18px;
  padding: 20px 24px;
}

.admin-training-grade-block > header {
  min-height: 52px;
  display: grid;
  grid-template-columns: 40px 1fr auto;
  align-items: center;
  gap: 16px;
}

.admin-training-grade-block header i {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #eef5ff;
  color: #2f76ff;
  font-style: normal;
  font-size: 20px;
  font-weight: 900;
}

.admin-training-grade-layout {
  display: grid;
  grid-template-columns: minmax(620px, 1.45fr) minmax(420px, .95fr);
  gap: 20px;
  margin-top: 12px;
}

.admin-training-grade-table,
.admin-training-grade-block aside {
  overflow: hidden;
  border: 1px solid #e3e8f0;
  border-radius: 12px;
  background: #ffffff;
}

.admin-training-grade-table h3,
.admin-training-grade-block aside h3 {
  margin: 0;
  padding: 18px 24px;
  color: #152238;
  font-size: 15px;
}

.admin-training-video-box {
  min-height: 340px;
  margin: 0 24px 24px;
}

.admin-training-grade-footer {
  position: fixed;
  right: 0;
  left: 240px;
  border-radius: 0;
}

.admin-training-grade-footer label {
  flex: 1;
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
}

.admin-training-stat-metrics {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin: 24px 0;
}

.admin-training-stat-metrics article {
  min-height: 104px;
  padding: 20px;
}

.admin-training-stat-metrics span {
  display: block;
  color: #8aa0bd;
  font-size: 13px;
}

.admin-training-stat-metrics strong {
  display: block;
  margin-top: 8px;
  color: #152238;
  font-size: 20px;
  line-height: 24px;
}

.admin-training-class-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding: 16px 24px;
}

.admin-training-class-filter button {
  height: 36px;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  background: #ffffff;
  color: #52657d;
  padding: 0 16px;
}

.admin-training-class-filter button.active {
  border-color: #4f46e5;
  background: #4f46e5;
  color: #ffffff;
}

.admin-training-stat-grid {
  display: grid;
  grid-template-columns: minmax(520px, 1.4fr) minmax(360px, .8fr);
  gap: 20px;
  margin-top: 20px;
}

.admin-training-stat-grid article {
  min-height: 360px;
  padding: 24px;
}

.admin-training-stat-grid article.wide {
  min-width: 0;
}

.fake-bars,
.fake-stack {
  height: 250px;
  display: flex;
  align-items: end;
  justify-content: center;
  gap: 28px;
  margin-top: 28px;
  border-bottom: 1px solid #dbe4ef;
}

.fake-bars i {
  width: 34px;
  background: linear-gradient(180deg, #6366f1 0 48%, #35c498 48%);
}

.fake-stack i {
  width: 38px;
  height: 180px;
  background: linear-gradient(180deg, #ef4444 0 18%, #f59e0b 18% 42%, #3b82f6 42% 63%, #10b981 63%);
}

.fake-donut {
  width: 180px;
  height: 180px;
  display: grid;
  place-items: center;
  margin: 42px auto;
  border: 28px solid #eef2f7;
  border-right-color: #f59e0b;
  border-bottom-color: #3b82f6;
  border-radius: 50%;
  color: #152238;
  font-size: 28px;
  font-weight: 900;
}

.fake-donut span {
  display: block;
  color: #8aa0bd;
  font-size: 12px;
}

.admin-training-stat-grid p {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) 44px;
  align-items: center;
  gap: 12px;
}

.admin-training-stat-grid p b {
  height: 24px;
  border-radius: 0;
  background: #3b82f6;
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

  .admin-training-create-grid,
  .admin-training-camera-grid,
  .admin-training-marking-layout,
  .admin-training-grade-layout,
  .admin-training-stat-metrics,
  .admin-training-stat-grid,
  .admin-training-desktop-info {
    grid-template-columns: 1fr;
  }

  .admin-training-grade-footer {
    left: 0;
  }
}
</style>
