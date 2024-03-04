package com.tt.mj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum StatusEnum {

    //执行中
    RUNNING(1, "ON_QUEUE"),

    //成功
    SUCCESS(2, "SUCCESS"),

    //失败
    FAILED(3, "FAILED"),

    //队列等待中
    PENDING_QUEUE(0, "PENDING_QUEUE");

    @Getter
    private Integer code;

    @Getter
    private String name;

    public static String getName(Integer code) {
        for (StatusEnum c : StatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.name;
            }
        }
        return null;
    }

    public static Integer getCode(String name) {
        for (StatusEnum c : StatusEnum.values()) {
            if (c.getName().equals(name)) {
                return c.code;
            }
        }
        return null;
    }


}
