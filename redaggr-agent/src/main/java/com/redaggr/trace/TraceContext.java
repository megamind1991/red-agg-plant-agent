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
     * 3. 想办法清除session TODO 办法是在每个确定的入口进行清除
     * 4. 如果是子线程的话 如何清除 TODO 依靠父线程的remove?
     */
    public static final InheritableThreadLocal<TraceSession> TRACE_SESSION_THREAD_LOCAL = new InheritableThreadLocal<>();


    public TraceContext() {
    }

    public static TraceContext getInstance() {
        return traceContext;
    }

    public TraceSession createSession() {
        if ("main".equals(Thread.currentThread().getName())) {
            // 如果是main线程不创建session , 直接抛出异常 因为main线程如果有session所有的子线程将出现问题
            throw new RuntimeException("main线程不创建trace会话");
        }
        TraceRequest traceRequest = new TraceRequest();
        traceRequest.setTraceId(TraceSession.createTraceId());
        TraceSession traceSession = new TraceSession(this, traceRequest);
        TRACE_SESSION_THREAD_LOCAL.set(traceSession);
        return traceSession;
    }

    /**
     * 跨服务调用的时候需要使用此方法
     * @param traceRequest
     * @return
     */
    public TraceSession createSessionByRequest(TraceRequest traceRequest) {
        TraceSession traceSession = new TraceSession(this, traceRequest);
        TRACE_SESSION_THREAD_LOCAL.set(traceSession);
        return traceSession;
    }

    public TraceSession getCurrentSession() {
        TraceSession currentSession = TRACE_SESSION_THREAD_LOCAL.get();
        if (currentSession != null) {
            return currentSession;
        }
        return createSession();
    }
}

