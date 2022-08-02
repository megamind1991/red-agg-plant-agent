package com.redaggr.agent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class Agent {

    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    private static final Pattern OPTION_SPLIT = Pattern
            .compile(",(?=[a-zA-Z0-9_\\-]+=)");

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            HashMap<String, String> argsMap = new HashMap<>();
            if (args != null && args.length() > 0) {
                for (final String entry : OPTION_SPLIT.split(args)) {
                    final int pos = entry.indexOf('=');
                    if (pos == -1) {
                        throw new IllegalArgumentException(format(
                                "Invalid agent option syntax \"%s\".", args));
                    }
                    final String key = entry.substring(0, pos);
                    final String value = entry.substring(pos + 1);
                    argsMap.put(key, value);
                }
            }
            
            logger.info("agent start");
            WebServletAgent.premain(argsMap, instrumentation);
            ServiceAgent.premain(argsMap, instrumentation);
            DubboConsumerAgent.premain(argsMap, instrumentation);
            DubboProviderAgent.premain(argsMap, instrumentation);
            XxlJobAgent.premain(argsMap, instrumentation);
            RocketMqSenderAgent.premain(argsMap, instrumentation);
            RocketMqListenerAgent.premain(argsMap, instrumentation);
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
