package com.xuya.plus.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import dev.xuya.core.annotation.QueryField;
import dev.xuya.core.annotation.QueryType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志表（登录成功/失败由 AuthService 写入；物理删除）
 */
@Data
@TableName("sys_logininfor")
public class SysLogininfor {

    @TableId(type = IdType.AUTO)
    private Long id;

    @QueryField(QueryType.LIKE)
    private String username;

    @QueryField(QueryType.LIKE)
    private String ip;

    /**
     * 日志内容，如"登录成功"/"密码错误"
     */
    @QueryField(QueryType.LIKE)
    private String msg;

    /**
     * 0=成功 1=失败
     */
    @QueryField(QueryType.EQ)
    private Integer status;

    @QueryField(QueryType.BETWEEN)
    private LocalDateTime loginTime;
}
