package hms.server;

import hms.network.NetworkMessage;
import hms.model.User;
import hms.model.UserDataManager;
import java.io.*;
import java.net.*;

public class HMSServer {
    public static void main(String[] args) {
        // 5000번 포트 개방
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println(">>> HMS 서버(192.168.0.2)가 시작되었습니다. 접속 대기중...");

            // 데이터베이스(파일) 관리자 생성 (서버에만 파일이 있음)
            UserDataManager userDataManager = new UserDataManager();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> 클라이언트 접속됨: " + clientSocket.getInetAddress());

                // 각 클라이언트를 스레드로 처리
                new Thread(() -> handleClient(clientSocket, userDataManager)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket, UserDataManager userDataManager) {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            while (true) {
                try {
                    // 클라이언트로부터 요청 수신
                    NetworkMessage request = (NetworkMessage) in.readObject();
                    NetworkMessage response = null;

                    switch (request.getCommand()) {
                        case "LOGIN":
                            // 데이터 형식: "id,password" 문자열 분리
                            String[] loginData = ((String) request.getData()).split(",");
                            String id = loginData[0];
                            String pw = loginData[1];

                            User user = userDataManager.findUserById(id);

                            if (user != null && user.getPassword().equals(pw)) {
                                // 로그인 성공 시 User 객체를 담아서 보냄 (직렬화 필수!)
                                response = new NetworkMessage(true, "로그인 성공", user);
                                System.out.println("[로그인 성공] ID: " + id);
                            } else {
                                response = new NetworkMessage(false, "아이디 또는 비번 불일치", null);
                                System.out.println("[로그인 실패] ID: " + id);
                            }
                            break;

                        // 여기에 회원가입(SIGNUP) 등 다른 기능 케이스 추가 가능
                    }

                    // 처리 결과 전송
                    out.writeObject(response);
                    out.flush();

                } catch (EOFException e) {
                    System.out.println("클라이언트 연결 종료.");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("통신 중 오류 발생: " + e.getMessage());
        }
    }
}