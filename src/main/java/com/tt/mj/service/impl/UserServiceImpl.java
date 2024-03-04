package com.tt.mj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tt.mj.entity.Users;
import com.tt.mj.mapper.UsersMapper;
import com.tt.mj.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users> implements UserService {

}
