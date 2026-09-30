package dev.xuya.web.controller.system;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import dev.xuya.system.domain.SysOss;
import dev.xuya.system.framework.oss.OssFactory;
import dev.xuya.system.service.SysOssService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;

/**
 * 文件管理：列表/详情由 @QuickCrud 生成；上传/下载/删除走业务逻辑（对象本体与记录联动）
 */
@RestController
@RequestMapping("/system/oss")
@RequiredArgsConstructor
@QuickCrud(entity = SysOss.class, permission = "sys:oss",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.DETAIL})
public class SysOssController {

    private final SysOssService ossService;
    private final OssFactory ossFactory;

    /**
     * 上传文件（到当前使用中的对象存储）
     */
    @RequiresPerm("sys:oss:upload")
    @QuickLog(module = "文件管理", description = "上传文件")
    @PostMapping("/upload")
    public R<SysOss> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(ossService.upload(file));
    }

    /**
     * 下载文件（流式输出，私有桶同样可用）
     */
    @RequiresPerm("sys:oss:download")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) throws Exception {
        SysOss oss = requireOss(id);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + oss.getOriginalName() + "\"");
        if (oss.getFileSize() != null) {
            response.setContentLengthLong(oss.getFileSize());
        }
        try (InputStream in = ossFactory.getByKey(oss.getService()).download(oss.getFileName())) {
            in.transferTo(response.getOutputStream());
        }
    }

    /**
     * 本地存储资源读取（对象名为 uuid 不暴露路径；供 <img>/预览直连）
     */
    @GetMapping("/local/{date}/{name}")
    public void local(@PathVariable String date, @PathVariable String name,
                      HttpServletResponse response) throws Exception {
        String objectName = date + "/" + name;
        String nameOnly = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        response.setContentType(contentTypeOf(nameOnly));
        try (InputStream in = ossFactory.getByKey("local").download(objectName)) {
            in.transferTo(response.getOutputStream());
        }
    }

    /**
     * 批量删除（对象本体 + 记录）
     */
    @RequiresPerm("sys:oss:remove")
    @QuickLog(module = "文件管理", description = "删除文件")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        ossService.deleteOssList(Arrays.stream(ids.split(",")).map(String::trim)
                .map(Long::valueOf).toList());
        return R.ok();
    }

    private SysOss requireOss(Long id) {
        SysOss oss = ossService.getById(id);
        if (oss == null) {
            throw new QuickDevException("文件不存在或已被删除");
        }
        return oss;
    }

    private String contentTypeOf(String suffix) {
        return switch (suffix) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}
