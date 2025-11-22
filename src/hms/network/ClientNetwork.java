package hms.network;

import java.io.*;
import java.net.Socket;
import javax.swing.JOptionPane;

public class ClientNetwork {
    private static ClientNetwork instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final String SERVER_IP = "127.0.0.1"; // 로컬호스트 (내 컴퓨터)
    private final int SERVER_PORT = 5000;         // 서버와 약속된 포트

    // 1. 싱글톤: 프로그램 내에서 단 하나만 존재
    public static synchronized ClientNetwork getInstance() {
        if (instance == null) {
            instance = new ClientNetwork();
        }
        return instance;
    }

    // 2. 생성자: 서버 연결 시도
    private ClientNetwork() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("[Client] 서버에 연결되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "서버에 연결할 수 없습니다.\n서버가 켜져 있는지 확인해주세요.");
            System.exit(0); // 연결 못하면 프로그램 종료
        }
    }

    // 3. 요청 보내고 응답 받기 (핵심 메서드)
    public Message sendRequest(Message request) {
        try {
            // 서버로 전송
            out.writeObject(request);
            out.flush();

            // 서버 응답 대기 (받을 때까지 멈춤)
            return (Message) in.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 연결 종료
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}