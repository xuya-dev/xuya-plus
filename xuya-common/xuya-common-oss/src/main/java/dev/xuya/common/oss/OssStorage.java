package dev.xuya.common.oss;

import java.io.InputStream;

/**
 * 对象存储 SPI：按 sys_oss_config.config_key 路由（local / S3 协议：MinIO、阿里云 OSS、腾讯云 COS）
 */
public interface OssStorage {

    /** 存储标识（对应 config_key） */
    String configKey();

    /**
     * 上传文件
     *
     * @param originalName 原始文件名（取后缀生成对象名）
     * @param data         文件内容
     * @return 访问地址（公开桶直连；私有桶配合下载接口）
     */
    UploadResult upload(String originalName, byte[] data);

    /** 删除对象 */
    void delete(String objectName);

    /** 读取对象内容（调用方负责关闭流） */
    InputStream download(String objectName);

    /** 上传结果：对象名 + 访问地址 + 大小 */
    record UploadResult(String objectName, String url, long size) {
    }
}
