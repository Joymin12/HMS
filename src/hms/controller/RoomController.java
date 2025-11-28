package hms.controller;

import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class RoomController {
    // 🔧 문제 원인 해결: 110.46.46.92 → 127.0.0.1
    private final String SERVER_IP = "127.0.0.1";
    private final int SERVER_PORT = 5000;

    // 공통 전송 메소드
    private NetworkMessage send(String cmd, Object data) {
        try (Socket s = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            out.writeObject(new NetworkMessage(cmd, data));
            out.flush();
            return (NetworkMessage) in.readObject();
        } catch (Exception e) {
            return new NetworkMessage(false, "통신오류", null);
        }
    }

    public List<String[]> getAllRooms() {
        NetworkMessage res = send("ROOM_GET_ALL", null);
        if (res.isSuccess()) return (List<String[]>) res.getData();
        return new ArrayList<>();
    }

    public boolean addRoom(String roomNum, String grade, int price) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomNum", roomNum);
        data.put("grade", grade);
        data.put("price", price);
        return send("ROOM_ADD", data).isSuccess();
    }

    // [핵심 수정] 사유(reason) 파라미터 추가
    public boolean updateRoom(String roomNum, String grade, int price, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomNum", roomNum);
        data.put("grade", grade);
        data.put("price", price);
        data.put("reason", reason); // ⭐ 서버로 사유 전송
        return send("ROOM_UPDATE", data).isSuccess();
    }

    public boolean deleteRoom(String roomNum) {
        return send("ROOM_DELETE", roomNum).isSuccess();
    }
}