package hms.view;

import hms.controller.UserController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter; // ⭐ [NEW] WindowAdapter 임포트
import java.awt.event.WindowEvent;   // ⭐ [NEW] WindowEvent 임포트
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * 회원가입 화면 (View)
 * (플레이스홀더 기능 포함)
 */
public class SignUpFrame extends JFrame {

    private JTextField idField;
    private JPasswordField pwField;
    private JPasswordField pwConfirmField;
    private JTextField nameField;
    private JTextField numberField;
    private JTextField ageField;

    // ⭐ [핵심 수정] LoginFrame에서 전달받을 UserController 필드
    private final UserController userController;
    private final JFrame loginFrame;
    private final String ID_PLACEHOLDER = "사용할 ID (필수)";
    private final String PW_PLACEHOLDER = "비밀번호 (필수)";
    private final String PW_CONFIRM_PLACEHOLDER = "비밀번호 확인";
    private final String NAME_PLACEHOLDER = "이름 (필수)";
    private final String NUMBER_PLACEHOLDER = "010-1234-1234";
    private final String AGE_PLACEHOLDER = "20";


    // ⭐ [핵심 수정] UserController 객체를 인수로 받도록 생성자 수정
    public SignUpFrame(UserController userController, JFrame loginFrame) {
        this.userController = userController; // 전달받은 Controller를 필드에 저장
        this.loginFrame = loginFrame;

        setTitle("호텔 관리 시스템 - 회원가입");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // (이 창만 닫기)
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("회원가입", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        formPanel.add(new JLabel("아이디 (ID):"));
        idField = new JTextField();
        formPanel.add(idField);

        formPanel.add(new JLabel("비밀번호 (PW):"));
        pwField = new JPasswordField();
        formPanel.add(pwField);

        formPanel.add(new JLabel("비밀번호 확인:"));
        pwConfirmField = new JPasswordField();
        formPanel.add(pwConfirmField);

        formPanel.add(new JLabel("이름 (Name):"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("전화번호 (Number):"));
        numberField = new JTextField();
        formPanel.add(numberField);

        formPanel.add(new JLabel("나이 (Age):"));
        ageField = new JTextField();
        formPanel.add(ageField);

        add(formPanel, BorderLayout.CENTER);

        // 플레이스홀더 적용
        addPlaceholder(idField, ID_PLACEHOLDER);
        addPlaceholder(pwField, PW_PLACEHOLDER);
        addPlaceholder(pwConfirmField, PW_CONFIRM_PLACEHOLDER);
        addPlaceholder(nameField, NAME_PLACEHOLDER);
        addPlaceholder(numberField, NUMBER_PLACEHOLDER);
        addPlaceholder(ageField, AGE_PLACEHOLDER);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton submitButton = new JButton("회원가입 완료");
        JButton cancelButton = new JButton("취소");
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // "회원가입 완료" 버튼
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String id = idField.getText();
                String name = nameField.getText();
                String number = numberField.getText();
                String ageStr = ageField.getText();
                String password = new String(pwField.getPassword());
                String pwConfirm = new String(pwConfirmField.getPassword());

                // 플레이스홀더 텍스트가 남아있으면 오류 처리
                if (id.isEmpty() || id.equals(ID_PLACEHOLDER)) {
                    showError("아이디를 입력해주세요.");
                    return;
                }
                if (password.isEmpty() || password.equals(PW_PLACEHOLDER)) {
                    showError("비밀번호를 입력해주세요.");
                    return;
                }
                if (name.isEmpty() || name.equals(NAME_PLACEHOLDER)) {
                    showError("이름을 입력해주세요.");
                    return;
                }

                if (!password.equals(pwConfirm)) {
                    showError("비밀번호가 일치하지 않습니다.");
                    return;
                }

                // 플레이스홀더가 남아있으면 빈 값/0으로 처리
                if (number.equals(NUMBER_PLACEHOLDER)) number = "";
                if (ageStr.equals(AGE_PLACEHOLDER)) ageStr = "0";

                int result = userController.signUp(id, password, name, number, ageStr);

                switch (result) {
                    case 0: // 성공
                        JOptionPane.showMessageDialog(null, name + "님, 회원가입이 완료되었습니다!");
                        dispose();
                        // ⭐️ 수정: 기존 로그인 프레임을 다시 활성화합니다.
                        if (loginFrame != null) {
                            loginFrame.setVisible(true);
                        } else {
                            // LoginFrame이 null일 경우에만 새로 생성 (LoginFrame 생성자에 문제가 없다면)
                            // new LoginFrame().setVisible(true);
                            JOptionPane.showMessageDialog(null, "시스템 오류: 로그인 화면 객체를 찾을 수 없습니다.");
                        }
                        break;
                    case 1: // ID 중복
                        showError("이미 사용 중인 아이디입니다.");

                        // ID 필드를 비우고 포커스를 줍니다.
                        idField.setText("");
                        idField.setForeground(Color.BLACK); // 플레이스홀더 색상 제거
                        idField.requestFocusInWindow();

                        break;
                    case 2: // 파일 저장 실패
                        showError("시스템 오류: 파일 저장에 실패했습니다.");
                        break;
                    case 3: // 나이 형식 오류
                        showError("나이는 숫자로만 입력해주세요.");
                        break;
                    case 99: // 통신 오류 (UserController에서 정의한 코드)
                        showError("서버 통신 오류가 발생했습니다. 서버가 켜져 있는지 확인해주세요.");
                        break;
                }
            }
        });

        // "취소" 버튼
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ⭐️ [수정] 취소 시 부모 창을 다시 보이게 합니다.
                if (loginFrame != null) {
                    loginFrame.setVisible(true);
                }
                dispose(); // 이 창 닫기
            }
        });

        // ⭐ [NEW] X 버튼 클릭 시 부모 창을 활성화하는 리스너 추가
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                // X 버튼을 눌러 창이 닫힐 때, 부모 창을 다시 보이게 합니다.
                if (loginFrame != null) {
                    loginFrame.setVisible(true);
                }
            }
        });


        setVisible(true);
        loginFrame.setVisible(true);
    }

    // --- (이하 헬퍼 메서드: 플레이스홀더 및 에러 팝업) ---

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "입력 오류", JOptionPane.ERROR_MESSAGE);
    }

    private void addPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void addPlaceholder(JPasswordField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.setEchoChar((char) 0);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String currentText = new String(field.getPassword());
                if (currentText.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar('*');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                String currentText = new String(field.getPassword());
                if (currentText.isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                    field.setEchoChar((char) 0);
                }
            }
        });
    }
}