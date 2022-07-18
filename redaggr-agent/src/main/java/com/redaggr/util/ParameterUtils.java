package com.redaggr.util;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.redaggr.servletAgent.ServletResponseProxy;
import com.redaggr.trace.TraceContext;
import com.redaggr.trace.TraceNode;
import com.redaggr.trace.TraceSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author : 0006841 油面筋
 * @Description : 这个方法应该是获取到对 象之后,异步解析数据往文件中写入,不影响代码执行 TODO<br>
 * @taskId <br>
 * @return : null
 */
public class ParameterUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParameterUtils.class);

    private static final DateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Deprecated
    public static void printValueOnStack(boolean value) {
        System.out.println("    " + value);
    }

    @Deprecated
    public static void printValueOnStack(byte value) {
        System.out.println("    " + value);
    }

    @Deprecated
    public static void printValueOnStack(char value) {
        System.out.println("    " + value);
    }

    @Deprecated
    public static void printValueOnStack(short value) {
        System.out.println("    " + value);
    }

    @Deprecated
    public static void printValueOnStack(int value) {
        System.out.println("    " + value);
    }

    @Deprecated
    public static void printValueOnStack(float value) {
        System.out.println("    " + value);
    }

    @Deprecated
    public static void printValueOnStack(long value) {
        System.out.println("    " + value);
    }

    @Deprecated
    public static void printValueOnStack(double value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack4Return(boolean value) {
        printValueOnStack4Return("" + value);
    }

    public static void printValueOnStack4Return(byte value) {
        printValueOnStack4Return("" + value);
    }

    public static void printValueOnStack4Return(char value) {
        printValueOnStack4Return("" + value);
    }

    public static void printValueOnStack4Return(short value) {
        printValueOnStack4Return("" + value);
    }

    public static void printValueOnStack4Return(int value) {
        printValueOnStack4Return("" + value);
    }

    public static void printValueOnStack4Return(float value) {
        printValueOnStack4Return("" + value);
    }

    public static void printValueOnStack4Return(long value) {
        printValueOnStack4Return("" + value);
    }

    public static void printValueOnStack4Return(double value) {
        printValueOnStack4Return("" + value);
    }

    public static void setDubboUrl(Object value) {
        // TODO 在这边把dubbo中attachement中带入traceRequest 在生产者那边可以使用
        try {
            TraceSession currentSession = TraceContext.getInstance().getCurrentSession();
            if (currentSession.getTraceNodes().size() <= 0) {
                return;
            }
            TraceNode currentNode = currentSession.getTraceNodes().peek();
            currentNode.setNodeType("dubbo");
            if (value instanceof com.alibaba.dubbo.common.URL) {
                com.alibaba.dubbo.common.URL url = (com.alibaba.dubbo.common.URL) value;
                currentNode.setServicePath(url.getServiceKey());
            } else if (value instanceof org.apache.dubbo.common.URL) {
                org.apache.dubbo.common.URL url = (org.apache.dubbo.common.URL) value;
                currentNode.setServicePath(url.getServiceKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printValueOnStack4Return(Object value) {
        try {
            // 从node栈中获取当前node
            Stack<TraceNode> traceNodes = TraceContext.getInstance().getCurrentSession().getTraceNodes();
            TraceNode node = traceNodes.pop();
            node.setEndTime(System.currentTimeMillis());
            // 把上一次执行的node spanId 重新赋值给session
            TraceContext.getInstance().getCurrentSession().setSpanId(traceNodes.size() > 0 ? traceNodes.peek().getSpanId() : "0");

            // 打印设置返回值
            TraceNode traceNode = printValueOnStackOutParamV2(value, node);
            logger.info(traceNode == null ? null : traceNode.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printValueOnStackInParamByArr(Object... params) {
        try {
            // 打印入参的时候创建node
            TraceNode node = new TraceNode();
            node.setThreadId(Thread.currentThread().getName());
            node.setBeginTime(System.currentTimeMillis());
            node.setTraceId(TraceContext.getInstance().getCurrentSession().getTraceId());
            // 如果堆栈中有值的话node的spanId为nextSpanId
            // 如果是异步线程 或者是mq消费的时候 currentSpanId = request.spanId的next 传输的过程中spanId和countNumber都要进行传递
            String currentSpanId;
//            if (!"0".equals(TraceContext.getInstance().getCurrentSession().getCurrentSpanId())) {
                currentSpanId = TraceContext.getInstance().getCurrentSession().getNextSpanId();
//            } else {
//                currentSpanId = TraceContext.getInstance().getCurrentSession().getTraceNodes().size() > 0 ?
//                        TraceContext.getInstance().getCurrentSession().getNextSpanId()
//                        : TraceContext.getInstance().getCurrentSession().getCurrentSpanId();
//            }
            node.setSpanId(currentSpanId);
            // session的当前spanId也为这个值
            TraceContext.getInstance().getCurrentSession().setSpanId(currentSpanId);
            // 向堆栈中加入当前node TODO 这边需要检查是否达到栈中的约定最大堆栈深度
            TraceContext.getInstance().getCurrentSession().getTraceNodes().push(node);

            // 为node设置业务参数信息 下面的可能需要逻辑异步化 TODO
            for (Object param : params) {
                if (param != null) {
                    // 入参整合
                    printValueOnStackInParamV2(param, node);
                }
            }
            // 输出入参
//            logger.info(node.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printValueOnStackOutParamByArr(Object... params) {
        try {
            // 从node栈中获取当前node
            Stack<TraceNode> traceNodes = TraceContext.getInstance().getCurrentSession().getTraceNodes();
            TraceNode node = traceNodes.pop();
            node.setEndTime(System.currentTimeMillis());
            // 把上一次执行的node spanId 重新赋值给session
            TraceContext.getInstance().getCurrentSession().setSpanId(traceNodes.size() > 0 ? traceNodes.peek().getSpanId() : "0");

            // 设置node的返回参数 TODO 可能需要异步
            for (Object param : params) {
                if (param != null) {
                    // 入参整合
                    printValueOnStackOutParamV2(param, node);
                }
            }
            // 输出入参
            logger.info(node.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printValueOnStackInParamV2(Object value, TraceNode node) {
        if (value == null) {
            node.setInParam(null);
        } else if (value instanceof String) {
            node.setInParam(value.toString());
        } else if (value instanceof Date) {
            node.setInParam(fm.format(value));
        } else if (value instanceof char[]) {
            node.setInParam(Arrays.toString((char[]) value));
        } else if (value instanceof byte[]) {
            node.setInParam(new String((byte[]) value, StandardCharsets.UTF_8));
        } else if (value instanceof HttpServletRequest) {
            // web servlet 入参打印
            // 后缀匹配资源文件不打印请求信息
            HttpServletRequest request = (HttpServletRequest) value;
            String[] ignoreSuffix = {".css", ".js", ".gif", ".jpg", ".png", ".svg"};
            for (String suffix : ignoreSuffix) {
                if (request.getRequestURI().endsWith(suffix)) {
                    return;
                }
            }
            // 输出请求入参
            handlerDoDispatchMethod(request, node);
        } else if (value instanceof ServletResponseProxy) {
            // web servlet 出参打印
            // 后缀匹配资源文件不打印请求信息
            ServletResponseProxy response = (ServletResponseProxy) value;
            node.setInParam(getResponseValue(null, response));
        } else if (value instanceof com.alibaba.dubbo.rpc.RpcInvocation) {
            com.alibaba.dubbo.rpc.RpcInvocation request = (com.alibaba.dubbo.rpc.RpcInvocation) value;
            String traceId = request.getAttachment("traceId");
            String spanId = request.getAttachment("spanId");
            String countNumber = request.getAttachment("countNumber");
            if (traceId == null || "".equals(traceId)) {
                request.setAttachment("traceId", node.getTraceId());
            } else {
                // 重新设置当前节点的traceId
                node.setTraceId(traceId);
                // 重新设置当前session的traceId
                TraceContext.getInstance().getCurrentSession().restTraceId(traceId);
            }
            if (spanId == null || "".equals(spanId)) {
                request.setAttachment("spanId", TraceContext.getInstance().getCurrentSession().getCurrentSpanId());
                request.setAttachment("countNumber", String.valueOf(TraceContext.getInstance().getCurrentSession().getCountNumber()));
            } else {
                // 重新设置当前节点的spanId
                node.setSpanId(spanId);
                // 重新设置当前session的spanId
                TraceContext.getInstance().getCurrentSession().setSpanId(spanId);
                // 重新设置当前session的countNumber
                TraceContext.getInstance().getCurrentSession().setCountNumber(Integer.parseInt(countNumber));
            }

            node.setServiceName(request.getAttachments().get("path") + "." + request.getMethodName() + "#" + JSONObject.toJSONString(request.getParameterTypes()));
            node.setInParam(JSONObject.toJSONString(request.getArguments()));
            node.setNodeType("dubbo");
        } else if (value instanceof org.apache.dubbo.rpc.RpcInvocation) {
            org.apache.dubbo.rpc.RpcInvocation request = (org.apache.dubbo.rpc.RpcInvocation) value;
            String traceId = request.getAttachment("traceId");
            String spanId = request.getAttachment("spanId");
            String countNumber = request.getAttachment("countNumber");
            if (traceId == null || "".equals(traceId)) {
                request.setAttachment("traceId", node.getTraceId());
            } else {
                // 重新设置当前节点的traceId
                node.setTraceId(traceId);
                // 重新设置当前session的traceId
                TraceContext.getInstance().getCurrentSession().restTraceId(traceId);
            }
            if (spanId == null || "".equals(spanId)) {
                request.setAttachment("spanId", TraceContext.getInstance().getCurrentSession().getCurrentSpanId());
                request.setAttachment("countNumber", String.valueOf(TraceContext.getInstance().getCurrentSession().getCountNumber()));
            } else {
                // 重新设置当前节点的spanId
                node.setSpanId(spanId);
                // 重新设置当前session的spanId
                TraceContext.getInstance().getCurrentSession().setSpanId(spanId);
                // 重新设置当前session的countNumber
                TraceContext.getInstance().getCurrentSession().setCountNumber(Integer.parseInt(countNumber));
            }
            node.setServiceName(request.getAttachments().get("path") + "." + request.getMethodName() + "#" + JSONObject.toJSONString(request.getParameterTypes()));
            node.setInParam(JSONObject.toJSONString(request.getArguments()));
            node.setNodeType("dubbo");
        } else if (value instanceof java.lang.reflect.Method) {
            java.lang.reflect.Method method = (java.lang.reflect.Method) value;
            node.setServiceName(method.getDeclaringClass().getName() + "#" + method.getName());
//            node.setInParam(); TODO xxl的入参是从xxl工具栏中获取的
            node.setNodeType("xxlJob");
        } else if (value instanceof org.springframework.amqp.core.Message) {
            node.setNodeType("rabbitmq");
            org.springframework.amqp.core.Message msg = (org.springframework.amqp.core.Message) value;
            String msgBody = new String(msg.getBody(), StandardCharsets.UTF_8);
            String traceId = msg.getMessageProperties().getHeader("traceId");
            String spanId = msg.getMessageProperties().getHeader("spanId");
            Integer countNumber = msg.getMessageProperties().getHeader("countNumber");
            if (traceId == null || "".equals(traceId)) {
                msg.getMessageProperties().setHeader("traceId", node.getTraceId());
            } else {
                // 重新设置当前节点的traceId
                node.setTraceId(traceId);
                // 重新设置当前session的traceId
                TraceContext.getInstance().getCurrentSession().restTraceId(traceId);
            }
            if (spanId == null || "".equals(spanId)) {
                msg.getMessageProperties().setHeader("spanId", TraceContext.getInstance().getCurrentSession().getCurrentSpanId());
                msg.getMessageProperties().setHeader("countNumber", TraceContext.getInstance().getCurrentSession().getCountNumber());
            } else {
                // 重新设置当前节点的spanId
                node.setSpanId(spanId);
                // 重新设置当前session的spanId
                TraceContext.getInstance().getCurrentSession().setSpanId(spanId);
                // 重新设置当前session的countNumber
                TraceContext.getInstance().getCurrentSession().setCountNumber(countNumber);
            }
            node.setInParam(msgBody);
            node.setInParam(traceId);
            node.setInParam(JSONObject.toJSONString(msg.getMessageProperties()));
        } else if (value instanceof Channel) {
            // 不打印
        } else if (value instanceof com.alibaba.dubbo.rpc.Invoker) {
            // 不打印
        } else if (value instanceof org.apache.dubbo.rpc.Invoker) {
            // 不打印
        } else {
            node.setInParam(JSONObject.toJSONString(value));
        }
    }

    public static TraceNode printValueOnStackOutParamV2(Object value, TraceNode node) {
        if (value == null) {
            node.setOutParam(null);
        } else if (value instanceof String) {
            node.setOutParam(value.toString());
        } else if (value instanceof Date) {
            node.setOutParam(fm.format(value));
        } else if (value instanceof char[]) {
            node.setOutParam(Arrays.toString((char[]) value));
        } else if (value instanceof HttpServletRequest) {
            // web servlet 入参打印
            // 后缀匹配资源文件不打印请求信息
            HttpServletRequest request = (HttpServletRequest) value;
            String[] ignoreSuffix = {".css", ".js", ".gif", ".jpg", ".png", ".svg"};
            for (String suffix : ignoreSuffix) {
                if (request.getRequestURI().endsWith(suffix)) {
                    return null;
                }
            }
            // 输出请求入参
            handlerDoDispatchMethod(request, node);
            node.setOutParam(JSONObject.toJSONString(buildRequestParam(request)));
        } else if (value instanceof ServletResponseProxy) {
            // web servlet 出参打印
            // 后缀匹配资源文件不打印请求信息
            ServletResponseProxy response = (ServletResponseProxy) value;
            node.setOutParam(getResponseValue(null, response));
        } else if (value instanceof org.apache.dubbo.rpc.Result) {
            // web servlet 出参打印
            // 后缀匹配资源文件不打印请求信息
            org.apache.dubbo.rpc.Result response = (org.apache.dubbo.rpc.Result) value;
            node.setOutParam(JSONObject.toJSONString(response.getValue()));
        } else if (value instanceof com.alibaba.dubbo.rpc.Result) {
            // web servlet 出参打印
            // 后缀匹配资源文件不打印请求信息
            com.alibaba.dubbo.rpc.Result response = (com.alibaba.dubbo.rpc.Result) value;
            node.setOutParam(JSONObject.toJSONString(response.getValue()));
        } else {
            node.setOutParam(JSONObject.toJSONString(value));
        }

        return node;
    }

    @Deprecated
    public static void printValueOnStack(Object value) {
        if (value == null) {
            logger.info("    " + value);
        } else if (value instanceof String) {
            logger.info("    " + value);
        } else if (value instanceof Date) {
            logger.info("    " + fm.format(value));
        } else if (value instanceof char[]) {
            logger.info("    " + Arrays.toString((char[]) value));
        } else if (value instanceof HttpServletRequest) {
            // web servlet 入参打印
            // 后缀匹配资源文件不打印请求信息
            HttpServletRequest request = (HttpServletRequest) value;
            String[] ignoreSuffix = {".css", ".js", ".gif", ".jpg", ".png", ".svg"};
            for (String suffix : ignoreSuffix) {
                if (request.getRequestURI().endsWith(suffix)) {
                    return;
                }
            }
            // 输出请求入参
            logger.info("HttpServletRequest    " + handlerDoDispatchMethod(request, null));
        } else if (value instanceof ServletResponseProxy) {
            // web servlet 出参打印
            // 后缀匹配资源文件不打印请求信息
            ServletResponseProxy response = (ServletResponseProxy) value;
            TraceNode node = new TraceNode();
            node.setOutParam(getResponseValue(null, response));
            node.setEndTime(System.currentTimeMillis());
            logger.info("HttpServletResponse    " + node);
        } else {
            logger.info("    " + value.getClass() + ": " + value.toString());
        }
    }

    @Deprecated
    public static void printText(String str) {
        System.out.println(str);
    }

    public static void cleanSession() {
        try {
            TraceContext.TRACE_SESSION_THREAD_LOCAL.remove();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setClassInfo(String str) {
        try {
            TraceSession currentSession = TraceContext.getInstance().getCurrentSession();
            if (currentSession.getTraceNodes().size() > 0) {
                currentSession.getTraceNodes().peek().setServiceName(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void output(PrintStream printStream, int val) {
        printStream.println("ParameterUtils: " + val);
    }


    protected static TraceNode handlerDoDispatchMethod(HttpServletRequest request, TraceNode node) {
        if (node == null) {
            node = new TraceNode();
        }
        node.setNodeType("http");
        node.setAddressIp(NetUtils.getLocalHost());
        node.setFromIp(HttpUtils.getIp(request));
        node.setServicePath(request.getRequestURL().toString());
        node.setInParam(JSONObject.toJSONString(buildRequestParam(request)));
        return node;
    }

    private static HashMap<String, Serializable> buildRequestParam(HttpServletRequest request) {
        String[] values;
        HashMap<String, Serializable> params = new HashMap<>();
        for (String name : Collections.list(request.getParameterNames())) {
            values = request.getParameterValues(name);
            if (values.length == 1) {
                params.put(name, values[0]);
            } else {
                params.put(name, values);
            }
        }
        return params;
    }

    private static String getResponseValue(HttpServletRequest request, ServletResponseProxy responseProxy) {
        String result = new String();
        HttpServletResponse response = (HttpServletResponse) responseProxy.getResponse();
        String contentType = responseProxy.getContentType();
        contentType = contentType == null ? "" : contentType;
        if (contentType.contains("json")) {
            StringWriter copyWriter = responseProxy.getCopyWriter();
            ByteArrayOutputStream copyOutput = responseProxy.getCopyOutput();
            if (copyWriter != null) {
                result = copyWriter.toString();
            } else if (copyOutput != null) {
                try {
                    result = response.getCharacterEncoding() != null ?
                            copyOutput.toString(response.getCharacterEncoding()) : copyOutput.toString();
                } catch (UnsupportedEncodingException e) {
//                    logger.error("获取http输出转码异常:"+request.getServletPath(), e);
                }
            }
            responseProxy.clearCopyOut();
        } else if (contentType.contains("html")) {
//            Enumeration<String> names = request.getAttributeNames();
//            Map<String, Serializable> values = new HashMap<>();
//            for (int i = 0; names.hasMoreElements(); i++) {
//                String name = names.nextElement();
//                Object attr = request.getAttribute(name);
//                if (attr instanceof Serializable) {
//                    values.put(name, (Serializable) attr);
//                }
//            }
//            result = values.size() > 0 ? JSONObject.toJSONString(values) : "";
            result = "html";
        }
        return result;

    }
}
