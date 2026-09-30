package dev.xuya.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-岗位关联
 */
@Data
@TableName("sys_user_post")
public class SysUserPost {

    private Long userId;

    private Long postId;
}
