package com.redaggr.agent;

import com.redaggr.handel.MethodDubboProviderParameterVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.Instrumentation;

public class DubboProviderAgent {
    public static void premain(String args, Instrumentation instrumentation) {

        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            // org.apache.dubbo.rpc.filter.ClassLoaderFilter
            // com.alibba.dubbo.rpc.filter.ClassLoaderFilter
            if ("org.apache.dubbo.rpc.filter.ClassLoaderFilter".replaceAll("\\.", "/").equals(className) ||
                    "com.alibaba.dubbo.rpc.filter.ClassLoaderFilter".replaceAll("\\.", "/").equals(className)) {
                System.out.println("匹配到" + className);
                byte[] bytes2 = null;

                try {
                    // (1)构建ClassReader
                    ClassReader cr = new ClassReader(classfileBuffer);

                    // (2)构建ClassWriter
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

                    // (3)串连ClassNode
                    int api = Opcodes.ASM9;
                    // 方法入参出参
                    ClassVisitor cn = new MethodDubboProviderParameterVisitor(api, cw, className);

                    //（4）结合ClassReader和ClassNode
                    int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
                    cr.accept(cn, parsingOptions);

                    // (5) 生成byte[]
                    bytes2 = cw.toByteArray();

//                    System.out.println(className + "写入文件");
//                    // (6) 写入文件用于检查
//                    FileUtils.writeBytes("D:\\idea\\workspacegit\\itstack-demo-agent\\redaggr-agent\\target\\classes\\com\\redaggr\\delete\\S.class", bytes2);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("异常" + e.getMessage());
                }
                return bytes2;
            }

            return null;
        });

    }
}
