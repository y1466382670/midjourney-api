package com.tt.mj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tt.mj.entity.LogModel;
import com.tt.mj.mapper.LogModelMapper;
import com.tt.mj.service.LogModelService;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends ServiceImpl<LogModelMapper, LogModel> implements LogModelService {
}
