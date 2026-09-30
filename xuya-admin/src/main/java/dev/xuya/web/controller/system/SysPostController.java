package dev.xuya.web.controller.system;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.system.domain.SysPost;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位管理：全部接口（含写）由 @QuickCrud 生成，零手写。
 */
@RestController
@QuickCrud(entity = SysPost.class, permission = "sys:post",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.COUNT, CrudOp.DETAIL,
                CrudOp.SAVE, CrudOp.UPDATE, CrudOp.REMOVE, CrudOp.EXPORT})
public class SysPostController {
}
