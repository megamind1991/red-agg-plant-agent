package com.redaggr;

import com.redaggr.shlf.promotions.UserServiceImpl;

public class AgentTest {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        System.out.println("hello word");
        UserServiceImpl userService = new UserServiceImpl();
        userService.getUser("1", "2");
        userService.userHasGift("");
        Thread.sleep(5000);
    }
}
