package com.tt.mj.service;

import com.tt.mj.Constants;
import com.tt.mj.ReturnCode;
import com.tt.mj.enums.BlendDimensions;
import com.tt.mj.loadbalancer.DiscordInstance;
import com.tt.mj.loadbalancer.DiscordLoadBalancer;
import com.tt.mj.result.Message;
import com.tt.mj.result.ResultJson;
import com.tt.mj.support.Task;
import com.tt.mj.util.MimeTypeUtils;
import eu.maxschuster.dataurl.DataUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
	private final DiscordLoadBalancer discordLoadBalancer;

	/**
	 * @param task 任务实例
	 * @return
	 */
	@Override
	public ResultJson submitImagine(Task task) {
		//获取可用的discord账号
		DiscordInstance instance = this.discordLoadBalancer.chooseInstance();
		if (instance == null) {
			return new ResultJson().fail("No available accounts.");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, instance.getInstanceId());
		return instance.submitImagineTask(task, () ->
			 instance.imagine(task.getPromptEn(), task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE))
		);
	}

	@Override
	public ResultJson submitUpscale(Task task, String targetMessageId, String targetMessageHash, String actionCommand) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return new ResultJson<>().fail("\"账号不可用: \"" + instanceId);
		}
		return discordInstance.submitImagineTask(task, () -> discordInstance.upscale(targetMessageId, actionCommand, targetMessageHash, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public ResultJson submitVariation(Task task, String targetMessageId, String targetMessageHash, int index) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return new ResultJson<>().fail("\"账号不可用: \"" + instanceId);
		}
		return discordInstance.submitImagineTask(task, () -> discordInstance.variation(targetMessageId, index, targetMessageHash, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public ResultJson submitAction(Task task, String targetMessageId, String targetMessageHash, String actionCommand) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return new ResultJson<>().fail("\"账号不可用: \"" + instanceId);
		}
		return discordInstance.submitImagineTask(task, () -> discordInstance.action(targetMessageId, targetMessageHash, actionCommand, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public ResultJson submitDescribe(Task task, DataUrl dataUrl) {
		DiscordInstance discordInstance = this.discordLoadBalancer.chooseInstance();
		if (discordInstance == null) {
			return new ResultJson<>().fail("No available accounts");
		}
		//账号ID
		task.setProperty(Constants.TASK_PROPERTY_ACCOUNT_ID, discordInstance.account().getId());
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, discordInstance.getInstanceId());
		return discordInstance.submitImagineTask(task, () -> {
			String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
			Message<String> uploadResult = discordInstance.upload(taskFileName, dataUrl);
			if (uploadResult.getCode() != ReturnCode.SUCCESS) {
				return Message.of(uploadResult.getCode(), uploadResult.getDescription());
			}
			String finalFileName = uploadResult.getResult();
			return discordInstance.describe(finalFileName, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		});
	}

	@Override
	public ResultJson submitBlend(Task task, List<DataUrl> dataUrls, BlendDimensions dimensions) {
		DiscordInstance discordInstance = this.discordLoadBalancer.chooseInstance();
		if (discordInstance == null) {
			return new ResultJson<>().fail("No available accounts");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, discordInstance.getInstanceId());
		return discordInstance.submitImagineTask(task, () -> {
			List<String> finalFileNames = new ArrayList<>();
			for (DataUrl dataUrl : dataUrls) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
				Message<String> uploadResult = discordInstance.upload(taskFileName, dataUrl);
				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
					return Message.of(uploadResult.getCode(), uploadResult.getDescription());
				}
				finalFileNames.add(uploadResult.getResult());
			}
			return discordInstance.blend(finalFileNames, dimensions, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		});
	}

}
