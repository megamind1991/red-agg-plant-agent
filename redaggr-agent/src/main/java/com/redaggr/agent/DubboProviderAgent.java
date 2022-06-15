package com.redaggr.agent;

import java.lang.instrument.Instrumentation;

public class DubboProviderAgent {
    public static void premain(String args, Instrumentation instrumentation) {

        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            // org.apache.dubbo.rpc.filter.ClassLoaderFilter
            // com.alibba.dubbo.rpc.filter.ClassLoaderFilter
            if ("org.apache.dubbo.rpc.filter.ClassLoaderFilter".replaceAll("\\.", "/").equals(className) ||
                    "com.alibba.dubbo.rpc.filter.ClassLoaderFilter".replaceAll("\\.", "/").equals(className)) {

            }

            return null;
        });

    }
}
