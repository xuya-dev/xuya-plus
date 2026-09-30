package dev.xuya.system.framework.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.datascope.DataScopeResolver;
import dev.xuya.system.domain.SysDept;
import dev.xuya.system.domain.SysRole;
import dev.xuya.system.domain.SysUser;
import dev.xuya.system.mapper.SysDeptMapper;
import dev.xuya.system.mapper.SysRoleDeptMapper;
import dev.xuya.system.mapper.SysRoleMapper;
import dev.xuya.system.mapper.SysUserMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 行级数据权限：按角色的 data_scope 决定当前用户可见的部门集合。
 * <pre>
 * 1=全部     -> null（不限制）
 * 2=自定义   -> sys_role_dept 配置的部门
 * 3=本部门   -> 用户所属部门
 * 4=本部门及以下 -> 用户部门及其全部子部门
 * 5=仅本人   -> 本脚手架简化为用户所属部门（@DataScope 仅支持单列过滤）
 * </pre>
 */
@Component
public class AdminDataScopeResolver implements DataScopeResolver {

    /** 空结果占位：IN (-1) 保证无权限用户查不到任何数据 */
    private static final Set<Long> NOTHING = Set.of(-1L);

    private final RbacCacheService rbacCacheService;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    public AdminDataScopeResolver(RbacCacheService rbacCacheService, SysRoleMapper roleMapper,
                                  SysRoleDeptMapper roleDeptMapper, SysUserMapper userMapper,
                                  SysDeptMapper deptMapper) {
        this.rbacCacheService = rbacCacheService;
        this.roleMapper = roleMapper;
        this.roleDeptMapper = roleDeptMapper;
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
    }

    @Override
    public Collection<?> visibleScope(Class<?> entityClass, String column, Object currentUser) {
        Long userId = currentUser == null ? null : Long.valueOf(currentUser.toString());
        if (userId == null) {
            return NOTHING;
        }
        if (rbacCacheService.isSuperAdmin(userId)) {
            return null;
        }
        Set<Long> roleIds = rbacCacheService.getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return NOTHING;
        }
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        if (roles.stream().anyMatch(r -> r.getDataScope() != null && r.getDataScope() == 1)) {
            return null;
        }

        SysUser user = userMapper.selectById(userId);
        Long userDeptId = user == null ? null : user.getDeptId();
        Set<Long> visible = new HashSet<>();
        for (SysRole role : roles) {
            Integer scope = role.getDataScope();
            if (scope == null) {
                continue;
            }
            switch (scope) {
                case 2 -> visible.addAll(roleDeptMapper.selectList(
                        new LambdaQueryWrapper<dev.xuya.system.domain.SysRoleDept>()
                                .eq(dev.xuya.system.domain.SysRoleDept::getRoleId, role.getId()))
                        .stream().map(dev.xuya.system.domain.SysRoleDept::getDeptId).toList());
                case 3, 5 -> {
                    if (userDeptId != null) {
                        visible.add(userDeptId);
                    }
                }
                case 4 -> {
                    if (userDeptId != null) {
                        visible.add(userDeptId);
                        visible.addAll(descendantDeptIds(userDeptId));
                    }
                }
                default -> {
                }
            }
        }
        return visible.isEmpty() ? NOTHING : visible;
    }

    /**
     * 全量部门内存中递归子部门（部门表数据量小，一次查出）
     */
    private Set<Long> descendantDeptIds(Long deptId) {
        List<SysDept> all = deptMapper.selectList(null);
        Set<Long> result = new HashSet<>();
        collectChildren(deptId, all, result);
        return result;
    }

    private void collectChildren(Long parentId, List<SysDept> all, Set<Long> result) {
        for (SysDept dept : all) {
            if (parentId.equals(dept.getParentId()) && result.add(dept.getId())) {
                collectChildren(dept.getId(), all, result);
            }
        }
    }
}
