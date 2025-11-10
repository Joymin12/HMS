package hms.controller;

import hms.model.User;
import hms.model.UserDataManager;

/**
 * '주방장' (Controller)
 * (★수정: isCurrentUserAdmin() 추가, signUp() 로직 변경, logout() 추가)
 */
public class UserController {

    private UserDataManager userDataManager;
    private User currentlyLoggedInUser = null;

    public UserController() {
        this.userDataManager = new UserDataManager();
    }

    // ... (deleteAccount 메서드는 변경 없음) ...
    public boolean deleteAccount() {
        if (currentlyLoggedInUser == null) {
            return false;
        }
        String userIdToDelete = currentlyLoggedInUser.getId();
        boolean success = userDataManager.deleteUserById(userIdToDelete);

        if (success) {
            currentlyLoggedInUser = null;
        }
        return success;
    }

    // ... (login 메서드는 변경 없음) ...
    public boolean login(String id, String password) {
        User user = userDataManager.findUserById(id);
        if (user == null) {
            currentlyLoggedInUser = null;
            return false;
        }
        if (user.getPassword().equals(password)) {
            currentlyLoggedInUser = user;
            return true;
        } else {
            currentlyLoggedInUser = null;
            return false;
        }
    }

    /**
     * 현재 로그인된 사용자를 반환합니다.
     */
    public User getCurrentlyLoggedInUser() {
        return currentlyLoggedInUser;
    }

    /**
     * (★신규★) 현재 로그인된 사용자를 로그아웃 처리합니다.
     */
    public void logout() {
        this.currentlyLoggedInUser = null;
        // 세션을 끊고 현재 로그인된 사용자 정보를 제거합니다.
    }

    /**
     * (★신규★) '관리' 버튼이 호출할 메서드
     * @return true (관리자임), false (일반 사용자임)
     */
    public boolean isCurrentUserAdmin() {
        if (currentlyLoggedInUser == null) {
            return false;
        }
        // User 객체에서 'role'을 꺼내서 "admin"인지 확인
        return currentlyLoggedInUser.getRole().equals("admin");
    }

    /**
     * (★수정★) 회원가입 로직
     * 6번째 항목에 'user' 역할을 '자동으로' 추가해서 저장합니다.
     */
    public int signUp(String id, String pw, String name, String number, String ageStr) {

        if (userDataManager.checkIfUserExists(id)) {
            return 1; // ID 중복
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age == 0) age = 0; // (플레이스홀더가 0으로 넘어오는 경우 처리)
        } catch (NumberFormatException e) {
            return 3; // 나이 형식 오류
        }

        // (★수정) User 객체 생성 시 6번째 인자로 "user"를 하드코딩
        User newUser = new User(id, pw, name, number, age, "user");

        boolean success = userDataManager.addUser(newUser);

        if (success) {
            return 0; // 회원가입 성공
        } else {
            return 2; // 저장 실패
        }
    }
}