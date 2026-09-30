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
 * 文件表（上传记录；对象本体在存储服务中，删除时同步移除）
 */
@Data
@TableName("sys_oss")
public class SysOss {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对象名（含日期路径，uuid 防覆盖） */
    private String fileName;

    @QueryField(QueryType.LIKE)
    private String originalName;

    private String fileSuffix;

    /** 访问地址（公开桶直连；私有桶走下载接口） */
    private String url;

    /** 存储标识（sys_oss_config.config_key） */
    @QueryField(QueryType.EQ)
    private String service;

    private Long fileSize;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
