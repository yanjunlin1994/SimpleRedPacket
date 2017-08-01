package com.heli.mybatis.page.servlet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Drive {
    private static final int groupChatID = 80;
    private static final int threadCount = 200;
    private static boolean ifTestDuplicate = false;
    public static void main(String[] args) throws InterruptedException {
        List<String> packets = generateMultipleRedPacket(-1);
        testGetMultiRedPacket(packets);
//        testGetMultiRedPacket(packets);
//        if (ifTestDuplicate) {
//            String[] duplicateUser = {};
//            testDuplicate(rid, duplicateUser);
//        }
        System.out.println("[main thread] get red packet finished");
        ReidsMatchServlet.showAllPacketsResultAndCheck(packets);
        System.out.println("[main thread] finished");
    }
    /**
     * Generate many red packets.
     * @param num the number of packets
     * @return
     */
    public static List<String> generateMultipleRedPacket(int num) {
        String rid1 = generateSingleRedPacket(1000, 50, 1929, groupChatID);
        String rid2 = generateSingleRedPacket(590, 78, 1930, groupChatID);
        String rid3 = generateSingleRedPacket(2000, 98, 1931, groupChatID);
        String rid4 = generateSingleRedPacket(900, 38, 1932, groupChatID);
        String rid5 = generateSingleRedPacket(200, 78, 1933, groupChatID);
        String rid6 = generateSingleRedPacket(800, 18, 1934, groupChatID);
        List<String> list = new ArrayList<String>();
        list.add(rid1);
        list.add(rid2);
        list.add(rid3);
        list.add(rid4);
        list.add(rid5);
        list.add(rid6);
        return list;        
    }
    /**
     * Generate single red packet.
     * @param senderID
     * @param groupChatID
     * @param money
     * @param share
     * @return
     */
    public static String generateSingleRedPacket(int senderID, int groupChatID, 
                                    int money, int share) {
        SingleRedPacket rp = new SingleRedPacket(senderID, groupChatID, money, share);
        return rp.getIDString();
    }
    /**
     * Test Get Single Red Packet.
     * @param rid
     * @throws InterruptedException
     */
    static public void testGetMultiRedPacket(List<String> packets) throws InterruptedException {  
        System.out.println("[main thread] testGetRedPacket");
        final CountDownLatch latch = new CountDownLatch(threadCount);  
        for(int i = 0; i < threadCount; ++i) { 
            int temp = i;
            Thread thread = new Thread() {  
                public void run() {  
                    ReidsMatchServlet.doGetWrapper(packets.get(temp * 31 % 6), String.valueOf(temp));
                    latch.countDown();  
                }
            };
//            thread.setName("thread" + i);
            thread.start();  
        }
        latch.await();
    } 
    /**
     * Test Get Single Red Packet.
     * @param rid
     * @throws InterruptedException
     */
    static public void testGetRedPacket(String rid) throws InterruptedException {  
        System.out.println("[main thread] testGetRedPacket");
        final CountDownLatch latch = new CountDownLatch(threadCount);  
        for(int i = 0; i < threadCount; ++i) { 
            int temp = i;
            Thread thread = new Thread() {  
                public void run() {  
                    ReidsMatchServlet.doGetWrapper(rid, String.valueOf(temp));
                    latch.countDown();  
                }
            };
            thread.setName("thread" + i);
            thread.start();  
        }
        latch.await();
    } 
    static public void testDuplicate(String rid, String[] duplicateUser) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(duplicateUser.length);  
        for (String duser : duplicateUser) {
            for(int i = 0; i < duplicateUser.length; ++i) {
                Thread thread = new Thread() {  
                    public void run() {  
                        ReidsMatchServlet.doGetWrapper(rid, duser);
                        latch.countDown();  
                    }
                };
                thread.setName("thread duplicate" + i);
                thread.start();  
            }       
        }
        latch.await(); 
    }
}
