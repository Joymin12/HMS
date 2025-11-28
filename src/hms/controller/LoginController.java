package hms.controller;

import hms.model.User;
import hms.model.UserDataManager;
import hms.view.AdminMainFrame;
import hms.view.UserMainFrame;
import javax.swing.JOptionPane;
import java.util.List;

public class LoginController {

    private final UserDataManager userMgr;
    private final UserController userController;

    // ⭐ [필수] 라우팅에 필요한 Controller 종속성 필드
    private final ReservationController resController;
    private final RoomController roomController;

    /**
     * ⭐ [핵심] LoginFrame에서 모든 Controller 인스턴스를 주입받는 생성자
     */
    public LoginController(UserDataManager userMgr,
                           UserController userController,
                           ReservationController resController,
                           RoomController roomController) {
        this.userMgr = userMgr;
        this.userController = userController;
        this.resController = resController;
        this.roomController = roomController;
    }


    /**
     * ID/PW 인증을 처리하고, 성공 시 역할에 따라 메인 프레임을 분기하여 띄웁니다.
     * @param id 사용자 ID
     * @param pw 사용자 비밀번호
     * @return 로그인 성공 여부 (성공: true, 실패: false)
     */
    public boolean handleLogin(String id, String pw) {
        // 1. 사용자 인증 (UserDataManager를 통해 파일에서 사용자 정보를 찾음)
        User loggedInUser = userMgr.findUserById(id);

        if (loggedInUser != null && loggedInUser.getPassword().equals(pw)) {
            // 인증 성공
            userController.setCurrentUser(loggedInUser); // UserController에 세션 정보 저장

            String role = loggedInUser.getRole(); // ⭐ [핵심] ROLE 값 추출

            // 2. 역할에 따른 화면 분기
            if (role.equals("admin") || role.equals("csr")) {
                // ADMIN 또는 CSR
                JOptionPane.showMessageDialog(null, "안녕하세요! " + loggedInUser.getName() + " 관리자님!");

                //  AdminMainFrame에 4개 인자 (Controller 3개 + Role) 전달
                new AdminMainFrame(userController, resController, roomController, role).setVisible(true);

            } else if (role.equals("user")) {
                // 일반 고객
                JOptionPane.showMessageDialog(null, "안녕하세요! " + loggedInUser.getName() + "님!");
                new UserMainFrame(loggedInUser, userController).setVisible(true);

            } else {
                JOptionPane.showMessageDialog(null, "오류: 알 수 없는 사용자 역할입니다.");
                return false;
            }

            return true; // 로그인 성공

        } else {
            // 3. 로그인 실패
            JOptionPane.showMessageDialog(null,
                    "로그인 실패: ID 또는 비밀번호가 일치하지 않습니다.",
                    "로그인 실패",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}