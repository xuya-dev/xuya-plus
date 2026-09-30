package com.xuya.plus.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import dev.xuya.core.annotation.QueryField;
import dev.xuya.core.annotation.QueryType;
import dev.xuya.core.translate.Translate;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门表（树形：CrudOp.TREE 依据 parentId + children 构树）
 */
@Data
@TableName("sys_dept")
public class SysDept {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("parent_id")
    private Long parentId;

    /**
     * 祖级路径，如 0,100,101
     */
    private String ancestors;

    @QueryField(QueryType.LIKE)
    private String deptName;

    private Integer orderNum;

    private String leader;

    private String phone;

    private String email;

    /**
     * 0=正常 1=停用
     */
    @QueryField(QueryType.EQ)
    @Translate(dict = "sys_normal_disable")
    private Integer status;

    private Integer delFlag;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(exist = false)
    private List<SysDept> children;
}
