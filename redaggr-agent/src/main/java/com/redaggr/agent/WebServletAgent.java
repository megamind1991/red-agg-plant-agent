package com.redaggr.agent;

import com.redaggr.handel.WebServletMethodOnceParameterVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;

public class WebServletAgent {
    private static final Logger logger = LoggerFactory.getLogger(WebServletAgent.class);

    public static void premain(HashMap<String, String> args, Instrumentation instrumentation) throws FileNotFoundException, UnmodifiableClassException {
        // 注意类加载器
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            // javax.servlet.http.HttpServlet
            if ("javax.servlet.http.HttpServlet".replaceAll("\\.", "/").equals(className)) {
                logger.info("匹配到" + className);
                byte[] bytes2 = null;

                try {
                    // (1)构建ClassReader
                    ClassReader cr = new ClassReader(classfileBuffer);

                    // (2)构建ClassWriter
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

                    // (3)串连ClassNode
                    int api = Opcodes.ASM5;
                    // 方法耗时
//                    ClassNode cn = new ClassAddTimerNode(api, cw);
                    // 方法入参出参
                    ClassVisitor cn = new WebServletMethodOnceParameterVisitor(api, cw, className);

                    //（4）结合ClassReader和ClassNode
                    int parsingOptions = 0;
                    cr.accept(cn, parsingOptions);

                    // (5) 生成byte[]
                    bytes2 = cw.toByteArray();

//                    logger.info(className + "写入文件");
//                    // (6) 写入文件用于检查
//                    FileUtils.writeBytes("D:\\idea\\workspacegit\\itstack-demo-agent\\redaggr-agent\\target\\classes\\com\\redaggr\\delete\\S.class", bytes2);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("异常" + e.getMessage());
                }
                return bytes2;
            }
            // 最后返回不变
            return null;
        });

    }
}
