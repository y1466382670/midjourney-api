package com.tt.mj.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.exceptions.CheckedUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.mj.Constants;
import com.tt.mj.ProxyProperties;
import com.tt.mj.dto.resp.HookNotify;
import com.tt.mj.entity.LogModel;
import com.tt.mj.enums.StatusEnum;
import com.tt.mj.enums.TaskStatus;
import com.tt.mj.mapper.LogModelMapper;
import com.tt.mj.support.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private final ThreadPoolTaskExecutor executor;
	private final TimedCache<String, Object> taskLocks = CacheUtil.newTimedCache(Duration.ofHours(1).toMillis());

	@Autowired
	private LogModelMapper logModelMapper;

	public NotifyServiceImpl(ProxyProperties properties) {
		this.executor = new ThreadPoolTaskExecutor();
		this.executor.setCorePoolSize(properties.getNotifyPoolSize());
		this.executor.setThreadNamePrefix("TaskNotify-");
		this.executor.initialize();
	}

	@Override
	public void notifyTaskChange(Task task) {
		String notifyHook = task.getPropertyGeneric(Constants.TASK_PROPERTY_NOTIFY_HOOK);
		if (CharSequenceUtil.isBlank(notifyHook)) {
			return;
		}
		String taskId = task.getId();
		TaskStatus taskStatus = task.getStatus();
		Object taskLock = this.taskLocks.get(taskId, (CheckedUtil.Func0Rt<Object>) Object::new);
		try {
			String paramsStr = OBJECT_MAPPER.writeValueAsString(task);
			this.executor.execute(() -> {
				synchronized (taskLock) {
					try {
						ResponseEntity<String> responseEntity = postJson(notifyHook, paramsStr);
						if (responseEntity.getStatusCode() == HttpStatus.OK) {
							log.debug("推送任务变更成功, 任务ID: {}, status: {}, notifyHook: {}", taskId, taskStatus, notifyHook);
						} else {
							log.warn("推送任务变更失败, 任务ID: {}, notifyHook: {}, code: {}, msg: {}", taskId, notifyHook, responseEntity.getStatusCodeValue(), responseEntity.getBody());
						}
					} catch (Exception e) {
						log.warn("推送任务变更失败, 任务ID: {}, notifyHook: {}, 描述: {}", taskId, notifyHook, e.getMessage());
					}
				}
			});
		} catch (JsonProcessingException e) {
			log.warn("推送任务变更失败, 任务ID: {}, notifyHook: {}, 描述: {}", taskId, notifyHook, e.getMessage());
		}
	}


	@Async
	public void hookUrl(LogModel logModel){

		String notifyHook = logModel.getHookUrl();
		if(StrUtil.isBlank(notifyHook)){
			return;
		}
		HookNotify hookNotify = new HookNotify();
		hookNotify.setStatus(StatusEnum.getName(logModel.getStatus()));
		hookNotify.setMessage(logModel.getMessage());
		hookNotify.setJobId(logModel.getJobId());
		HookNotify.SecondData secondData = new HookNotify.SecondData();

		if(logModel.getStatus().equals(2)){
			BeanUtils.copyProperties(logModel, secondData);
			try {
				secondData.setComponents(new ObjectMapper().readValue(logModel.getComponents(), new TypeReference<List<String>>(){}));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		hookNotify.setData(secondData);
		try {
			//默认最多请求三次
			for (int i = 0; i < 3; i++) {
				GsonBuilder builder = new GsonBuilder();
				Gson gson = builder.serializeNulls().setPrettyPrinting().create();
				ResponseEntity<String> responseEntity = postJson(notifyHook, gson.toJson(hookNotify));
				logModel.setHookTimes( i + 1);
				logModelMapper.updateById(logModel);
				if (responseEntity.getStatusCode() == HttpStatus.OK) {
					break;
				}
			}
		} catch (Exception e) {
			log.debug("回调失败：log_id:【{}】", logModel.getId());
		}
	}

	private ResponseEntity<String> postJson(String notifyHook, String paramsJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsJson, headers);
		return new RestTemplate().postForEntity(notifyHook, httpEntity, String.class);
	}

}
