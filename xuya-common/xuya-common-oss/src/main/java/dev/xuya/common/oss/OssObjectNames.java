package dev.xuya.common.oss;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 对象名生成：yyyy/MM/uuid.suffix（防覆盖 + 按日期归档，uuid 不暴露真实路径）
 */
public final class OssObjectNames {

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy/MM");

    private OssObjectNames() {
    }

    public static String build(String originalName) {
        String suffix = "";
        int dot = originalName == null ? -1 : originalName.lastIndexOf('.');
        if (dot >= 0) {
            suffix = originalName.substring(dot);
        }
        return MONTH.format(LocalDate.now()) + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;
    }
}
