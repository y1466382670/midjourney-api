package com.tt.mj.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tt.mj.entity.LogModel;
import com.tt.mj.enums.MessageType;
import com.tt.mj.mapper.LogModelMapper;
import com.tt.mj.util.ContentParseData;
import com.tt.mj.util.ConvertUtils;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class StartAndProgressHandler extends MessageHandler {

    @Autowired
    private LogModelMapper logModelMapper;

    @Override
    public void handle(MessageType messageType, DataObject message) {
        String nonce = getMessageNonce(message);
        String content = getMessageContent(message);
        ContentParseData parseData = ConvertUtils.parseContent(content);
        if (MessageType.CREATE.equals(messageType) && CharSequenceUtil.isNotBlank(nonce)) {
            if (isError(message)) {
                return;
            }
            log.debug("首次更新id：" + message.getString("id"));

            //首次更新id
            LogModel logModel = logModelMapper.selectOne(
                    new QueryWrapper<LogModel>().lambda().eq(LogModel::getNonce, message.getString("nonce"))
            );
            if (logModel != null) {
                logModel.setProgressMessageId(message.getString("id"));
                //绑定hash
                if(!message.getArray("components").isEmpty()){
                    List<String> customIds = JsonPath.read(message.getArray("components").toString(), "$..components[*].custom_id");
                    String cusId = customIds.get(0);
                    String hash = CharSequenceUtil.sub(cusId, cusId.lastIndexOf("::") + 2, cusId.length());
                    //U在第一步 绑定hash
                    logModel.setImageHash(hash);
                }
                logModelMapper.updateById(logModel);
            }

        } else if (MessageType.UPDATE.equals(messageType) && parseData != null) {

            String imageUrl = getImageUrl(message);
            String imagineHash = this.discordHelper.getMessageHash(imageUrl);

            //任务进行中
            LogModel logModel = logModelMapper.selectOne(
                    new QueryWrapper<LogModel>().lambda().eq(LogModel::getProgressMessageId, message.getString("id"))
            );
            if (logModel != null) {
                logModel.setDiscordImage(imageUrl);
                logModel.setImageHash(imagineHash);
                logModel.setProgress(parseData.getStatus().replace("%", ""));
                logModelMapper.updateById(logModel);
            }

        }
    }

    private boolean isError(DataObject message) {
        Optional<DataArray> embedsOptional = message.optArray("embeds");
        if (embedsOptional.isEmpty() || embedsOptional.get().isEmpty()) {
            return false;
        }
        DataObject embed = embedsOptional.get().getObject(0);
        return embed.getInt("color", 0) == 16711680;
    }

}
