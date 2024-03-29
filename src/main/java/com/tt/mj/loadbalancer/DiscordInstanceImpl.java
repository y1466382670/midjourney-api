package com.tt.mj.loadbalancer;

import com.tt.mj.Constants;
import com.tt.mj.ReturnCode;
import com.tt.mj.domain.DiscordAccount;
import com.tt.mj.enums.BlendDimensions;
import com.tt.mj.enums.StatusEnum;
import com.tt.mj.enums.TaskStatus;
import com.tt.mj.result.Message;
import com.tt.mj.result.ResultJson;
import com.tt.mj.service.DiscordService;
import com.tt.mj.service.DiscordServiceImpl;
import com.tt.mj.service.NotifyService;
import com.tt.mj.support.Task;
import com.tt.mj.wss.WebSocketStarter;
import com.tt.mj.wss.user.UserWebSocketStarter;
import eu.maxschuster.dataurl.DataUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
public class DiscordInstanceImpl implements DiscordInstance {
	private final DiscordAccount account;
	private final WebSocketStarter socketStarter;
	private final DiscordService service;
	private final NotifyService notifyService;

	private final ThreadPoolTaskExecutor taskExecutor;
	private final List<Task> runningTasks;
	private final Map<String, Future<?>> taskFutureMap = Collections.synchronizedMap(new HashMap<>());

	public DiscordInstanceImpl(DiscordAccount account, UserWebSocketStarter socketStarter, RestTemplate restTemplate,
			NotifyService notifyService, String discordServer, Map<String, String> paramsMap) {
		this.account = account;
		this.socketStarter = socketStarter;
		this.notifyService = notifyService;
		this.service = new DiscordServiceImpl(account, restTemplate, discordServer, paramsMap);
		this.runningTasks = new CopyOnWriteArrayList<>();
		this.taskExecutor = new ThreadPoolTaskExecutor();
		this.taskExecutor.setCorePoolSize(account.getMaxQueue());
		this.taskExecutor.setMaxPoolSize(account.getMaxQueue());
		this.taskExecutor.setQueueCapacity(account.getMaxJob());
		this.taskExecutor.setThreadNamePrefix("TaskQueue-" + account.getDisplay() + "-");
		this.taskExecutor.initialize();
	}

	@Override
	public String getInstanceId() {
		return this.account.getChannelId();
	}

	@Override
	public DiscordAccount account() {
		return this.account;
	}

	@Override
	public boolean isAlive() {
		return this.account.isEnable();
	}

	@Override
	public void startWss() throws Exception {
		this.socketStarter.setTrying(true);
		this.socketStarter.start();
	}

	@Override
	public List<Task> getRunningTasks() {
		return this.runningTasks;
	}

	@Override
	public void exitTask(Task task) {
		try {
			Future<?> future = this.taskFutureMap.get(task.getId());
			if (future != null) {
				future.cancel(true);
			}
			saveAndNotify(task);
		} finally {
			this.runningTasks.remove(task);
			this.taskFutureMap.remove(task.getId());
		}
	}

	@Override
	public Map<String, Future<?>> getRunningFutures() {
		return this.taskFutureMap;
	}

	@Override
	public synchronized ResultJson submitImagineTask(Task task, Callable<Message<Void>> discordSubmit) {
		int currentWaitNumbers;
		try {
			currentWaitNumbers = this.taskExecutor.getThreadPoolExecutor().getQueue().size();
			Future<?> future = this.taskExecutor.submit(() -> executeTask(task, discordSubmit));
			this.taskFutureMap.put(task.getId(), future);
		} catch (RejectedExecutionException e) {
			return new ResultJson().fail( "队列已满，请稍后尝试");
		} catch (Exception e) {
			return new ResultJson().fail( "提交失败，系统异常");
		}
		//0：队列等待中  1：执行中  2：已完成  3：已失败
		if (currentWaitNumbers == 0) {
			task.setProperty("logState", StatusEnum.RUNNING.getCode());
		} else {
			task.setProperty("logState", 0);
		}
		Map<String, Object> map = new HashMap<>();
		map.put(Constants.TASK_PROPERTY_JOB_ID, task.getProperty(Constants.TASK_PROPERTY_JOB_ID));
		return new ResultJson().success(map);
	}

	private void executeTask(Task task, Callable<Message<Void>> discordSubmit) {
		this.runningTasks.add(task);
		try {
			task.start();
			Message<Void> result = discordSubmit.call();
			if (result.getCode() != ReturnCode.SUCCESS) {
				task.fail(result.getDescription());
				saveAndNotify(task);
				return;
			}
			saveAndNotify(task);
			do {
				task.sleep();
				saveAndNotify(task);
			} while (task.getStatus() == TaskStatus.IN_PROGRESS);
			log.debug("task finished, id: {}, status: {}", task.getId(), task.getStatus());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("task execute error", e);
			task.fail("执行错误，系统异常");
			saveAndNotify(task);
		} finally {
			this.runningTasks.remove(task);
			this.taskFutureMap.remove(task.getId());
		}
	}

	private void saveAndNotify(Task task) {
		this.notifyService.notifyTaskChange(task);
	}

	@Override
	public Message<Void> imagine(String prompt, String nonce) {
		return this.service.imagine(prompt, nonce);
	}

	@Override
	public Message<Void> upscale(String messageId, String actionCommand, String messageHash, String nonce) {
		return this.service.upscale(messageId, actionCommand, messageHash, nonce);
	}

	@Override
	public Message<Void> variation(String messageId, int index, String messageHash, String nonce) {
		return this.service.variation(messageId, index, messageHash, nonce);
	}

	@Override
	public Message<Void> action(String messageId, String messageHash, String action, String nonce) {
		return this.service.action(messageId, messageHash, action, nonce);
	}

	@Override
	public Message<Void> reroll(String messageId, String messageHash, String nonce) {
		return this.service.reroll(messageId, messageHash, nonce);
	}

	@Override
	public Message<Void> zoomout(String messageId, String messageHash, int number, String nonce) {
		return this.service.zoomout(messageId, messageHash, number, nonce);
	}

	@Override
	public Message<Void> describe(String finalFileName, String nonce) {
		return this.service.describe(finalFileName, nonce);
	}

	@Override
	public Message<Void> blend(List<String> finalFileNames, BlendDimensions dimensions, String nonce) {
		return this.service.blend(finalFileNames, dimensions, nonce);
	}

	@Override
	public Message<String> upload(String fileName, DataUrl dataUrl) {
		return this.service.upload(fileName, dataUrl);
	}

	@Override
	public Message<String> sendImageMessage(String content, String finalFileName) {
		return this.service.sendImageMessage(content, finalFileName);
	}

}
