package com.nini.niaicodeking.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.nini.niaicodeking.model.entity.User;
import com.nini.niaicodeking.mapper.UserMapper;
import com.nini.niaicodeking.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author <a herf="https://github.com/XsuperTitan">Blume Ni</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
