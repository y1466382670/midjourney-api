package com.tt.mj.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tt.mj.entity.LogModel;
import com.tt.mj.enums.MessageType;
import com.tt.mj.mapper.LogModelMapper;
import com.tt.mj.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SeedSuccessHandler  extends MessageHandler {

    @Autowired
    private LogModelMapper logModelMapper;
    private static final String CONTENT_SEED = "\\*\\*(.*?)\\n\\*\\*seed\\*\\*";

    @Autowired
    private NotifyService notifyService;


    @Override
    public void handle(MessageType messageType, DataObject message) {
        String content = getMessageContent(message);
        Matcher matcher = Pattern.compile(CONTENT_SEED).matcher(content);

        if (matcher.find()) {

            if (MessageType.CREATE.equals(messageType) && hasImage(message) && CharSequenceUtil.isBlank(getMessageNonce(message))) {
                log.debug("SeedSuccessHandler：" + message);

                int seedIndex = content.indexOf("seed**");
                String seed = content.substring(seedIndex + 6).trim();

                String imageUrl = getImageUrl(message);
                String messageHash = this.discordHelper.getMessageHash(imageUrl);


                LogModel logModel = logModelMapper.selectOne(
                        new QueryWrapper<LogModel>().lambda()
//                                .eq(LogModel::getPid, logModelPrent.getId())
                                .eq(LogModel::getImageHash, messageHash)
                                .eq(LogModel::getAction, "seed")
                                .eq(LogModel::getStatus, 1)
                );

                if (logModel != null) {
                    logModel.setSeed(seed);
                    logModel.setUpdatedAt(new Date());
                    logModel.setStatus(2);
                    int updateLine =  logModelMapper.updateById(logModel);

                    if (updateLine > 0) {
                        //回调接口
                        notifyService.hookUrl(logModel);
                    }
                }
            }
        }
    }
}
