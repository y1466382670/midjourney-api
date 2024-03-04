package com.tt.mj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Components {
    //imagine
    U1("MJ::JOB::upsample::1::", "upsample1",""),
    U2("MJ::JOB::upsample::2::", "upsample2",""),
    U3("MJ::JOB::upsample::3::", "upsample3",""),
    U4("MJ::JOB::upsample::4::", "upsample4",""),
    V1("MJ::JOB::variation::1::", "variation1",""),
    V2("MJ::JOB::variation::2::", "variation2",""),
    V3("MJ::JOB::variation::3::", "variation3",""),
    V4("MJ::JOB::variation::4::", "variation4",""),

    //U
    VaryStrong("MJ::JOB::high_variation::1::", "high_variation", "::SOLO"),
    VarySubtle("MJ::JOB::low_variation::1::", "low_variation", "::SOLO"),
    Inpaint("MJ::Inpaint::1::", "inpaint", "::SOLO"), //勾选
    Upscale2("MJ::JOB::upsample_v5_2x::1::", "upscale2", "::SOLO"),
    Upscale4("MJ::JOB::upsample_v5_4x::1::", "upscale4", "::SOLO"),
    ZoomOut2("MJ::Outpaint::50::1::", "zoom_out_2", "::SOLO"),
    ZoomOut1_5("MJ::Outpaint::75::1::", "zoom_out_1_5", "::SOLO"),
    PanLeft("MJ::JOB::pan_left::1::", "pan_left", "::SOLO"),
    PanRight("MJ::JOB::pan_right::1::", "pan_right", "::SOLO"),
    PanUp("MJ::JOB::pan_up::1::", "pan_up", "::SOLO"),
    PanDown("MJ::JOB::pan_down::1::", "pan_down", "::SOLO");


    @Getter
    private String action;

    @Getter
    private String name;

    @Getter
    private String addr;

}
