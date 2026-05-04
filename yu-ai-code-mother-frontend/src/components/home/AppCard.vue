<script setup lang="ts">
defineProps<{
  app: API.AppVO
  userNameFallback: string
  avatarFallback: string
}>()

const emit = defineEmits<{
  viewChat: [appId?: string | number]
  viewWork: [deployKey?: string]
}>()
</script>

<template>
  <a-card hoverable>
    <template #cover>
      <img v-if="app.cover" :src="app.cover" alt="应用封面" class="app-cover" />
      <div v-else class="app-cover app-cover-placeholder">暂无封面</div>
    </template>
    <div class="app-info">
      <a-avatar :src="app.user?.userAvatar">
        {{ app.user?.userName?.slice(0, 1) || avatarFallback }}
      </a-avatar>
      <div class="app-info-content">
        <div class="app-title">{{ app.appName || `应用 #${app.id}` }}</div>
        <div class="muted">{{ app.user?.userName || userNameFallback }}</div>
      </div>
    </div>
    <a-space class="card-actions">
      <a-button type="link" @click="emit('viewChat', app.id)">查看对话</a-button>
      <a-button v-if="app.deployKey" type="link" @click="emit('viewWork', app.deployKey)">
        查看作品
      </a-button>
    </a-space>
  </a-card>
</template>

<style scoped>
.muted {
  color: #888;
}

.app-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.app-info-content {
  min-width: 0;
}

.app-title {
  overflow: hidden;
  color: #222;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.app-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  background: #f5f5f5;
  font-size: 14px;
}

.card-actions {
  margin-top: 8px;
}
</style>
