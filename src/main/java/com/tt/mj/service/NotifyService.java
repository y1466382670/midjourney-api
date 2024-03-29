package com.tt.mj.service;

import com.tt.mj.entity.LogModel;
import com.tt.mj.support.Task;


public interface NotifyService {

	void notifyTaskChange(Task task);

	void hookUrl(LogModel logModel);
}
