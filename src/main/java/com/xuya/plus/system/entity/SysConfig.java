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
 * 参数配置表（全量驻留 ConfigCacheService 内存缓存）
 */
@Data
@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "参数名称不能为空")
    @QueryField(QueryType.LIKE)
    private String configName;

    /**
     * 参数键名，如 sys.account.captchaEnabled
     */
    @NotBlank(message = "参数键名不能为空")
    @QueryField(QueryType.EQ)
    private String configKey;

    /**
     * 参数键值
     */
    @NotBlank(message = "参数键值不能为空")
    private String configValue;

    /**
     * 系统内置：Y=是 N=否（内置参数不允许删除）
     */
    @QueryField(QueryType.EQ)
    @Translate(dict = "sys_yes_no")
    private String configType;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
