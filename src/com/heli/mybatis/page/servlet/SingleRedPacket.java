package com.heli.mybatis.page.servlet;
import com.commnon.RedisAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
public class SingleRedPacket {
    public final byte[] id;
    public final String ids;
    private final int money;
    private final int share;
    private final byte[] senderID;
    private final byte[] groupChatID;
    private int remainMoney;
    private int remainShare;
    public SingleRedPacket(int m, int s, byte[] sid, byte[] gcid) {
        this.id = generateRedPacketID(sid, gcid);
        this.ids = String.valueOf(java.nio.ByteBuffer.wrap(this.id).getInt());
        this.money = m;
        this.share = s;
        this.senderID = sid;
        this.groupChatID = gcid;
        this.remainMoney = m;
        this.remainShare = s;
        this.putInRedis();
    }
    public byte[] generateRedPacketID(byte[] senderID, byte[] groupChatID) {
        int random = new Random().nextInt(999);
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(random);
        byte[] uniqueID = buf.array();
//        byte[] id = new byte[uniqueID.length + senderID.length + groupChatID.length];
        
//        System.arraycopy(uniqueID, 0, id, 0, uniqueID.length);
//        System.arraycopy(senderID, 0, id, uniqueID.length, senderID.length);
//        System.arraycopy(groupChatID, 0, id, 
//                uniqueID.length + senderID.length, groupChatID.length);
        System.out.println("[SingleRedPacket] generate red packet ID: " + 
                    String.valueOf(java.nio.ByteBuffer.wrap(uniqueID).getInt()));
        return uniqueID;
    }
    public void putInRedis() {
        RedisAPI.lpush(this.ids, String.valueOf(this.share));
        RedisAPI.lpush(this.ids, String.valueOf(this.money)); 
    }
    public String getIDString() {
        return this.ids;
    }

}
