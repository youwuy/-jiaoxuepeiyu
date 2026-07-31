<template>
  <AdminShell activeKey="admin-courses">
    <section class="admin-course-form-page">
      <header class="admin-course-form-topbar">
        <el-breadcrumb class="admin-course-form-breadcrumb" separator="/">
          <el-breadcrumb-item>教学实训</el-breadcrumb-item>
          <el-breadcrumb-item>教学课程</el-breadcrumb-item>
          <el-breadcrumb-item>新增课程</el-breadcrumb-item>
        </el-breadcrumb>

        <el-button class="admin-course-form-back" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </header>

      <section class="admin-course-form-card basic">
        <header class="admin-course-form-card-title">
          <el-icon><InfoFilled /></el-icon>
          <strong>基本信息</strong>
        </header>

        <div class="admin-course-form-fields">
          <label class="admin-course-form-field full">
            <span>课程名称 <b>*</b></span>
            <el-input v-model="form.courseName" placeholder="请输入课程名称" />
          </label>

          <label class="admin-course-form-field">
            <span>
              教学开始时间 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="请选择开始时间" />
          </label>

          <label class="admin-course-form-field">
            <span>
              教学结束时间 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-date-picker v-model="form.endTime" type="datetime" placeholder="请选择结束时间" />
          </label>

          <label class="admin-course-form-field">
            <span>所属学年学期 <b>*</b></span>
            <el-select v-model="form.term" placeholder="请选择学期">
              <el-option label="2024-2025学年 下学期" value="2024-2025学年 下学期" />
              <el-option label="2025-2026学年 上学期" value="2025-2026学年 上学期" />
            </el-select>
          </label>

          <label class="admin-course-form-field">
            <span>
              课件完成度满分 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-input v-model="form.coursewareScore" placeholder="请输入满分值" />
          </label>

          <div class="admin-course-form-field full radio">
            <span>
              学生学习模式 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-radio-group v-model="form.learningMode" class="admin-course-form-radio">
              <el-radio label="FREE">自由学习</el-radio>
              <el-radio label="LOCKED">顺序解锁</el-radio>
            </el-radio-group>
          </div>
        </div>
      </section>

      <section class="admin-course-form-card compact">
        <header class="admin-course-form-card-title">
          <el-icon><UserFilled /></el-icon>
          <strong>教学团队</strong>
        </header>
        <div class="admin-course-form-tags">
          <el-tag closable type="primary" effect="light">李立峰</el-tag>
          <el-button plain @click="showComingSoon('添加教师')">
            <el-icon><Plus /></el-icon>
            添加教师
          </el-button>
        </div>
      </section>

      <section class="admin-course-form-card compact">
        <header class="admin-course-form-card-title">
          <el-icon><User /></el-icon>
          <strong>授课班级</strong>
        </header>
        <div class="admin-course-form-tags">
          <el-tag closable type="success" effect="light">城轨信号2401班</el-tag>
          <el-button plain @click="showComingSoon('添加班级')">
            <el-icon><Plus /></el-icon>
            添加班级
          </el-button>
        </div>
      </section>

      <section class="admin-course-form-card content">
        <header class="admin-course-form-card-title split">
          <span>
            <el-icon><Menu /></el-icon>
            <strong>教学内容</strong>
          </span>
          <el-button type="primary" class="admin-course-form-primary" @click="showComingSoon('新增章节')">
            <el-icon><Plus /></el-icon>
            新增章节
          </el-button>
        </header>

        <div class="admin-course-outline">
          <article v-for="chapter in chapters" :key="chapter.id" class="admin-course-outline-chapter">
            <div class="admin-course-outline-row admin-course-outline-chapter-row">
              <span class="admin-course-outline-left">
                <el-icon><ArrowDown /></el-icon>
                <el-icon class="folder"><Folder /></el-icon>
                <strong>{{ chapter.title }}</strong>
              </span>
              <span class="admin-course-outline-actions">
                <el-button text type="success" @click="showComingSoon('添加课件资源')">添加课件资源</el-button>
                <el-button text type="success" @click="showComingSoon('添加作业')">添加作业</el-button>
                <el-button text type="primary" @click="showComingSoon('编辑')">编辑</el-button>
                <el-button text type="danger" @click="showComingSoon('删除')">删除</el-button>
              </span>
            </div>

            <template v-for="section in chapter.sections" :key="section.id">
              <div class="admin-course-outline-row admin-course-outline-section-row">
                <span class="admin-course-outline-left">
                  <el-icon><ArrowDown /></el-icon>
                  <strong>{{ section.title }}</strong>
                </span>
                <span class="admin-course-outline-actions">
                  <el-button text type="success" @click="showComingSoon('添加课件资源')">添加课件资源</el-button>
                  <el-button text type="success" @click="showComingSoon('添加作业')">添加作业</el-button>
                  <el-button text type="primary" @click="showComingSoon('编辑')">编辑</el-button>
                  <el-button text type="danger" @click="showComingSoon('删除')">删除</el-button>
                </span>
              </div>

              <div
                v-for="item in section.items"
                :key="item.id"
                class="admin-course-outline-row admin-course-outline-resource-row"
              >
                <span class="admin-course-outline-drag">::</span>
                <span class="admin-course-outline-icon" :class="item.type">
                  <el-icon><component :is="item.type === 'homework' ? Checked : Document" /></el-icon>
                </span>
                <span class="admin-course-outline-info">
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.desc }}</small>
                </span>
                <span class="admin-course-outline-actions compact-actions">
                  <el-button text type="primary" @click="showComingSoon('编辑')">编辑</el-button>
                  <el-button text type="danger" @click="showComingSoon('删除')">删除</el-button>
                </span>
              </div>
            </template>
          </article>
        </div>
      </section>

      <footer class="admin-course-form-footer">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="saveCourse">保存</el-button>
      </footer>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowDown,
  ArrowLeft,
  Checked,
  Document,
  Folder,
  InfoFilled,
  Menu,
  Plus,
  User,
  UserFilled
} from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

const router = useRouter();

const form = reactive({
  courseName: '',
  startTime: '',
  endTime: '',
  term: '2024-2025学年 下学期',
  coursewareScore: '',
  learningMode: 'FREE'
});

const chapters = [
  {
    id: 1,
    title: '第一章 信号系统概述',
    sections: [
      {
        id: 11,
        title: '1.1 轨道交通与信号系统基本概念',
        items: [
          {
            id: 111,
            type: 'homework',
            title: '课程作业：信号系统组成分析报告',
            desc: '提交即完成 | 截止时间：2025-04-15 23:59'
          },
          {
            id: 112,
            type: 'resource',
            title: '课件资源：信号系统原理.pptx',
            desc: '最低预览：2分钟 | 可学时段：2025-01-01至2025-04-10'
          }
        ]
      },
      {
        id: 12,
        title: '1.2 信号系统核心作用与特点',
        items: []
      }
    ]
  },
  {
    id: 2,
    title: '第二章 联锁系统原理',
    sections: []
  },
  {
    id: 3,
    title: '第三章 ATS系统',
    sections: []
  }
];

function goBack() {
  router.push('/admin/courses');
}

function saveCourse() {
  ElMessage.success('课程已保存');
  goBack();
}

function showComingSoon(label: string) {
  ElMessage.info(`${label}功能待接入接口`);
}
</script>
