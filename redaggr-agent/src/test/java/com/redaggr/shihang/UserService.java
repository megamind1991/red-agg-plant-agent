package com.redaggr.shihang;/**
 * Created by Administrator on 2018/5/31.
 */


import com.redaggr.User;

/**
 * @author Tommy
 * Created by Tommy on 2018/5/31
 **/
public interface UserService extends UserService1 {
    User getUser(String userid, String name);
}
