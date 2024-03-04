package com.tt.mj.loadbalancer;

import com.tt.mj.domain.DiscordAccount;
import com.tt.mj.result.Message;
import com.tt.mj.result.ResultJson;
import com.tt.mj.service.DiscordService;
import com.tt.mj.support.Task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface DiscordInstance extends DiscordService {

	String getInstanceId();

	DiscordAccount account();

	boolean isAlive();

	void startWss() throws Exception;

	List<Task> getRunningTasks();

	void exitTask(Task task);

	Map<String, Future<?>> getRunningFutures();

	ResultJson submitImagineTask(Task task, Callable<Message<Void>> discordSubmit);

}
