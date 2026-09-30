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
 * 通知公告表（纯 @QuickCrud）
 */
@Data
@TableName("sys_notice")
public class SysNotice {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "公告标题不能为空")
    @QueryField(QueryType.LIKE)
    private String noticeTitle;

    /**
     * 1=通知 2=公告
     */
    @QueryField(QueryType.EQ)
    @Translate(dict = "sys_notice_type")
    private Integer noticeType;

    /**
     * 公告内容（富文本）
     */
    private String noticeContent;

    /**
     * 0=正常 1=关闭
     */
    @QueryField(QueryType.EQ)
    @Translate(dict = "sys_notice_status")
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
