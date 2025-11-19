package hms.controller;

import hms.model.User;
import hms.model.UserDataManager;

/**
 * '주방장' (Controller) - 사용자 인증 및 계정 관리를 담당합니다.
 */
public class UserController {

    private UserDataManager userDataManager;
    private User currentlyLoggedInUser = null; // ★ 현재 로그인된 사용자 객체 (세션 역할)

    public UserController() {
        this.userDataManager = new UserDataManager();
    }

    // ----------------------------------------------------------------
    // 1. 계정 삭제 (Delete Account)
    // ----------------------------------------------------------------
    public boolean deleteAccount() {
        if (currentlyLoggedInUser == null) {
            return false;
        }
        String userIdToDelete = currentlyLoggedInUser.getId();

        // (주의: 예약 정보 삭제 로직이 여기에 추가되어야 합니다.)

        boolean success = userDataManager.deleteUserById(userIdToDelete);

        if (success) {
            currentlyLoggedInUser = null;
        }
        return success;
    }

    // ----------------------------------------------------------------
    // 2. 로그인 (Login)
    // ----------------------------------------------------------------
    public boolean login(String id, String password) {
        User user = userDataManager.findUserById(id);
        if (user == null) {
            currentlyLoggedInUser = null;
            return false;
        }
        if (user.getPassword().equals(password)) {
            currentlyLoggedInUser = user; // ★ 로그인 성공 시 현재 사용자 설정
            return true;
        } else {
            currentlyLoggedInUser = null;
            return false;
        }
    }

    // ----------------------------------------------------------------
    // 3. 현재 사용자 조회 (Get Current User)
    // ----------------------------------------------------------------
    public User getCurrentlyLoggedInUser() {
        return currentlyLoggedInUser;
    }

    // ----------------------------------------------------------------
    // 4. 로그아웃 (Logout)
    // ----------------------------------------------------------------
    public void logout() {
        this.currentlyLoggedInUser = null;
        System.out.println("[DEBUG] 로그아웃 처리 완료.");
    }

    // ----------------------------------------------------------------
    // 5. 관리자 확인 (Check Admin Role)
    // ----------------------------------------------------------------
    public boolean isCurrentUserAdmin() {
        if (currentlyLoggedInUser == null) {
            return false;
        }
        // User 객체에서 'role'을 꺼내서 "admin"인지 확인
        return currentlyLoggedInUser.getRole().equals("admin");
    }

    // ----------------------------------------------------------------
    // 6. 회원가입 (Sign Up) - 최종 활성화된 로직
    // ----------------------------------------------------------------
    /**
     * 회원가입 로직을 실행하고 결과를 반환합니다.
     * @return 0: 성공, 1: ID 중복, 2: 저장 실패, 3: 나이 형식 오류
     */
    public int signUp(String id, String pw, String name, String number, String ageStr) {

        // 1. ID 중복 확인
        if (userDataManager.checkIfUserExists(id)) {
            return 1; // ID 중복
        }

        // 2. 나이 형식 및 유효성 검사
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            return 3; // 나이 형식 오류
        }

        // 3. User 객체 생성
        // User 모델은 6개 인자: id, pw, name, number, age, role("user")를 받습니다.
        User newUser = new User(id, pw, name, number, age, "user");

        // 4. UserDataManager를 통해 userinfo.txt 파일에 저장 요청
        boolean success = userDataManager.addUser(newUser);

        if (success) {
            return 0; // 회원가입 성공
        } else {
            return 2; // 저장 실패 (파일 쓰기 오류 등)
        }
    }
}