<template>
  <div class="not-found-container">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-message">页面未找到</h2>
      <p class="error-description">页面不存在或已被移除</p>
      <el-button type="primary" @click="goHome" class="home-button">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  methods: {
    goHome() {
      this.$router.push({ name: 'Home' });
    }
  }
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.error-content {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.error-code {
  font-size: 4rem;
  color: #ff6b6b;
  margin: 0;
}

.error-message {
  font-size: 1.5rem;
  color: #333;
  margin: 1rem 0;
}

.error-description {
  color: #666;
  margin-bottom: 1.5rem;
}

.home-button {
  background-color: #409eff;
  border-color: #409eff;
}
</style>