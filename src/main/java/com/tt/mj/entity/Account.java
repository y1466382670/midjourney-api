package com.tt.mj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tt.mj.Constants;
import lombok.Data;

import java.util.Date;

@Data
@TableName("account")
public class Account {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务器ID.
     */
    private String guildId;
    /**
     * 频道ID.
     */
    private String channelId;
    /**
     * 用户Token.
     */
    private String token;
    /**
     * 用户UserAgent.
     */
    private String userAgent = Constants.DEFAULT_DISCORD_USER_AGENT;

    private Integer enable;


    private String discordInfo;

    /**
     * 代理IP端口
     */
    private String proxyIp;

    /**
     * 代理密码
     */
    private String proxyPass;

    private Date updatedAt;

    private String mjBotId;

}
