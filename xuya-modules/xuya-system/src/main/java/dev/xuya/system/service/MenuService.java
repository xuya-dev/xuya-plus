package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.domain.vo.RouterVo;
import dev.xuya.system.framework.security.RbacCacheService;
import dev.xuya.system.domain.SysMenu;
import dev.xuya.system.domain.SysRoleMenu;
import dev.xuya.system.mapper.SysMenuMapper;
import dev.xuya.system.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * MenuService 接口（阿里巴巴分层规范：Service 层接口 + 实现分离）
 */
public interface MenuService {

    void createMenu(SysMenu menu);

    void updateMenu(SysMenu menu);

    void deleteMenu(Long id);

    /**
     * 将扁平菜单构建为 RuoYi 风格动态路由（仅 M 目录 / C 菜单，F 按钮不参与路由）
     */
    List<RouterVo> buildRouters(List<SysMenu> menus);
}