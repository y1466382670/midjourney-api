package com.tt.mj.wss.handle;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tt.mj.entity.LogModel;
import com.tt.mj.enums.MessageType;
import com.tt.mj.enums.StatusEnum;
import com.tt.mj.loadbalancer.DiscordLoadBalancer;
import com.tt.mj.mapper.LogModelMapper;
import com.tt.mj.service.NotifyService;
import com.tt.mj.support.DiscordHelper;
import com.tt.mj.support.Task;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public abstract class MessageHandler {
    @Autowired
    protected DiscordLoadBalancer discordLoadBalancer;
    @Autowired
    protected DiscordHelper discordHelper;

    @Autowired
    private LogModelMapper logModelMapper;

    @Autowired
    private NotifyService notifyService;

    public abstract void handle(MessageType messageType, DataObject message);

    protected String getMessageContent(DataObject message) {
        return message.hasKey("content") ? message.getString("content") : "";
    }

    protected String getMessageNonce(DataObject message) {
        return message.hasKey("nonce") ? message.getString("nonce") : "";
    }

    protected String getMessageReferenceDataObject (DataObject message){
        return message.hasKey("message_reference") ? message.getObject("message_reference").getString("message_id") : "";
    }

    public void finishUTask(DataObject message, String action)
    {
        String imageUrl = getImageUrl(message);
        String messageHash = this.discordHelper.getMessageHash(imageUrl);
        String parentsMessageId = message.getObject("message_reference").getString("message_id");
        if(StrUtil.isNotBlank(parentsMessageId)){
            LogModel parents = logModelMapper.selectOne(
                    new QueryWrapper<LogModel>().lambda().eq(LogModel::getMessageId, parentsMessageId));
            if(parents == null){
                return;
            }
            LogModel logModel = logModelMapper.selectOne(
                    new QueryWrapper<LogModel>().lambda().eq(LogModel::getPid, parents.getId())
                            .eq(LogModel::getAction, action)
            );
            if(logModel == null){
                return;
            }
            logModel.setImageHash(messageHash);
            setSuccessEnd(logModel, message);
        }
    }

    public void finishImageTask(DataObject message) {
        String imageUrl = getImageUrl(message);
        String messageHash = this.discordHelper.getMessageHash(imageUrl);
        LogModel logModel = logModelMapper.selectOne(
                new QueryWrapper<LogModel>().lambda().eq(LogModel::getImageHash, messageHash)
        );
        if (logModel != null) {
            setSuccessEnd(logModel, message);
        }
    }

    /**
     * 生成成功最终调用
     * @param logModel
     * @param message
     */
    protected void setSuccessEnd(LogModel logModel, DataObject message){
        String imageUrl = getImageUrl(message);
        logModel.setMessageId(message.getString("id"));
        logModel.setDiscordImage(imageUrl);
        logModel.setProgress("100");
        logModel.setStatus(StatusEnum.SUCCESS.getCode());
        logModel.setComponents(getComponents(message));
        logModel.setUpdatedAt(new Date());
        logModel.setMessage("");
        int updateLine = logModelMapper.updateById(logModel);
        if (updateLine > 0) {
            //回调接口
            notifyService.hookUrl(logModel);
        }
        finishTask(logModel.getJobId());
    }

    protected void finishTask(String jobId) {
        Task task = this.discordLoadBalancer.getRunningTask(jobId);
        if (!ObjectUtils.isEmpty(task)) {
            task.success();
            task.awake();
        }
    }

    protected String getComponents(DataObject message) {
        DataArray arr = message.getArray("components");
        if (arr.isEmpty()) {
            return "";
        }
        List<String> customIds = JsonPath.read(arr.toString(), "$..components[*].custom_id");

        Map<String, String> support = new HashMap<>();
        try {
            ClassPathResource classPathResource = new ClassPathResource("classpath:support.json");
            InputStream inputStream = classPathResource.getStream();
            JSONArray array = new JSONArray(inputStream);
            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                support.put(jsonObject.getStr("action"), jsonObject.getStr("replace"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<String> sortedKeys = support.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
        List<String> results = customIds.stream()
                .map(a -> {
                    for (String key : sortedKeys) {
                        if (a.startsWith(key)) {
                            return support.get(key);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return (new Gson()).toJson(results);
    }

    protected boolean hasImage(DataObject message) {
        DataArray attachments = message.optArray("attachments").orElse(DataArray.empty());
        return !attachments.isEmpty();
    }

    protected String getImageUrl(DataObject message) {
        DataArray attachments = message.getArray("attachments");
        if (!attachments.isEmpty()) {
            String imageUrl = attachments.getObject(0).getString("url");
            return replaceCdnUrl(imageUrl);
        }
        return null;
    }

    protected String replaceCdnUrl(String imageUrl) {
        if (CharSequenceUtil.isBlank(imageUrl)) {
            return imageUrl;
        }
        String cdn = this.discordHelper.getCdn();
        if (CharSequenceUtil.startWith(imageUrl, cdn)) {
            return imageUrl;
        }
        return CharSequenceUtil.replaceFirst(imageUrl, DiscordHelper.DISCORD_CDN_URL, cdn);
    }

}
