<template>
  <div class="login-page">
    <div class="card">
      <h2 class="title">欢迎登录</h2>
      
      <!-- 用户/管理员切换 -->
      <div class="toggle-container">
        <button
          @click="loginType = 'user'"
          :class="['toggle-btn', { active: loginType === 'user' }]"
        >
          用户登录
        </button>
        <button
          @click="loginType = 'admin'"
          :class="['toggle-btn', { active: loginType === 'admin' }]"
        >
          管理员登录
        </button>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>邮箱账号</label>
          <div class="input-wrapper">
            <span class="icon">📧</span>
            <input
              type="email"
              v-model="form.email"
              placeholder="请输入注册邮箱"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper">
            <span class="icon">🔒</span>
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="form.password"
              placeholder="请输入密码"
              required
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="eye-btn"
            >
              {{ showPassword ? '👁️' : '👁️‍🗨️' }}
            </button>
          </div>
        </div>

        <!-- 消息提示 -->
        <div v-if="message.text" :class="['message', message.type]">
          {{ message.text }}
        </div>

        <button type="submit" class="submit-btn">登录</button>

        <!-- 仅用户登录显示 -->
        <div v-if="loginType === 'user'" class="links">
          <a @click="$emit('change-page', 'forgot-password')" class="link">忘记密码？</a>
          <a @click="$emit('change-page', 'register')" class="link">用户注册</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginPage',
  emits: ['change-page'],
  data() {
    return {
      loginType: 'user',
      showPassword: false,
      form: {
        email: '',
        password: ''
      },
      message: {
        type: '',
        text: ''
      }
    }
  },
  methods: {
    handleLogin() {
      if (!this.form.email || !this.form.password) {
        this.message = { type: 'error', text: '请填写完整信息' }
        return
      }

      // 这里可以调用实际的登录 API
      this.message = { 
        type: 'success', 
        text: `${this.loginType === 'user' ? '用户' : '管理员'}登录成功！` 
      }
      
      console.log(`${this.loginType}登录:`, this.form)
      
      // 清空消息
      setTimeout(() => {
        this.message = { type: '', text: '' }
      }, 3000)
    }
  }
}
</script>

<style scoped>
.login-page {
  width: 100%;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  color: #333;
  font-size: 28px;
  margin-bottom: 30px;
  font-weight: 600;
}

.toggle-container {
  display: flex;
  background: #f5f5f5;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 25px;
}

.toggle-btn {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
  font-weight: 500;
}

.toggle-btn.active {
  background: white;
  color: #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.icon {
  position: absolute;
  left: 12px;
  font-size: 18px;
}

.input-wrapper input {
  width: 100%;
  padding: 12px 40px 12px 40px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #667eea;
}

.eye-btn {
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  padding: 0;
}

.message {
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  text-align: center;
}

.message.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
}

.links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.link {
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}
</style>