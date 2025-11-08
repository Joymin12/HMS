package hms.controller;

import hms.model.User;
import hms.model.UserDataManager;
import java.util.List; // (List 임포트 추가)

/**
 * '주방장' (Controller)
 * (★수정: getAllUsersList() 메서드 추가됨)
 */
public class UserController {

    private UserDataManager userDataManager;
    private User currentlyLoggedInUser = null;

    public UserController() {
        this.userDataManager = new UserDataManager();
    }

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

    public User getCurrentlyLoggedInUser() {
        return currentlyLoggedInUser;
    }

    public boolean isCurrentUserAdmin() {
        if (currentlyLoggedInUser == null) {
            return false;
        }
        return currentlyLoggedInUser.getRole().equals("admin");
    }

    public int signUp(String id, String pw, String name, String number, String ageStr) {
        if (userDataManager.checkIfUserExists(id)) {
            return 1; // ID 중복
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age == 0) age = 0;
        } catch (NumberFormatException e) {
            return 3; // 나이 형식 오류
        }
        User newUser = new User(id, pw, name, number, age, "user");
        boolean success = userDataManager.addUser(newUser);
        if (success) {
            return 0; // 회원가입 성공
        } else {
            return 2; // 저장 실패
        }
    }

    public boolean deleteUserByAdmin(String userIdToDelete) {
        if (userIdToDelete == null || userIdToDelete.isEmpty()) {
            return false;
        }
        if (userIdToDelete.equals("admin")) {
            return false;
        }
        return userDataManager.deleteUserById(userIdToDelete);
    }

    /**
     * (★★★ 이 메서드가 추가되어야 합니다 ★★★)
     * MainFrame이 호출할 메서드. 모든 사용자 목록을 반환합니다.
     */
    public List<User> getAllUsersList() {
        return userDataManager.getAllUsers();
    }
}