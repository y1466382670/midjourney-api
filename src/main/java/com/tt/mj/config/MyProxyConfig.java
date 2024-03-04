package com.tt.mj.config;

import lombok.Data;

@Data
public class MyProxyConfig {

    /**
     * 代理host.
     */
    private String host;
    /**
     * 代理端口.
     */
    private Integer port;

}
