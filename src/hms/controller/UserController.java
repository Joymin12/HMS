package hms.controller;

import hms.model.User;
import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class UserController {

    private User currentlyLoggedInUser = null;
    private String serverIp = "110.46.46.92"; // 서버 IP
    private int serverPort = 5000;

    // 통신 헬퍼 메소드 (sendRequest)
    private NetworkMessage sendRequest(String command, Object data) {
        // 소켓 생성 및 서버 연결(IP, Port)
        try (Socket socket = new Socket(serverIp, serverPort);
             // 출력 스트림 생성
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             //입력 스트림 생성
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            //객체 전송
            out.writeObject(new NetworkMessage(command, data));
            out.flush();
            //응답 수신
            return (NetworkMessage) in.readObject();

        } catch (Exception e) { // 예외 처리
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
            // User 생성자에 6개 인자 전달 (role="user")
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


    // 4. [수정됨] 전체 사용자 목록 조회 (HMSServer의 "USER_GET_ALL" 명령에 맞춤)
    public List<User> getAllUsers() {
        try {
            // ⭐⭐⭐ 명령어를 서버의 HMSServer.java에 맞게 "USER_GET_ALL"로 수정 ⭐⭐⭐
            NetworkMessage response = sendRequest("USER_GET_ALL", null);

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
            // ⭐ HMSServer의 USER_GET_BY_ID 명령에 맞춤
            NetworkMessage response = sendRequest("USER_GET_BY_ID", userId);

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
            // ⭐ HMSServer의 ADMIN_DELETE_USER 명령에 맞춤
            NetworkMessage response = sendRequest("DELETE_USER", userId);
            return response.isSuccess();

        } catch (Exception e) {
            System.err.println("관리자 삭제 요청 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    // ⭐ 6. [추가 기능] 관리자 권한으로 사용자 정보 수정 (EditUserDialog 지원) ⭐
    public boolean updateUserByAdmin(User updatedUser) {
        try {
            // ⭐ HMSServer의 USER_UPDATE_ADMIN 명령에 맞춤
            NetworkMessage response = sendRequest("USER_UPDATE_ADMIN", updatedUser);
            return response.isSuccess();

        } catch (Exception e) {
            System.err.println("관리자 수정 요청 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    /**
     * ⭐ [NEW] 현재 로그인된 사용자의 세션 정보를 저장합니다. (LoginController에서 사용)
     */
    public void setCurrentUser(User user) {
        this.currentlyLoggedInUser = user;
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