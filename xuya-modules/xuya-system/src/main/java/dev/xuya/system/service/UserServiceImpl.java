package dev.xuya.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.framework.security.RbacCacheService;
import dev.xuya.system.domain.bo.SysUserBo;
import dev.xuya.system.domain.SysUser;
import dev.xuya.system.domain.SysUserPost;
import dev.xuya.system.domain.SysUserRole;
import dev.xuya.system.mapper.SysUserMapper;
import dev.xuya.system.mapper.SysUserPostMapper;
import dev.xuya.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户聚合写服务：用户 + 角色 + 岗位在同一事务内落库，变更后刷新 RBAC 缓存
 */
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final RbacCacheService rbacCacheService;

    public UserServiceImpl(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper,
                       SysUserPostMapper userPostMapper, RbacCacheService rbacCacheService) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.userPostMapper = userPostMapper;
        this.rbacCacheService = rbacCacheService;
    }

    @Transactional
    @Override
    public void createUser(SysUserBo form) {
        checkUsernameUnique(form.getUsername(), null);
        SysUser user = new SysUser();
        BeanUtil.copyProperties(form, user);
        String rawPassword = form.getPassword() == null || form.getPassword().isBlank()
                ? "123456" : form.getPassword();
        user.setPassword(BCrypt.hashpw(rawPassword));
        if (user.getStatus() == null) {
            user.setStatus(0);
        }
        userMapper.insert(user);
        bind(user.getId(), form.getRoleIds(), form.getPostIds());
        rbacCacheService.refresh();
    }

    @Transactional
    @Override
    public void updateUser(SysUserBo form) {
        if (form.getId() == null) {
            throw new QuickDevException("用户 ID 不能为空");
        }
        SysUser exists = requireUser(form.getId());
        checkUsernameUnique(form.getUsername(), form.getId());
        SysUser user = new SysUser();
        BeanUtil.copyProperties(form, user);
        user.setPassword(null); // 密码走独立的重置接口
        user.setDelFlag(null);
        userMapper.updateById(user);
        bind(form.getId(), form.getRoleIds(), form.getPostIds());
        if (exists.getStatus() != null && exists.getStatus() == 0
                && form.getStatus() != null && form.getStatus() != 0) {
            rbacCacheService.refresh(); // 停用的用户权限立即失效
        }
    }

    @Transactional
    @Override
    public void deleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new QuickDevException("请选择要删除的用户");
        }
        if (ids.contains(1L)) {
            throw new QuickDevException("不允许删除超级管理员");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().in(SysUserPost::getUserId, ids));
        userMapper.deleteBatchIds(ids);
        rbacCacheService.refresh();
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        requireUser(id);
        if (id == 1L && status != 0) {
            throw new QuickDevException("不允许停用超级管理员");
        }
        SysUser patch = new SysUser();
        patch.setId(id);
        patch.setStatus(status);
        userMapper.updateById(patch);
        rbacCacheService.refresh();
    }

    @Override
    public void resetPwd(Long id, String password) {
        requireUser(id);
        SysUser patch = new SysUser();
        patch.setId(id);
        patch.setPassword(BCrypt.hashpw(password));
        userMapper.updateById(patch);
    }

    /**
     * 用户的角色/岗位 ID（编辑表单回显）
     */
    @Override
    public Map<String, List<Long>> getRoleAndPostIds(Long id) {
        requireUser(id);
        Map<String, List<Long>> result = new LinkedHashMap<>();
        result.put("roleIds", userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id)).stream().map(SysUserRole::getRoleId).toList());
        result.put("postIds", userPostMapper.selectList(new LambdaQueryWrapper<SysUserPost>()
                        .eq(SysUserPost::getUserId, id)).stream().map(SysUserPost::getPostId).toList());
        return result;
    }

    /**
     * 个人中心：修改自己的基础资料
     */
    @Override
    public void updateProfile(Long userId, SysUserBo form) {
        SysUser patch = new SysUser();
        patch.setId(userId);
        patch.setNickname(form.getNickname());
        patch.setPhone(form.getPhone());
        patch.setEmail(form.getEmail());
        patch.setSex(form.getSex());
        userMapper.updateById(patch);
    }

    /**
     * 个人中心：校验旧密码后修改自己的密码
     */
    @Override
    public void updateProfilePwd(Long userId, String oldPassword, String newPassword) {
        SysUser user = requireUser(userId);
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new QuickDevException("旧密码错误");
        }
        SysUser patch = new SysUser();
        patch.setId(userId);
        patch.setPassword(BCrypt.hashpw(newPassword));
        userMapper.updateById(patch);
    }

    private void bind(Long userId, List<Long> roleIds, List<Long> postIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        if (postIds != null) {
            for (Long postId : postIds) {
                SysUserPost up = new SysUserPost();
                up.setUserId(userId);
                up.setPostId(postId);
                userPostMapper.insert(up);
            }
        }
    }

    private void checkUsernameUnique(String username, Long excludeId) {
        SysUser exists = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (exists != null && !exists.getId().equals(excludeId)) {
            throw new QuickDevException("用户名已存在: " + username);
        }
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new QuickDevException("用户不存在或已被删除");
        }
        return user;
    }
}
