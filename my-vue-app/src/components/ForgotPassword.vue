<template>
  <div class="forgot-password">
    <div class="card">
      <button @click="$emit('change-page', 'login')" class="back-btn">
        ← 返回登录
      </button>
      
      <h2 class="title">忘记密码</h2>
      <p class="subtitle">请输入您的注册邮箱，我们将发送验证码</p>

      <form @submit.prevent="handleResetPassword">
        <div class="form-group">
          <label>邮箱</label>
          <div class="input-wrapper">
            <span class="icon">📧</span>
            <input
              type="email"
              v-model="form.email"
              placeholder="请输入注册邮箱"
              required
              :disabled="codeSent"
            />
          </div>
        </div>

        <!-- 发送验证码按钮 -->
        <button
          v-if="!codeSent"
          type="button"
          @click="handleSendCode"
          class="send-code-btn"
        >
          发送验证码
        </button>

        <!-- 验证码和新密码输入框（发送验证码后显示） -->
        <template v-if="codeSent">
          <div class="form-group">
            <label>验证码</label>
            <div class="input-wrapper">
              <span class="icon">🔑</span>
              <input
                type="text"
                v-model="form.code"
                placeholder="请输入6位验证码"
                required
                maxlength="6"
              />
            </div>
          </div>

          <div class="form-group">
            <label>新密码</label>
            <div class="input-wrapper">
              <span class="icon">🔒</span>
              <input
                :type="showPassword ? 'text' : 'password'"
                v-model="form.newPassword"
                placeholder="请输入新密码（至少6位）"
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

          <button type="submit" class="submit-btn">重置密码</button>
        </template>

        <!-- 消息提示 -->
        <div v-if="message.text" :class="['message', message.type]">
          {{ message.text }}
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ForgotPassword',
  emits: ['change-page'],
  data() {
    return {
      showPassword: false,
      codeSent: false,
      verificationCode: '',
      form: {
        email: '',
        code: '',
        newPassword: ''
      },
      message: {
        type: '',
        text: ''
      }
    }
  },
  methods: {
    handleSendCode() {
      if (!this.form.email) {
        this.message = { type: 'error', text: '请输入邮箱' }
        return
      }

      // 生成6位随机验证码
      this.verificationCode = Math.floor(100000 + Math.random() * 900000).toString()
      this.codeSent = true
      
      // 模拟发送验证码（实际项目中应该调用后端API）
      this.message = { 
        type: 'success', 
        text: `验证码已发送到 ${this.form.email}（模拟验证码: ${this.verificationCode}）` 
      }

      console.log('发送验证码:', this.verificationCode)
    },

    handleResetPassword() {
      if (!this.form.code || !this.form.newPassword) {
        this.message = { type: 'error', text: '请填写完整信息' }
        return
      }

      if (this.form.newPassword.length < 6) {
        this.message = { type: 'error', text: '密码至少需要6位' }
        return
      }

      if (this.form.code !== this.verificationCode) {
        this.message = { type: 'error', text: '验证码错误，请重新输入' }
        return
      }

      // 这里可以调用实际的重置密码 API
      this.message = { type: 'success', text: '密码重置成功！即将跳转到登录页面...' }
      
      console.log('重置密码成功')

      // 2秒后跳转到登录页面
      setTimeout(() => {
        this.$emit('change-page', 'login')
      }, 1500)
    }
  }
}
</script>

<style scoped>
.forgot-password {
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
  margin-bottom: 10px;
  font-weight: 600;
}

.subtitle {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-bottom: 30px;
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

.input-wrapper input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
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

.send-code-btn {
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
  margin-bottom: 20px;
}

.send-code-btn:hover {
  transform: translateY(-2px);
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

.message {
  padding: 12px;
  border-radius: 8px;
  margin-top: 20px;
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
</style>