package dev.xuya.common.oss;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地磁盘存储：文件落在 storage_path（默认 ./uploads）下按日期归档；
 * 读取经 /system/oss/local/** 资源端点输出（对象名为 uuid，不暴露真实路径）。
 */
public class LocalStorage implements OssStorage {

    private final String baseDir;

    public LocalStorage(String baseDir) {
        this.baseDir = baseDir == null || baseDir.isBlank() ? "./uploads" : baseDir;
    }

    @Override
    public String configKey() {
        return "local";
    }

    @Override
    public UploadResult upload(String originalName, byte[] data) {
        String objectName = OssObjectNames.build(originalName);
        Path target = Path.of(baseDir, objectName);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, data);
        } catch (IOException e) {
            throw new IllegalStateException("本地存储写入失败: " + e.getMessage(), e);
        }
        return new UploadResult(objectName, "/system/oss/local/" + objectName, data.length);
    }

    @Override
    public void delete(String objectName) {
        try {
            Files.deleteIfExists(Path.of(baseDir, objectName));
        } catch (IOException e) {
            throw new IllegalStateException("本地存储删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String objectName) {
        try {
            return Files.newInputStream(Path.of(baseDir, objectName));
        } catch (IOException e) {
            throw new IllegalStateException("本地存储读取失败: " + e.getMessage(), e);
        }
    }
}
