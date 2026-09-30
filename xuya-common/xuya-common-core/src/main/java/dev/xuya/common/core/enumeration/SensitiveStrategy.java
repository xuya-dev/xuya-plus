package dev.xuya.common.core.enumeration;

/**
 * 脱敏策略：正则匹配 + 替换模板
 */
public enum SensitiveStrategy {

    /** 手机号：138****1234 */
    PHONE("(\\d{3})\\d{4}(\\d{4})", "$1****$2"),

    /** 身份证：前 6 后 4 */
    ID_CARD("(\\d{6})\\d{8}(\\w{4})", "$1********$2"),

    /** 邮箱：保留首字符与域名 */
    EMAIL("(^\\w)[^@]*(@.*$)", "$1****$2"),

    /** 银行卡：前 4 后 4 */
    BANK_CARD("(\\d{4})\\d+(\\d{4})", "$1****$2"),

    /** 地址：保留前 9 个字符 */
    ADDRESS("(^.{9}).*(.$)", "$1********"),

    /** 自定义正则配合 replacement 使用 */
    CUSTOM(null, null);

    private final String regex;
    private final String replacement;

    SensitiveStrategy(String regex, String replacement) {
        this.regex = regex;
        this.replacement = replacement;
    }

    public String desensitize(String value) {
        if (regex == null) {
            return value;
        }
        return value.replaceAll(regex, replacement);
    }
}
