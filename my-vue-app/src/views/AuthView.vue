<template>
  <div class="auth-container">
    <transition name="fade" mode="out-in">
      <component :is="currentComponent" @change-page="handlePageChange" />
    </transition>
  </div>
</template>

<script>
import LoginPage from '../components/LoginPage.vue'
import RegisterPage from '../components/RegisterPage.vue'
import ForgotPassword from '../components/ForgotPassword.vue'

export default {
  name: 'AuthView',
  components: {
    LoginPage,
    RegisterPage,
    ForgotPassword
  },
  computed: {
    currentComponent() {
      const routeName = this.$route.name
      if (routeName === 'Register') return 'RegisterPage'
      if (routeName === 'ForgotPassword') return 'ForgotPassword'
      return 'LoginPage'
    }
  },
  methods: {
    handlePageChange(page) {
      this.$router.push(`/${page}`)
    }
  }
}
</script>

<style scoped>
.auth-container {
  width: 100%;
  max-width: 450px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>