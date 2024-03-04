package com.tt.mj.wss.handle;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tt.mj.Constants;
import com.tt.mj.entity.LogModel;
import com.tt.mj.enums.MessageType;
import com.tt.mj.enums.StatusEnum;
import com.tt.mj.loadbalancer.DiscordLoadBalancer;
import com.tt.mj.mapper.LogModelMapper;
import com.tt.mj.service.NotifyService;
import com.tt.mj.support.DiscordHelper;
import com.tt.mj.support.Task;
import com.tt.mj.support.TaskCondition;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

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
//        logModel.setResponseData(message.toString());
        logModel.setComponents(getComponents(message));
        logModel.setUpdatedAt(new Date());
        logModel.setCdnImage(uploadImage(imageUrl));
        logModel.setMessage("");
        int updateLine = logModelMapper.updateById(logModel);

        if (updateLine > 0) {
            //回调接口
            notifyService.hookUrl(logModel);
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
//            FileInputStream infile = new FileInputStream(String.valueOf(inputStream));
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

    public String uploadImage(String image){
        return StringUtils.replace(image,"cdn.discordapp.com","mjcdn.ttapi.io");
//        String url = "https://api.imgbb.com/1/upload";
//        try {
//            URL obj = new URL(url);
//            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//            // 设置请求方法
//            con.setRequestMethod("POST");
//            // 添加请求头
//            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            // 设置请求体
//            String urlParameters = "key=" + URLEncoder.encode("0ce8e0b8b05cf9c073ac5550abaab7ce", "UTF-8") + "&image=" +
//                    URLEncoder.encode(image, "UTF-8");
//            // 发送请求
//            con.setDoOutput(true);
//            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//            wr.writeBytes(urlParameters);
//            wr.flush();
//            wr.close();
//            // 获取响应内容
//            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//            org.json.JSONObject jsonObject = new org.json.JSONObject(response.toString());
//            return jsonObject.getJSONObject("data").getString("url");
//
//        } catch (Exception e) {
//            return "";
//        }

    }

    protected void findAndFinishImageTask(TaskCondition condition, String finalPrompt, DataObject message) {
        String imageUrl = getImageUrl(message);
        String messageHash = this.discordHelper.getMessageHash(imageUrl);

        condition.setMessageHash(messageHash);
        Task task = this.discordLoadBalancer.findRunningTask(condition)
                .findFirst().orElseGet(() -> {
                    condition.setMessageHash(null);
                    return this.discordLoadBalancer.findRunningTask(condition)
                            .min(Comparator.comparing(Task::getStartTime))
                            .orElse(null);
                });
        if (task == null) {
            return;
        }
        task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, finalPrompt);
        task.setProperty(Constants.TASK_PROPERTY_MESSAGE_HASH, messageHash);
        task.setImageUrl(imageUrl);
        finishTask(task, message);
        task.awake();
    }


    protected void finishTask(Task task, DataObject message) {
        task.setProperty(Constants.TASK_PROPERTY_MESSAGE_ID, message.getString("id"));
        task.setProperty(Constants.TASK_PROPERTY_FLAGS, message.getInt("flags", 0));
        task.setProperty(Constants.TASK_PROPERTY_MESSAGE_HASH, this.discordHelper.getMessageHash(task.getImageUrl()));
        task.success();
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
