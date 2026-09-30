package dev.xuya.system.framework.log;

import dev.xuya.core.log.LogRecord;
import dev.xuya.core.log.OperationLogSink;
import dev.xuya.system.domain.SysOperLog;
import dev.xuya.system.mapper.SysOperLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 操作日志落地：@QuickLog 产生的审计记录异步写入 sys_oper_log
 * （quick-dev.log.async=true 时由框架异步线程调用，队列满丢弃并告警）。
 */
@Component
public class DbOperationLogSink implements OperationLogSink {

    private static final Logger log = LoggerFactory.getLogger(DbOperationLogSink.class);

    private final SysOperLogMapper operLogMapper;

    public DbOperationLogSink(SysOperLogMapper operLogMapper) {
        this.operLogMapper = operLogMapper;
    }

    @Override
    public void save(LogRecord record) {
        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setModule(record.getModule());
            operLog.setDescription(record.getDescription());
            operLog.setOperator(record.getOperator() == null ? null : record.getOperator().toString());
            operLog.setUri(record.getUri());
            operLog.setHttpMethod(record.getHttpMethod());
            operLog.setIp(record.getIp());
            operLog.setParams(record.getParams());
            operLog.setResultCode(record.getResultCode());
            operLog.setStatus(record.isSuccess() ? 0 : 1);
            operLog.setErrorMsg(record.getErrorMessage());
            operLog.setCostMs(record.getCostMs());
            operLog.setOperTime(record.getTimestamp());
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            // 日志落库失败不影响业务，仅告警
            log.warn("操作日志写入失败: {}", e.getMessage());
        }
    }
}
