package com.xuya.plus.framework.log;

import com.xuya.plus.system.entity.SysLogininfor;
import com.xuya.plus.system.mapper.SysLogininforMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 登录日志：登录成功/失败统一落 sys_logininfor
 */
@Service
public class LoginLogService {

    private static final Logger log = LoggerFactory.getLogger(LoginLogService.class);

    private final SysLogininforMapper logininforMapper;

    public LoginLogService(SysLogininforMapper logininforMapper) {
        this.logininforMapper = logininforMapper;
    }

    public void record(String username, String ip, String msg, boolean success) {
        try {
            SysLogininfor info = new SysLogininfor();
            info.setUsername(username);
            info.setIp(ip);
            info.setMsg(msg);
            info.setStatus(success ? 0 : 1);
            info.setLoginTime(java.time.LocalDateTime.now());
            logininforMapper.insert(info);
        } catch (Exception e) {
            log.warn("登录日志写入失败: {}", e.getMessage());
        }
    }
}
