package com.redaggr.agent;

import com.redaggr.handel.MethodOnceParameterVisitor;
import com.redaggr.util.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ServiceAgent {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAgent.class);

    public static void premain(HashMap<String, String> args, Instrumentation instrumentation) {
        logger.info("匹配service规则" + args);
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            // 监听符合规则的service类
            if (Pattern.matches(args.get("serviceMatch") == null ? ".*(shihang|shlf).*promotions.*ServiceImpl" : args.get("serviceMatch"), className.replace("/", "."))) {
                logger.info("匹配到" + className);
                byte[] bytes2 = null;

                try {
                    // (1)构建ClassReader
                    ClassReader cr = new ClassReader(classfileBuffer);

                    // (2)构建ClassWriter
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

                    // (3)串连ClassNode
                    int api = Opcodes.ASM5;
                    // 方法入参出参
                    MethodOnceParameterVisitor cn = new MethodOnceParameterVisitor(api, cw, className);

                    //（4）结合ClassReader和ClassNode
                    int parsingOptions = 0;
                    cr.accept(cn, parsingOptions);

                    // accept之后才是链路完成，所以cn的值要在accept之后再判断
                    if (!cn.isMeet) {
                        // 检查不符合的情况下直接返回
                        logger.info(className + "检查不符合的情况下直接返回");
                        return null;
                    }

                    // (5) 生成byte[]
                    bytes2 = cw.toByteArray();

                    if (className.contains("UserServiceImpl")) {
                        logger.info(className + "写入文件");
                        // (6) 写入文件用于检查
                        FileUtils.writeBytes("D:\\idea\\workspacegit\\itstack-demo-agent\\redaggr-agent\\target\\classes\\com\\redaggr\\delete\\S.class", bytes2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("异常" + e.getMessage());
                }
                return bytes2;
            }
            return null;
        });

    }
}
