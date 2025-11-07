package hms.view;

import hms.controller.UserController;
import hms.model.User; // (★ 1/2) User 모델 임포트
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private JTextField idField;
    private JPasswordField pwField;
    private UserController userController = new UserController();

    public LoginFrame() {
        // ... (UI 코드는 이전과 동일) ...
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

        // --- (★ 2/2) 이벤트 리스너 수정 ---

        // "로그인" 버튼 클릭 시
        // "로그인" 버튼 클릭 시
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = idField.getText();
                String password = new String(pwField.getPassword());

                boolean loginSuccess = userController.login(id, password);

                if (loginSuccess) {
                    // (수정) 컨트롤러에서 로그인한 사용자 정보를 가져옴
                    User loggedInUser = userController.getCurrentlyLoggedInUser();
                    String userName = loggedInUser.getName(); // '이름' 추출

                    // ★★★ 사용자님이 요청하신 수정 라인 ★★★
                    // "로그인 성공!" 대신 "안녕하세요! [이름]님!" 팝업을 띄웁니다.
                    JOptionPane.showMessageDialog(null, "안녕하세요! " + userName + "님!");

                    dispose(); // 로그인 창 닫기

                    // MainFrame에도 이름을 전달해서
                    // "안녕하세요! [이름]님!" 메시지를 또 띄웁니다.
                    new MainFrame(userName);

                } else {
                    JOptionPane.showMessageDialog(null,
                            "아이디 또는 비밀번호가 일치하지 않습니다.",
                            "로그인 실패",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // "회원가입" 버튼 클릭 시 (변경 없음)
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SignUpFrame();
            }
        });

        setVisible(true);
    }
}