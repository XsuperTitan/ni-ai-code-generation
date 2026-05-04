<script setup lang="ts">
defineProps<{
  creating: boolean
  quickPrompts: string[]
}>()

const prompt = defineModel<string>({ required: true })
const emit = defineEmits<{
  create: []
}>()
</script>

<template>
  <section class="hero-section">
    <div class="hero-content">
      <div class="hero-kicker">AI Website Builder</div>
      <h1 class="hero-title">AI 应用生成平台</h1>
      <p class="hero-subtitle">一句话轻松创建网站应用</p>

      <div class="prompt-card">
        <a-textarea
          v-model:value="prompt"
          class="prompt-input"
          placeholder="帮我创建个人博客网站"
          :auto-size="{ minRows: 3, maxRows: 6 }"
        />
        <div class="prompt-action">
          <a-button type="primary" size="large" :loading="creating" @click="emit('create')">
            开始创建应用
          </a-button>
        </div>
      </div>

      <div class="quick-prompts">
        <button
          v-for="quickPrompt in quickPrompts"
          :key="quickPrompt"
          class="quick-prompt"
          type="button"
          @click="prompt = quickPrompt"
        >
          {{ quickPrompt }}
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero-section {
  position: relative;
  max-width: 1200px;
  width: 100%;
  padding: 64px 0 28px;
  margin: 0 auto;
  color: #fff;
}

.hero-section::before {
  position: absolute;
  inset: 16px 8%;
  z-index: 0;
  pointer-events: none;
  content: '';
  background:
    linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: radial-gradient(circle, #000 0%, transparent 68%);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 920px;
  margin: 0 auto;
  text-align: center;
}

.hero-kicker {
  display: inline-flex;
  padding: 6px 14px;
  margin-bottom: 16px;
  color: #c5fff0;
  font-size: 13px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  border: 1px solid rgba(197, 255, 240, 0.42);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
}

.hero-title {
  margin: 0;
  color: #fff;
  font-size: 56px;
  line-height: 1.12;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.hero-subtitle {
  margin: 16px 0 28px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 20px;
}

.prompt-card {
  padding: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 80px rgba(8, 17, 34, 0.28);
  backdrop-filter: blur(18px);
}

.prompt-input {
  border: 0;
  box-shadow: none;
  resize: none;
  font-size: 16px;
}

.prompt-input:focus {
  border: 0;
  box-shadow: none;
}

.prompt-action {
  margin-top: 8px;
  text-align: right;
}

.quick-prompts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
  text-align: left;
}

.quick-prompt {
  min-height: 96px;
  padding: 14px 16px;
  color: rgba(255, 255, 255, 0.86);
  line-height: 1.6;
  text-align: left;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.09);
  backdrop-filter: blur(12px);
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.quick-prompt:hover {
  transform: translateY(-2px);
  border-color: rgba(129, 255, 225, 0.72);
  background: rgba(255, 255, 255, 0.15);
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 38px;
  }

  .quick-prompts {
    grid-template-columns: 1fr;
  }
}
</style>
