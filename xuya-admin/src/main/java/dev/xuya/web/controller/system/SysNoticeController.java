package dev.xuya.web.controller.system;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.system.domain.SysNotice;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知公告管理：全部接口由 @QuickCrud 生成，零手写。
 */
@RestController
@QuickCrud(entity = SysNotice.class, permission = "sys:notice",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.COUNT, CrudOp.DETAIL,
                CrudOp.SAVE, CrudOp.UPDATE, CrudOp.REMOVE})
public class SysNoticeController {
}
