package com.nini.niaicodeking.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.nini.niaicodeking.model.dto.app.AppQueryRequest;
import com.nini.niaicodeking.model.entity.App;
import com.nini.niaicodeking.model.vo.AppVO;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a herf="https://github.com/XsuperTitan">Blume Ni</a>
 */
public interface AppService extends IService<App> {


    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 管理员查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 校验应用参数
     *
     * @param app 应用
     * @param add 是否为创建场景
     */
    void validApp(App app, boolean add);

    /**
     * 获取应用视图对象
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 分页获取应用视图对象
     *
     * @param appPage 应用分页
     * @return 视图分页
     */
    Page<AppVO> getAppVOPage(Page<App> appPage);
}
