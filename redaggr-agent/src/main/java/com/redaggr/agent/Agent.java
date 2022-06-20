package com.redaggr.agent;

import com.redaggr.logger.Logger;
import com.redaggr.logger.LoggerFactory;

import java.lang.instrument.Instrumentation;

public class Agent {

    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            logger.info("agent start");
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
