package com.heli.mybatis.page.servlet;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

public class Drive {
    private static final int threadCount = 100;
    private static boolean ifTestDuplicate = false;
    public static void main(String[] args) throws InterruptedException {
        String rid = generateRedPacket(1929, 10, 1000, threadCount - 20);
        testGetRedPacket(rid);   
        if (ifTestDuplicate) {
            String[] duplicateUser = {};
            testDuplicate(rid, duplicateUser);
        }
        System.out.println("[main thread] get red packet finished");
        ReidsMatchServlet.showResultAndCheck(rid);
        System.out.println("[main thread] finished");
    }
//    static public void testGetRedPacketSingle() {
//        ReidsMatchServlet.doGetWrapper();
//    }
    public static String generateRedPacket(int senderID, int groupChatID, 
                                    int money, int share) {
        System.out.println("[main thread] generateRedPacket");
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putInt(senderID);        
        buf.putInt(groupChatID);
        byte[] array = buf.array();
        byte[] sid = new byte[4];
        
        byte[] gcid = new byte[4];
        System.arraycopy(array, 0, sid, 0, 4);
        System.arraycopy(array, 4, gcid, 0, 4);
        System.out.println("[main thread] sender ID: " + 
                    String.valueOf(java.nio.ByteBuffer.wrap(sid).getInt()));
        System.out.println("[main thread] groupChat ID: " + 
                String.valueOf(java.nio.ByteBuffer.wrap(gcid).getInt()));
        SingleRedPacket rp = new SingleRedPacket(money, share, sid, gcid);
        System.out.println("[main thread] red Packet ID: " + rp.getIDString());
        return rp.getIDString();
        
    }
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
