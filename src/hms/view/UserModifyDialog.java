package hms.view;

import hms.controller.UserController;
import hms.model.User;
import hms.network.NetworkMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

/**
 * 사용자 정보 수정 다이얼로그 (Dialog)
 * <p>
 * 관리자 모드(AdminUserManagementFrame)와 고객 본인 수정 모드(UserMainFrame)를 모두 지원한다.
 * 데이터를 로드하고 유효성 검사 후, UserController를 통해 서버에 수정 요청을 전송한다.
 * </p>
 * * <h3>주요 기능:</h3>
 * <ul>
 * <li>사용자 정보 조회 및 폼 데이터 바인딩</li>
 * <li>필수 필드 및 형식(나이, 연락처) 유효성 검증</li>
 * <li>관리자 권한을 이용한 사용자 정보 수정 요청 전송</li>
 * </ul>
 * * @author [작성자 이름]
 * @version 1.1 (주석 최적화)
 * @since 2025-11-30
 */
public class UserModifyDialog extends JDialog {

    // 부모 창 타입을 Window로 일반화 (관리자 창, 메인 창 모두 지원)
    private final Window parentFrame;
    private final UserController userController;
    private final String userIdToModify;

    // 모드 구분 플래그: 수정 결과 및 본인 수정 여부
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
    // [1] 관리자용 생성자 (다른 사용자 정보를 수정할 때 사용)
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
    // [2] 고객용 생성자 (본인 정보를 수정할 때 사용)
    // -----------------------------------------------------------
    public UserModifyDialog(JFrame parentFrame, User currentUser) {
        super(parentFrame, "내 정보 관리", true);
        this.parentFrame = parentFrame;
        // 주의: 고객 모드에서는 새로운 UserController 인스턴스를 생성하여 사용 (책임 분리)
        this.userController = new UserController();
        this.userIdToModify = currentUser.getId();
        this.isSelfEdit = true; // 본인 수정 모드 ON

        initUI();
    }

    /**
     * 다이얼로그의 공통 초기화 로직
     * - UI 컴포넌트를 생성하고 데이터를 로드한 후 다이얼로그를 표시한다.
     */
    private void initUI() {
        setSize(450, 420);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        // 수정할 사용자 정보를 서버에서 비동기적으로 가져와 폼에 채운다.
        loadUserDataToForm();

        setVisible(true);
    }

    /**
     * 사용자 정보 입력을 위한 폼 패널을 생성한다.
     * - ID는 수정 불가능하도록 설정한다.
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. ID (수정 불가)
        panel.add(new JLabel("ID (수정 불가):", SwingConstants.RIGHT));
        idField = new JTextField(userIdToModify);
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240));
        panel.add(idField);

        // 2. 비밀번호 (새 비밀번호 입력 필드)
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
        // 현재 시스템에서 사용되는 3가지 역할 정의
        String[] roles = {"admin", "csr", "user"};
        roleComboBox = new JComboBox<>(roles);

        // 본인 수정 모드라면 권한 필드를 잠가서 역할 변경을 방지
        if (isSelfEdit) {
            roleComboBox.setEnabled(false);
        }
        panel.add(roleComboBox);

        return panel;
    }

    /**
     * 수정 및 취소 버튼 패널을 생성한다.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        String btnText = isSelfEdit ? "내 정보 저장" : "정보 수정";
        JButton modifyButton = new JButton(btnText);
        modifyButton.setBackground(new Color(70, 130, 180));
        modifyButton.setForeground(Color.WHITE);
        modifyButton.addActionListener(this::handleModify); // 수정 로직 연결

        JButton cancelButton = new JButton("취소");
        cancelButton.setBackground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose()); // 창 닫기

        panel.add(modifyButton);
        panel.add(cancelButton);
        return panel;
    }

    /**
     * 서버에서 userIdToModify에 해당하는 사용자 정보를 조회하여 폼에 채운다.
     * - UserController의 getUserById(String) 메서드를 사용한다.
     * - 서버로부터 User 객체를 수신하고 폼 필드에 바인딩한다.
     */
    private void loadUserDataToForm() {
        // 서버 통신을 통해 User 객체 로드
        User user = userController.getUserById(userIdToModify);

        if (user != null) {
            // 조회 성공 시 필드에 데이터 바인딩
            nameField.setText(user.getName());
            numberField.setText(user.getPhoneNumber());
            ageField.setText(String.valueOf(user.getAge()));
            roleComboBox.setSelectedItem(user.getRole());
            // 비밀번호 필드에 현재 값 로드 (평문 전송 로직 가정)
            pwField.setText(user.getPassword());
        } else {
            // 조회 실패 시 사용자에게 알림 후 다이얼로그 닫기
            JOptionPane.showMessageDialog(this, "정보를 불러오는데 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    /**
     * [액션 핸들러] 수정 버튼 클릭 시 유효성 검사 및 서버 수정 요청을 수행한다.
     * - 모든 필드의 Null 여부와 형식(연락처, 나이)을 검증한다.
     * - UserController의 updateUserByAdmin(User) 메서드를 통해 서버에 요청을 전송한다.
     * * @param e 액션 이벤트
     */
    private void handleModify(ActionEvent e) {
        String id = idField.getText().trim();
        String newPw = new String(pwField.getPassword()).trim();
        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        String ageStr = ageField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // 1. 유효성 검증 (Validation)
        // 필수 필드 검사
        if (newPw.isEmpty() || name.isEmpty() || number.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 나이 형식 검사 (숫자 여부)
        if (!Pattern.matches("^\\d+$", ageStr)) {
            JOptionPane.showMessageDialog(this, "나이는 숫자만 입력 가능합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 연락처 형식 검사 (예: 010-1234-5678 형태의 정규식 검사)
        if (!Pattern.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$", number)) {
            JOptionPane.showMessageDialog(this, "연락처 형식이 올바르지 않습니다.\n예: 010-1234-5678", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            // 나이 유효 범위 추가 검사 (선택 사항)
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "나이 형식이 올바르지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. User 객체 생성 및 서버 수정 요청
        // User 객체 생성 (평문 비밀번호를 포함)
        User updatedUser = new User(id, newPw, name, number, age, role);

        // 서버에 수정 요청 전송 (USER_UPDATE_ADMIN 명령 호출)
        boolean success = userController.updateUserByAdmin(updatedUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "정보가 수정되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            isUpdated = true;

            // 관리자 창에서 열린 경우, 부모 창의 목록을 갱신 요청 (테이블 리프레시)
            if (parentFrame instanceof AdminUserManagementFrame) {
                ((AdminUserManagementFrame) parentFrame).loadUserData();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "수정에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 외부(UserMainFrame)에서 이 다이얼로그의 수정 성공 여부를 확인하기 위한 Getter 메서드
     * @return 수정 성공 시 true, 아니면 false
     */
    public boolean isUpdated() {
        return isUpdated;
    }
}