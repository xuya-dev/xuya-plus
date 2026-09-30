package dev.xuya.system.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import dev.xuya.core.annotation.QueryField;
import dev.xuya.core.annotation.QueryType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单权限表（M=目录 C=菜单 F=按钮；物理删除）
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    @QueryField(QueryType.LIKE)
    private String menuName;

    private Long parentId;

    private Integer orderNum;

    /**
     * 路由地址（如 user）
     */
    private String path;

    /**
     * 组件路径（如 system/user/index）
     */
    private String component;

    /**
     * M=目录 C=菜单 F=按钮
     */
    @QueryField(QueryType.EQ)
    private String menuType;

    /**
     * 权限标识（如 sys:user:add）
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 显示状态：0=显示 1=隐藏
     */
    private String visible;

    /**
     * 0=正常 1=停用
     */
    @QueryField(QueryType.EQ)
    private Integer status;

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
    private List<SysMenu> children;
}
