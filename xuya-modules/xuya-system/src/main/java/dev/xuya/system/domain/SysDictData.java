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
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据表（DictLoader 的数据源，全量驻留内存）
 */
@Data
@TableName("sys_dict_data")
public class SysDictData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属字典类型编码
     */
    @NotBlank(message = "字典类型不能为空")
    @QueryField(QueryType.EQ)
    private String dictType;

    @NotBlank(message = "数据标签不能为空")
    @QueryField(QueryType.LIKE)
    private String dictLabel;

    @NotBlank(message = "数据键值不能为空")
    private String dictValue;

    private Integer dictSort;

    /**
     * 是否默认（Y=是 N=否）
     */
    @Translate(dict = "sys_yes_no")
    private String isDefault;

    @QueryField(QueryType.EQ)
    @Translate(dict = "sys_normal_disable")
    private Integer status;

    private String remark;

    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
