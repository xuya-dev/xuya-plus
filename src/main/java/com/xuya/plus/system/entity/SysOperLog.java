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
 * 操作日志表（@QuickLog -> DbOperationLogSink 异步写入；物理删除）
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务模块，如"用户管理"
     */
    @QueryField(QueryType.LIKE)
    private String module;

    /**
     * 操作描述，如"新增用户"
     */
    @QueryField(QueryType.LIKE)
    private String description;

    /**
     * 操作人（loginId）
     */
    @QueryField(QueryType.LIKE)
    private String operator;

    private String uri;

    private String httpMethod;

    private String ip;

    /**
     * 入参 JSON（已截断）
     */
    private String params;

    private Integer resultCode;

    /**
     * 0=成功 1=失败
     */
    @QueryField(QueryType.EQ)
    private Integer status;

    private String errorMsg;

    private Long costMs;

    @QueryField(QueryType.BETWEEN)
    private LocalDateTime operTime;
}
