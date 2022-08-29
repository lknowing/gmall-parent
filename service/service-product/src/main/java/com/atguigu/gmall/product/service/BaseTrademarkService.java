package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/27 19:04
 * @FileName: BaseTrademarkService
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {
    /**
     * 查询品牌分页对象
     *
     * @param baseTrademarkPage
     * @return
     */
    IPage<BaseTrademark> getBaseTrademarkPage(Page<BaseTrademark> baseTrademarkPage);
}
