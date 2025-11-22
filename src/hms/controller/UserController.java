package hms.controller;

import hms.model.User;
import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;

public class UserController {

    private User currentlyLoggedInUser = null;
    private String serverIp = "127.0.0.1"; // 서버 IP
    private int serverPort = 5000;

    // 통신 헬퍼 메소드
    private NetworkMessage sendRequest(String command, Object data) {
        try (Socket socket = new Socket(serverIp, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new NetworkMessage(command, data));
            out.flush();
            return (NetworkMessage) in.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            return new NetworkMessage(false, "통신 오류", null);
        }
    }

    // 1. 로그인
    public boolean login(String id, String password) {
        NetworkMessage res = sendRequest("LOGIN", id + "," + password);
        if (res.isSuccess()) {
            this.currentlyLoggedInUser = (User) res.getData();
            return true;
        }
        return false;
    }

    // 2. 회원가입
    public int signUp(String id, String pw, String name, String number, String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            User newUser = new User(id, pw, name, number, age, "user");

            NetworkMessage res = sendRequest("SIGNUP", newUser);
            if (res.isSuccess()) return 0; // 성공

            return (int) res.getData(); // 에러 코드 반환

        } catch (NumberFormatException e) {
            return 3; // 나이 오류
        }
    }

    // 3. 회원 탈퇴
    public boolean deleteAccount() {
        if (currentlyLoggedInUser == null) return false;
        NetworkMessage res = sendRequest("DELETE_USER", currentlyLoggedInUser.getId());
        if (res.isSuccess()) {
            currentlyLoggedInUser = null;
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentlyLoggedInUser = null;
    }

    public User getCurrentlyLoggedInUser() {
        return currentlyLoggedInUser;
    }

    public boolean isCurrentUserAdmin() {
        return currentlyLoggedInUser != null && "admin".equals(currentlyLoggedInUser.getRole());
    }
}