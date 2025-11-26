package hms.controller;

import hms.model.User;
import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class UserController {

    private User currentlyLoggedInUser = null;
    private String serverIp = "127.0.0.1"; // 서버 IP
    private int serverPort = 5000;

    // 통신 헬퍼 메소드 (sendRequest)
    private NetworkMessage sendRequest(String command, Object data) {
        try (Socket socket = new Socket(serverIp, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new NetworkMessage(command, data));
            out.flush();
            return (NetworkMessage) in.readObject();

        } catch (Exception e) {
            System.err.println("서버 통신 오류: " + e.getMessage());
            return new NetworkMessage(false, "통신 오류", null);
        }
    }

    // 1. 로그인 (기존 코드)
    public boolean login(String id, String password) {
        NetworkMessage res = sendRequest("LOGIN", id + "," + password);
        if (res.isSuccess()) {
            this.currentlyLoggedInUser = (User) res.getData();
            return true;
        }
        return false;
    }

    // 2. 회원가입 (일반 사용자: 기존 코드)
    public int signUp(String id, String pw, String name, String number, String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            // User 생성자에 7개 인자 전달 (role="user", email="")
            User newUser = new User(id, pw, name, number, age, "user");
            NetworkMessage res = sendRequest("SIGNUP", newUser);
            if (res.isSuccess()) return 0;
            return (int) res.getData();
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    // 2-2. 회원가입 (관리자: User 객체를 직접 받음) 오버로드 메서드 (기존 코드)
    public NetworkMessage signUp(User newUser) {
        NetworkMessage res = sendRequest("SIGNUP", newUser);
        return res;
    }

    // ⭐ 계정 탈퇴 (서버 통신)
    public boolean deleteAccount() {
        if (currentlyLoggedInUser == null) return false;
        NetworkMessage res = sendRequest("DELETE_USER", currentlyLoggedInUser.getId());
        if (res.isSuccess()) {
            currentlyLoggedInUser = null;
            return true;
        }
        return false;
    }


    // 4. [기존 기능] 전체 사용자 목록 조회 (SFR-205)
    public List<User> getAllUsers() {
        try {
            NetworkMessage response = sendRequest("GET_ALL_USERS", null);

            if (response.isSuccess()) {
                // 응답 데이터는 사용자 목록(List<User>)
                return (List<User>) response.getData();
            }

        } catch (Exception e) {
            System.err.println("사용자 목록 조회 중 치명적인 오류 발생:");
            e.printStackTrace();
        }
        return null; // 실패 시 null 반환
    }

    // ⭐ 4-1. [추가 기능] ID로 사용자 정보 조회 (EditUserDialog 지원) ⭐
    public User getUserById(String userId) {
        try {
            NetworkMessage response = sendRequest("GET_USER_BY_ID", userId);

            if (response.isSuccess()) {
                // 서버에서 User 객체를 성공적으로 반환했을 경우
                return (User) response.getData();
            }

        } catch (Exception e) {
            System.err.println("사용자 ID 조회 중 오류 발생: " + e.getMessage());
        }
        return null; // 실패 시 null 반환
    }

    // 5. [기존 기능] 관리자 권한으로 사용자 삭제 (SFR-207)
    public boolean deleteUserByAdmin(String userId) {
        try {
            NetworkMessage response = sendRequest("ADMIN_DELETE_USER", userId);
            return response.isSuccess();

        } catch (Exception e) {
            System.err.println("관리자 삭제 요청 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    // ⭐ 6. [추가 기능] 관리자 권한으로 사용자 정보 수정 (EditUserDialog 지원) ⭐
    public boolean updateUserByAdmin(User updatedUser) {
        try {
            NetworkMessage response = sendRequest("ADMIN_UPDATE_USER", updatedUser);
            return response.isSuccess();

        } catch (Exception e) {
            System.err.println("관리자 수정 요청 중 오류 발생: " + e.getMessage());
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