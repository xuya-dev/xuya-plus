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

/**
 * 定时任务调度日志表
 */
@Data
@TableName("sys_job_log")
public class SysJobLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;

    @QueryField(QueryType.LIKE)
    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    @QueryField(QueryType.LIKE)
    private String jobMessage;

    /** 0成功 1失败 */
    @QueryField(QueryType.EQ)
    private Integer status;

    private String exceptionInfo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
