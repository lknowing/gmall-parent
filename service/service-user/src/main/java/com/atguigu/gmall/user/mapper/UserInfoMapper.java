package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.model.user.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/08 12:39
 * @FileName: UserInfoMapper
 */
@Mapper
@Repository
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
