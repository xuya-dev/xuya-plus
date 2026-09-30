package com.xuya.plus.auth.dto;

import lombok.Data;

import java.util.List;

/**
 * 前端动态路由（RuoYi 风格）
 */
@Data
public class RouterVo {

    private String name;
    private String path;
    private Boolean hidden;
    private String component;
    private Meta meta;
    private List<RouterVo> children;

    @Data
    public static class Meta {
        private String title;
        private String icon;

        public Meta(String title, String icon) {
            this.title = title;
            this.icon = icon;
        }
    }
}
