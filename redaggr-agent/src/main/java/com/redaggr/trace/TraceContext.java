package com.redaggr.trace;

/**
 * @author : 0006841 油面筋
 * @Description : trace 单例上下文<br>
 * @taskId <br>
 * @return : null
 */
public class TraceContext {


    private static final TraceContext traceContext = new TraceContext();

    /**
     * 存放当前请求线程的trace会话
     * 存入方式为
     * 1. 真实入口级别进行创建session 统一可以认为是没有携带上次session的情况下，进行创建
     * 2. 如果有上次的session信息携带, 如果为空的情况下，依赖上次session进行创建
     */
    private static final ThreadLocal<TraceSession> TRACE_SESSION_THREAD_LOCAL = new ThreadLocal<>();


    public TraceContext() {
    }

    public static TraceContext getInstance() {
        return traceContext;
    }

    public TraceSession createSession() {
        TraceRequest traceRequest = new TraceRequest();
        traceRequest.setTraceId(TraceSession.createTraceId());
        TraceSession traceSession = new TraceSession(this, traceRequest);
        TRACE_SESSION_THREAD_LOCAL.set(traceSession);
        return traceSession;
    }
}

