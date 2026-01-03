<template>
  <div class="register-page">
    <div class="register-wrapper">
      <!-- 左侧：视觉展示区 -->
      <div class="brand-side">
        <div class="brand-content">
          <div class="badge">MEMBER ENROLLMENT</div>
          <h1 class="brand-title">
            JOIN<br />THE HERITAGE
          </h1>
          <div class="brand-divider"></div>
          <p class="brand-desc">
            成为白族非遗平台的一员<br />
          </p>
          <!-- 装饰性元素 -->
          <div class="pattern-decor"></div>
        </div>
      </div>

      <!-- 右侧：注册表单区 -->
      <div class="form-side">
        <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="register-form">
          <div class="form-header">
            <h3 class="title">新用户注册 / REGISTER</h3>
            <p class="subtitle">请创建您的数字准入凭证</p>
          </div>

          <el-form-item prop="username">
            <el-input v-model="registerForm.username" type="text" size="large" placeholder="账号">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" size="large" placeholder="密码">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" size="large" placeholder="确认密码">
              <template #prefix><el-icon><CircleCheck /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="code" v-if="captchaEnabled">
            <el-input v-model="registerForm.code" size="large" placeholder="验证码" style="width: 60%">
              <template #prefix><el-icon><Key /></el-icon></template>
            </el-input>
            <div class="register-code">
              <img :src="codeUrl" @click="getCode" class="register-code-img"/>
            </div>
          </el-form-item>

          <div class="form-options">
            <router-link class="login-link" :to="'/login'">← 使用已有账户登录</router-link>
          </div>

          <el-form-item style="width:100%; margin-top: 10px;">
            <button
                :disabled="loading"
                class="poizon-btn"
                @click.prevent="handleRegister"
            >
              <span v-if="!loading">提交注册</span>
              <span v-else>正在归档数据...</span>
            </button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!--  底部  -->
    <div class="el-register-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from "element-plus"
import { getCodeImg, register } from "@/api/login"
import defaultSettings from '@/settings'
import { User, Lock, CircleCheck, Key } from '@element-plus/icons-vue'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const router = useRouter()
const { proxy } = getCurrentInstance()

const registerForm = ref({
  username: "",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: ""
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" },
    { min: 2, max: 20, message: "账号长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" },
    { min: 5, max: 20, message: "密码长度必须介于 5 和 20 之间", trigger: "blur" },
    { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(res => {
        const username = registerForm.value.username
        ElMessageBox.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", "系统提示", {
          dangerouslyUseHTMLString: true,
          type: "success",
        }).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled) {
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
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style lang='scss' scoped>
.register-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f0f0;
  background-image: linear-gradient(#e0e0e0 1px, transparent 1px),
  linear-gradient(90deg, #e0e0e0 1px, transparent 1px);
  background-size: 40px 40px;
  overflow: hidden;
  position: relative;
}

.register-wrapper {
  display: flex;
  width: 950px;
  height: 580px; // 比登录页稍高以容纳确认密码框
  background: #fff;
  border: 3px solid #000;
  box-shadow: 20px 20px 0px #000;
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
    font-size: 70px;
    line-height: 0.9;
    font-weight: 900;
    letter-spacing: -4px;
    margin-bottom: 30px;
    font-family: "Impact", sans-serif;
  }

  .brand-divider {
    width: 60px;
    height: 8px;
    background: #ffd700;
    margin-bottom: 30px;
  }

  .brand-desc {
    font-size: 18px;
    font-weight: 600;
    line-height: 1.6;
  }

  &::after {
    content: "JOIN";
    position: absolute;
    font-size: 180px;
    font-weight: 900;
    color: rgba(255,255,255,0.03);
    bottom: -40px;
    right: -10px;
    transform: rotate(-15deg);
  }
}

/* 右侧：表单区 */
.form-side {
  flex: 1;
  padding: 40px 45px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;

  .form-header {
    margin-bottom: 30px;
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

.register-form {
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
  margin: 15px 0;
  .login-link {
    font-size: 13px;
    color: #000;
    font-weight: 700;
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }
}

/* 验证码 */
.register-code {
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

/* 按钮优化 */
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

  &:hover {
    background: #333;
    transform: translate(-3px, -3px);
    box-shadow: 6px 6px 0px #ffd700;
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

.el-register-footer {
  position: fixed;
  bottom: 20px;
  font-size: 11px;
  color: #888;
  letter-spacing: 1px;
  z-index: 10;
}

@media screen and (max-width: 900px) {
  .brand-side { display: none; }
  .register-wrapper { width: 400px; }
}
</style>