package com.redaggr.trace;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : 0006841 油面筋
 * @Description : 链路事务会话<br>
 * @taskId <br>
 * @return : null
 */
public class TraceSession {

    /**
     * 入口trace信息
     */
    private TraceRequest traceRequest;

    /**
     * 当前一次追踪的spanId
     */
    private String spanId;

    /**
     * 用于记录一些额外信息
     */
    private final Map<String, Object> tags = new ConcurrentHashMap<String, Object>();

    /**
     * 为了方便内部存储一份context
     */
    private TraceContext traceContext;


    /**
     * 当前trace中span的计数器 X.X.X.X
     */
    private int countNumber = 0;
    /**
     * @param traceContext : trace上下文
     * @param traceRequest : 上次trace信息
     * @return : null
     * @Description : 功能说明<br>
     * @author : 0006841 油面筋
     * @taskId <br>
     */
    public TraceSession(TraceContext traceContext, TraceRequest traceRequest) {
        this.traceRequest = traceRequest;
        this.traceContext = traceContext;
    }

    /**
     * @return : null
     * @Description : 获取当前spanId<br>
     * @author : 0006841 油面筋
     * @taskId <br>
     */
    public String getCurrentSpanId() {
        return spanId == null ? "0" : spanId;
    }


    public String getNextRpcId() {
        countNumber++;
        spanId = traceRequest.getParentSpanId() + "." + countNumber;
        return spanId;
    }

    public static String createTraceId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}

