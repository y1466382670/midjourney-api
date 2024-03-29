package com.tt.mj.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.tt.mj.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * imagine消息处理.
 */
@Slf4j
@Component
public class ImagineSuccessHandler extends MessageHandler {

	private static final String CONTENT_REGEX_UP = "\\*\\*(.*?)\\*\\* - Image #\\d <@\\d+>";

	private static final String CONTENT_SEED = "\\*\\*(.*?)\\n\\*\\*seed\\*\\*";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		Matcher matcher = Pattern.compile(CONTENT_REGEX_UP).matcher(content);
		Matcher matcher2 = Pattern.compile(CONTENT_SEED).matcher(content);
		System.out.println(content);
		if (matcher.find() || matcher2.find()) {
			return;
		}
		if (MessageType.CREATE.equals(messageType) && hasImage(message) && CharSequenceUtil.isBlank(getMessageNonce(message))) {
			log.debug("ImagineSuccessHandler：" + message);
			finishImageTask(message);
		}
	}

}
