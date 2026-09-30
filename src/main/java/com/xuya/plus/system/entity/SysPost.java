package com.xuya.plus.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import dev.xuya.core.annotation.QueryField;
import dev.xuya.core.annotation.QueryType;
import dev.xuya.core.translate.Translate;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位表（纯 @QuickCrud：读写全部由框架生成）
 */
@Data
@TableName("sys_post")
public class SysPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "岗位编码不能为空")
    @QueryField(QueryType.EQ)
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    @QueryField(QueryType.LIKE)
    private String postName;

    private Integer postSort;

    @QueryField(QueryType.EQ)
    @Translate(dict = "sys_normal_disable")
    private Integer status;

    private String remark;

    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
