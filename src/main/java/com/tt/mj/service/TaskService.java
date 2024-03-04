package com.tt.mj.service;

import com.tt.mj.enums.BlendDimensions;
import com.tt.mj.result.ResultJson;
import com.tt.mj.support.Task;
import eu.maxschuster.dataurl.DataUrl;

import java.util.List;

public interface TaskService {

	ResultJson submitImagine(Task task);

	ResultJson submitUpscale(Task task, String targetMessageId, String targetMessageHash, String actionCommand);

	ResultJson submitVariation(Task task, String targetMessageId, String targetMessageHash, int index);

	ResultJson submitAction(Task task, String messageId, String messageHash, String actionCommand);

	ResultJson submitDescribe(Task task, DataUrl dataUrl);

	ResultJson submitBlend(Task task, List<DataUrl> dataUrls, BlendDimensions dimensions);
}