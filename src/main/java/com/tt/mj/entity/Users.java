package com.tt.mj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users")
public class Users {
    private Long id;

    private String name;

    private String email;

    private String token;

    private String emailVerifiedAt;

    private Integer maxQueue;

    private String createdAt;
}
