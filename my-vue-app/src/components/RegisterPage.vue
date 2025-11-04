<template>
  <div class="register-page">
    <div class="card">
      <button @click="$emit('change-page', 'login')" class="back-btn">
        ← 返回登录
      </button>
      
      <h2 class="title">用户注册</h2>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper">
            <span class="icon">👤</span>
            <input
              type="text"
              v-model="form.username"
              placeholder="请输入用户名"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label>邮箱</label>
          <div class="input-wrapper">
            <span class="icon">📧</span>
            <input
              type="email"
              v-model="form.email"
              placeholder="请输入邮箱"
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
              placeholder="请输入密码（至少6位）"
              required
              minlength="6"
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

        <button type="submit" class="submit-btn">注册</button>

        <div class="footer-text">
          已有账号？
          <a @click="$emit('change-page', 'login')" class="link">立即登录</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RegisterPage',
  emits: ['change-page'],
  data() {
    return {
      showPassword: false,
      form: {
        username: '',
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
    handleRegister() {
      if (!this.form.username || !this.form.email || !this.form.password) {
        this.message = { type: 'error', text: '请填写完整信息' }
        return
      }

      if (this.form.password.length < 6) {
        this.message = { type: 'error', text: '密码至少需要6位' }
        return
      }

      // 这里可以调用实际的注册 API
      this.message = { type: 'success', text: '注册成功！即将跳转到登录页面...' }
      
      console.log('注册信息:', this.form)

      // 2秒后跳转到登录页面
      setTimeout(() => {
        this.$emit('change-page', 'login')
      }, 1500)
    }
  }
}
</script>

<style scoped>
.register-page {
  width: 100%;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  position: relative;
}

.back-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  background: none;
  border: none;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background 0.3s;
}

.back-btn:hover {
  background: #f5f5f5;
}

.title {
  text-align: center;
  color: #333;
  font-size: 28px;
  margin-bottom: 30px;
  font-weight: 600;
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
  padding: 12px 40px;
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

.footer-text {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.link {
  color: #667eea;
  cursor: pointer;
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}
</style>