<template>
  <div id="appEditPage">
    <a-card title="编辑应用信息" :loading="loading">
      <a-form :model="formState" layout="vertical" @finish="doSubmit">
        <a-form-item
          label="应用名称"
          name="appName"
          :rules="[{ required: true, message: '请输入应用名称' }]"
        >
          <a-input v-model:value="formState.appName" placeholder="请输入应用名称" />
        </a-form-item>
        <template v-if="isAdmin">
          <a-form-item label="应用封面" name="cover">
            <a-input v-model:value="formState.cover" placeholder="请输入封面 URL" />
          </a-form-item>
          <a-form-item label="优先级" name="priority">
            <a-input-number v-model:value="formState.priority" :min="0" :max="99" />
          </a-form-item>
        </template>
        <a-space>
          <a-button type="primary" html-type="submit" :loading="submitting">保存</a-button>
          <a-button @click="router.back()">取消</a-button>
        </a-space>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const appId = String(route.params.id || '')

const loading = ref(false)
const submitting = ref(false)
const appDetail = ref<API.AppVO>()
const formState = reactive<API.AppAdminUpdateRequest>({
  appName: '',
  cover: '',
  priority: 0,
})

const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

const checkPermission = () => {
  if (!appDetail.value) {
    return false
  }
  if (isAdmin.value) {
    return true
  }
  return String(appDetail.value.userId) === String(loginUserStore.loginUser.id)
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId })
    if (res.data.code !== 0 || !res.data.data) {
      message.error('获取应用失败，' + res.data.message)
      return false
    }
    appDetail.value = res.data.data
    if (!checkPermission()) {
      message.error('你只能编辑自己的应用')
      await router.push('/')
      return false
    }
    formState.appName = appDetail.value.appName
    formState.cover = appDetail.value.cover
    formState.priority = appDetail.value.priority
    return true
  } finally {
    loading.value = false
  }
}

const doSubmit = async () => {
  if (!appId) {
    return
  }
  submitting.value = true
  try {
    if (isAdmin.value) {
      const res = await updateAppByAdmin({
        id: appId,
        appName: formState.appName,
        cover: formState.cover,
        priority: formState.priority,
      })
      if (res.data.code === 0) {
        message.success('更新成功')
        await router.push('/admin/appManage')
        return
      }
      message.error('更新失败，' + res.data.message)
      return
    }
    const res = await updateApp({
      id: appId,
      appName: formState.appName,
    })
    if (res.data.code === 0) {
      message.success('更新成功')
      await router.push(`/app/chat/${appId}`)
      return
    }
    message.error('更新失败，' + res.data.message)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!loginUserStore.loginUser.id) {
    await loginUserStore.fetchLoginUser()
  }
  if (!appId) {
    message.error('应用 id 不合法')
    router.push('/')
    return
  }
  await fetchDetail()
})
</script>
