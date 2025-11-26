package hms.controller;

import hms.model.User;
import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class UserController {

    private User currentlyLoggedInUser = null;
    private String serverIp = "127.0.0.1";
    private int serverPort = 5000;

    // 클라이언트에서 DataManager를 직접 소유/사용하지 않습니다.

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

    // LoginFrame 사용 메서드 1
    public boolean login(String id, String password) {
        NetworkMessage res = sendRequest("LOGIN", id + "," + password);
        if (res.isSuccess()) {
            this.currentlyLoggedInUser = (User) res.getData();
            return true;
        }
        return false;
    }

    // LoginFrame 사용 메서드 2
    public User getCurrentlyLoggedInUser() {
        return currentlyLoggedInUser;
    }

    // LoginFrame 사용 메서드 3
    public boolean isCurrentUserAdmin() {
        return currentlyLoggedInUser != null && "admin".equals(currentlyLoggedInUser.getRole());
    }

    public void logout() {
        this.currentlyLoggedInUser = null;
    }

    // ⭐ [추가] UserModifyDialog에서 사용자 정보를 가져오기 위한 메서드
    public User getUserById(String id) {
        NetworkMessage res = sendRequest("USER_GET_BY_ID", id);
        if (res.isSuccess() && res.getData() instanceof User) {
            return (User) res.getData();
        }
        return null;
    }

    // 관리자용 전체 사용자 조회 (서버 통신)
    public List<User> getAllUsersForAdmin() {
        NetworkMessage res = sendRequest("USER_GET_ALL", null);
        if (res.isSuccess() && res.getData() instanceof List) {
            return (List<User>) res.getData();
        }
        return null; // 통신 실패 또는 데이터 형식 오류
    }

    // 관리자용 사용자 추가 (서버 통신)
    public int addUserByAdmin(String id, String pw, String name, String number, String ageStr, String role) {
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            return 3; // 나이 형식 오류
        }

        User newUser = new User(id, pw, name, number, age, role);
        NetworkMessage res = sendRequest("USER_ADD_ADMIN", newUser);

        if (res.isSuccess()) return 0; // 성공

        // 서버에서 ID 중복(1) 또는 저장 실패(2)를 반환했다고 가정
        if (res.getData() instanceof Integer) return (int)res.getData();
        return 2; // 통신/처리 오류
    }

    // 관리자용 사용자 삭제 (서버 통신)
    public boolean deleteUserByAdmin(String id) {
        NetworkMessage res = sendRequest("DELETE_USER", id);
        return res.isSuccess(); // 서버에서 삭제 성공 여부를 반환
    }

    // 관리자용 사용자 수정 (서버 통신)
    public int updateUserByAdmin(String id, String pw, String name, String number, String ageStr, String role) {
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            return 1; // 나이 형식 오류를 1로 지정 (SignUp과 충돌하지 않게)
        }

        User updatedUser = new User(id, pw, name, number, age, role);
        NetworkMessage res = sendRequest("USER_UPDATE_ADMIN", updatedUser);

        if (res.isSuccess()) return 0; // 성공

        // 서버에서 저장 실패(2)를 반환했다고 가정
        return 2;
    }

    // 회원가입 (서버 통신)
    public int signUp(String id, String pw, String name, String number, String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            User newUser = new User(id, pw, name, number, age, "user");

            NetworkMessage res = sendRequest("SIGNUP", newUser);

            if (res.isSuccess()) return 0; // 성공

            // 서버에서 ID 중복(1) 또는 저장 실패(2)를 반환했다고 가정
            if (res.getData() instanceof Integer) return (int) res.getData();

            return 2; // 통신/처리 오류
        } catch (NumberFormatException e) {
            return 3; // 나이 형식 오류
        }
    }

    // 계정 탈퇴 (서버 통신)
    public boolean deleteAccount() {
        if (currentlyLoggedInUser == null) return false;
        NetworkMessage res = sendRequest("DELETE_USER", currentlyLoggedInUser.getId());
        if (res.isSuccess()) {
            currentlyLoggedInUser = null;
            return true;
        }
        return false;
    }
}