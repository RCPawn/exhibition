<template>
  <div class="login-page">
    <div class="login-wrapper">
      <!-- 左侧：视觉展示区 -->
      <div class="brand-side">
        <div class="brand-content">
          <div class="badge">SYSTEM ACCESS</div>
          <h1 class="brand-title">
            BAI<br />HERITAGE
          </h1>
          <div class="brand-divider"></div>
          <p class="brand-desc">
            白族非遗智能展示与管理平台<br />
          </p>
          <!-- 装饰性元素：极简白族雷纹装饰 -->
          <div class="pattern-decor"></div>
        </div>
      </div>

      <!-- 右侧：登录表单区 -->
      <div class="form-side">
        <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
          <div class="form-header">
            <h3 class="title">身份准入 / LOGIN</h3>
            <p class="subtitle">请输入凭证以进入数字资源控制中心</p>
          </div>

          <el-form-item prop="username">
            <el-input
                v-model="loginForm.username"
                type="text"
                size="large"
                placeholder="账号"
            >
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                size="large"
                placeholder="密码"
                @keyup.enter="handleLogin"
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="code" v-if="captchaEnabled">
            <el-input
                v-model="loginForm.code"
                size="large"
                placeholder="验证码"
                style="width: 60%"
                @keyup.enter="handleLogin"
            >
              <template #prefix><el-icon><CircleCheck /></el-icon></template>
            </el-input>
            <div class="login-code">
              <img :src="codeUrl" @click="getCode" class="login-code-img"/>
            </div>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">保持登录状态</el-checkbox>
            <router-link v-if="register" class="reg-link" :to="'/register'">新用户注册</router-link>
          </div>

          <el-form-item style="width:100%; margin-top: 10px;">
            <button
                :disabled="loading"
                class="poizon-btn"
                @click.prevent="handleLogin"
            >
              <span v-if="!loading">授权进入</span>
              <span v-else>正在同步数据...</span>
            </button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!--  底部  -->
<!--    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>-->
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
// import defaultSettings from '@/settings'
import { User, Lock, CircleCheck } from '@element-plus/icons-vue'

// const title = import.meta.env.VITE_APP_TITLE
// const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)
const register = ref(true)
const redirect = ref(undefined)

watch(route, (newRoute) => {
  redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      userStore.login(loginForm.value).then(() => {
        router.push({ path: redirect.value || "/" })
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f0f0;
  // 数字化网格背景
  background-image: linear-gradient(#e0e0e0 1px, transparent 1px),
  linear-gradient(90deg, #e0e0e0 1px, transparent 1px);
  background-size: 40px 40px;
  overflow: hidden;
  position: relative;
}

.login-wrapper {
  display: flex;
  width: 950px;
  height: 550px;
  background: #fff;
  border: 3px solid #000;
  box-shadow: 20px 20px 0px #000; // 核心：硬核块状投影
  z-index: 2;
}

/* 左侧：视觉宣传 */
.brand-side {
  flex: 1.2;
  background: #000;
  color: #fff;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;

  .badge {
    display: inline-block;
    border: 1px solid #fff;
    padding: 2px 10px;
    font-size: 10px;
    font-weight: 900;
    margin-bottom: 20px;
    letter-spacing: 2px;
  }

  .brand-title {
    font-size: 80px;
    line-height: 0.9;
    font-weight: 900;
    letter-spacing: -4px;
    margin-bottom: 30px;
    font-family: "Impact", sans-serif;
  }

  .brand-divider {
    width: 60px;
    height: 8px;
    background: #ffd700; // 经典的白族金装饰色
    margin-bottom: 30px;
  }

  .brand-desc {
    font-size: 18px;
    font-weight: 600;
    line-height: 1.6;
  }

  // 背景装饰性文字
  &::after {
    content: "ARCHIVE";
    position: absolute;
    font-size: 150px;
    font-weight: 900;
    color: rgba(255,255,255,0.03);
    bottom: -30px;
    right: -20px;
    transform: rotate(-10deg);
  }
}

/* 右侧：表单逻辑 */
.form-side {
  flex: 1;
  padding: 50px 45px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;

  .form-header {
    margin-bottom: 40px;
    .title {
      font-size: 24px;
      font-weight: 900;
      color: #000;
      margin-bottom: 8px;
    }
    .subtitle {
      font-size: 12px;
      color: #999;
    }
  }
}

.login-form {
  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 0 !important;
      border: 1.5px solid #eee;
      box-shadow: none !important;
      padding: 5px 15px;
      transition: all 0.2s;
      &.is-focus {
        border-color: #000;
        background-color: #f9f9f9;
      }
    }
    .el-input__icon {
      color: #000;
      font-size: 18px;
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;

  :deep(.el-checkbox__label) {
    font-size: 13px;
    color: #666;
  }
  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: #000;
    border-color: #000;
  }

  .reg-link {
    font-size: 13px;
    color: #000;
    font-weight: 700;
    text-decoration: underline;
  }
}

/* 验证码 */
.login-code {
  width: 35%;
  height: 44px;
  float: right;
  border: 1.5px solid #000;
  img {
    width: 100%;
    height: 100%;
    cursor: pointer;
  }
}

/* 按钮优化：得物黑金风格 */
.poizon-btn {
  width: 100%;
  height: 50px;
  background: #000;
  color: #fff;
  border: none;
  font-size: 14px;
  font-weight: 900;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;

  &:hover {
    background: #333;
    transform: translate(-3px, -3px);
    box-shadow: 6px 6px 0px #ffd700; // 悬浮时露出金色光泽
  }

  &:active {
    transform: translate(0, 0);
    box-shadow: none;
  }

  &:disabled {
    background: #999;
    cursor: not-allowed;
  }
}

.el-login-footer {
  position: fixed;
  bottom: 20px;
  font-size: 11px;
  color: #888;
  letter-spacing: 1px;
  z-index: 10;
}

/* 响应式调整 */
@media screen and (max-width: 900px) {
  .brand-side { display: none; }
  .login-wrapper { width: 400px; }
}
</style>