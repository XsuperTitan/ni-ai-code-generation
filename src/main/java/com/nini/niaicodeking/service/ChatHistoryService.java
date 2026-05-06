package com.nini.niaicodeking.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.nini.niaicodeking.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.nini.niaicodeking.model.entity.ChatHistory;
import com.nini.niaicodeking.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a herf="https://github.com/XsuperTitan">Blume Ni</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 添加聊天消息。
     *
     * @param appId        应用ID
     * @param message      消息内容
     * @param messageType  消息类型
     * @param userId       用户ID
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页查询应用的对话历史
     *
     * @param appId 应用ID
     * @param pageSize 每页大小
     * @param lastCreateTime 上次查询的创建时间（用于分页）
     * @param loginUser 登录用户
     * @return 分页结果
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                              LocalDateTime lastCreateTime,
                                              User loginUser);
}
