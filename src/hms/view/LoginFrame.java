package hms.view;

import hms.controller.UserController;
import hms.controller.ReservationController;
import hms.controller.RoomController;
import hms.controller.LoginController;
import hms.model.UserDataManager;
import hms.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;

/**
 * 로그인 화면 (View)
 */
public class LoginFrame extends JFrame {

    private JTextField idField;
    private JPasswordField pwField;

    // ⭐ [NEW] 모든 컨트롤러 인스턴스 생성 및 LoginController 초기화에 필요한 필드
    private final UserDataManager userMgr = new UserDataManager();
    private final UserController userController = new UserController();
    private final ReservationController reservationController = new ReservationController();
    private final RoomController roomController = new RoomController();

    private final LoginController loginController; // ⭐ [NEW] LoginController 필드

    public LoginFrame() {
        // 모든 의존성 초기화 및 LoginController 생성 (중요: 이 4개의 인스턴스를 LoginController가 사용함)
        this.loginController = new LoginController(
                userMgr,
                userController,
                reservationController,
                roomController
        );

        setTitle("호텔 관리 시스템 - 로그인");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("로그인", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        formPanel.add(new JLabel("아이디:"));
        idField = new JTextField();
        formPanel.add(idField);
        formPanel.add(new JLabel("비밀번호:"));
        pwField = new JPasswordField();
        formPanel.add(pwField);
        add(formPanel, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loginButton = new JButton("로그인");
        JButton signupButton = new JButton("회원가입");
        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- 이벤트 리스너 ---

        // "로그인" 버튼 클릭 시
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = idField.getText();
                String password = new String(pwField.getPassword());

                // [핵심 수정] LoginController에게 인증 및 라우팅 책임을 위임
                if (loginController.handleLogin(id, password)) {
                    dispose(); // 로그인 성공 시 창 닫기 (LoginController가 MainFrame을 띄움)
                }
            }
        });

        // "회원가입" 버튼 클릭 시
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // SignUpFrame에 자기 자신(this)과 UserController 전달 (창 복귀 로직용)
                new SignUpFrame(userController, LoginFrame.this).setVisible(true);
                LoginFrame.this.setVisible(false); // 회원가입 창이 뜰 동안 현재 창 숨기기
            }
        });

        setVisible(true);
    }
}