package hms.view;

import hms.controller.UserController;
import hms.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

public class UserModifyDialog extends JDialog {

    private final AdminUserManagementFrame parentFrame;
    private final UserController userController;
    private final String userIdToModify;

    // GUI 컴포넌트
    private JTextField idField;
    private JPasswordField pwField;
    private JTextField nameField;
    private JTextField numberField;
    private JTextField ageField;
    private JComboBox<String> roleComboBox;

    public UserModifyDialog(AdminUserManagementFrame parentFrame, UserController userController, String userIdToModify) {
        super(parentFrame, "사용자 정보 수정 (ID: " + userIdToModify + ")", true);
        this.parentFrame = parentFrame;
        this.userController = userController;
        this.userIdToModify = userIdToModify;

        setSize(450, 400);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 폼 패널 생성 및 중앙에 배치
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        // 버튼 패널 생성 및 하단에 배치
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        // 초기 데이터 로드
        loadUserDataToForm();

        setVisible(true);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. ID
        panel.add(new JLabel("ID (수정 불가):", SwingConstants.RIGHT));
        idField = new JTextField(userIdToModify);
        idField.setEditable(false); // ID는 수정 불가
        panel.add(idField);

        // 2. 비밀번호
        panel.add(new JLabel("새 비밀번호:", SwingConstants.RIGHT));
        pwField = new JPasswordField();
        panel.add(pwField);

        // 3. 이름
        panel.add(new JLabel("이름:", SwingConstants.RIGHT));
        nameField = new JTextField();
        panel.add(nameField);

        // 4. 연락처
        panel.add(new JLabel("연락처:", SwingConstants.RIGHT));
        numberField = new JTextField();
        panel.add(numberField);

        // 5. 나이
        panel.add(new JLabel("나이:", SwingConstants.RIGHT));
        ageField = new JTextField();
        panel.add(ageField);

        // 6. 권한
        panel.add(new JLabel("권한:", SwingConstants.RIGHT));
        String[] roles = {"admin", "user"};
        roleComboBox = new JComboBox<>(roles);
        panel.add(roleComboBox);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton modifyButton = new JButton("정보 수정");
        modifyButton.addActionListener(this::handleModify);

        JButton cancelButton = new JButton("취소");
        cancelButton.addActionListener(e -> dispose());

        panel.add(modifyButton);
        panel.add(cancelButton);
        return panel;
    }

    // 서버에서 기존 사용자 정보를 불러와 폼에 채우기
    private void loadUserDataToForm() {
        // UserController의 getUserById를 사용하여 서버에서 사용자 정보를 가져옴
        User user = userController.getUserById(userIdToModify);

        if (user != null) {
            // ID는 이미 생성자에서 설정됨
            nameField.setText(user.getName());
            numberField.setText(user.getPhoneNumber());
            // age는 int형이므로 String으로 변환하여 필드에 설정
            ageField.setText(String.valueOf(user.getAge()));
            roleComboBox.setSelectedItem(user.getRole());
            // 보안상 기존 비밀번호를 표시하는 것은 권장되지 않으나, 편의상 불러온 정보를 필드에 설정
            pwField.setText(user.getPassword());
        } else {
            JOptionPane.showMessageDialog(this, "수정할 사용자 정보를 불러오는 데 실패했습니다. (ID 오류 또는 통신 오류)", "오류", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void handleModify(ActionEvent e) {
        String id = idField.getText().trim();
        // pwField에 입력된 값 (새 비밀번호이거나, 불러온 기존 비밀번호)을 사용
        String newPw = new String(pwField.getPassword()).trim();
        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        String ageStr = ageField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // 1. 필수 필드 검증
        if (newPw.isEmpty() || name.isEmpty() || number.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. 나이 및 연락처 형식 검증
        if (!Pattern.matches("^\\d+$", ageStr)) {
            JOptionPane.showMessageDialog(this, "나이는 숫자만 입력 가능합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Pattern.matches("^\\d{10,11}$", number)) {
            JOptionPane.showMessageDialog(this, "연락처는 10~11자리 숫자만 입력 가능합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            // Pattern.matches 때문에 발생할 가능성은 낮지만, 혹시 모를 상황 대비
            JOptionPane.showMessageDialog(this, "나이 형식이 올바르지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ⭐ 3. User 객체 생성 및 인자 1개로 Controller 호출 ⭐
        // User 클래스 생성자 가정: User(id, pw, name, phoneNumber, age, role)
        // 실제 User 클래스의 생성자 형태에 따라 인자 수를 조정해야 합니다.
        // 현재 뷰에서 얻은 6개의 인자를 사용하여 User 객체를 생성합니다.
        User updatedUser = new User(id, newPw, name, number, age, role);

        // UserController의 updateUserByAdmin(User updatedUser)를 호출하고 boolean 결과를 받습니다.
        boolean success = userController.updateUserByAdmin(updatedUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "사용자 [" + id + "] 정보가 성공적으로 수정되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            parentFrame.loadUserData(); // 부모 프레임의 테이블 데이터 새로고침
            dispose();
        } else {
            // 통신 오류, 서버 처리 오류 등 기타 모든 실패 케이스
            JOptionPane.showMessageDialog(this, "사용자 정보 수정 중 오류가 발생했습니다. (서버 통신 또는 처리 오류)", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}