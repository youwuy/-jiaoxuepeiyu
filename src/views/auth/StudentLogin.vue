<template>
  <section class="login-shell student-theme calicat-login">
    <aside class="calicat-login-visual" aria-hidden="true">
      <span class="calicat-orb orb-large"></span>
      <span class="calicat-orb orb-medium"></span>
      <span class="calicat-orb orb-small"></span>

      <div v-if="mode === 'phone'" class="calicat-brand-content">
        <div class="calicat-brand-icon">
          <el-icon><Monitor /></el-icon>
        </div>
        <h1>城轨实训平台</h1>
        <p>城市轨道交通虚拟仿真实训系统</p>
        <ul>
          <li v-for="feature in brandFeatures" :key="feature">
            <span><el-icon><Check /></el-icon></span>
            {{ feature }}
          </li>
        </ul>
      </div>
    </aside>

    <main class="calicat-login-panel">
      <form class="calicat-login-form" @submit.prevent="submit">
        <h2>欢迎</h2>

        <div class="calicat-login-tabs" role="tablist" aria-label="登录方式">
          <button
            type="button"
            role="tab"
            :aria-selected="mode === 'studentId'"
            :class="{ active: mode === 'studentId' }"
            @click="setMode('studentId')"
          >
            <el-icon><User /></el-icon>
            学号登录
          </button>
          <button
            type="button"
            role="tab"
            :aria-selected="mode === 'phone'"
            :class="{ active: mode === 'phone' }"
            @click="setMode('phone')"
          >
            <el-icon><Iphone /></el-icon>
            手机号登录
          </button>
        </div>

        <div v-if="mode === 'studentId'" class="calicat-field">
          <label for="student-login-id">学号</label>
          <el-input
            id="student-login-id"
            v-model="form.studentId"
            class="calicat-input"
            :prefix-icon="User"
            placeholder="请输入学号"
            @input="onStudentIdInput"
          />
          <p v-if="errors.studentId" class="calicat-field-error">{{ errors.studentId }}</p>
        </div>

        <div v-else class="calicat-field">
          <label for="student-login-phone">手机号</label>
          <el-input
            id="student-login-phone"
            v-model="form.phone"
            class="calicat-input"
            :prefix-icon="Iphone"
            placeholder="请输入手机号"
            maxlength="11"
            @input="onPhoneInput"
          />
          <p v-if="errors.phone" class="calicat-field-error">{{ errors.phone }}</p>
        </div>

        <div class="calicat-field">
          <label for="student-login-password">密码</label>
          <el-input
            id="student-login-password"
            v-model="form.password"
            class="calicat-input"
            :prefix-icon="Lock"
            placeholder="请输入密码"
            show-password
            maxlength="20"
            @input="onPasswordInput"
            @keyup.enter="submit"
          />
          <p v-if="errors.password" class="calicat-field-error">{{ errors.password }}</p>
        </div>

        <el-button class="calicat-login-button" native-type="submit" :loading="loading">登 录</el-button>
      </form>
    </main>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Check, Iphone, Lock, Monitor, User } from '@element-plus/icons-vue';
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
const brandFeatures = ['沉浸式虚拟仿真实训环境', '多岗位协同演练与考核', '实时成绩分析与智能评估'];

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

function setMode(nextMode: StudentLoginMode) {
  if (mode.value === nextMode) {
    return;
  }

  mode.value = nextMode;

  if (nextMode === 'studentId') {
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
