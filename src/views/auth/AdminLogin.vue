<template>
  <section class="login-shell admin-theme">
    <div class="login-visual">
      <div class="brand-mark">教</div>
      <p class="system-kicker">Web 教辅系统</p>
      <h1>管理端</h1>
      <p class="system-copy">资源管理、课程组课、实训监考与成绩统计一体化工作台。</p>
      <div class="visual-grid" aria-hidden="true">
        <span v-for="item in 12" :key="item"></span>
      </div>
    </div>

    <main class="login-panel">
      <div class="panel-header">
        <p>欢迎登录</p>
        <h2>后台管理平台</h2>
      </div>

      <el-form label-position="top" @submit.prevent>
        <el-form-item label="账号" :error="errors.account">
          <el-input
            v-model="form.account"
            size="large"
            placeholder="请输入账号"
            clearable
            @input="errors.account = undefined"
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

        <el-button class="login-button" type="primary" size="large" @click="submit">登录</el-button>
      </el-form>

      <div class="panel-footer">
        <RouterLink to="/student/login">进入学员端</RouterLink>
      </div>
    </main>
  </section>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { ElMessageBox } from 'element-plus';
import { normalizePasswordInput, validateAdminLogin, type AdminLoginErrors } from '../../features/auth/validation';

const form = reactive({
  account: '',
  password: ''
});

const errors = reactive<AdminLoginErrors>({});

function onPasswordInput(value: string) {
  form.password = normalizePasswordInput(value);
  errors.password = undefined;
}

function submit() {
  Object.assign(errors, { account: undefined, password: undefined }, validateAdminLogin(form));

  if (Object.values(errors).some(Boolean)) {
    return;
  }

  ElMessageBox.alert('登录接口待后端联调接入', '提示', {
    confirmButtonText: '知道了'
  });
}
</script>
