package hms.view;

import hms.controller.UserController;
import hms.controller.ReservationController;
import hms.model.User; // [추가] User 모델 임포트
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
    private User currentUser; // [변경] 이름(String) 대신 User 객체 저장

    private final ReservationController reservationController = new ReservationController();
    private String authenticatedRoomNumber = null;

    // [추가] 정보를 수정했을 때 갱신하기 위해 필드로 승격
    private JLabel welcomeLabel;

    /**
     * 생성자
     * ⭐ [핵심 수정] String userName -> User user 객체 전체를 받도록 변경
     * (LoginFrame에서 이 창을 열 때 user 객체를 넘겨줘야 합니다)
     */
    public UserMainFrame(User user, UserController userController) {
        this.currentUser = user;
        this.userController = userController;

        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. 헤더 ---
        add(createHeaderPanel(), BorderLayout.NORTH);

        // --- 2. 메인 메뉴 ---
        add(createMainPanel(), BorderLayout.CENTER);

        // --- 3. 푸터 ---
        add(createFooterPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // --- 1. 헤더 ---
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 144, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("호텔 예약 시스템");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroupPanel.setOpaque(false);

        JButton logoutButton = new JButton("로그아웃");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(30, 144, 255));

        JButton deleteAccountButton = new JButton("계정탈퇴");
        deleteAccountButton.setBackground(Color.RED);
        deleteAccountButton.setForeground(Color.WHITE);

        // 로그아웃
        logoutButton.addActionListener(e -> {
            if (userController != null) userController.logout();
            JOptionPane.showMessageDialog(null, "로그아웃 되었습니다.");
            dispose();
            new LoginFrame().setVisible(true);
        });

        // 계정 탈퇴
        deleteAccountButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    null, "정말로 계정을 탈퇴하시겠습니까?\n모든 정보가 삭제됩니다.", "계정 탈퇴 확인",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                // UserController에 deleteAccount(userId) 메서드가 필요할 수 있음
                boolean deleteSuccess = userController.deleteAccount();

                if (deleteSuccess) {
                    JOptionPane.showMessageDialog(null, "회원 탈퇴가 완료되었습니다.");
                    dispose();
                    // new LoginFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "탈퇴 중 오류가 발생했습니다.");
                }
            }
        });

        buttonGroupPanel.add(logoutButton);
        buttonGroupPanel.add(deleteAccountButton);
        panel.add(buttonGroupPanel, BorderLayout.EAST);
        return panel;
    }

    // --- 2. 메인 패널 ---
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // 환영 메시지 패널
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createLineBorder(new Color(30, 144, 255), 2));

        // [수정] 필드로 선언된 welcomeLabel 사용
        welcomeLabel = new JLabel();
        updateWelcomeMessage(); // 메시지 설정 메서드 분리

        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        welcomePanel.add(welcomeLabel);
        panel.add(welcomePanel, BorderLayout.NORTH);

        // 메뉴 버튼 그리드
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setOpaque(false);

        JButton btnReservation = createMenuButton("✅ 신규 예약");
        JButton btnReservationCheck = createMenuButton("🗓️ 예약 조회/변경");
        JButton btnRoomService = createMenuButton("🍽️ 룸서비스 주문");
        JButton btnMyInfo = createMenuButton("👤 내 정보 관리");

        // [이벤트 연결]
        btnReservation.addActionListener(e -> {
            this.setVisible(false);
            // UserController, ReservationController 모두 전달
            new ReservationFrame(this, this.reservationController, this.userController);
        });

        btnReservationCheck.addActionListener(e -> {
            this.setVisible(false);
            new ReservationCheckFrame(this, this.reservationController, false);
        });

        // 룸서비스 주문 (기존 코드 유지)
        btnRoomService.addActionListener(e -> {
            JTextField idField = new JTextField(6);
            JTextField roomField = new JTextField(5);
            JPanel authPanel = new JPanel(new BorderLayout(10, 10));
            authPanel.add(new JLabel("<html><h3>객실 인증</h3>예약 ID 6자리와 객실 번호를 입력해주세요.</html>"), BorderLayout.NORTH);
            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            inputPanel.add(new JLabel("인증번호 (6자리):")); inputPanel.add(idField);
            inputPanel.add(new JLabel("객실번호:")); inputPanel.add(roomField);
            authPanel.add(inputPanel, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(this, authPanel, "룸서비스 인증", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String lastSix = idField.getText().trim();
                String roomNum = roomField.getText().trim();
                if (reservationController.validateReservationAndCheckIn(lastSix, roomNum)) {
                    this.authenticatedRoomNumber = roomNum;
                    JDialog dialog = new JDialog(this, "룸서비스 주문", true);
                    dialog.setContentPane(new JScrollPane(new RoomServiceOrderPanel(this)));
                    dialog.setSize(750, 700);
                    dialog.setLocationRelativeTo(this);
                    dialog.setVisible(true);
                    this.authenticatedRoomNumber = null;
                } else {
                    JOptionPane.showMessageDialog(this, "인증 실패: 정보 불일치 또는 미체크인 상태", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ⭐ [핵심] 내 정보 관리 버튼 연결
        btnMyInfo.addActionListener(e -> {
            // 1. 고객용 수정 창 띄우기 (currentUser 전달)
            UserModifyDialog dialog = new UserModifyDialog(this, currentUser);
            // (dialog 내부에서 setVisible(true)가 호출되어 모달로 뜸)

            // 2. 창이 닫힌 후 수정되었는지 확인
            if (dialog.isUpdated()) {
                // 상단 환영 메시지 갱신 (이름이 바뀌었을 수 있으므로)
                updateWelcomeMessage();
                JOptionPane.showMessageDialog(this, "회원 정보가 갱신되었습니다.");
            }
        });

        gridPanel.add(btnReservation);
        gridPanel.add(btnReservationCheck);
        gridPanel.add(btnRoomService);
        gridPanel.add(btnMyInfo);

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(31, 41, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel footerLabel = new JLabel("호텔 예약 시스템 © 2025");
        footerLabel.setForeground(Color.WHITE);
        panel.add(footerLabel);
        return panel;
    }

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

    // 환영 메시지 업데이트 헬퍼 메서드
    private void updateWelcomeMessage() {
        String name = (currentUser != null) ? currentUser.getName() : "고객";
        welcomeLabel.setText("<html><h2 style='margin-bottom: 4px; color:#3090ff;'>환영합니다, " + name + " 고객님!</h2><p>호텔 예약 및 서비스 이용이 가능합니다.</p></html>");
    }

    // Getters
    public String getAuthenticatedRoomNumber() { return this.authenticatedRoomNumber; }
    public ReservationController getReservationController() { return reservationController; }

    // [추가] 현재 로그인한 유저 정보 반환 (필요시 사용)
    public User getCurrentUser() { return currentUser; }
}