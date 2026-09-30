package com.xuya.plus.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import com.xuya.plus.framework.security.RbacCacheService;
import com.xuya.plus.system.entity.SysRole;
import com.xuya.plus.system.entity.SysRoleDept;
import com.xuya.plus.system.entity.SysRoleMenu;
import com.xuya.plus.system.entity.SysUserRole;
import com.xuya.plus.system.mapper.SysRoleDeptMapper;
import com.xuya.plus.system.mapper.SysRoleMapper;
import com.xuya.plus.system.mapper.SysRoleMenuMapper;
import com.xuya.plus.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色聚合写服务：角色 + 菜单授权 + 自定义数据权限部门，事务内落库并刷新 RBAC 缓存
 */
@Service
public class RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final RbacCacheService rbacCacheService;

    public RoleService(SysRoleMapper roleMapper, SysRoleMenuMapper roleMenuMapper,
                       SysRoleDeptMapper roleDeptMapper, SysUserRoleMapper userRoleMapper,
                       RbacCacheService rbacCacheService) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleDeptMapper = roleDeptMapper;
        this.userRoleMapper = userRoleMapper;
        this.rbacCacheService = rbacCacheService;
    }

    @Transactional
    public void createRole(SysRole role) {
        checkRoleKeyUnique(role.getRoleKey(), null);
        if (role.getStatus() == null) {
            role.setStatus(0);
        }
        if (role.getDataScope() == null) {
            role.setDataScope(1);
        }
        roleMapper.insert(role);
        bind(role);
        rbacCacheService.refresh();
    }

    @Transactional
    public void updateRole(SysRole role) {
        if (role.getId() == null) {
            throw new QuickDevException("角色 ID 不能为空");
        }
        SysRole exists = requireRole(role.getId());
        if (RbacCacheService.SUPER_ROLE_KEY.equals(exists.getRoleKey())) {
            throw new QuickDevException("内置超级管理员角色不允许修改");
        }
        checkRoleKeyUnique(role.getRoleKey(), role.getId());
        role.setRoleKey(null); // 权限字符创建后不允许变更，避免授权语义漂移
        role.setDelFlag(null);
        roleMapper.updateById(role);
        bind(role);
        rbacCacheService.refresh();
    }

    @Transactional
    public void deleteRoles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new QuickDevException("请选择要删除的角色");
        }
        for (Long id : ids) {
            SysRole role = requireRole(id);
            if (RbacCacheService.SUPER_ROLE_KEY.equals(role.getRoleKey())) {
                throw new QuickDevException("内置超级管理员角色不允许删除");
            }
            Long assigned = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getRoleId, id));
            if (assigned != null && assigned > 0) {
                throw new QuickDevException("角色[" + role.getRoleName() + "]已分配用户，不能删除");
            }
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, ids));
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, ids));
        roleMapper.deleteBatchIds(ids);
        rbacCacheService.refresh();
    }

    public void changeStatus(Long id, Integer status) {
        SysRole role = requireRole(id);
        if (RbacCacheService.SUPER_ROLE_KEY.equals(role.getRoleKey()) && status != 0) {
            throw new QuickDevException("内置超级管理员角色不允许停用");
        }
        SysRole patch = new SysRole();
        patch.setId(id);
        patch.setStatus(status);
        roleMapper.updateById(patch);
        rbacCacheService.refresh();
    }

    public List<Long> getMenuIds(Long roleId) {
        requireRole(roleId);
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)).stream().map(SysRoleMenu::getMenuId).toList();
    }

    public List<Long> getDeptIds(Long roleId) {
        requireRole(roleId);
        return roleDeptMapper.selectList(new LambdaQueryWrapper<SysRoleDept>()
                        .eq(SysRoleDept::getRoleId, roleId)).stream().map(SysRoleDept::getDeptId).toList();
    }

    private void bind(SysRole role) {
        if (role.getMenuIds() != null) {
            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                    .eq(SysRoleMenu::getRoleId, role.getId()));
            for (Long menuId : role.getMenuIds()) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
        if (role.getDeptIds() != null) {
            roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                    .eq(SysRoleDept::getRoleId, role.getId()));
            for (Long deptId : role.getDeptIds()) {
                SysRoleDept rd = new SysRoleDept();
                rd.setRoleId(role.getId());
                rd.setDeptId(deptId);
                roleDeptMapper.insert(rd);
            }
        }
    }

    private void checkRoleKeyUnique(String roleKey, Long excludeId) {
        SysRole exists = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey));
        if (exists != null && !exists.getId().equals(excludeId)) {
            throw new QuickDevException("权限字符已存在: " + roleKey);
        }
    }

    private SysRole requireRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new QuickDevException("角色不存在或已被删除");
        }
        return role;
    }
}
