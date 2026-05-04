<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import HomeHeroSection from '@/components/home/HomeHeroSection.vue'
import AppCardSection from '@/components/home/AppCardSection.vue'
import { joinUrl } from '@/utils/url'

const router = useRouter()
const creating = ref(false)
const initPrompt = ref('')

const myParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  appName: '',
})
const myKeyword = ref('')
const myApps = ref<API.AppVO[]>([])
const myTotal = ref(0)

const goodParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  appName: '',
})
const goodKeyword = ref('')
const goodApps = ref<API.AppVO[]>([])
const goodTotal = ref(0)
const appDeployBaseUrl = import.meta.env.VITE_APP_DEPLOY_BASE_URL || 'http://localhost'
const quickPrompts = [
  '创建一个适合独立开发者的个人博客网站，包含首页、文章列表、文章详情、关于我和联系方式模块，整体风格简洁高级，支持展示技术标签、精选文章和个人项目经历。',
  '生成一个现代化企业官网，用于展示一家人工智能解决方案公司，包含首页横幅、核心服务、客户案例、团队介绍和预约咨询表单，视觉风格要科技感强、可信赖。',
  '创建一个在线课程销售落地页，面向想学习前端开发的用户，包含课程亮点、适合人群、讲师介绍、课程大纲、学员评价和立即报名按钮，整体转化导向明确。',
  '生成一个摄影作品集网站，适合自由摄影师展示商业拍摄案例，包含大图首页、作品分类、项目详情、服务报价和联系预约入口，风格要高级、留白充足、图片突出。',
]

const doCreateApp = async () => {
  if (!initPrompt.value.trim()) {
    message.warning('请输入应用需求提示词')
    return
  }
  creating.value = true
  try {
    const res = await addApp({ initPrompt: initPrompt.value })
    if (res.data.code === 0 && res.data.data) {
      await router.push({
        path: `/app/chat/${res.data.data}`,
        query: {
          initPrompt: initPrompt.value,
        },
      })
      return
    }
    message.error('创建应用失败，' + res.data.message)
  } finally {
    creating.value = false
  }
}

const fetchMyApps = async () => {
  const res = await listMyAppVoByPage(myParams)
  if (res.data.code === 0 && res.data.data) {
    myApps.value = res.data.data.records ?? []
    myTotal.value = Number(res.data.data.totalRow ?? 0)
    return
  }
  message.error('获取我的应用失败，' + res.data.message)
}

const fetchGoodApps = async () => {
  const res = await listGoodAppVoByPage(goodParams)
  if (res.data.code === 0 && res.data.data) {
    goodApps.value = res.data.data.records ?? []
    goodTotal.value = Number(res.data.data.totalRow ?? 0)
    return
  }
  message.error('获取精选应用失败，' + res.data.message)
}

const doSearchMy = () => {
  myParams.pageNum = 1
  myParams.appName = myKeyword.value.trim()
  fetchMyApps()
}

const doSearchGood = () => {
  goodParams.pageNum = 1
  goodParams.appName = goodKeyword.value.trim()
  fetchGoodApps()
}

const viewChat = (appId?: string | number) => {
  if (!appId) {
    return
  }
  router.push({
    path: `/app/chat/${appId}`,
    query: {
      view: '1',
    },
  })
}

const getWorkUrl = (deployKey?: string) => {
  if (!deployKey) {
    return ''
  }
  return joinUrl(appDeployBaseUrl, deployKey)
}

const viewWork = (deployKey?: string) => {
  const workUrl = getWorkUrl(deployKey)
  if (!workUrl) {
    return
  }
  window.open(workUrl, '_blank')
}

onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<template>
  <div id="homePage">
    <HomeHeroSection v-model="initPrompt" :creating="creating" :quick-prompts="quickPrompts" @create="doCreateApp" />

    <AppCardSection
      v-model:keyword="myKeyword"
      v-model:current="myParams.pageNum"
      v-model:page-size="myParams.pageSize"
      title="我的应用"
      :apps="myApps"
      :total="myTotal"
      empty-description="暂无应用"
      user-name-fallback="我"
      avatar-fallback="我"
      @search="doSearchMy"
      @page-change="fetchMyApps"
      @view-chat="viewChat"
      @view-work="viewWork"
    />

    <AppCardSection
      v-model:keyword="goodKeyword"
      v-model:current="goodParams.pageNum"
      v-model:page-size="goodParams.pageSize"
      title="精选应用"
      :apps="goodApps"
      :total="goodTotal"
      empty-description="暂无精选应用"
      user-name-fallback="无名"
      avatar-fallback="匿"
      @search="doSearchGood"
      @page-change="fetchGoodApps"
      @view-chat="viewChat"
      @view-work="viewWork"
    />
  </div>
</template>

<style scoped>
#homePage {
  min-height: calc(100vh - 56px);
  padding: 56px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background:
    radial-gradient(circle at 16% 10%, rgba(118, 255, 221, 0.5), transparent 32%),
    radial-gradient(circle at 82% 12%, rgba(88, 196, 255, 0.38), transparent 30%),
    radial-gradient(circle at 50% 78%, rgba(213, 255, 236, 0.72), transparent 36%),
    linear-gradient(135deg, #061613 0%, #0b3332 42%, #eafff7 100%);
}

@media (max-width: 768px) {
  #homePage {
    padding: 36px 16px;
  }
}
</style>
