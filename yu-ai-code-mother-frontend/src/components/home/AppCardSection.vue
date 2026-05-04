<script setup lang="ts">
import AppCard from '@/components/home/AppCard.vue'

defineProps<{
  title: string
  apps: API.AppVO[]
  total: number
  current?: number
  pageSize?: number
  keyword: string
  emptyDescription: string
  userNameFallback: string
  avatarFallback: string
}>()

const emit = defineEmits<{
  'update:keyword': [value: string]
  'update:current': [value: number]
  'update:pageSize': [value: number]
  search: []
  pageChange: []
  viewChat: [appId?: string | number]
  viewWork: [deployKey?: string]
}>()

const handlePageChange = (current: number, pageSize: number) => {
  emit('update:current', current)
  emit('update:pageSize', pageSize)
  emit('pageChange')
}
</script>

<template>
  <a-card :title="title" class="list-card">
    <div class="search-row">
      <a-input
        :value="keyword"
        allow-clear
        placeholder="按应用名称搜索"
        @update:value="emit('update:keyword', $event)"
        @pressEnter="emit('search')"
      />
      <a-button type="primary" @click="emit('search')">搜索</a-button>
    </div>
    <a-list :grid="{ gutter: 16, column: 3 }" :data-source="apps">
      <template #renderItem="{ item }">
        <a-list-item>
          <AppCard
            :app="item"
            :user-name-fallback="userNameFallback"
            :avatar-fallback="avatarFallback"
            @view-chat="emit('viewChat', $event)"
            @view-work="emit('viewWork', $event)"
          />
        </a-list-item>
      </template>
    </a-list>
    <a-empty v-if="apps.length === 0" :description="emptyDescription" />
    <a-pagination
      class="pager"
      :current="current ?? 1"
      :page-size="pageSize ?? 6"
      :total="total"
      :page-size-options="['6', '12', '20']"
      show-size-changer
      @change="handlePageChange"
    />
  </a-card>
</template>

<style scoped>
.list-card {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  border: 1px solid rgba(255, 255, 255, 0.52);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 60px rgba(13, 31, 61, 0.12);
}

.search-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-bottom: 12px;
}

.pager {
  margin-top: 12px;
  text-align: right;
}
</style>
