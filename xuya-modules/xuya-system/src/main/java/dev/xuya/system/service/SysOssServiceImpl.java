package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.domain.SysOss;
import dev.xuya.system.framework.oss.OssFactory;
import dev.xuya.common.oss.OssStorage;
import dev.xuya.system.mapper.SysOssMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件服务实现：上传到当前使用中的对象存储并落记录；
 * 删除/读取按文件的存储标识路由（历史文件不随配置切换漂移）
 */
@Service
@RequiredArgsConstructor
public class SysOssServiceImpl implements SysOssService {

    private final SysOssMapper ossMapper;
    private final OssFactory ossFactory;

    @Override
    public SysOss upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new QuickDevException("上传文件不能为空");
        }
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new QuickDevException("读取上传文件失败: " + e.getMessage(), e);
        }
        String originalName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();

        OssStorage storage = ossFactory.getActive();
        OssStorage.UploadResult result = storage.upload(originalName, data);

        SysOss oss = new SysOss();
        oss.setFileName(result.objectName());
        oss.setOriginalName(originalName);
        oss.setFileSuffix(suffixOf(originalName));
        oss.setUrl(result.url());
        oss.setService(storage.configKey());
        oss.setFileSize(result.size());
        ossMapper.insert(oss);
        return oss;
    }

    @Override
    public SysOss getById(Long id) {
        return ossMapper.selectById(id);
    }

    @Override
    public void deleteOssList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new QuickDevException("请选择要删除的文件");
        }
        for (SysOss oss : ossMapper.selectBatchIds(ids)) {
            try {
                // 对象本体删除失败（如已手工清理）不阻断记录删除
                ossFactory.getByKey(oss.getService()).delete(oss.getFileName());
            } catch (Exception ignored) {
            }
        }
        ossMapper.deleteBatchIds(ids);
    }

    private String suffixOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : "";
    }
}
