package dev.xuya.common.core.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dev.xuya.common.core.annotation.Sensitive;
import dev.xuya.common.core.enumeration.SensitiveStrategy;

import java.io.IOException;

/**
 * 脱敏序列化器：按 @Sensitive 声明的策略遮蔽字符串字段（非字符串原样输出）
 */
public class SensitiveJsonSerializer extends StdSerializer<Object> implements ContextualSerializer {

    private SensitiveStrategy strategy;

    public SensitiveJsonSerializer() {
        super(Object.class);
    }

    private SensitiveJsonSerializer(SensitiveStrategy strategy) {
        super(Object.class);
        this.strategy = strategy;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value instanceof String text && strategy != null) {
            gen.writeString(strategy.desensitize(text));
        } else {
            gen.writeObject(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) {
        if (property != null) {
            Sensitive anno = property.getAnnotation(Sensitive.class);
            if (anno != null) {
                return new SensitiveJsonSerializer(anno.strategy());
            }
        }
        return this;
    }
}
