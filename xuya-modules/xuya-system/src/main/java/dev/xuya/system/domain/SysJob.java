package dev.xuya.system.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import dev.xuya.core.annotation.QueryField;
import dev.xuya.core.annotation.QueryType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务调度表：invokeTarget 形如 beanName.method('字符串', 123, true)，
 * 目标 Bean 必须是 Spring 容器管理的组件
 */
@Data
@TableName("sys_job")
public class SysJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "任务名称不能为空")
    @QueryField(QueryType.LIKE)
    private String jobName;

    @QueryField(QueryType.EQ)
    private String jobGroup;

    /** 调用目标（beanName.method(参数)），保存时校验 Bean 与方法存在 */
    @NotBlank(message = "调用目标不能为空")
    private String invokeTarget;

    /** cron 表达式（保存时校验合法性） */
    @NotBlank(message = "cron表达式不能为空")
    private String cronExpression;

    /** 是否并发执行（0允许 1禁止） */
    private Integer concurrent;

    /** 状态（0正常 1暂停） */
    @QueryField(QueryType.EQ)
    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
