package com.redaggr.trace;

/**
 * @author : 0006841 油面筋
 * @Description : 隔离session传输会话信息的时候必要<br>
 * @taskId <br>
 * @return : null
 */
public class TraceRequest {

    /**
     * 一条请求链路的唯一id
     */
    private String traceId;

    /**
     * 当前span的父级spanId
     */
    private String parentSpanId;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }
}
