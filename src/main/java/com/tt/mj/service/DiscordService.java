package com.tt.mj.service;

import com.tt.mj.enums.BlendDimensions;
import com.tt.mj.result.Message;
import eu.maxschuster.dataurl.DataUrl;

import java.util.List;

public interface DiscordService {

	Message<Void> imagine(String prompt, String nonce);

	Message<Void> upscale(String messageId, String actionCommand, String messageHash, String nonce);

	Message<Void> variation(String messageId, int index, String messageHash, String nonce);

	Message<Void> action(String messageId, String messageHash, String actionCommand, String nonce);

	Message<Void> reroll(String messageId, String messageHash, String nonce);

	Message<Void> zoomout(String messageId, String messageHash, int number, String nonce);

	Message<Void> describe(String finalFileName, String nonce);

	Message<Void> blend(List<String> finalFileNames, BlendDimensions dimensions, String nonce);

	Message<String> upload(String fileName, DataUrl dataUrl);

	Message<String> sendImageMessage(String content, String finalFileName);
}
