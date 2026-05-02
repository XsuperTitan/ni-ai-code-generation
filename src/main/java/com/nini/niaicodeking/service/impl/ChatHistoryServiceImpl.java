package com.nini.niaicodeking.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.nini.niaicodeking.model.entity.ChatHistory;
import com.nini.niaicodeking.mapper.ChatHistoryMapper;
import com.nini.niaicodeking.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a herf="https://github.com/XsuperTitan">Blume Ni</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
