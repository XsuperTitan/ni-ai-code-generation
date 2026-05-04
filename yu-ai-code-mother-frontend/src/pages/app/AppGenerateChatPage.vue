<template>
  <div id="appGenerateChatPage">
    <a-card>
      <div class="page-header">
        <div class="app-name">{{ appDetail?.appName || `应用 #${appId}` }}</div>
        <a-space>
          <a-button @click="showDetailModal = true">应用详情</a-button>
          <a-button :loading="deployLoading" @click="doDeploy">部署</a-button>
        </a-space>
      </div>
      <a-typography-link v-if="deployUrl" :href="deployUrl" target="_blank">
        部署地址：{{ deployUrl }}
      </a-typography-link>
    </a-card>

    <AppDetailModal
      v-model:open="showDetailModal"
      :app-detail="appDetail"
      :can-manage-app="canManageApp"
      :delete-loading="deleteLoading"
      @edit="goToEdit"
      @delete="doDeleteApp"
    />

    <div class="content">
      <a-card title="应用生成对话" class="chat-panel">
        <div class="message-list">
          <ChatMessageItem
            v-for="(item, index) in messages"
            :key="`${item.role}-${index}`"
            :item="item"
          />
          <a-spin v-if="streaming" />
        </div>
        <div class="input-row">
          <a-tooltip :title="canSendMessage ? null : noPermissionTip">
            <div class="input-wrapper">
              <a-textarea
                v-model:value="inputMessage"
                placeholder="请描述你想生成的网站，越详细效果越好哦"
                :auto-size="{ minRows: 2, maxRows: 4 }"
                :disabled="!canSendMessage || streaming"
              />
            </div>
          </a-tooltip>
          <a-button type="primary" :loading="streaming" :disabled="!canSendMessage" @click="sendMessage">
            发送
          </a-button>
        </div>
      </a-card>

      <a-card title="网页展示" class="preview-panel">
        <a-empty v-if="!previewUrl" description="等待 AI 完成代码生成后展示" />
        <iframe v-else :src="previewUrl" class="preview-iframe" />
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { deleteApp, deleteAppByAdmin, deployApp, getAppVoById } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import AppDetailModal from '@/components/app/AppDetailModal.vue'
import ChatMessageItem from '@/components/app/ChatMessageItem.vue'
import { joinUrl } from '@/utils/url'
import type { ChatMessage } from '@/types/chat'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = String(route.params.id || '')
const appDetail = ref<API.AppVO>()
const inputMessage = ref('')
const deployLoading = ref(false)
const deleteLoading = ref(false)
const streaming = ref(false)
const messages = ref<ChatMessage[]>([])
const showDetailModal = ref(false)
let streamAbortController: AbortController | null = null
const noPermissionTip = '无法在别人的作品下对话哦~'
const appDeployBaseUrl = import.meta.env.VITE_APP_DEPLOY_BASE_URL || 'http://localhost'
const appPreviewBaseUrl =
  import.meta.env.VITE_APP_PREVIEW_BASE_URL || 'http://localhost:8123/api/static'

const previewUrl = computed(() => {
  if (!appDetail.value?.codeGenType || !appId || !hasStreamDone.value) {
    return ''
  }
  return joinUrl(appPreviewBaseUrl, `${appDetail.value.codeGenType}_${appId}/`)
})

const deployUrl = computed(() => {
  if (!appDetail.value?.deployKey) {
    return ''
  }
  return joinUrl(appDeployBaseUrl, appDetail.value.deployKey)
})

const hasStreamDone = ref(false)

const isViewMode = computed(() => route.query.view !== undefined)

const canSendMessage = computed(() => {
  return isOwner.value
})

const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

const isOwner = computed(() => {
  const loginUserId = loginUserStore.loginUser.id
  const ownerId = appDetail.value?.userId
  if (!loginUserId || ownerId === undefined || ownerId === null) {
    return false
  }
  return String(loginUserId) === String(ownerId)
})

const canManageApp = computed(() => isOwner.value || isAdmin.value)

const fetchAppDetail = async () => {
  const res = await getAppVoById({ id: appId })
  if (res.data.code === 0 && res.data.data) {
    appDetail.value = res.data.data
    return true
  }
  message.error('获取应用信息失败，' + res.data.message)
  return false
}

const toChatMessage = (history: API.ChatHistory): ChatMessage => {
  return {
    role: history.messageType === 'ai' ? 'assistant' : 'user',
    content: history.message || '',
  }
}

const fetchChatHistory = async () => {
  const res = await listAppChatHistory({
    appId,
    pageSize: 50,
  })
  if (res.data.code === 0 && res.data.data) {
    const histories = res.data.data.records ?? []
    messages.value = histories.slice().reverse().map(toChatMessage)
    hasStreamDone.value =
      histories.some((item) => item.messageType === 'ai') || Boolean(appDetail.value?.deployKey)
    return true
  }
  if (isOwner.value || isAdmin.value) {
    message.error('获取对话历史失败，' + res.data.message)
  }
  return false
}

const doDeploy = async () => {
  deployLoading.value = true
  try {
    const res = await deployApp({ appId })
    if (res.data.code === 0) {
      await fetchAppDetail()
      message.success('部署成功')
      return
    }
    message.error('部署失败，' + res.data.message)
  } finally {
    deployLoading.value = false
  }
}

const goToEdit = () => {
  showDetailModal.value = false
  router.push(`/app/edit/${appId}`)
}

const doDeleteApp = async () => {
  if (!canManageApp.value) {
    return
  }
  deleteLoading.value = true
  try {
    const deleteBody = { id: appId } as unknown as API.DeleteRequest
    const res = isAdmin.value ? await deleteAppByAdmin(deleteBody) : await deleteApp(deleteBody)
    if (res.data.code === 0) {
      message.success('删除成功')
      showDetailModal.value = false
      await router.push('/')
      return
    }
    message.error('删除失败，' + res.data.message)
  } finally {
    deleteLoading.value = false
  }
}

const appendAssistantChunk = (chunk: string) => {
  if (!chunk) {
    return
  }
  const lastMessage = messages.value[messages.value.length - 1]
  if (lastMessage && lastMessage.role === 'assistant') {
    lastMessage.content += chunk
  } else {
    messages.value.push({
      role: 'assistant',
      content: chunk,
    })
  }
}

const parseSSEChunk = (raw: string) => {
  const lines = raw.split('\n')
  let eventName = ''
  const dataLines: string[] = []
  for (const line of lines) {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5))
    }
  }
  return {
    eventName,
    data: dataLines.join('\n'),
  }
}

/**
 * 将流式分片规范化为可展示文本：
 * - 兼容 {"d":"..."} 这类增量 JSON
 * - 兼容 OpenAI 风格 {"choices":[{"delta":{"content":"..."}}]}
 * - 解析失败时回退原始文本，避免丢失输出
 */
const normalizeChunkText = (rawData: string) => {
  const data = rawData.trim()
  if (!data) {
    return ''
  }
  if (data === '[DONE]') {
    return ''
  }
  try {
    const parsed = JSON.parse(data)
    if (typeof parsed === 'string') {
      return parsed
    }
    if (typeof parsed?.d === 'string') {
      return parsed.d
    }
    if (typeof parsed?.content === 'string') {
      return parsed.content
    }
    const deltaContent = parsed?.choices?.[0]?.delta?.content
    if (typeof deltaContent === 'string') {
      return deltaContent
    }
    const messageContent = parsed?.choices?.[0]?.message?.content
    if (typeof messageContent === 'string') {
      return messageContent
    }
  } catch {
    // 非 JSON 片段，按文本回退
  }
  return data
}

const streamChat = async (content: string) => {
  streaming.value = true
  hasStreamDone.value = false
  streamAbortController = new AbortController()
  const encodedMessage = encodeURIComponent(content)
  const streamUrl = `http://localhost:8123/api/app/chat/gen/code?appId=${appId}&message=${encodedMessage}`
  try {
    const res = await fetch(streamUrl, {
      method: 'GET',
      credentials: 'include',
      signal: streamAbortController.signal,
    })
    if (!res.ok || !res.body) {
      message.error('连接流式接口失败')
      return
    }
    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        break
      }
      buffer += decoder.decode(value, { stream: true })
      const events = buffer.split('\n\n')
      buffer = events.pop() ?? ''
      for (const eventChunk of events) {
        const { eventName, data } = parseSSEChunk(eventChunk)
        if (eventName === 'done') {
          hasStreamDone.value = true
          continue
        }
        appendAssistantChunk(normalizeChunkText(data))
      }
    }
    if (buffer.trim()) {
      const { eventName, data } = parseSSEChunk(buffer)
      if (eventName === 'done') {
        hasStreamDone.value = true
      } else {
        appendAssistantChunk(normalizeChunkText(data))
      }
    }
    await fetchAppDetail()
  } catch {
    if (!streamAbortController?.signal.aborted) {
      message.error('生成失败，请重试')
    }
  } finally {
    streaming.value = false
  }
}

const sendMessage = async () => {
  if (!canSendMessage.value) {
    message.warning(noPermissionTip)
    return
  }
  const content = inputMessage.value.trim()
  if (!content) {
    return
  }
  messages.value.push({
    role: 'user',
    content,
  })
  inputMessage.value = ''
  await streamChat(content)
}

onMounted(async () => {
  if (!loginUserStore.loginUser.id) {
    await loginUserStore.fetchLoginUser()
  }
  if (!appId) {
    message.error('应用 id 无效')
    router.push('/')
    return
  }
  const ok = await fetchAppDetail()
  if (!ok) {
    return
  }
  await fetchChatHistory()
  if (isViewMode.value) {
    return
  }
  const initPrompt = (route.query.initPrompt as string) || appDetail.value?.initPrompt || ''
  if (initPrompt) {
    inputMessage.value = initPrompt
    await sendMessage()
  }
})

onBeforeUnmount(() => {
  streamAbortController?.abort()
})
</script>

<style scoped>
#appGenerateChatPage {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.app-name {
  font-size: 18px;
  font-weight: 600;
}

.content {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 3fr);
  gap: 8px;
}

.chat-panel,
.preview-panel {
  min-width: 0;
  min-height: calc(100vh - 188px);
}

.chat-panel :deep(.ant-card-body),
.preview-panel :deep(.ant-card-body) {
  min-width: 0;
}

.message-list {
  height: calc(100vh - 316px);
  overflow-y: auto;
  padding: 6px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

.input-row {
  margin-top: 8px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.input-wrapper {
  width: 100%;
}

.preview-iframe {
  width: 100%;
  height: calc(100vh - 252px);
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}
</style>
