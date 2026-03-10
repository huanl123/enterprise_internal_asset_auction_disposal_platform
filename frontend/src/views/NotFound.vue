<template>
  <!-- 404 错误页面 -->
  <div class="not-found-container">
    <!-- 错误内容卡片 -->
    <div class="error-content">
      <!-- 错误代码：404 -->
      <h1 class="error-code">404</h1>
      <!-- 错误信息 -->
      <h2 class="error-message">页面未找到</h2>
      <p class="error-description">页面不存在或已被移除</p>
      <!-- 返回首页按钮 -->
      <el-button type="primary" @click="goHome" class="home-button">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script>
// 404 页面组件
export default {
  name: 'NotFound',
  methods: {
    // 跳转到首页
    goHome() {
      this.$router.push({ name: 'Home' });
    }
  }
}
</script>

<style scoped>
/* 容器：全屏居中 */
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 错误内容卡片 */
.error-content {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 404 错误代码样式 */
.error-code {
  font-size: 4rem;
  color: #ff6b6b;
  margin: 0;
}

/* 错误信息标题 */
.error-message {
  font-size: 1.5rem;
  color: #333;
  margin: 1rem 0;
}

/* 错误描述文本 */
.error-description {
  color: #666;
  margin-bottom: 1.5rem;
}

/* 首页按钮样式 */
.home-button {
  background-color: #409eff;
  border-color: #409eff;
}
</style>