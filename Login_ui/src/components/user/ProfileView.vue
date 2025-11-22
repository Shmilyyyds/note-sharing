<template>
  <div class="profile-page">
    <div class="profile-card">
      <header class="profile-header">
        <div class="avatar-shell" aria-hidden="true">
          <span class="avatar-icon"></span>
        </div>
        <div class="header-text">
          <p class="section-label">账户设置</p>
          <h2>个人信息</h2>
        </div>
      </header>

      <div class="profile-content">
        <!-- 用户信息显示 -->
        <div class="info-section">
          <div class="info-item">
            <label>用户名</label>
            <div class="info-value">{{ userInfo.username }}</div>
          </div>

          <div class="info-item">
            <label>学号</label>
            <div class="info-value">{{ userInfo.studentId }}</div>
          </div>

          <div class="info-item">
            <label>邮箱</label>
            <div class="info-value">{{ userInfo.email }}</div>
          </div>

          <p class="info-note">* 邮箱不可修改</p>
        </div>

        <!-- 操作按钮 -->
        <div class="actions-section">
          <button class="text-action" @click="showChangePasswordDialog = true">
            <span>修改密码</span>
            <span class="action-indicator" aria-hidden="true">↗</span>
          </button>
          <button class="text-action danger" @click="handleLogout">
            <span>退出登录</span>
            <span class="action-indicator" aria-hidden="true">↗</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <div v-if="showChangePasswordDialog" class="modal" @click.self="showChangePasswordDialog = false">
      <div class="modal-content">
        <h3>修改密码</h3>

        <div class="form-group">
          <label>当前密码</label>
          <input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
          />
        </div>

        <div class="form-group">
          <label>新密码</label>
          <input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
          />
        </div>

        <div class="form-group">
          <label>确认新密码</label>
          <input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
          />
        </div>

        <div v-if="passwordError" class="error-message">
          {{ passwordError }}
        </div>

        <div class="modal-actions">
          <button @click="closePasswordDialog">取消</button>
          <button class="primary" @click="handleChangePassword">确认修改</button>
        </div>
      </div>
    </div>

    <!-- 退出登录确认对话框 -->
    <div v-if="showLogoutDialog" class="modal" @click.self="showLogoutDialog = false">
      <div class="modal-content">
        <h3>确认退出</h3>
        <p>确定要退出登录吗?</p>
        <div class="modal-actions">
          <button @click="showLogoutDialog = false">取消</button>
          <button class="primary danger" @click="confirmLogout">确认退出</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户信息
const userInfo = ref({
  username: '',
  studentId: '',
  email: ''
})

// 对话框显示状态
const showChangePasswordDialog = ref(false)
const showLogoutDialog = ref(false)

// 修改密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordError = ref('')

/**
 * API: 获取用户信息
 * GET /api/user/profile
 * 输出: {
 *   code: number,
 *   data: {
 *     username: string,
 *     studentId: string,
 *     email: string,
 *     createdAt: string
 *   }
 * }
 */
const loadUserInfo = async () => {
  try {
    // const response = await fetch('/api/user/profile')
    // const result = await response.json()
    // userInfo.value = result.data
    
    // 模拟数据
    userInfo.value = {
      username: '张三',
      studentId: '2021001234',
      email: 'zhangsan@example.com'
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

/**
 * API: 修改密码
 * POST /api/user/change-password
 * 输入: {
 *   oldPassword: string,
 *   newPassword: string
 * }
 * 输出: {
 *   code: number,
 *   message: string
 * }
 */
const handleChangePassword = async () => {
  passwordError.value = ''

  // 表单验证
  if (!passwordForm.value.oldPassword) {
    passwordError.value = '请输入当前密码'
    return
  }

  if (!passwordForm.value.newPassword) {
    passwordError.value = '请输入新密码'
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    passwordError.value = '新密码长度不能少于6位'
    return
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }

  try {
    // const response = await fetch('/api/user/change-password', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({
    //     oldPassword: passwordForm.value.oldPassword,
    //     newPassword: passwordForm.value.newPassword
    //   })
    // })
    // const result = await response.json()
    
    // if (result.code === 200) {
    //   alert('密码修改成功,请重新登录')
    //   closePasswordDialog()
    //   handleLogout()
    // } else {
    //   passwordError.value = result.message
    // }

    // 模拟成功
    alert('密码修改成功')
    closePasswordDialog()
  } catch (error) {
    console.error('修改密码失败:', error)
    passwordError.value = '修改密码失败,请稍后重试'
  }
}

const closePasswordDialog = () => {
  showChangePasswordDialog.value = false
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordError.value = ''
}

const handleLogout = () => {
  showLogoutDialog.value = true
}

/**
 * API: 退出登录
 * POST /api/auth/logout
 * 输出: {
 *   code: number,
 *   message: string
 * }
 */
const confirmLogout = async () => {
  try {
    // const response = await fetch('/api/auth/logout', { method: 'POST' })
    // const result = await response.json()
    
    // 清除本地token/session
    localStorage.removeItem('token')
    
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 48px 20px 80px;
  background: var(--surface-muted, #f8faf9);
  display: flex;
  justify-content: center;
}

.profile-card {
  width: min(760px, 100%);
  background: var(--surface-base, #ffffff);
  border-radius: 32px;
  border: 1px solid var(--line-soft, #e8ecec);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.profile-header {
  display: flex;
  gap: 24px;
  align-items: center;
  padding: 36px 40px 24px;
  border-bottom: 1px solid var(--line-soft, #e8ecec);
}

.avatar-shell {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  border: 1px solid var(--brand-primary, #22ee99);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.avatar-icon {
  width: 60%;
  height: 60%;
  border-radius: 50%;
  border: 2px solid var(--brand-primary, #22ee99);
  position: relative;
}

.avatar-icon::after {
  content: '';
  position: absolute;
  bottom: -20%;
  left: 50%;
  transform: translateX(-50%);
  width: 120%;
  height: 50%;
  border: 2px solid var(--brand-primary, #22ee99);
  border-top: none;
  border-radius: 40% 40% 60% 60%;
}

.header-text h2 {
  margin: 4px 0 0;
  font-size: 28px;
  color: var(--text-strong, #1f2a37);
}

.section-label {
  font-size: 14px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-muted, #8a9199);
  margin: 0;
}

.profile-content {
  padding: 32px 40px 40px;
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line-soft, #e8ecec);
}

.info-item:last-of-type {
  border-bottom: none;
  padding-bottom: 0;
}

.info-item label {
  font-size: 14px;
  color: var(--text-muted, #8a9199);
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 20px;
  color: var(--text-strong, #1f2a37);
  font-weight: 600;
}

.info-note {
  margin: -8px 0 0;
  font-size: 13px;
  color: var(--text-muted, #8a9199);
}

.actions-section {
  border-top: 1px solid var(--line-soft, #e8ecec);
  padding-top: 24px;
  display: flex;
  flex-direction: column;
}

.text-action {
  appearance: none;
  border: none;
  background: transparent;
  padding: 16px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  color: var(--text-secondary, #4b5563);
  cursor: pointer;
  transition: color 0.2s ease;
  border-bottom: 1px solid var(--line-soft, #e8ecec);
}

.text-action:last-child {
  border-bottom: none;
}

.text-action:hover,
.text-action:focus-visible {
  color: var(--brand-primary, #22ee99);
}

.text-action.danger {
  color: var(--text-danger, #c6534c);
}

.text-action.danger:hover,
.text-action.danger:focus-visible {
  color: var(--brand-primary, #22ee99);
}

.action-indicator {
  font-size: 18px;
  color: inherit;
}

.modal {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  z-index: 1000;
}

.modal-content {
  width: min(440px, 100%);
  background: var(--surface-base, #ffffff);
  border-radius: 28px;
  border: 1px solid var(--line-soft, #e8ecec);
  padding: 32px;
  box-shadow: 0 25px 60px rgba(15, 23, 42, 0.2);
}

.modal-content h3 {
  margin: 0 0 16px;
  font-size: 22px;
  color: var(--text-strong, #1f2a37);
}

.modal-content p {
  margin: 8px 0 24px;
  color: var(--text-secondary, #4b5563);
  font-size: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 18px;
}

.form-group label {
  font-size: 14px;
  color: var(--text-muted, #8a9199);
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  font-size: 15px;
  border-radius: 12px;
  border: 1px solid var(--line-soft, #e8ecec);
  background: var(--surface-muted, #f8faf9);
  transition: border-color 0.2s ease, background 0.2s ease;
}

.form-group input:focus {
  outline: none;
  border-color: var(--brand-primary, #22ee99);
  background: #fff;
}

.error-message {
  padding: 12px 16px;
  border-radius: 14px;
  background: rgba(198, 83, 76, 0.08);
  color: var(--text-danger, #c6534c);
  font-size: 14px;
  margin-bottom: 16px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.modal-actions button {
  min-width: 110px;
  padding: 12px 18px;
  border-radius: 999px;
  border: 1px solid var(--line-soft, #e8ecec);
  background: transparent;
  font-size: 14px;
  color: var(--text-secondary, #4b5563);
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-actions button:hover,
.modal-actions button:focus-visible {
  color: var(--brand-primary, #22ee99);
  border-color: var(--brand-primary, #22ee99);
}

.modal-actions button.primary {
  background: var(--brand-primary, #22ee99);
  border-color: var(--brand-primary, #22ee99);
  color: #0b1f14;
  font-weight: 600;
}

.modal-actions button.primary:hover,
.modal-actions button.primary:focus-visible {
  filter: brightness(0.95);
}

.modal-actions button.primary.danger {
  background: rgba(198, 83, 76, 0.1);
  border-color: rgba(198, 83, 76, 0.4);
  color: var(--text-danger, #c6534c);
}

.modal-actions button.primary.danger:hover,
.modal-actions button.primary.danger:focus-visible {
  background: rgba(198, 83, 76, 0.18);
  border-color: var(--text-danger, #c6534c);
}

@media (max-width: 640px) {
  .profile-card {
    border-radius: 24px;
  }

  .profile-header,
  .profile-content {
    padding: 24px;
  }

  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .avatar-shell {
    width: 64px;
    height: 64px;
  }
}
</style>