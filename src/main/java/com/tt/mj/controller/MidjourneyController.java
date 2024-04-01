package com.tt.mj.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tt.mj.Constants;
import com.tt.mj.ProxyProperties;
import com.tt.mj.domain.DiscordAccount;
import com.tt.mj.dto.*;
import com.tt.mj.entity.LogModel;
import com.tt.mj.enums.StatusEnum;
import com.tt.mj.enums.TaskAction;
import com.tt.mj.enums.TranslateWay;
import com.tt.mj.exception.BannedPromptException;
import com.tt.mj.mapper.AccountMapper;
import com.tt.mj.result.ResultJson;
import com.tt.mj.service.*;
import com.tt.mj.support.Task;
import com.tt.mj.util.BannedPromptUtils;
import com.tt.mj.util.MimeTypeUtils;
import com.tt.mj.util.SnowFlake;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class MidjourneyController {
	private final TranslateService translateService;
	private final ProxyProperties properties;
	private final TaskService taskService;

	@Autowired
	AccountMapper accountMapper;

	@Autowired
	LogModelService logModelService;

	/**
	 * 绘图任务提交
	 * @param imagineDTO
	 * @return
	 */
	@PostMapping("/imagine")
	public ResultJson imagine(@RequestBody @Validated SubmitImagineDTO imagineDTO) {
		String prompt = imagineDTO.getPrompt();
		Task task = newTask(imagineDTO);
		task.setAction(TaskAction.IMAGINE.getName());
		task.setPrompt(prompt);
		//翻译英文
		String promptEn = translatePrompt(prompt);
		try {
			BannedPromptUtils.checkBanned(promptEn);
		} catch (BannedPromptException e) {
			return new ResultJson().fail("存在敏感词：" + e.getMessage());
		}
		task.setPromptEn(promptEn);
		task.setDescription("/imagine " + prompt);
		task.setProperty(Constants.TASK_PROPERTY_MODE, imagineDTO.getMode());
		return this.taskService.submitImagine(task);
	}

	@PostMapping("/action")
	public ResultJson action(@RequestBody @Validated SubmitActionDTO actionDTO) {
		if (ObjectUtils.isEmpty(TaskAction.getActionCommand(actionDTO.getAction()))) {
			return new ResultJson().fail("操作类型异常");
		}
		LogModel logModel = logModelService.getOne(new LambdaQueryWrapper<LogModel>()
				.eq(LogModel::getJobId, actionDTO.getJobId()));
		if (ObjectUtils.isEmpty(logModel)) {
			return new ResultJson<>().fail("关联任务不存在");
		}
		if (!logModel.getStatus().equals(StatusEnum.SUCCESS.getCode())) {
			return new ResultJson().fail("关联任务未成功");
		}
		if (!logModel.getComponents().contains(actionDTO.getAction())) {
			return new ResultJson().fail("关联任务不存在此操作");
		}
		DiscordAccount account = accountMapper.selectById(logModel.getAccountId());
		Task task = newTask(actionDTO);
		task.setAction(actionDTO.getAction());
		task.setPrompt(logModel.getPrompt());
		task.setPromptEn(logModel.getPrompt());
		task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, logModel.getPrompt());
		task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, logModel.getProgressMessageId());
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, account.getChannelId());
		task.setProperty(Constants.TASK_PROPERTY_PID, logModel.getId());
		String messageId = logModel.getMessageId();
		String messageHash = logModel.getImageHash();
		if (actionDTO.getAction().contains("upsample") || actionDTO.getAction().contains("upscale")) {
			return this.taskService.submitUpscale(task, messageId, messageHash, TaskAction.getActionCommand(actionDTO.getAction()));
		} else if (actionDTO.getAction().contains("variation") && actionDTO.getAction().startsWith("variation")) {
			int index = Integer.parseInt(actionDTO.getAction().substring(actionDTO.getAction().length() - 1));
			return this.taskService.submitVariation(task, messageId, messageHash, index);
		} else {
			return this.taskService.submitAction(task, messageId, messageHash, TaskAction.getActionCommand(actionDTO.getAction()));
		}
	}

	/**
	 * describe 图转文
	 * @param describeDTO
	 * @return
	 */
	@PostMapping("/describe")
	public ResultJson describe(@RequestBody SubmitDescribeDTO describeDTO) {
		if (CharSequenceUtil.isBlank(describeDTO.getBase64())) {
			return new ResultJson().fail("base64 cannot be empty");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		DataUrl dataUrl;
		try {
			dataUrl = serializer.unserialize(describeDTO.getBase64());
		} catch (MalformedURLException e) {
			return new ResultJson().fail("base64 format error");
		}
		Task task = newTask(describeDTO);
		task.setAction(TaskAction.DESCRIBE.getName());
		String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
		task.setDescription("/describe " + taskFileName);
		return this.taskService.submitDescribe(task, dataUrl);
	}

	/**
	 * 混图生成
	 * @param blendDTO
	 * @return
	 */
	@PostMapping("/blend")
	public ResultJson blend(@RequestBody SubmitBlendDTO blendDTO) {
		List<String> base64Array = blendDTO.getImgBase64Array();
		if (base64Array == null || base64Array.size() < 2 || base64Array.size() > 5) {
			return new ResultJson<>().fail("The 'imgBase64Array' array has a length of 2 to 5");
		}
		if (blendDTO.getDimensions() == null) {
			return new ResultJson<>().fail("\"dimensions\" parameter error!");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		List<DataUrl> dataUrlList = new ArrayList<>();
		try {
			for (String base64 : base64Array) {
				DataUrl dataUrl = serializer.unserialize(base64);
				dataUrlList.add(dataUrl);
			}
		} catch (MalformedURLException e) {
			return new ResultJson<>().fail("base64 format error");
		}
		Task task = newTask(blendDTO);
		task.setAction(TaskAction.BLEND.getName());
		task.setDescription("/blend " + task.getId() + " " + dataUrlList.size());
		return this.taskService.submitBlend(task, dataUrlList, blendDTO.getDimensions());
	}

	private Task newTask(BaseSubmitDTO base) {
		Task task = new Task();
		task.setId(System.currentTimeMillis() + RandomUtil.randomNumbers(3));
		task.setSubmitTime(System.currentTimeMillis());
		task.setState(base.getState());
		task.setProperty(Constants.TASK_PROPERTY_NOTIFY_HOOK, base.getHookUrl());
		task.setProperty(Constants.TASK_PROPERTY_NONCE, SnowFlake.INSTANCE.nextId());
		//任务ID
		task.setProperty(Constants.TASK_PROPERTY_JOB_ID, UUID.randomUUID().toString());
		return task;
	}

	/**
	 * 翻译输入信息
	 * @param prompt
	 * @return
	 */
	private String translatePrompt(String prompt) {
		if (TranslateWay.NULL.equals(this.properties.getTranslateWay()) || CharSequenceUtil.isBlank(prompt)) {
			return prompt;
		}
		List<String> imageUrls = new ArrayList<>();
		Matcher imageMatcher = Pattern.compile("https?://[a-z0-9-_:@&?=+,.!/~*'%$]+\\x20+", Pattern.CASE_INSENSITIVE).matcher(prompt);
		while (imageMatcher.find()) {
			imageUrls.add(imageMatcher.group(0));
		}
		String paramStr = "";
		Matcher paramMatcher = Pattern.compile("\\x20+-{1,2}[a-z]+.*$", Pattern.CASE_INSENSITIVE).matcher(prompt);
		if (paramMatcher.find()) {
			paramStr = paramMatcher.group(0);
		}
		String imageStr = CharSequenceUtil.join("", imageUrls);
		String text = prompt.substring(imageStr.length(), prompt.length() - paramStr.length());
		if (CharSequenceUtil.isNotBlank(text)) {
			text = this.translateService.translateToEnglish(text).trim();
		}
		return imageStr + text + paramStr;
	}

}
