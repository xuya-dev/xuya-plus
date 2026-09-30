package dev.xuya.system.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import dev.xuya.core.annotation.QueryField;
import dev.xuya.core.annotation.QueryType;
import dev.xuya.core.translate.Translate;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OSS 对象存储配置（config_key=local 为本地存储；其余走 S3 协议：MinIO/阿里云 OSS/腾讯云 COS）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_oss_config")
public class SysOssConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置标识（minio/local），存储实现据此路由 */
    @NotBlank(message = "配置标识不能为空")
    @QueryField(QueryType.EQ)
    private String configKey;

    @NotBlank(message = "配置名称不能为空")
    @QueryField(QueryType.LIKE)
    private String configName;

    private String accessKey;

    private String secretKey;

    /** S3 桶名（local 时忽略） */
    private String bucketName;

    /** 端点（含协议，如 http://localhost:9000） */
    private String endpoint;

    /** 自定义访问域名（留空用端点拼装） */
    private String domain;

    /** 区域（MinIO 可留空，默认 us-east-1） */
    private String region;

    /** 本地存储根路径（configKey=local 时生效） */
    private String storagePath;

    /** 0=使用中（全局唯一），1=备用 */
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
}
