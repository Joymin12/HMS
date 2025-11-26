package hms.controller;

import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class RoomServiceController {
    // 🔧 문제 원인: 110.46.46.92 → 127.0.0.1로 수정
    private final String SERVER_IP = "127.0.0.1";
    private final int SERVER_PORT = 5000;

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

    public List<String> getAllCategories() {
        NetworkMessage res = send("RS_GET_CATEGORIES", null);
        return res.isSuccess() ? (List<String>) res.getData() : new ArrayList<>();
    }

    public List<String[]> getMenuByCategory(String category) {
        NetworkMessage res = send("RS_GET_MENU_BY_CAT", category);
        return res.isSuccess() ? (List<String[]>) res.getData() : new ArrayList<>();
    }

    public List<String[]> getAllMenu() {
        NetworkMessage res = send("RS_GET_ALL_MENU", null);
        return res.isSuccess() ? (List<String[]>) res.getData() : new ArrayList<>();
    }

    public String addMenuItem(String name, int price, String category) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("price", price);
        data.put("cat", category);
        NetworkMessage res = send("RS_ADD_MENU", data);
        return res.isSuccess() ? (String) res.getData() : null;
    }

    public boolean updateMenuItem(String id, String name, int price, String category) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);
        data.put("price", price);
        data.put("cat", category);
        return send("RS_UPDATE_MENU", data).isSuccess();
    }

    public boolean deleteMenuItem(String id) {
        return send("RS_DELETE_MENU", id).isSuccess();
    }

    public String addServiceRequest(String room, String items, long price) {
        Map<String, Object> data = new HashMap<>();
        data.put("room", room);
        data.put("items", items);
        data.put("price", price);
        NetworkMessage res = send("RS_ADD_REQUEST", data);
        return res.isSuccess() ? (String) res.getData() : null;
    }

    public List<String[]> getAllRequests() {
        NetworkMessage res = send("RS_GET_ALL_REQUESTS", null);
        return res.isSuccess() ? (List<String[]>) res.getData() : new ArrayList<>();
    }

    public List<String[]> getRequestsByStatus(String status) {
        NetworkMessage res = send("RS_GET_REQ_BY_STATUS", status);
        return res.isSuccess() ? (List<String[]>) res.getData() : new ArrayList<>();
    }

    public boolean updateRequestStatus(String id, String status) {
        return send("RS_UPDATE_REQ_STATUS", id + "," + status).isSuccess();
    }

    public boolean updateStatusByRoomAndStatus(String room, String oldStat, String newStat) {
        return send("RS_UPDATE_STATUS_BY_ROOM", room + "," + oldStat + "," + newStat).isSuccess();
    }
}
