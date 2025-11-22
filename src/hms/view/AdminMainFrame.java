package hms.view;

import hms.controller.UserController;
import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
// 실제 프레임 호출을 위해 필요한 import 문을 가정합니다.
// import hms.view.LoginFrame;
// import hms.view.ReservationFrame;
// import hms.view.ReservationCheckFrame;
// import hms.view.RoomServiceOrderFrame;
// import hms.view.CheckInOutFrame;

public class AdminMainFrame extends JFrame {

    private final String TITLE = "HMS - 호텔 예약 시스템 (관리자용)";
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private final UserController userController;
    private final String userName;

    // ⭐ [수정 1] ReservationController 필드를 선언하고 내부에서 생성합니다.
    private final ReservationController reservationController = new ReservationController();

    // ⭐ [수정 2] 생성자 시그니처를 2개의 인자로 복구합니다.
    public AdminMainFrame(String userName, UserController userController) {
        this.userName = userName;
        this.userController = userController;
        // this.reservationController는 필드에서 이미 초기화됨

        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = createMainPanel(userName);
        add(mainPanel, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // --- 1. 헤더 패널 생성 (로그아웃 로직 포함) ---
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(178, 34, 34));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("호텔 예약 시스템 (관리자)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("로그아웃");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(178, 34, 34));

        logoutButton.addActionListener(e -> {
            if (userController != null) userController.logout();
            JOptionPane.showMessageDialog(null, "관리자 계정에서 로그아웃 되었습니다.");
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroupPanel.setOpaque(false);
        buttonGroupPanel.add(logoutButton);

        panel.add(buttonGroupPanel, BorderLayout.EAST);
        return panel;
    }


    // --- 2. 메인 메뉴 패널 (액션 활성화) ---
    private JPanel createMainPanel(String userName) {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(new Color(255, 230, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // 환영 메시지
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createLineBorder(new Color(178, 34, 34), 2));
        String welcomeText = "<html><h2 style='margin-bottom: 4px; color:#b22222;'>환영합니다, " + userName + " 관리자님!</h2><p>호텔 운영 및 관리를 시작하세요.</p></html>";
        JLabel welcomeLabel = new JLabel(welcomeText);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        welcomePanel.add(welcomeLabel);
        panel.add(welcomePanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setOpaque(false);

        JButton btnReservation = createMenuButton("예약 (고객 대리)");
        JButton btnReservationCheck = createMenuButton("예약 전체 조회");
        JButton btnRoomService = createMenuButton("🍽️ 룸서비스 관리");

        JButton btnCheckInOut = createMenuButton("🚪 체크인/아웃 관리");
        JButton btnRoomManagement = createMenuButton("🔑 객실/가격 관리");
        JButton btnReport = createMenuButton("📊 매출 보고서");

        // --- 액션 리스너 연결 ---

        // 1. 예약 생성/대리
        btnReservation.addActionListener(e -> {
            this.setVisible(false);
            new ReservationFrame(this, this.reservationController, this.userController);
        });

        // 2. 예약 전체 조회
        btnReservationCheck.addActionListener(e -> {
            this.setVisible(false);
            new ReservationCheckFrame(this, this.reservationController);
        });

        // 3. 룸서비스 관리
        btnRoomService.addActionListener(e -> {
            this.setVisible(false);
            new RoomServiceOrderFrame(this, this.reservationController);
        });

        // 4. 체크인/아웃 관리
        btnCheckInOut.addActionListener(e -> {
            this.setVisible(false);
            new CheckInOutFrame(this, this.reservationController);
        });

        // 5. 객실/가격 관리 (준비 중 유지)
        btnRoomManagement.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "객실 및 가격 관리 화면 호출 (RoomManagementFrame 구현 필요)", "기능 안내", JOptionPane.INFORMATION_MESSAGE);
        });

        // 6. 매출 보고서 (액션 활성화)
        btnReport.addActionListener(e -> {
            // ⭐ [수정] 현재 창을 숨기고 ReportFrame 호출 시 this(AdminMainFrame)를 인수로 전달합니다.
            this.setVisible(false);
            new ReportFrame(this);
        });



        gridPanel.add(btnReservation);
        gridPanel.add(btnReservationCheck);
        gridPanel.add(btnRoomService); // ⭐ [수정] 이전에 btnReservation.add()가 아닌 gridPanel.add()로 수정했습니다.
        gridPanel.add(btnCheckInOut);
        gridPanel.add(btnRoomManagement);
        gridPanel.add(btnReport);

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- 3. 푸터 (정상 복구) ---
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(240, 240, 240));
        JLabel footerLabel = new JLabel("© 2025 Hotel Management System. All Rights Reserved.");
        footerLabel.setForeground(Color.GRAY);
        panel.add(footerLabel);
        return panel;
    }

    // --- 4. 헬퍼 메소드 (버튼 스타일, 정상 복구) ---
    private JButton createMenuButton(String title) {
        JButton button = new JButton(title);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(178, 34, 34));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 100));
        button.setBorder(BorderFactory.createLineBorder(new Color(178, 34, 34), 2));

        return button;
    }
}