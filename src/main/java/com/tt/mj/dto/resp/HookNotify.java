package com.tt.mj.dto.resp;

import lombok.Data;

import java.util.List;

@Data
public class HookNotify {

    private String status;

    private String message;

    private String jobId;

    private SecondData data;

    @Data
    public static class SecondData{

        private String action;

        private String jobId;

        private String progress;

        private String prompt;

        private String discordImage;

        private String cdnImage;

        private String hookUrl;

        private List<String> components;

        private String seed;

    }



}
