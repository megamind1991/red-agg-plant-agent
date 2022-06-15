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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
