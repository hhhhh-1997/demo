<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../stores/user'
import logoWhite from '../../assets/logo-white.png'

/** 登录表单模型。 */
interface LoginFormModel {
  username: string
  password: string
}

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive<LoginFormModel>({
  username: '',
  password: '',
})

const rules: FormRules<LoginFormModel> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

/** 提交登录：校验通过后调用 store 登录并跳转首页。 */
async function handleLogin(): Promise<void> {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    await router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg" aria-hidden="true">
      <span class="blob blob-1"></span>
      <span class="blob blob-2"></span>
      <span class="blob blob-3"></span>
    </div>
    <div class="login-card">
      <div class="login-logo">
        <img class="login-logo-img" :src="logoWhite" alt="logo" />
      </div>
      <h1 class="login-title">储备项目管理系统</h1>
      <p class="login-subtitle">综合计划储备项目管理平台</p>
      <p class="login-tagline">项目全生命周期 · 论证审核下达 · 接口链路自动化</p>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="'User'"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="'Lock'"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
  background: radial-gradient(
    ellipse at center,
    var(--bg-secondary) 0%,
    var(--bg-tertiary) 100%
  );
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.3;
}

.blob-1 {
  width: 420px;
  height: 420px;
  top: -120px;
  left: -80px;
  background: #409eff;
}

.blob-2 {
  width: 360px;
  height: 360px;
  bottom: -100px;
  right: -60px;
  background: #67c23a;
}

.blob-3 {
  width: 300px;
  height: 300px;
  bottom: 18%;
  left: 10%;
  background: #e6a23c;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 380px;
  padding: 2.5rem 2rem;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
}

.login-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.login-logo-img {
  height: 64px;
  width: auto;
}

.login-title {
  margin: 0 0 0.5rem;
  font-size: 1.5rem;
  text-align: center;
  color: var(--text-primary);
}

.login-subtitle {
  margin: 0 0 0.5rem;
  text-align: center;
  color: var(--text-secondary);
}

.login-tagline {
  margin: 0 0 2rem;
  text-align: center;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.login-button {
  width: 100%;
}
</style>
