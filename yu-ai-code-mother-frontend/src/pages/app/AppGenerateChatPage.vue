<template>
  <div id="appGenerateChatPage">
    <a-card>
      <div class="page-header">
        <div class="app-name">{{ appDetail?.appName || `应用 #${appId}` }}</div>
        <a-space>
          <a-button :loading="deployLoading" @click="doDeploy">部署</a-button>
          <a-button @click="router.push(`/app/edit/${appId}`)">编辑信息</a-button>
        </a-space>
      </div>
      <a-typography-link v-if="deployUrl" :href="deployUrl" target="_blank">
        部署地址：{{ deployUrl }}
      </a-typography-link>
    </a-card>

    <div class="content">
      <a-card title="应用生成对话" class="chat-panel">
        <div class="message-list">
          <div
            v-for="(item, index) in messages"
            :key="`${item.role}-${index}`"
            :class="['message-item', item.role === 'user' ? 'user' : 'assistant']"
          >
            <div class="message-bubble">{{ item.content }}</div>
          </div>
          <a-spin v-if="streaming" />
        </div>
        <div class="input-row">
          <a-textarea
            v-model:value="inputMessage"
            placeholder="继续描述你要修改或新增的功能..."
            :auto-size="{ minRows: 2, maxRows: 4 }"
          />
          <a-button type="primary" :loading="streaming" @click="sendMessage">发送</a-button>
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
import { deployApp, getAppVoById } from '@/api/appController'

type ChatRole = 'user' | 'assistant'

type ChatMessage = {
  role: ChatRole
  content: string
}

const route = useRoute()
const router = useRouter()

const appId = String(route.params.id || '')
const appDetail = ref<API.AppVO>()
const inputMessage = ref('')
const deployLoading = ref(false)
const deployUrl = ref('')
const streaming = ref(false)
const messages = ref<ChatMessage[]>([])
let streamAbortController: AbortController | null = null

const previewUrl = computed(() => {
  if (!appDetail.value?.codeGenType || !appId || !hasStreamDone.value) {
    return ''
  }
  return `http://localhost:8123/api/static/${appDetail.value.codeGenType}_${appId}/`
})

const hasStreamDone = ref(false)

const fetchAppDetail = async () => {
  const res = await getAppVoById({ id: appId })
  if (res.data.code === 0 && res.data.data) {
    appDetail.value = res.data.data
    return true
  }
  message.error('获取应用信息失败，' + res.data.message)
  return false
}

const doDeploy = async () => {
  deployLoading.value = true
  try {
    const res = await deployApp({ appId })
    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      message.success('部署成功')
      return
    }
    message.error('部署失败，' + res.data.message)
  } finally {
    deployLoading.value = false
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
        appendAssistantChunk(data)
      }
    }
    if (buffer.trim()) {
      const { eventName, data } = parseSSEChunk(buffer)
      if (eventName === 'done') {
        hasStreamDone.value = true
      } else {
        appendAssistantChunk(data)
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
  if (!appId) {
    message.error('应用 id 无效')
    router.push('/')
    return
  }
  const ok = await fetchAppDetail()
  if (!ok) {
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
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.app-name {
  font-size: 18px;
  font-weight: 600;
}

.content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chat-panel,
.preview-panel {
  min-height: 620px;
}

.message-list {
  height: 500px;
  overflow-y: auto;
  padding: 8px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

.message-item {
  display: flex;
  margin-bottom: 10px;
}

.message-item.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: #1677ff;
  color: #fff;
}

.message-item.assistant .message-bubble {
  background: #fff;
  border: 1px solid #e8e8e8;
}

.input-row {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.preview-iframe {
  width: 100%;
  height: 560px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}
</style>
