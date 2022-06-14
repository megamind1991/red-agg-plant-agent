package com.redaggr.shlf.promotions;


import com.redaggr.User;
import com.redaggr.shihang.UserService;

/**
 * Created by Tommy on 2018/3/8.
 */

public class UserServiceImpl implements UserService {

    @Override
    public User getUser(String userid, String name) {
        System.out.println("获取用户信息:" + userid);
        User user = new User();
        user.setUserid(userid);
        user.setUsername(name);
        return user;
    }

    public boolean userHasGift(String customerGuid) {
        return "".equals(customerGuid);
    }


}
