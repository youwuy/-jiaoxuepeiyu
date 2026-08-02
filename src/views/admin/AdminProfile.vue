<template>
  <AdminShell activeKey="profile">
    <section class="admin-profile-page">
      <header class="admin-profile-top">个人中心</header>

      <section class="admin-profile-card">
        <label v-for="field in profileFields" :key="field.key" class="admin-profile-field">
          <span>{{ field.label }}</span>
          <button
            type="button"
            class="admin-profile-value"
            :class="{ editable: field.editable }"
            :disabled="!field.editable"
            @click="field.editable && openEdit(field.key)"
          >
            <b>{{ field.value }}</b>
            <el-icon v-if="field.editable"><EditPen /></el-icon>
          </button>
        </label>
      </section>

      <el-dialog v-model="phoneVisible" class="admin-profile-edit-dialog is-compact" width="480px" :show-close="false" append-to-body>
        <template #header><DialogHead title="修改手机号" @close="phoneVisible = false" /></template>
        <div class="admin-profile-dialog-body">
          <el-input v-model="forms.phone" placeholder="请输入手机号" />
        </div>
        <template #footer><DialogFooter @cancel="phoneVisible = false" @confirm="savePhone" /></template>
      </el-dialog>

      <el-dialog v-model="idCardVisible" class="admin-profile-edit-dialog is-compact" width="480px" :show-close="false" append-to-body>
        <template #header><DialogHead title="修改身份证号" @close="idCardVisible = false" /></template>
        <div class="admin-profile-dialog-body">
          <el-input v-model="forms.idCard" placeholder="请输入身份证号" />
        </div>
        <template #footer><DialogFooter @cancel="idCardVisible = false" @confirm="saveIdCard" /></template>
      </el-dialog>

      <el-dialog v-model="passwordVisible" class="admin-profile-edit-dialog is-password" width="480px" :show-close="false" append-to-body>
        <template #header><DialogHead title="修改密码" @close="passwordVisible = false" /></template>
        <div class="admin-profile-password-form">
          <label>
            <span>原密码 <b>*</b></span>
            <el-input v-model="forms.oldPassword" :prefix-icon="Lock" placeholder="请输入原密码" type="password" show-password />
          </label>
          <label>
            <span>新密码 <b>*</b></span>
            <el-input v-model="forms.newPassword" :prefix-icon="Lock" placeholder="请输入新密码" type="password" show-password />
            <em><el-icon><InfoFilled /></el-icon>密码长度8-20位，需包含字母、数字</em>
          </label>
          <label>
            <span>确认新密码 <b>*</b></span>
            <el-input v-model="forms.confirmPassword" :prefix-icon="Lock" placeholder="请再次输入新密码" type="password" show-password />
          </label>
        </div>
        <template #footer><DialogFooter @cancel="passwordVisible = false" @confirm="savePassword" /></template>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, EditPen, InfoFilled, Lock } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

type EditKey = 'phone' | 'idCard' | 'password' | 'name' | 'workNo' | 'organization';

const phoneVisible = ref(false);
const idCardVisible = ref(false);
const passwordVisible = ref(false);
const user = reactive({
  name: '张鸣',
  workNo: 'J201938432',
  organization: '交通与车辆工程院/运输管理教研室/车辆管理',
  phone: '132****3209',
  idCard: '410***********1234',
  password: '········'
});
const forms = reactive({
  phone: '13208948888',
  idCard: '410322201005124734',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const profileFields = computed(() => [
  { key: 'name' as EditKey, label: '姓名', value: user.name, editable: false },
  { key: 'workNo' as EditKey, label: '工号', value: user.workNo, editable: false },
  { key: 'organization' as EditKey, label: '所属组织', value: user.organization, editable: false },
  { key: 'phone' as EditKey, label: '手机号', value: user.phone, editable: true },
  { key: 'idCard' as EditKey, label: '身份证号', value: user.idCard, editable: true },
  { key: 'password' as EditKey, label: '密码', value: user.password, editable: true }
]);

const DialogHead = defineComponent({
  props: { title: { type: String, required: true } },
  emits: ['close'],
  setup(props, { emit }) {
    return () => h('div', { class: 'admin-profile-dialog-head' }, [
      h('strong', props.title),
      h('button', { type: 'button', onClick: () => emit('close') }, [h(Close)])
    ]);
  }
});

const DialogFooter = defineComponent({
  emits: ['cancel', 'confirm'],
  setup(_, { emit }) {
    return () => h('div', { class: 'admin-profile-dialog-footer' }, [
      h('button', { type: 'button', class: 'ghost', onClick: () => emit('cancel') }, '取消'),
      h('button', { type: 'button', class: 'primary', onClick: () => emit('confirm') }, '确定修改')
    ]);
  }
});

/** 打开个人中心字段编辑弹窗。 */
function openEdit(key: EditKey) {
  if (key === 'phone') phoneVisible.value = true;
  if (key === 'idCard') idCardVisible.value = true;
  if (key === 'password') passwordVisible.value = true;
}

/** 保存手机号并回显脱敏值。 */
function savePhone() {
  user.phone = forms.phone.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2');
  phoneVisible.value = false;
  ElMessage.success('手机号已修改');
}

/** 保存身份证号并回显脱敏值。 */
function saveIdCard() {
  user.idCard = forms.idCard.replace(/^(.{3}).+(.{4})$/, '$1***********$2');
  idCardVisible.value = false;
  ElMessage.success('身份证号已修改');
}

/** 保存密码并清空密码表单。 */
function savePassword() {
  passwordVisible.value = false;
  forms.oldPassword = '';
  forms.newPassword = '';
  forms.confirmPassword = '';
  ElMessage.success('密码已修改');
}
</script>
