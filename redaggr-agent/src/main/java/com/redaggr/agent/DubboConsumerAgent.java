package com.redaggr.agent;

import com.redaggr.handel.MethodDubboParameterVisitor;
import com.redaggr.util.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.Instrumentation;

/**
 * @author : 0006841 油面筋
 * @Description : dubbo 消费者埋点<br>
 * @taskId <br>
 * @return : null
 */
public class DubboConsumerAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            // 这边我们拦截
            // org.apache.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker
            // com.alibaba.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker
            if ("org.apache.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker".replaceAll("\\.", "/").equals(className) ||
                    "com.alibaba.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker".replaceAll("\\.", "/").equals(className)) {
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
                    ClassVisitor cn = new MethodDubboParameterVisitor(api, cw, className);

                    //（4）结合ClassReader和ClassNode
                    int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
                    cr.accept(cn, parsingOptions);

                    // (5) 生成byte[]
                    bytes2 = cw.toByteArray();

                    System.out.println(className + "写入文件");
                    // (6) 写入文件用于检查
                    FileUtils.writeBytes("D:\\idea\\workspacegit\\itstack-demo-agent\\redaggr-agent\\target\\classes\\com\\redaggr\\delete\\S.class", bytes2);
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
