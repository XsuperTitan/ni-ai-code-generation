<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />
    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>
    <!-- 底部版权信息 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at 12% 8%, rgba(138, 255, 229, 0.42), transparent 30%),
    radial-gradient(circle at 86% 14%, rgba(112, 202, 255, 0.32), transparent 28%),
    linear-gradient(135deg, #071813 0%, #0d2f31 48%, #e9fff7 100%);
}

.main-content {
  width: 100%;
  padding: 0;
  background: transparent;
  margin: 0 auto 56px;
}
</style>
