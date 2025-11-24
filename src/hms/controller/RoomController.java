package hms.controller;

import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomController {
    private final String SERVER_IP = "110.46.46.92";
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

    // 조회
    public List<String[]> getAllRooms() {
        NetworkMessage res = send("ROOM_GET_ALL", null);
        if (!res.isSuccess()) {
            System.out.println("데이터 가져오기 실패: " + res.getMessage());
        }
        return res.isSuccess() ? (List<String[]>) res.getData() : new ArrayList<>();
    }

    // 추가
    public boolean addRoom(String roomNum, String grade, int price) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomNum", roomNum);
        data.put("grade", grade);
        data.put("price", price);
        return send("ROOM_ADD", data).isSuccess();
    }

    // 수정
    public boolean updateRoom(String roomNum, String grade, int price) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomNum", roomNum);
        data.put("grade", grade);
        data.put("price", price);
        return send("ROOM_UPDATE", data).isSuccess();
    }

    // 삭제
    public boolean deleteRoom(String roomNum) {
        return send("ROOM_DELETE", roomNum).isSuccess();
    }
}
