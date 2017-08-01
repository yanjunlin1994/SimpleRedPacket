package com.heli.mybatis.page.servlet;

import java.util.HashMap;
//import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.commnon.RedisAPI;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

public class ReidsMatchServlet {
    private static final String USERMAP_SUFFIX = "usermap";
    private static final int IDLE_OR_ERROR = 0;
    private static final int SUCCEED = 1;
    private static final int ALREADY_GET = 2;
    private static final int EMPTY = 3;
    
    public static JedisPool pool = RedisAPI.getPool();

    private static final long serialVersionUID = 1L;
    
    public static void doGetWrapper(String redPacktID, String user) {
        doGet(redPacktID, user);
    }

    protected static void doGet(String redPacktID, String user) {
//        System.out.println("[doGet] Red Packet: " + redPacktID + " User: " + user); 
        Jedis jedis = pool.getResource();
        long start = System.currentTimeMillis();
        int flag = 0;
        try {
            flag = bid (redPacktID, user, jedis);
        } catch (Exception e) {
            System.out.println("[doGet] Exception. failed.");
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            RedisAPI.returnResource(pool, jedis);
        }
        if (flag == SUCCEED) {
//            System.out.println("[doGet] succeed ");
        } else if (flag == ALREADY_GET) {
            System.out.println("[doGet] have already bought");
        } else if (flag == EMPTY) {
            System.out.println("[doGet] empty ");
        } else {
            System.out.println("[doGet] Exception. failed.");
        }
        long end = System.currentTimeMillis();
        System.out.println("-----Duration：" + (end - start) + "ms");
    }
    private static int bid(String redPacktID, String user, Jedis jedis) throws Exception {
        int flag = IDLE_OR_ERROR;
        
        while ("OK".equals(jedis.watch(redPacktID))) {  
            int oldShare = Integer.parseInt(RedisAPI.lindex(redPacktID, 1));
            int oldMoney = Integer.parseInt(RedisAPI.lindex(redPacktID, 0));
            //check whether empty
            if (oldShare == 0 || oldMoney == 0) {
                flag = EMPTY;
                return flag;
            }
            // check whether taken before
            Boolean hasBuy = RedisAPI.hexists(redPacktID + USERMAP_SUFFIX, user);
            if (hasBuy) {
                flag = ALREADY_GET;
                return flag;
            }
            int redp = getMyShareMoney(oldMoney, oldShare);
            int balance = oldMoney - redp;
            int newShare = oldShare - 1;
            
            Transaction tx = jedis.multi();
            tx.lset(redPacktID, 0, String.valueOf(balance));
            tx.lset(redPacktID, 1, String.valueOf(newShare));
            List<Object> result = tx.exec();
            if (result == null || result.isEmpty()) {
                jedis.unwatch();
            } else {
//                System.out.println("[OK] User: " + user + " Red Packet: " + redp + 
//                        " remain money: " + String.valueOf(balance) + " share: " + String.valueOf(newShare));

                RedisAPI.hset(redPacktID + USERMAP_SUFFIX, user, String.valueOf(redp));
                flag = SUCCEED;
                break;
            }
        }
        return flag;
    }
    /**
     * Calculate how much money I will get.
     * Currently only works for integer amount.
     * @param remainMoney
     * @param remainShare
     * @return
     */
    private static int getMyShareMoney(int remainMoney, int remainShare) {
        if (remainShare == 1) {
            return remainMoney;
        }
        Random r = new Random();
        int min = 1;
        int max = (int) ((remainMoney / remainShare) * 2);
        int redpacket = r.nextInt(max);
        if (redpacket < min) {
            redpacket = min;
        }
        if (remainMoney - redpacket < remainShare - 1) {
            redpacket = min;
        }
        return redpacket;
    }
    public static void showResultAndCheck(String redPacktID) {
        int allShare = 0;
        int allMoney = 0;
        Map<String, String> resultMap = RedisAPI.hgetall(redPacktID + USERMAP_SUFFIX);
        System.out.println("-------------- Result ------------");
        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            
            String key = entry.getKey();
            String value = entry.getValue();
            allShare++;
            allMoney += Integer.parseInt(value);
            System.out.println("User: " + key + " Red Packet: " + value);
        }
        System.out.println("++ Share: " + String.valueOf(allShare) + " Money: " + String.valueOf(allMoney));
        System.out.println("-------------- Result End ------------");
        
    }
}