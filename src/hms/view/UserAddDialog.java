package hms.view;

import hms.controller.UserController;

import javax.swing.*;
import java.awt.*;

/**
 * 관리자용 사용자 추가 다이얼로그 (SFR-202)
 */
public class UserAddDialog extends JDialog {

    private final AdminUserManagementFrame parentFrame;
    private final UserController userController;

    // ⭐ [추가] 입력 필드 정의
    private final JTextField idField = new JTextField(15);
    private final JPasswordField pwField = new JPasswordField(15);
    private final JTextField nameField = new JTextField(15);
    private final JTextField numberField = new JTextField(15);
    private final JTextField ageField = new JTextField(15);
    private final JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"user", "admin"});

    public UserAddDialog(AdminUserManagementFrame parentFrame, UserController userController) {
        super(parentFrame, "사용자 추가", true);
        this.parentFrame = parentFrame;
        this.userController = userController;

        setSize(400, 350);
        setResizable(false);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());

        // ⭐ [추가] UI 구성 메서드 호출
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ⭐ [추가] 입력 필드 배치 UI 구성
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

    // ⭐ [추가] 버튼 배치 UI 구성
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton("추가");
        addButton.addActionListener(e -> addUser());

        JButton cancelButton = new JButton("취소");
        cancelButton.addActionListener(e -> dispose());

        panel.add(addButton);
        panel.add(cancelButton);
        return panel;
    }

    // ⭐ 사용자 추가 로직 (SFR-202, 203, 204)
    private void addUser() {
        String id = idField.getText().trim();
        String pw = new String(pwField.getPassword());
        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        String ageStr = ageField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // 유효성 검사 (입력 필드 값 확인)
        if (id.isEmpty() || pw.isEmpty() || name.isEmpty() || number.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력해 주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ⭐ UserController의 addUserByAdmin 호출 (SFR-203)
        int result = userController.addUserByAdmin(id, pw, name, number, ageStr, role);

        switch (result) {
            case 0: // 성공
                JOptionPane.showMessageDialog(this, "사용자 [" + id + "]가 성공적으로 추가되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE); // SFR-204
                parentFrame.loadUserData(); // 부모 테이블 새로고침
                dispose();
                break;
            case 1: // ID 중복 오류
                JOptionPane.showMessageDialog(this, "이미 존재하는 ID입니다. 다른 ID를 사용해 주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                break;
            case 2: // 파일 저장 오류
                JOptionPane.showMessageDialog(this, "데이터 저장 중 오류가 발생했습니다. 파일을 확인하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                break;
            case 3: // 나이 오류
                JOptionPane.showMessageDialog(this, "나이는 숫자로 입력해야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }
}