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
 * 部门写服务：维护 ancestors 祖级路径（含子树级联重算）
 */
@Service
public class DeptServiceImpl implements DeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    public DeptServiceImpl(SysDeptMapper deptMapper, SysUserMapper userMapper) {
        this.deptMapper = deptMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public void createDept(SysDept dept) {
        SysDept parent = dept.getParentId() == null ? null : deptMapper.selectById(dept.getParentId());
        if (dept.getParentId() != null && parent == null) {
            throw new QuickDevException("上级部门不存在");
        }
        dept.setAncestors(parent == null ? "0" : parent.getAncestors() + "," + parent.getId());
        if (dept.getStatus() == null) {
            dept.setStatus(0);
        }
        deptMapper.insert(dept);
    }

    @Transactional
    @Override
    public void updateDept(SysDept dept) {
        if (dept.getId() == null) {
            throw new QuickDevException("部门 ID 不能为空");
        }
        SysDept exists = requireDept(dept.getId());
        if (dept.getId().equals(dept.getParentId())) {
            throw new QuickDevException("上级部门不能为自己");
        }
        String newAncestors;
        if (dept.getParentId() == null) {
            newAncestors = exists.getAncestors();
        } else {
            SysDept parent = dept.getParentId() == 0L ? null : deptMapper.selectById(dept.getParentId());
            if (dept.getParentId() != 0L && parent == null) {
                throw new QuickDevException("上级部门不存在");
            }
            newAncestors = parent == null ? "0" : parent.getAncestors() + "," + parent.getId();
        }
        dept.setAncestors(newAncestors);
        dept.setDelFlag(null);
        deptMapper.updateById(dept);
        if (!newAncestors.equals(exists.getAncestors())) {
            // 祖级路径变化：级联重算整棵子树
            List<SysDept> all = deptMapper.selectList(null);
            recomputeChildren(dept.getId(), all, newAncestors);
        }
    }

    @Transactional
    @Override
    public void deleteDept(Long id) {
        requireDept(id);
        Long children = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, id));
        if (children != null && children > 0) {
            throw new QuickDevException("存在下级部门，不允许删除");
        }
        Long users = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, id));
        if (users != null && users > 0) {
            throw new QuickDevException("部门下存在用户，不允许删除");
        }
        deptMapper.deleteById(id);
    }

    private void recomputeChildren(Long deptId, List<SysDept> all, String ancestors) {
        for (SysDept child : all) {
            if (deptId.equals(child.getParentId())) {
                SysDept patch = new SysDept();
                patch.setId(child.getId());
                patch.setAncestors(ancestors + "," + deptId);
                deptMapper.updateById(patch);
                recomputeChildren(child.getId(), all, patch.getAncestors());
            }
        }
    }

    private SysDept requireDept(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new QuickDevException("部门不存在或已被删除");
        }
        return dept;
    }
}
