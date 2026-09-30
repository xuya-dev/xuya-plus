package dev.xuya.system.service;
/**
 * UserService 接口（阿里巴巴分层规范：Service 层接口 + 实现分离）
 */
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

public interface UserService {

    void createUser(SysUserBo form);

    void updateUser(SysUserBo form);

    void deleteUsers(List<Long> ids);

    void changeStatus(Long id, Integer status);

    void resetPwd(Long id, String password);

    /**
     * 用户的角色/岗位 ID（编辑表单回显）
     */
    Map<String, List<Long>> getRoleAndPostIds(Long id);

    /**
     * 个人中心：修改自己的基础资料
     */
    void updateProfile(Long userId, SysUserBo form);

    /**
     * 个人中心：校验旧密码后修改自己的密码
     */
    void updateProfilePwd(Long userId, String oldPassword, String newPassword);
}