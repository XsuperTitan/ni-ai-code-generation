<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'

const open = defineModel<boolean>('open', { required: true })

const props = defineProps<{
  appDetail?: API.AppVO
  canManageApp: boolean
  deleteLoading: boolean
}>()

const emit = defineEmits<{
  edit: []
  delete: []
}>()

const formatCreateTime = computed(() => {
  const createTime = props.appDetail?.createTime
  if (!createTime) {
    return '-'
  }
  return dayjs(createTime).format('YYYY-MM-DD HH:mm:ss')
})
</script>

<template>
  <a-modal v-model:open="open" title="应用详情" :footer="null" width="520">
    <div class="detail-section-title">应用基础信息</div>
    <div class="detail-row">
      <span class="detail-label">创建者：</span>
      <a-space>
        <a-avatar :src="appDetail?.user?.userAvatar">
          {{ appDetail?.user?.userName?.slice(0, 1) || '匿' }}
        </a-avatar>
        <span>{{ appDetail?.user?.userName || '无名' }}</span>
      </a-space>
    </div>
    <div class="detail-row">
      <span class="detail-label">创建时间：</span>
      <span>{{ formatCreateTime }}</span>
    </div>

    <div v-if="canManageApp" class="detail-actions">
      <div class="detail-section-title">操作栏</div>
      <a-space>
        <a-button type="primary" @click="emit('edit')">修改</a-button>
        <a-button danger :loading="deleteLoading" @click="emit('delete')">删除</a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<style scoped>
.detail-section-title {
  margin-bottom: 10px;
  font-weight: 600;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.detail-label {
  width: 80px;
  color: #666;
}

.detail-actions {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>
