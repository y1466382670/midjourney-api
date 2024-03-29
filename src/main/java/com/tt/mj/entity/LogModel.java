package com.tt.mj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("log")
public class LogModel {

    private long id;

    private String jobId;

    private String model;

    private Long accountId;

    private String action;

    private String progress;

    private Integer status;

    private String prompt;

    private String nonce;

    private String messageId;

    private String progressMessageId;

    private String imageHash;

    private String discordImage;

    private String hookUrl;

    private Integer hookTimes;

    private String components;

    private String message;

    private String responseData;

    private String createdAt;

    private Date updatedAt;

    private long pid;

    private String seed;
}
