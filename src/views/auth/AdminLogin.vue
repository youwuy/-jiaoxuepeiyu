<template>
  <section class="admin-calicat-login">
    <aside class="admin-calicat-photo" aria-hidden="true"></aside>

    <main class="admin-calicat-stage">
      <form class="admin-calicat-card" @submit.prevent="submit">
        <header class="admin-calicat-header">
          <div class="admin-calicat-logo">
            <el-icon><UserFilled /></el-icon>
          </div>
          <h1>城轨实训管理平台</h1>
        </header>

        <div class="admin-calicat-tabs" role="tablist" aria-label="登录方式">
          <button
            type="button"
            role="tab"
            :aria-selected="mode === 'workNo'"
            :class="{ active: mode === 'workNo' }"
            @click="setMode('workNo')"
          >
            <el-icon><Postcard /></el-icon>
            工号登录
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

        <div v-if="mode === 'workNo'" class="admin-calicat-field">
          <label for="admin-work-no">工号</label>
          <el-input
            id="admin-work-no"
            v-model="form.account"
            class="admin-calicat-input"
            :prefix-icon="User"
            placeholder="请输入工号"
            @input="errors.account = undefined"
          />
          <p v-if="errors.account" class="admin-calicat-error">{{ errors.account }}</p>
        </div>

        <div v-else class="admin-calicat-field">
          <label for="admin-phone">手机号</label>
          <el-input
            id="admin-phone"
            v-model="form.account"
            class="admin-calicat-input"
            :prefix-icon="Iphone"
            placeholder="请输入手机号"
            maxlength="11"
            @input="onPhoneInput"
          />
          <p v-if="errors.account" class="admin-calicat-error">{{ errors.account }}</p>
        </div>

        <div class="admin-calicat-field">
          <label for="admin-password">密码</label>
          <el-input
            id="admin-password"
            v-model="form.password"
            class="admin-calicat-input"
            :prefix-icon="Lock"
            placeholder="请输入密码"
            show-password
            maxlength="20"
            @input="onPasswordInput"
          />
          <p v-if="errors.password" class="admin-calicat-error">{{ errors.password }}</p>
        </div>

        <el-button class="admin-calicat-button" native-type="submit" :loading="loading">登 录</el-button>

        <p class="admin-calicat-help">
          <el-icon><InfoFilled /></el-icon>
          如无法登录，请联系系统管理员
        </p>
      </form>
    </main>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { InfoFilled, Iphone, Lock, Postcard, User, UserFilled } from '@element-plus/icons-vue';
import { loginAdmin } from '../../api/auth';
import {
  normalizePasswordInput,
  normalizePhoneInput,
  validateAdminLogin,
  type AdminLoginErrors
} from '../../features/auth/validation';

type AdminLoginMode = 'workNo' | 'phone';

const router = useRouter();
const mode = ref<AdminLoginMode>('workNo');
const form = reactive({
  account: '',
  password: ''
});

const errors = reactive<AdminLoginErrors>({});
const loading = ref(false);

function clearErrors() {
  errors.account = undefined;
  errors.password = undefined;
}

function setMode(nextMode: AdminLoginMode) {
  if (mode.value === nextMode) {
    return;
  }

  mode.value = nextMode;
  form.account = '';
  form.password = '';
  clearErrors();
}

function onPhoneInput(value: string) {
  form.account = normalizePhoneInput(value);
  errors.account = undefined;
}

function onPasswordInput(value: string) {
  form.password = normalizePasswordInput(value);
  errors.password = undefined;
}

function validateForm() {
  Object.assign(errors, { account: undefined, password: undefined }, validateAdminLogin(form));

  if (mode.value === 'phone' && form.account && !/^\d{11}$/.test(form.account)) {
    errors.account = '手机号格式不正确';
  }
}

async function submit() {
  if (loading.value) {
    return;
  }

  validateForm();

  if (Object.values(errors).some(Boolean)) {
    return;
  }

  loading.value = true;
  try {
    await loginAdmin(form, mode.value === 'phone' ? 'phone' : 'username');
    ElMessage.success('登录成功');
    await router.push('/admin/training');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}
</script>
