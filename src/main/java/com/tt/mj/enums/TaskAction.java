package com.tt.mj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskAction {

	IMAGINE("imagine", "imagine"),

	UPSAMPLE1("upsample1", "MJ::JOB::upsample::1"),

	UPSAMPLE2("upsample2", "MJ::JOB::upsample::2"),

	UPSAMPLE3("upsample3", "MJ::JOB::upsample::3"),

	UPSAMPLE4("upsample4", "MJ::JOB::upsample::4"),

	VARIATION1("variation1", "MJ::JOB::variation::1"),

	VARIATION2("variation2", "MJ::JOB::variation::2"),

	VARIATION3("variation3", "MJ::JOB::variation::3"),

	VARIATION4("variation4", "MJ::JOB::variation::4"),

	UPSCALE2("upscale2", "MJ::JOB::upsample_v5_2x::1"),

	UPSCALE4("upscale4", "MJ::JOB::upsample_v5_4x::1"),

	LOW_VARIATION("low_variation", "MJ::JOB::low_variation::1"),

	HIGH_VARIATION("high_variation", "MJ::JOB::high_variation::1"),

	PAN_LEFT("pan_left", "MJ::JOB::pan_left::1"),

	PAN_RIGHT("pan_right", "MJ::JOB::pan_right::1"),

	PAN_UP("pan_up", "MJ::JOB::pan_up::1"),

	PAN_DOWN("pan_down", "MJ::JOB::pan_down::1"),

	REROLL("reroll", "MJ::JOB::reroll::0"),

	ZOOM_OUT_2("zoom_out_2", "MJ::Outpaint::50::1"),

	ZOOM_OUT_1_5("zoom_out_1_5", "MJ::Outpaint::75::1"),

	BLEND("blend", "blend"),

	DESCRIBE("describe", "describe");

	public String name;

	public String command;

	public static String getActionCommand(String name) {
		for (TaskAction c : TaskAction.values()) {
			if (c.getName().equals(name)) {
				return c.command;
			}
		}
		return null;
	}

}
