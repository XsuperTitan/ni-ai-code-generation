/* eslint-disable */
import request from '@/request'

/** 分页查询某个应用的对话历史 GET /chatHistory/app/{appId} */
export async function listAppChatHistory(
  params: API.listAppChatHistoryParams,
  options?: { [key: string]: any }
) {
  const { appId, ...queryParams } = params
  return request<API.BaseResponsePageChatHistory>(`/chatHistory/app/${appId}`, {
    method: 'GET',
    params: {
      ...queryParams,
    },
    ...(options || {}),
  })
}
