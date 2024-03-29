package com.tt.mj.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;


@Data
@ApiModel("Imagine提交参数")
@EqualsAndHashCode(callSuper = true)
public class SubmitImagineDTO extends BaseSubmitDTO {

	/**
	 * 提示词 示例：dog
	 */
	@NotBlank(message = "提示词不能为空")
	private String prompt;

	/**
	 * 模式 fast relax turbo
	 */
	@NotBlank(message = "模式不能为空")
	@Pattern(regexp = "relax|fast|turbo", message = "模式错误")
	private String mode = "fast";

}
