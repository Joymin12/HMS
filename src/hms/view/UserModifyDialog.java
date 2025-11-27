package hms.view;

import hms.controller.UserController;
import hms.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

public class UserModifyDialog extends JDialog {

    // 부모 창 타입을 Window로 일반화 (관리자 창, 메인 창 모두 지원)
    private final Window parentFrame;
    private final UserController userController;
    private final String userIdToModify;

    // 모드 구분 플래그
    private boolean isSelfEdit = false;
    private boolean isUpdated = false;

    // GUI 컴포넌트
    private JTextField idField;
    private JPasswordField pwField;
    private JTextField nameField;
    private JTextField numberField;
    private JTextField ageField;
    private JComboBox<String> roleComboBox;

    // -----------------------------------------------------------
    // [1] 관리자용 생성자 (기존 코드 호환)
    // -----------------------------------------------------------
    public UserModifyDialog(AdminUserManagementFrame parentFrame, UserController userController, String userIdToModify) {
        super(parentFrame, "사용자 정보 수정 (관리자 모드)", true);
        this.parentFrame = parentFrame;
        this.userController = userController;
        this.userIdToModify = userIdToModify;
        this.isSelfEdit = false;

        initUI();
    }

    // -----------------------------------------------------------
    // ⭐ [2] 고객용 생성자 (UserMainFrame에서 호출)
    // -----------------------------------------------------------
    public UserModifyDialog(JFrame parentFrame, User currentUser) {
        super(parentFrame, "내 정보 관리", true);
        this.parentFrame = parentFrame;
        this.userController = new UserController(); // 컨트롤러 신규 생성
        this.userIdToModify = currentUser.getId();
        this.isSelfEdit = true; // 본인 수정 모드 ON

        initUI();
    }

    // 공통 초기화 메서드
    private void initUI() {
        setSize(450, 420);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 폼 패널
        add(createFormPanel(), BorderLayout.CENTER);

        // 버튼 패널
        add(createButtonPanel(), BorderLayout.SOUTH);

        // 데이터 로드
        loadUserDataToForm();

        setVisible(true);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. ID
        panel.add(new JLabel("ID (수정 불가):", SwingConstants.RIGHT));
        idField = new JTextField(userIdToModify);
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240)); // 회색 배경
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

        // 6. 권한 (고객 모드에서는 비활성화)
        panel.add(new JLabel("권한:", SwingConstants.RIGHT));
        String[] roles = {"admin", "user"};
        roleComboBox = new JComboBox<>(roles);

        // ⭐ 본인 수정 모드라면 권한 변경 불가
        if (isSelfEdit) {
            roleComboBox.setEnabled(false);
        }
        panel.add(roleComboBox);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        String btnText = isSelfEdit ? "내 정보 저장" : "정보 수정";
        JButton modifyButton = new JButton(btnText);
        modifyButton.setBackground(new Color(70, 130, 180));
        modifyButton.setForeground(Color.WHITE);
        modifyButton.addActionListener(this::handleModify);

        JButton cancelButton = new JButton("취소");
        cancelButton.setBackground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose());

        panel.add(modifyButton);
        panel.add(cancelButton);
        return panel;
    }

    private void loadUserDataToForm() {
        User user = userController.getUserById(userIdToModify);

        if (user != null) {
            nameField.setText(user.getName());
            numberField.setText(user.getPhoneNumber());
            ageField.setText(String.valueOf(user.getAge()));
            roleComboBox.setSelectedItem(user.getRole());
            pwField.setText(user.getPassword());
        } else {
            JOptionPane.showMessageDialog(this, "정보를 불러오는데 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void handleModify(ActionEvent e) {
        String id = idField.getText().trim();
        String newPw = new String(pwField.getPassword()).trim();
        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        String ageStr = ageField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // 1. 검증
        if (newPw.isEmpty() || name.isEmpty() || number.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Pattern.matches("^\\d+$", ageStr)) {
            JOptionPane.showMessageDialog(this, "나이는 숫자만 입력 가능합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 예: 010-1234-5678 또는 02-123-4567
        if (!Pattern.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$", number)) {
            JOptionPane.showMessageDialog(this, "연락처 형식이 올바르지 않습니다.\n예: 010-1234-5678", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "나이 형식이 올바르지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. 객체 생성 및 수정 요청
        User updatedUser = new User(id, newPw, name, number, age, role);
        boolean success = userController.updateUserByAdmin(updatedUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "정보가 수정되었습니다.");
            isUpdated = true;

            // 관리자 창에서 열렸다면 테이블 갱신
            if (parentFrame instanceof AdminUserManagementFrame) {
                ((AdminUserManagementFrame) parentFrame).loadUserData();
            }
            // (고객 창의 갱신 처리는 UserMainFrame에서 isUpdated()를 확인하여 수행)

            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "수정에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 메인 화면에서 수정 성공 여부를 확인하기 위한 메서드
    public boolean isUpdated() {
        return isUpdated;
    }
}