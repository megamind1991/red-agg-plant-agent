package com.redaggr.util;

import com.alibaba.fastjson.JSONObject;
import com.redaggr.logger.Logger;
import com.redaggr.logger.LoggerFactory;
import com.redaggr.servletAgent.ServletResponseProxy;
import com.redaggr.trace.TraceNode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * @author : 0006841 油面筋
 * @Description : 这个方法应该是获取到对 象之后,异步解析数据往文件中写入,不影响代码执行 TODO<br>
 * @taskId <br>
 * @return : null
 */
public class ParameterUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParameterUtils.class);

    private static final DateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void printValueOnStack(boolean value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(byte value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(char value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(short value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(int value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(float value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(long value) {
        System.out.println("    " + value);
    }

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


    public static void printValueOnStackInParamByArr(Object... params) {
        TraceNode node = new TraceNode();
        for (Object param : params) {
            if (param != null) {
                // 入参整合
                printValueOnStackInParamV2(param, node);
            }
        }
        // 输出入参
        logger.info(node.toString());
    }

    public static void printValueOnStackOutParamByArr(Object... params) {
        TraceNode node = new TraceNode();
        for (Object param : params) {
            if (param != null) {
                // 入参整合
                printValueOnStackOutParamV2(param, node);
            }
        }
        // 输出入参
        logger.info(node.toString());
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
            node.setEndTime(System.currentTimeMillis());
        } else {
            node.setServiceName(value.getClass().toString());
            node.setInParam(JSONObject.toJSONString(value));
        }
    }

    public static TraceNode printValueOnStackOutParamV2(Object value, TraceNode node) {
        if (node == null) {
            node = new TraceNode();
        }
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
            // TODO
            node.setOutParam(JSONObject.toJSONString(buildRequestParam(request)));
        } else if (value instanceof ServletResponseProxy) {
            // web servlet 出参打印
            // 后缀匹配资源文件不打印请求信息
            ServletResponseProxy response = (ServletResponseProxy) value;
            node.setOutParam(getResponseValue(null, response));
            node.setEndTime(System.currentTimeMillis());
        } else {
            node.setServiceName(value.getClass().toString());
            node.setOutParam(JSONObject.toJSONString(value));
        }

        return node;
    }

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
        }
        else if (value instanceof ServletResponseProxy) {
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


    public static void printValueOnStack4Return(Object value) {
        TraceNode traceNode = printValueOnStackOutParamV2(value, null);
        logger.info(traceNode == null ? null : traceNode.toString());
    }

    public static void printText(String str) {
        System.out.println(str);
    }

    public static void output(PrintStream printStream, int val) {
        printStream.println("ParameterUtils: " + val);
    }


    protected static TraceNode handlerDoDispatchMethod(HttpServletRequest request, TraceNode node) {
        if (node == null) {
            node = new TraceNode();
        }
        node.setNodeType("http");
        node.setTraceId("session.getTraceId()");
        node.setRpcId("session.getCurrentRpcId()");
        node.setBeginTime(System.currentTimeMillis());
        node.setAddressIp(NetUtils.getLocalHost());
        node.setFromIp(HttpUtils.getIp(request));
        node.setServicePath(request.getRequestURL().toString());
        node.setServiceName(request.getRequestURI());
        node.setInParam(JSONObject.toJSONString(buildRequestParam(request)));
        node.setResultState("succeed");
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
