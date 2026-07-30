<template>
  <section class="login-shell student-theme">
    <div class="login-visual">
      <div class="brand-mark">学</div>
      <p class="system-kicker">Web 教辅系统</p>
      <h1>学员端</h1>
      <p class="system-copy">课程学习、实训任务、资源浏览与综合成绩复盘统一入口。</p>
      <div class="progress-card">
        <span>学习进度</span>
        <strong>76%</strong>
        <div><i /></div>
      </div>
    </div>

    <main class="login-panel">
      <div class="panel-header">
        <p>欢迎</p>
        <h2>登录学员端</h2>
      </div>

      <el-tabs v-model="mode" stretch class="login-tabs" @tab-change="switchMode">
        <el-tab-pane label="学号登录" name="studentId" />
        <el-tab-pane label="手机号登录" name="phone" />
      </el-tabs>

      <el-form label-position="top" @submit.prevent>
        <el-form-item v-if="mode === 'studentId'" label="学号" :error="errors.studentId">
          <el-input
            v-model="form.studentId"
            size="large"
            placeholder="请输入学号"
            clearable
            @input="onStudentIdInput"
          />
        </el-form-item>

        <el-form-item v-else label="手机号" :error="errors.phone">
          <el-input
            v-model="form.phone"
            size="large"
            placeholder="请输入手机号"
            maxlength="11"
            clearable
            @input="onPhoneInput"
          />
        </el-form-item>

        <el-form-item label="密码" :error="errors.password">
          <el-input
            v-model="form.password"
            size="large"
            placeholder="请输入密码"
            show-password
            maxlength="20"
            @input="onPasswordInput"
            @keyup.enter="submit"
          />
        </el-form-item>

        <el-button class="login-button" type="primary" size="large" :loading="loading" @click="submit">登录</el-button>
      </el-form>

    </main>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { loginStudent } from '../../api/auth';
import {
  normalizePasswordInput,
  normalizePhoneInput,
  normalizeStudentIdInput,
  validateStudentLogin,
  type StudentLoginErrors,
  type StudentLoginMode
} from '../../features/auth/validation';

const router = useRouter();
const mode = ref<StudentLoginMode>('studentId');
const loading = ref(false);

const form = reactive({
  studentId: '',
  phone: '',
  password: ''
});

const errors = reactive<StudentLoginErrors>({});

function clearErrors() {
  errors.studentId = undefined;
  errors.phone = undefined;
  errors.password = undefined;
}

function switchMode() {
  if (mode.value === 'studentId') {
    form.phone = '';
  } else {
    form.studentId = '';
  }
  form.password = '';
  clearErrors();
}

function onStudentIdInput(value: string) {
  form.studentId = normalizeStudentIdInput(value);
  errors.studentId = undefined;
}

function onPhoneInput(value: string) {
  form.phone = normalizePhoneInput(value);
  errors.phone = undefined;
}

function onPasswordInput(value: string) {
  form.password = normalizePasswordInput(value);
  errors.password = undefined;
}

async function submit() {
  clearErrors();
  Object.assign(errors, validateStudentLogin(mode.value, form));

  if (Object.values(errors).some(Boolean)) {
    return;
  }

  loading.value = true;
  try {
    await loginStudent(mode.value, form);
    ElMessage.success('登录成功');
    router.push('/student/courses');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}
</script>
