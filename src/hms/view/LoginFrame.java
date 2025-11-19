package hms.view;

import hms.controller.UserController;
import hms.controller.ReservationController; // ReservationController import (필드는 유지)
import hms.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 로그인 화면 (View)
 * (MainFrame에 userName과 userController를 전달)
 */
public class LoginFrame extends JFrame {

    private JTextField idField;
    private JPasswordField pwField;

    // (중요) LoginFrame이 '주방장' 객체를 '소유'합니다.
    private UserController userController = new UserController();
    // ReservationController 객체 생성 및 소유 (필드는 유지)
    private ReservationController reservationController = new ReservationController();

    public LoginFrame() {
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

                boolean loginSuccess = userController.login(id, password);

                if (loginSuccess) {
                    User loggedInUser = userController.getCurrentlyLoggedInUser();
                    String userName = loggedInUser.getName();

                    JOptionPane.showMessageDialog(null, "안녕하세요! " + userName + "님!");

                    dispose(); // 로그인 창 닫기

                    // ★★★ 수정 지점: 권한 확인 및 메인 프레임 분기 ★★★
                    if (userController.isCurrentUserAdmin()) {
                        // 1. 관리자 권한일 경우: AdminMainFrame은 2개의 인자만 받도록 복구
                        new AdminMainFrame(userName, userController).setVisible(true); // 2개 인자
                    } else {
                        // 2. 일반 사용자 권한일 경우: UserMainFrame도 2개의 인자만 받도록 통일 (내부에서 RC 생성)
                        new UserMainFrame(userName, userController).setVisible(true); // 2개 인자
                    }
                    // ★★★ 수정 완료 ★★★

                } else {
                    JOptionPane.showMessageDialog(null,
                            "아이디 또는 비밀번호가 일치하지 않습니다.",
                            "로그인 실패",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // "회원가입" 버튼 클릭 시
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // (SignUpFrame은 자체적으로 Controller를 생성합니다)
                new SignUpFrame();
            }
        });

        setVisible(true);
    }
}