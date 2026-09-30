package com.xuya.plus.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户表单体（密码仅在此接收，实体 password 已 @JsonIgnore）
 */
@Data
public class SysUserForm {

    private Long id;

    private Long deptId;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 30, message = "用户名长度 2~30 个字符")
    private String username;

    private String nickname;

    /**
     * 新增时可空（默认 123456），修改接口忽略此字段
     */
    private String password;

    private String email;

    private String phone;

    private String sex;

    private Integer status;

    private String avatar;

    private String remark;

    private List<Long> roleIds;

    private List<Long> postIds;
}
