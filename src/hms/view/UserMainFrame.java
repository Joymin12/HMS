package hms.view;

import hms.controller.UserController;
import hms.controller.ReservationController; // ReservationController import 유지
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 일반 사용자(User)에게 노출되는 메인 프레임입니다.
 */
public class UserMainFrame extends JFrame {

    private final String TITLE = "HMS - 호텔 예약 시스템";
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private final UserController userController;
    private final String userName;
    // ⭐ [수정] ReservationController 필드를 선언하고 내부에서 생성합니다.
    private final ReservationController reservationController = new ReservationController();

    // ⭐ [추가] 인증된 객실 번호를 임시로 저장할 필드
    private String authenticatedRoomNumber = null;

    /**
     * [수정된 부분] 🚨 ReservationController 인수를 제거하고 2개의 인수만 받습니다.
     */
    public UserMainFrame(String userName, UserController userController) {
        this.userName = userName;
        this.userController = userController;
        // this.reservationController는 필드에서 이미 초기화됨

        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        // AdminMainFrame과 동일하게 EXIT_ON_CLOSE를 유지합니다.
        // (단, 버튼 클릭 시 프로그램이 완전히 종료되는 문제는 이전처럼 DISPOSE_ON_CLOSE로 해결해야 함을 참고하세요.)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. 헤더 (로그아웃/탈퇴 버튼 포함) ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. 메인 메뉴 패널 ---
        JPanel mainPanel = createMainPanel(userName);
        add(mainPanel, BorderLayout.CENTER);

        // --- 3. 푸터 ---
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // --- 1. 헤더 (회원탈퇴 버튼 추가) ---
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 144, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("호텔 예약 시스템");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        // --- 로그아웃/계정탈퇴 버튼 ---
        JButton logoutButton = new JButton("로그아웃");
        JButton deleteAccountButton = new JButton("계정탈퇴");

        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(30, 144, 255));
        deleteAccountButton.setBackground(Color.RED);
        deleteAccountButton.setForeground(Color.WHITE);

        // --- 1-1. 로그아웃 액션 ---
        logoutButton.addActionListener(e -> {
            if (userController != null) userController.logout();
            JOptionPane.showMessageDialog(null, "로그아웃 되었습니다.");
            dispose();
            new LoginFrame().setVisible(true);
        });

        // --- 1-2. 회원탈퇴 액션 ---
        deleteAccountButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    null, "정말로 계정을 탈퇴하시겠습니까?\n모든 정보가 삭제됩니다.", "계정 탈퇴 확인",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                boolean deleteSuccess = userController.deleteAccount();

                if (deleteSuccess) {
                    JOptionPane.showMessageDialog(null, "회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.");
                    dispose();
                    new LoginFrame().setVisible(true); // 로그인 화면으로 복귀
                } else {
                    JOptionPane.showMessageDialog(null, "탈퇴 중 오류가 발생했습니다. (예: 활성화된 예약이 남아있습니다)");
                }
            }
        });

        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroupPanel.setOpaque(false);
        buttonGroupPanel.add(logoutButton);
        buttonGroupPanel.add(deleteAccountButton);

        panel.add(buttonGroupPanel, BorderLayout.EAST);
        return panel;
    }


    // --- 2. 메인 패널 (고객 메뉴 포함) ---
    private JPanel createMainPanel(String userName) {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // 환영 메시지
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createLineBorder(new Color(30, 144, 255), 2));
        String welcomeText = "<html><h2 style='margin-bottom: 4px; color:#3090ff;'>환영합니다, " + userName + " 고객님!</h2><p>호텔 예약 및 서비스 이용이 가능합니다.</p></html>";
        JLabel welcomeLabel = new JLabel(welcomeText);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        welcomePanel.add(welcomeLabel);
        panel.add(welcomePanel, BorderLayout.NORTH);

        // --- 4개 버튼 그리드 (2행 2열) ---
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setOpaque(false);

        JButton btnReservation = createMenuButton("✅ 신규 예약");
        JButton btnReservationCheck = createMenuButton("🗓️ 예약 조회/변경");
        JButton btnRoomService = createMenuButton("🍽️ 룸서비스 주문");
        JButton btnMyInfo = createMenuButton("👤 내 정보 관리");

        // --- 액션 리스너 연결 ---
        // ⭐ [수정] ReservationFrame과 ReservationCheckFrame 호출 시 컨트롤러 인자 전달 로직 유지
        btnReservation.addActionListener(e -> {
            this.setVisible(false);
            new ReservationFrame(this, this.reservationController, this.userController);
        });

        btnReservationCheck.addActionListener(e -> {
            this.setVisible(false);
            new ReservationCheckFrame(this, this.reservationController);
        });

        // 룸서비스 주문 액션 (임시 메시지)
        btnRoomService.addActionListener(e -> {

            // 1. 커스텀 UI를 위한 JTextField 생성
            JTextField idField = new JTextField(6);
            JTextField roomField = new JTextField(5);

            // 2. 인증 패널 레이아웃 생성
            JPanel authPanel = new JPanel(new BorderLayout(10, 10));
            authPanel.add(new JLabel("<html><h3>객실 인증</h3>룸서비스를 이용하려면 예약 ID 6자리와 객실 번호를 입력해주세요.</html>"), BorderLayout.NORTH);

            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            inputPanel.add(new JLabel("인증번호 (6자리):"));
            inputPanel.add(idField);
            inputPanel.add(new JLabel("객실번호:"));
            inputPanel.add(roomField);

            authPanel.add(inputPanel, BorderLayout.CENTER);

            // 3. JOptionPane을 사용하여 커스텀 입력 다이얼로그 표시
            int result = JOptionPane.showConfirmDialog(this,
                    authPanel,
                    "🍽️ 룸서비스 객실 인증",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String lastSixDigits = idField.getText().trim();
                String roomNumber = roomField.getText().trim();

                // 4. 입력 유효성 검사
                if (!lastSixDigits.matches("\\d{6}") || roomNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "인증번호는 6자리 숫자, 객실 번호는 필수 입력입니다.",
                            "입력 오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 5. ReservationController를 통한 인증 호출
                boolean isAuthenticated = reservationController.validateReservationAndCheckIn(lastSixDigits, roomNumber);

                if (isAuthenticated) {
                    // 6. ⭐ 인증 성공 시: 객실 번호를 필드에 저장
                    this.authenticatedRoomNumber = roomNumber;

                    // 7. 룸서비스 주문 패널 띄우기
                    JDialog dialog = new JDialog(this, "🍽️ 룸서비스 주문", true);
                    JScrollPane scrollPane = new JScrollPane(new RoomServiceOrderPanel(this));
                    dialog.setContentPane(scrollPane);
                    dialog.setSize(750, 700);
                    dialog.setLocationRelativeTo(this);
                    dialog.setVisible(true);

                    // 8. ⭐ 다이얼로그가 닫힐 때 인증 정보 초기화 (안전장치)
                    this.authenticatedRoomNumber = null;

                } else {
                    JOptionPane.showMessageDialog(this,
                            "인증 정보가 일치하지 않거나 해당 객실이 체크인 상태가 아닙니다.",
                            "인증 실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        });



        // 내 정보 관리 액션 (임시 메시지)
        btnMyInfo.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "내 정보 관리 화면은 준비 중입니다.", "기능 안내", JOptionPane.INFORMATION_MESSAGE);
        });


        gridPanel.add(btnReservation);
        gridPanel.add(btnReservationCheck);
        gridPanel.add(btnRoomService);
        gridPanel.add(btnMyInfo);

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- 3. 푸터 ---
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(31, 41, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel footerLabel = new JLabel("호텔 예약 시스템 © 2025");
        footerLabel.setForeground(Color.WHITE);
        panel.add(footerLabel);
        return panel;
    }

    // --- 4. 헬퍼 메소드 (버튼 스타일) ---
    private JButton createMenuButton(String title) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 10));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(30, 144, 255), 2));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        button.add(titleLabel, BorderLayout.CENTER);
        return button;
    }

    // =================================================================
    // ⭐ [추가] 룸서비스 주문을 위한 Getter 메서드
    // =================================================================

    /**
     * 룸서비스 주문 시 인증된 객실 번호를 반환합니다.
     */
    public String getAuthenticatedRoomNumber() {
        return this.authenticatedRoomNumber;
    }

    /**
     * RoomServiceOrderPanel이 ReservationController를 호출할 수 있도록 합니다.
     */
    public ReservationController getReservationController() {
        return reservationController;
    }
}