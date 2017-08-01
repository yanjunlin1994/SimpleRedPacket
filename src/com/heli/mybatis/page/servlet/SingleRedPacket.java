package com.heli.mybatis.page.servlet;
import com.commnon.RedisAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
public class SingleRedPacket {
//    public final byte[] id;
    public final int id;
    public final String idString;
    private final int money;
    private final int share;
    private final int senderID;
    private final int groupChatID;
    public SingleRedPacket(int m, int s, int sid, int gcid) {
        this.id = generateRedPacketID(sid, gcid);
        this.idString = String.valueOf(this.id);
        this.money = m;
        this.share = s;
        this.senderID = sid;
        this.groupChatID = gcid;
        this.putInRedis();
    }
    public int generateRedPacketID(int senderID, int groupChatID) {
        int id = new Random().nextInt(9999);
        return id;
    }
    public void putInRedis() {
        RedisAPI.lpush(this.idString, String.valueOf(this.share));
        RedisAPI.lpush(this.idString, String.valueOf(this.money)); 
    }
    public String getIDString() {
        return this.idString;
    }

}
