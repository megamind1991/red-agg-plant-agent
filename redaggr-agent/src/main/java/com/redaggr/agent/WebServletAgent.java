package com.redaggr.agent;

import com.redaggr.handel.WebServletMethodOnceParameterVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.FileNotFoundException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class WebServletAgent {

    public static void premain(String args, Instrumentation instrumentation) throws FileNotFoundException, UnmodifiableClassException {
        // 注意类加载器
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            // javax.servlet.http.HttpServlet
            if ("javax.servlet.http.HttpServlet".replaceAll("\\.", "/").equals(className)) {
                System.out.println("匹配到" + className);
                byte[] bytes2 = null;

                try {
                    // (1)构建ClassReader
                    ClassReader cr = new ClassReader(classfileBuffer);

                    // (2)构建ClassWriter
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

                    // (3)串连ClassNode
                    int api = Opcodes.ASM9;
                    // 方法耗时
//                    ClassNode cn = new ClassAddTimerNode(api, cw);
                    // 方法入参出参
                    ClassVisitor cn = new WebServletMethodOnceParameterVisitor(api, cw, className);

                    //（4）结合ClassReader和ClassNode
                    int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
                    cr.accept(cn, parsingOptions);

                    // (5) 生成byte[]
                    bytes2 = cw.toByteArray();

                    System.out.println(className + "写入文件");
                    // (6) 写入文件用于检查
//                    FileUtils.writeBytes("D:\\idea\\workspacegit\\itstack-demo-agent\\redaggr-agent\\target\\classes\\com\\redaggr\\delete\\S.class", bytes2);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("异常" + e.getMessage());
                }
                return bytes2;
            }
            // 最后返回不变
            return null;
        });

    }
}
