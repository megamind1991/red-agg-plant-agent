package com.redaggr.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String args, Instrumentation instrumentation) {
        try {
            System.out.println("agent start");
            WebServletAgent.premain(args, instrumentation);
            ServiceAgent.premain(args, instrumentation);
            DubboConsumerAgent.premain(args, instrumentation);
            DubboProviderAgent.premain(args, instrumentation);
            XxlJobAgent.premain(args, instrumentation);
            RocketMqSenderAgent.premain(args, instrumentation);
            RocketMqListenerAgent.premain(args, instrumentation);
//            SqlAgent
//            HttpAgent
//            RedisAgent
//            JmsAgent
//            ScheduledAgent
//            AnnotaionAgent
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
