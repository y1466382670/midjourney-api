package com.tt.mj.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubmitActionDTO extends BaseSubmitDTO {

    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    private String jobId;

    /**
     * 操作类型 ["upsample1","upsample2","upsample3","upsample4","variation1","variation2","variation3","variation4"]
     */
    @NotBlank(message = "操作类型不能为空")
    private String action;

}
