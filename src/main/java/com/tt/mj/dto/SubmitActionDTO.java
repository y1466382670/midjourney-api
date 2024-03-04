package com.tt.mj.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubmitActionDTO extends BaseSubmitDTO {

    /**
     * 任务ID
     */
    private String jobId;

    /**
     * 操作类型 ["upsample1","upsample2","upsample3","upsample4","variation1","variation2","variation3","variation4"]
     */
    private String action;

}
