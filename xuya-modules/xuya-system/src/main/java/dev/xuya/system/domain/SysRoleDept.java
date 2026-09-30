package dev.xuya.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-部门关联（自定义数据权限）
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept {

    private Long roleId;

    private Long deptId;
}
