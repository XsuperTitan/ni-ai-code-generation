package com.nini.niaicodeking.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.nini.niaicodeking.model.entity.App;
import com.nini.niaicodeking.mapper.AppMapper;
import com.nini.niaicodeking.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a herf="https://github.com/XsuperTitan">Blume Ni</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
