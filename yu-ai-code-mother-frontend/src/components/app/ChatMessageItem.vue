<script setup lang="ts">
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import aiAvatar from '@/assets/aiAvatar.png'
import type { ChatMessage } from '@/types/chat'

defineProps<{
  item: ChatMessage
}>()

const markdown = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true,
  highlight(code, language) {
    const validLanguage = language && hljs.getLanguage(language)
    if (validLanguage) {
      return hljs.highlight(code, { language }).value
    }
    return hljs.highlightAuto(code, ['html', 'css', 'javascript']).value
  },
})

const renderMarkdown = (content: string) => {
  return markdown.render(content)
}
</script>

<template>
  <div :class="['message-item', item.role === 'user' ? 'user' : 'assistant']">
    <a-avatar v-if="item.role === 'assistant'" :src="aiAvatar" class="ai-avatar" />
    <div
      v-if="item.role === 'assistant'"
      class="message-bubble markdown-body"
      v-html="renderMarkdown(item.content)"
    />
    <div v-else class="message-bubble">{{ item.content }}</div>
  </div>
</template>

<style scoped>
.message-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
  min-width: 0;
  margin-bottom: 8px;
}

.message-item.user {
  justify-content: flex-end;
}

.ai-avatar {
  flex: 0 0 auto;
}

.message-bubble {
  max-width: 80%;
  min-width: 0;
  padding: 8px 12px;
  border-radius: 8px;
  overflow-x: auto;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: #1677ff;
  color: #fff;
  white-space: pre-wrap;
}

.message-item.assistant .message-bubble {
  background: #fff;
  border: 1px solid #e8e8e8;
}

.markdown-body {
  line-height: 1.7;
  white-space: normal;
}

.markdown-body :deep(p),
.markdown-body :deep(ul),
.markdown-body :deep(ol),
.markdown-body :deep(pre),
.markdown-body :deep(blockquote) {
  margin-top: 0;
  margin-bottom: 10px;
}

.markdown-body :deep(p:last-child),
.markdown-body :deep(ul:last-child),
.markdown-body :deep(ol:last-child),
.markdown-body :deep(pre:last-child),
.markdown-body :deep(blockquote:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(pre) {
  max-width: 100%;
  padding: 12px;
  overflow-x: auto;
  border-radius: 8px;
  background: #f6f8fa;
}

.markdown-body :deep(code) {
  font-family:
    ui-monospace, SFMono-Regular, Consolas, 'Liberation Mono', Menlo, monospace;
}

.markdown-body :deep(:not(pre) > code) {
  padding: 2px 4px;
  border-radius: 4px;
  background: #f6f8fa;
}

.markdown-body :deep(blockquote) {
  padding-left: 12px;
  color: #666;
  border-left: 4px solid #d9d9d9;
}
</style>
