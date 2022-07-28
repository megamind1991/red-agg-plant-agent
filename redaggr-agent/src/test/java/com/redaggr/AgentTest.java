package com.redaggr;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.transport.ChannelDelegate;
import com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcInvocation;
import com.alibaba.dubbo.rpc.support.MockInvoker;

import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.List;

public class AgentTest {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
//        System.out.println("hello word");
//        UserServiceImpl userService = new UserServiceImpl();
//        userService.getUser("1", "2");
//        userService.userHasGift("", 1);
//        new PromotionsActivityServiceImpl();
//        Thread.sleep(5000);
//        System.out.println("end");
        DecodeableRpcInvocation decodeableRpcInvocation = new DecodeableRpcInvocation(new ChannelDelegate(), new Request(), new PipedInputStream(), (byte) 1);
        MockInvoker mockInvoker = new MockInvoker(new URL("", "", 1));
//        System.out.println(ParameterUtils.isInstanceof(decodeableRpcInvocation, "com.alibaba.dubbo.rpc.RpcInvocation"));
//        System.out.println(ParameterUtils.isInstanceof(new MockInvoker(new URL("", "", 1)), "com.alibaba.dubbo.rpc.Invoker"));

        System.out.println(isInstanceof(mockInvoker, "com.alibaba.dubbo.rpc.Invoker"));
    }

    static boolean isInstanceof(Object o, String className) {
        ArrayList<String> classNames = new ArrayList<>();
        getSuperClassAndInterface(classNames, o.getClass());
        return classNames.contains(className);
    }

    static void getSuperClassAndInterface(List<String> classNames, Class c) {
        if (c.getSuperclass() == null) {
            return;
        }
        classNames.add(c.getSuperclass().getName());
        for (Class<?> i : c.getInterfaces()) {
            classNames.add(i.getName());
        }
        getSuperClassAndInterface(classNames, c.getSuperclass());
    }
}
