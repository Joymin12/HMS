package hms.view;

import hms.controller.UserController;
import hms.model.User;
import hms.network.NetworkMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

/**
 * 관리자용 사용자 추가 다이얼로그 (Dialog)
 * <p>
 * 관리자가 시스템에 새로운 사용자(일반 고객, 관리자, CSR)를 등록할 때 사용된다.
 * 입력 데이터의 유효성을 검증하고, UserController의 signUp(User) 메서드를 호출하여 서버에 전송한다.
 * </p>
 * <h3>주요 기능:</h3>
 * <ul>
 * <li>새 사용자 정보 입력 폼 제공</li>
 * <li>필수 필드 입력 여부 검증</li>
 * <li>연락처 및 나이 형식 유효성 검증 (정규식/NumberFormat)</li>
 * <li>권한(role)을 지정하여 사용자 생성</li>
 * <li>사용자 추가 성공 시 부모 창 목록 갱신</li>
 * </ul>
 * @author [작성자 이름]
 * @version 1.1 (주석 최적화)
 * @since 2025-11-30
 */
public class UserAddDialog extends JDialog {

    private final AdminUserManagementFrame parentFrame;
    private final UserController userController;

    // 입력 필드 정의
    private final JTextField idField = new JTextField(15);
    private final JPasswordField pwField = new JPasswordField(15);
    private final JTextField nameField = new JTextField(15);
    private final JTextField numberField = new JTextField(15);
    private final JTextField ageField = new JTextField(15);

    // [수정] JComboBox에 "csr" 권한 추가
    private final JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"user", "admin", "csr"});

    public UserAddDialog(AdminUserManagementFrame parentFrame, UserController userController) {
        super(parentFrame, "사용자 추가", true);
        this.parentFrame = parentFrame;
        this.userController = userController;

        // UI 설정
        setSize(400, 350);
        setResizable(false);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());

        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * 입력 필드 배치를 위한 UI 컴포넌트 생성 (GridBagLayout 사용)
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel[] labels = {
                new JLabel("ID:"), new JLabel("비밀번호:"), new JLabel("이름:"),
                new JLabel("연락처:"), new JLabel("나이:"), new JLabel("권한:")
        };

        Component[] fields = {
                idField, pwField, nameField, numberField, ageField, roleComboBox
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            panel.add(labels[i], gbc);

            gbc.gridx = 1; gbc.gridy = i; gbc.weightx = 0.7;
            panel.add(fields[i], gbc);
        }

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    /**
     * 버튼 배치를 위한 UI 컴포넌트 생성 (추가/취소)
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton("추가");
        addButton.addActionListener(e -> addUser()); // addUser 로직 연결

        JButton cancelButton = new JButton("취소");
        cancelButton.addActionListener(e -> dispose()); // 다이얼로그 닫기

        panel.add(addButton);
        panel.add(cancelButton);
        return panel;
    }

    /**
     * [액션 핸들러] 사용자 추가 로직 (SFR-202, 203, 204)
     * <p>
     * 입력값의 유효성 검증을 수행하고, 성공 시 서버에 NetworkMessage를 전송한다.
     * </p>
     */
    private void addUser() {
        String id = idField.getText().trim();
        String pw = new String(pwField.getPassword());
        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        String ageStr = ageField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // 1. 유효성 검사 (Validation)
        // 필수 필드 Null/Empty 검사
        if (id.isEmpty() || pw.isEmpty() || name.isEmpty() || number.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력해 주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1-1. 연락처 형식 검사: 10~11자리 숫자만 허용 (정규식 ^\d{10,11}$)
        if (!Pattern.matches("^\\d{10,11}$", number)) {
            JOptionPane.showMessageDialog(this, "연락처는 10~11자리 숫자만 입력 가능합니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. 나이 형식 검사 및 변환
        int age;
        try {
            age = Integer.parseInt(ageStr);
            // 나이 범위 유효성 검사
            if (age < 0 || age > 150) {
                JOptionPane.showMessageDialog(this, "유효한 나이(0~150)를 입력해야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "나이는 숫자로 입력해야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. User 객체 생성
        // User 모델의 생성자에 평문 비밀번호가 담겨 서버로 전송된다. (평문 전송 가정)
        User newUser = new User(id, pw, name, number, age, role);

        // 4. UserController의 signUp(User) 오버로드 메서드 호출 (서버 통신 수행)
        // 서버 응답은 NetworkMessage 객체로 수신
        NetworkMessage res = userController.signUp(newUser);

        if (res.isSuccess()) {
            // 성공 (SFR-204)
            JOptionPane.showMessageDialog(this, "사용자 [" + id + "]가 성공적으로 추가되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            // 부모 창의 사용자 목록을 새로고침 요청
            parentFrame.loadUserData();
            dispose();
        } else {
            // 실패 시 UserController에서 반환된 오류 메시지 확인 및 사용자 피드백
            String errorMessage = res.getMessage();

            if ("통신 오류".equals(errorMessage)) {
                // 명시적인 통신 오류 처리 (e.g., 서버 연결 실패)
                JOptionPane.showMessageDialog(this, "서버와의 통신에 실패했습니다. HMSServer가 실행 중인지 확인하세요.", "통신 오류", JOptionPane.ERROR_MESSAGE);
            } else if (errorMessage != null && errorMessage.contains("ID중복")) {
                // 서버에서 ID 중복 오류 발생 시 (서버 응답 코드 1)
                JOptionPane.showMessageDialog(this, "이미 존재하는 ID입니다. 다른 ID를 사용해 주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            } else if (errorMessage != null && errorMessage.contains("저장실패")) {
                // 서버에서 저장 실패 오류 발생 시 (서버 응답 코드 2)
                JOptionPane.showMessageDialog(this, "서버에서 사용자 데이터 저장에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } else {
                // 기타 오류 (서버 응답 코드 99 등)
                String displayMsg = (errorMessage != null) ? errorMessage : "알 수 없는 오류가 발생했습니다.";
                JOptionPane.showMessageDialog(this, "사용자 추가 중 오류가 발생했습니다. (" + displayMsg + ")", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}