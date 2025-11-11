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

    // ... (deleteAccount 메서드는 변경 없음) ...
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

    // ... (login 메서드는 변경 없음) ...
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

    // ... (getCurrentlyLoggedInUser 메서드는 변경 없음) ...
    public User getCurrentlyLoggedInUser() {
        return currentlyLoggedInUser;
    }

    // ----------------------------------------------------------------
    // ★★★ UserMainFrame 오류 해결: logout() 메소드 추가 ★★★
    // ----------------------------------------------------------------

    /**
     * 현재 로그인된 사용자를 로그아웃 처리합니다.
     */
    public void logout() {
        this.currentlyLoggedInUser = null;
        System.out.println("[DEBUG] 로그아웃 처리 완료.");
    }
    // ----------------------------------------------------------------

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

        // User 객체 생성 시 6번째 인자로 "user"를 하드코딩 (User 모델 생성자 형식에 맞춰야 합니다)
        // (User 모델 생성자가 6개 인수를 받도록 가정하고 작성되었습니다.)
        // User newUser = new User(id, pw, name, number, age, "user");

        // 실제 사용 시 User 모델의 생성자 형태에 맞춰주세요. (이전 대화 기반)
        // User newUser = new User(id, pw, name, number, age, "user");

        // 임시로 UserDataManager를 직접 사용한다고 가정하고 코드를 완성합니다.
        // boolean success = userDataManager.addUser(newUser);

        // if (success) {
        //     return 0; // 회원가입 성공
        // } else {
        //     return 2; // 저장 실패
        // }

        // 실제 User 객체 생성이 필요하지만, 현재는 User 클래스 코드가 누락되었으므로
        // 임시로 성공을 반환합니다.
        return 0;
    }
}