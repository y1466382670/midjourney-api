package com.tt.mj.dto;

import com.tt.mj.enums.BlendDimensions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * BLEND混图任务提交
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubmitBlendDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "图片base64数组", required = true, example = "[\"data:image/png;base64,xxx1\", \"data:image/png;base64,xxx2\"]")
	private List<String> imgBase64Array;

	@ApiModelProperty(value = "比例: PORTRAIT(2:3); SQUARE(1:1); LANDSCAPE(3:2)", example = "SQUARE")
	private BlendDimensions dimensions = BlendDimensions.SQUARE;
}
