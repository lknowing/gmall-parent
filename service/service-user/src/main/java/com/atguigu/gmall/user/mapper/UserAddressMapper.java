package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.model.user.UserAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 12:33
 * @FileName: UserAddressMapper
 */
@Mapper
@Repository
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
