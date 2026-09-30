package dev.xuya.common.oss;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;

/**
 * S3 协议存储（AWS SDK v2）：兼容 MinIO / 阿里云 OSS / 腾讯云 COS 等。
 * MinIO 需开启 path-style 访问。
 */
public class S3Storage implements OssStorage {

    private final String configKey;
    private final String bucket;
    private final String endpoint;
    private final String domain;
    private final S3Client client;

    public S3Storage(String configKey, String accessKey, String secretKey, String bucket,
                     String endpoint, String domain, String region) {
        this.configKey = configKey;
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.domain = domain;
        this.client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region == null || region.isBlank() ? "us-east-1" : region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Override
    public String configKey() {
        return configKey;
    }

    @Override
    public UploadResult upload(String originalName, byte[] data) {
        String objectName = OssObjectNames.build(originalName);
        client.putObject(PutObjectRequest.builder()
                .bucket(bucket).key(objectName).build(), RequestBody.fromBytes(data));
        String base = domain == null || domain.isBlank() ? endpoint + "/" + bucket : domain;
        return new UploadResult(objectName, base + "/" + objectName, data.length);
    }

    @Override
    public void delete(String objectName) {
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectName).build());
    }

    @Override
    public InputStream download(String objectName) {
        return client.getObject(GetObjectRequest.builder().bucket(bucket).key(objectName).build());
    }
}
