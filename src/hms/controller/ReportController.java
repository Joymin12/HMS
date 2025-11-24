package hms.controller;

import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ReportController {

    // ⭐ 오류가 났던 부분: 변수 선언을 확실하게 추가했습니다.
    // 님의 IP (192.168.0.2)로 설정해두었습니다.
    private final String SERVER_IP = "110.46.46.92";
    private final int SERVER_PORT = 5000;

    private NetworkMessage send(String cmd, Object data) {
        // 여기서 SERVER_IP 변수를 사용합니다.
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

    public Map<String, Object> generateReport(String startDate, String endDate) {
        NetworkMessage res = send("REPORT_GENERATE", startDate + "," + endDate);
        if (res.isSuccess() && res.getData() instanceof Map) {
            return (Map<String, Object>) res.getData();
        }
        return new HashMap<>();
    }
}