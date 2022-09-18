package com.atguigu.gmall.payment.mapper;

import com.atguigu.gmall.model.payment.PaymentInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/17 10:56
 * @FileName: PaymentInfoMapper
 */
@Mapper
@Repository
public interface PaymentInfoMapper extends BaseMapper<PaymentInfo> {
}
