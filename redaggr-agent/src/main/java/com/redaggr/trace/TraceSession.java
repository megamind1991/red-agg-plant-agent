package com.redaggr.trace;

import java.util.Map;
import java.util.Stack;
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
     * 远程调用的时候，调用方传过来的trace request信息
     */
    private TraceRequest traceRequest;

    /**
     * 当前node的spanId
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
     * 一个会话产生的trace栈 如果堆栈中超过1000个大小，则提示错误 ,清空堆栈
     */
    private Stack<TraceNode> traceNodes = new Stack<>();

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


    public String getNextSpanId() {
        countNumber++;
        // 用request里面的parent？还是当前的spanId ？ TODO
        // 问题是X.x.x的出现问题
        return getCurrentSpanId() + "." + countNumber;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getTraceId() {
        return this.traceRequest.getTraceId();
    }

    public void restTraceId(String traceId) {
        this.traceRequest.setTraceId(traceId);
    }

    public static String createTraceId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public Stack<TraceNode> getTraceNodes() {
        return traceNodes;
    }

    public void setTraceNodes(Stack<TraceNode> traceNodes) {
        this.traceNodes = traceNodes;
    }

    public int getCountNumber() {
        return countNumber;
    }

    public void setCountNumber(int countNumber) {
        this.countNumber = countNumber;
    }
}

