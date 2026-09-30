package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.domain.SysDept;
import dev.xuya.system.domain.SysUser;
import dev.xuya.system.mapper.SysDeptMapper;
import dev.xuya.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
/**
 * DeptService 接口（阿里巴巴分层规范：Service 层接口 + 实现分离）
 */
public interface DeptService {

    void createDept(SysDept dept);

    void updateDept(SysDept dept);

    void deleteDept(Long id);
}