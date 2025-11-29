package hms.controller;

import hms.model.User;
import hms.model.UserDataManager;
import hms.network.NetworkMessage; // ⭐ [NEW] NetworkMessage 임포트
import hms.view.AdminMainFrame;
import hms.view.UserMainFrame;
import javax.swing.JOptionPane;
import java.util.List;
// ⭐ [NEW] 통신에 필요한 임포트 추가
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginController {

    private final UserDataManager userMgr;
    private final UserController userController;
    private final ReservationController resController;
    private final RoomController roomController;

    // ⭐ [NEW] 통신 설정 (UserController와 동일)
    private final String SERVER_IP = "127.0.0.1";
    private final int SERVER_PORT = 5000;

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

    // ⭐ [NEW] 서버 통신 헬퍼 메서드 (RoomServiceController와 동일 구조)
    private NetworkMessage sendRequest(String cmd, Object data) {
        try (Socket s = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            out.writeObject(new NetworkMessage(cmd, data));
            out.flush();
            return (NetworkMessage) in.readObject();
        } catch (Exception e) {
            System.err.println("로그인 통신 오류: " + e.getMessage());
            return new NetworkMessage(false, "통신 오류", null);
        }
    }


    /**
     * ID/PW 인증을 처리하고, 성공 시 역할에 따라 메인 프레임을 분기하여 띄웁니다.
     * @param id 사용자 ID
     * @param pw 사용자 비밀번호
     * @return 로그인 성공 여부 (성공: true, 실패: false)
     */
    public boolean handleLogin(String id, String pw) {

        // ⭐ [핵심 수정] 로컬 userMgr 인증 로직을 제거하고 서버로 LOGIN 명령 전송
        // 서버의 LOGIN 명령은 "ID,PW" 문자열을 기대합니다.
        NetworkMessage res = sendRequest("LOGIN", id + "," + pw);

        if (res.isSuccess()) {
            // 서버에서 User 객체를 성공적으로 반환했을 경우
            User loggedInUser = (User) res.getData();

            if (loggedInUser == null) {
                JOptionPane.showMessageDialog(null, "로그인 성공, 그러나 사용자 정보 로드 실패.", "오류", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // 인증 성공
            userController.setCurrentUser(loggedInUser); // UserController에 세션 정보 저장
            String role = loggedInUser.getRole(); // ROLE 값 추출

            // 역할에 따른 화면 분기
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
            // 3. 로그인 실패 (서버에서 받은 메시지 사용)
            String serverMessage = res.getMessage();
            JOptionPane.showMessageDialog(null,
                    "로그인 실패: " + serverMessage,
                    "로그인 실패",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}