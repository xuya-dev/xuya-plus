package dev.xuya.common.sms;

import java.util.List;
import java.util.Map;

/**
 * 短信发送 SPI：用户实现本接口对接具体运营商，注册为 Spring Bean 即可。
 */
public interface SmsService {

    boolean send(List<String> phoneNumbers, String templateCode, Map<String, String> params);

    String provider();
}
