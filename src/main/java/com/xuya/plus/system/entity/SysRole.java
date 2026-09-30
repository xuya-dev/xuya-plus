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
import java.util.List;

/**
 * 角色表
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @QueryField(QueryType.LIKE)
    private String roleName;

    /**
     * 角色权限字符串，如 admin / common
     */
    @NotBlank(message = "权限字符不能为空")
    @QueryField(QueryType.LIKE)
    private String roleKey;

    private Integer roleSort;

    /**
     * 数据范围：1=全部 2=自定义 3=本部门 4=本部门及以下 5=仅本人
     */
    @Translate(dict = "sys_data_scope")
    private Integer dataScope;

    /**
     * 菜单树选择项是否关联显示（父子联动）
     */
    private Boolean menuCheckStrictly;

    private Boolean deptCheckStrictly;

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

    /**
     * 表单辅助字段：授权菜单/自定义数据权限部门（不落库）
     */
    @TableField(exist = false)
    private List<Long> menuIds;

    @TableField(exist = false)
    private List<Long> deptIds;
}
