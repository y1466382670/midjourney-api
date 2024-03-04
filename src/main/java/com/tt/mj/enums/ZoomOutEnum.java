package com.tt.mj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ZoomOutEnum {

    zoom_out_2("zoom_out_2", 50),

    zoom_out_1_5("zoom_out_1_5", 75);

    private String name;

    private int number;


    public static int getNumber(String name) {
        for (ZoomOutEnum c : ZoomOutEnum.values()) {
            if (c.getName().equals(name)) {
                return c.number;
            }
        }
        return 0;
    }
}
