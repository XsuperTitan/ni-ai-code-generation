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

const defaultFenceRenderer =
  markdown.renderer.rules.fence ??
  ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))

markdown.renderer.rules.fence = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  const language = (token.info || '').trim().split(/\s+/)[0] || 'text'
  const renderedFence = defaultFenceRenderer(tokens, idx, options, env, self)
  return `<div class="code-block"><div class="code-lang">${language}</div>${renderedFence}</div>`
}

const decorateAssistantContent = (content: string) => {
  if (!content) {
    return ''
  }
  return content
    .replace(
      /^\[(工具调用|工具执行结果|工具执行|选择工具|tool call|tool execution)\]\s*(.+)$/gim,
      '<div class="tool-call-line">[$1] $2</div>',
    )
    .replace(/\n{3,}/g, '\n\n')
}

const renderMarkdown = (content: string) => {
  return markdown.render(decorateAssistantContent(content))
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
  margin: 0;
  padding: 12px;
  overflow-x: auto;
  border-radius: 0 0 8px 8px;
  background: #f6f8fa !important;
}

.markdown-body :deep(.code-block) {
  margin-bottom: 10px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.markdown-body :deep(.code-lang) {
  padding: 6px 12px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  text-transform: lowercase;
  background: #f1f5f9;
  border-bottom: 1px solid #e5e7eb;
}

.markdown-body :deep(.tool-call-line) {
  margin-bottom: 10px;
  padding: 8px 10px;
  color: #0f172a;
  font-size: 13px;
  font-family: ui-monospace, SFMono-Regular, Consolas, 'Liberation Mono', Menlo, monospace;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
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
