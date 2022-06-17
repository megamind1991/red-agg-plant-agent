package com.redaggr;

import org.objectweb.asm.Opcodes;

public class AgentTest {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
//        System.out.println("hello word");
//        UserServiceImpl userService = new UserServiceImpl();
//        userService.getUser("1", "2");
//        userService.userHasGift("");
//        Thread.sleep(5000);
        System.out.println(Opcodes.ASM9);
        System.out.println(Opcodes.ASM5);
        System.out.println(Opcodes.ASM5 | Opcodes.ASM9);
        System.out.println(Opcodes.ASM5 & Opcodes.ASM9);
    }
}
