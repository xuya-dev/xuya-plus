package dev.xuya.common.sensitive.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.xuya.common.sensitive.serialize.SensitiveJsonSerializer;
import dev.xuya.common.sensitive.enumeration.SensitiveStrategy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏：JSON 序列化时按策略遮蔽敏感字段（手机号/身份证/邮箱/银行卡/地址）
 *
 * <pre>
 * &#64;Sensitive(strategy = SensitiveStrategy.PHONE)
 * private String phone;      // 13800001234 -> 138****1234
 * </pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveJsonSerializer.class)
public @interface Sensitive {

    /** 脱敏策略 */
    SensitiveStrategy strategy();
}
