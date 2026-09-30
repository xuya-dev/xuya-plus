package com.xuya.plus.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.xuya.core.annotation.QueryField;
import dev.xuya.core.annotation.QueryType;
import dev.xuya.core.datascope.DataScope;
import dev.xuya.core.translate.Translate;
import dev.xuya.core.translate.TranslateMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户表（行级数据权限：普通用户按部门可见范围过滤）
 */
@Data
@DataScope(column = "dept_id")
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属部门（数据权限按此列过滤；APPEND 模式：id 保留原值 + 附加 deptName 部门名）
     */
    @Translate(entity = SysDept.class, field = "deptName",
            mode = TranslateMode.APPEND, appendField = "deptName")
    private Long deptId;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 30, message = "用户名长度 2~30 个字符")
    @QueryField(QueryType.LIKE)
    private String username;

    @QueryField(QueryType.LIKE)
    private String nickname;

    /**
     * BCrypt 密文；只在登录/重置流程中出现，任何 JSON 输出都不序列化
     */
    @JsonIgnore
    private String password;

    private String email;

    private String phone;

    /**
     * 0=男 1=女 2=未知
     */
    @Translate(dict = "sys_user_sex")
    private String sex;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 0=正常 1=停用
     */
    @QueryField(QueryType.EQ)
    @Translate(dict = "sys_normal_disable")
    private Integer status;

    /**
     * 最后登录 IP / 时间
     */
    private String loginIp;

    private LocalDateTime loginDate;

    private String remark;

    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Translate(entity = SysUser.class, field = "nickname",
            mode = TranslateMode.APPEND, appendField = "createByName")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @Translate(entity = SysUser.class, field = "nickname",
            mode = TranslateMode.APPEND, appendField = "updateByName")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 表单辅助字段：角色/岗位 ID（不落库，聚合写接口使用）
     */
    @TableField(exist = false)
    private List<Long> roleIds;

    @TableField(exist = false)
    private List<Long> postIds;
}
