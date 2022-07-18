package com.redaggr.agent;

import com.redaggr.handel.RabbitSenderParameterVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

public class RocketMqSenderAgent {

    private static final Logger logger = LoggerFactory.getLogger(RocketMqSenderAgent.class);

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            // com.rabbitmq.client.impl.ChannelN
            // basicPublish(java.lang.String, java.lang.String, boolean, boolean, com.rabbitmq.client.AMQP.BasicProperties, byte[])

            // org.springframework.amqp.rabbit.core.RabbitTemplate
            // doSend
            if ("org.springframework.amqp.rabbit.core.RabbitTemplate".replaceAll("\\.", "/").equals(className)) {
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
                    ClassVisitor cn = new RabbitSenderParameterVisitor(api, cw, className);

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
            return null;
        });

    }
}
