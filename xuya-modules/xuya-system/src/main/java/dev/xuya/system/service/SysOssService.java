package dev.xuya.system.service;

import dev.xuya.system.domain.SysOss;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 *
 * @see SysOssServiceImpl
 */
public interface SysOssService {

    /**
     * 上传文件到当前使用中的对象存储
     */
    SysOss upload(MultipartFile file);

    /**
     * 按文件 ID 查询记录
     */
    SysOss getById(Long id);

    /**
     * 批量删除（对象本体 + 记录）
     */
    void deleteOssList(List<Long> ids);
}
