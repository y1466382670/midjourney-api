package com.tt.mj.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@ApiModel("Imagine提交参数")
@EqualsAndHashCode(callSuper = true)
public class SubmitImagineDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "提示词", required = true, example = "Cat")
	private String prompt;

	/**
	 * 模式 fast relax turbo
	 */
	private String mode;

}
