package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.framework.security.RbacCacheService;
import dev.xuya.system.domain.SysRole;
import dev.xuya.system.domain.SysRoleDept;
import dev.xuya.system.domain.SysRoleMenu;
import dev.xuya.system.domain.SysUserRole;
import dev.xuya.system.mapper.SysRoleDeptMapper;
import dev.xuya.system.mapper.SysRoleMapper;
import dev.xuya.system.mapper.SysRoleMenuMapper;
import dev.xuya.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
/**
 * RoleService 接口（阿里巴巴分层规范：Service 层接口 + 实现分离）
 */
public interface RoleService {

    void createRole(SysRole role);

    void updateRole(SysRole role);

    void deleteRoles(List<Long> ids);

    void changeStatus(Long id, Integer status);

    List<Long> getMenuIds(Long roleId);

    List<Long> getDeptIds(Long roleId);
}