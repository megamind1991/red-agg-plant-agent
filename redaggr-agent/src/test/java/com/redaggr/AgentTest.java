package com.redaggr;

import com.redaggr.shlf.promotions.UserServiceImpl;
import com.shlf.promotions.micro.service.impl.PromotionsActivityServiceImpl;

public class AgentTest {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        System.out.println("hello word");
        UserServiceImpl userService = new UserServiceImpl();
        userService.getUser("1", "2");
        userService.userHasGift("", 1);
        new PromotionsActivityServiceImpl();
        Thread.sleep(5000);
        System.out.println("end");
    }
}
