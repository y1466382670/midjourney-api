package com.tt.mj.wss.handle;

import com.tt.mj.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * upscale消息处理.
 */
@Slf4j
@Component
public class UpscaleSuccessHandler extends MessageHandler {
    private static final String CONTENT_REGEX_UP = "\\*\\*(.*?)\\*\\* - Image #(.*?) <@\\d+>";

    @Override
    public void handle(MessageType messageType, DataObject message) {
        String content = getMessageContent(message);
        String action = getParseData(content);
        if (MessageType.CREATE.equals(messageType) && action != null && hasImage(message)) {
            finishUTask(message, action);
        }
    }

    private String getParseData(String content) {
        String action = null;
        Matcher matcher2 = Pattern.compile(CONTENT_REGEX_UP).matcher(content);
        if (matcher2.find()) {
            action = "upsample" + matcher2.group(2);
        }
        return action;
    }

}