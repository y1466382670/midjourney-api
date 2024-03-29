package com.tt.mj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tt.mj.domain.DiscordAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<DiscordAccount> {
}
