package hms.view;

import hms.controller.UserController;
import javax.swing.*;
import java.awt.*;

public class AdminMainFrame extends JFrame {

    private final String TITLE = "HMS - 호텔 예약 시스템 (관리자용)";
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private final UserController userController;
    private final String userName;

    public AdminMainFrame(String userName, UserController userController) {
        this.userName = userName;
        this.userController = userController;

        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. 헤더 (로그아웃만 포함) ---
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

    // --- 1. 헤더 (계정탈퇴 버튼 제거) ---
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(150, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("호텔 관리 시스템 (관리자)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        // --- 로그아웃 버튼만 남김 ---
        JButton logoutButton = new JButton("로그아웃");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(150, 0, 0));

        // 로그아웃 액션
        logoutButton.addActionListener(e -> {
            if (userController != null) userController.logout();
            JOptionPane.showMessageDialog(null, "로그아웃 되었습니다.");
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroupPanel.setOpaque(false);
        buttonGroupPanel.add(logoutButton);
        // deleteAccountButton 제거

        panel.add(buttonGroupPanel, BorderLayout.EAST);
        return panel;
    }

    // ... (createMainPanel 등 나머지 코드는 이전 AdminMainFrame과 동일) ...
    private JPanel createMainPanel(String userName) {
        // ... (버튼 연결 로직 동일) ...
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(new Color(255, 230, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // 환영 메시지
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createLineBorder(new Color(150, 0, 0), 2));
        String welcomeText = "<html><h2 style='margin-bottom: 4px;'>관리자, " + userName + "님!</h2><p>호텔 운영 시스템에 오신 것을 환영합니다. 모든 기능을 사용할 수 있습니다.</p></html>";
        JLabel welcomeLabel = new JLabel(welcomeText);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        welcomePanel.add(welcomeLabel);
        panel.add(welcomePanel, BorderLayout.NORTH);

        // --- 6개 버튼 그리드 (2행 3열) ---
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setOpaque(false);

        // 고객 기능
        JButton btnReservation = createMenuButton("예약 (고객 대리)");
        JButton btnReservationCheck = createMenuButton("예약 전체 조회");
        JButton btnRoomService = createMenuButton("룸서비스 요청 확인");

        // 관리자 전용 기능
        JButton btnCheckInOut = createMenuButton("🚪 체크인/아웃 관리");
        JButton btnRoomManagement = createMenuButton("🔑 객실/가격 관리");
        JButton btnReport = createMenuButton("📊 매출 보고서");

        // --- 액션 리스너 연결 ---

        // 예약 생성/조회
        btnReservation.addActionListener(e -> {
            this.setVisible(false);
            new ReservationFrame(this);
        });
        btnReservationCheck.addActionListener(e -> {
            this.setVisible(false);
            new ReservationCheckFrame(this);
        });

        // 룸서비스 요청 확인 및 기타 임시 메시지들
        btnRoomService.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "룸서비스 요청 목록 관리 화면은 준비 중입니다.", "기능 안내", JOptionPane.INFORMATION_MESSAGE);
        });

        btnCheckInOut.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "체크인/아웃 관리 화면은 준비 중입니다.", "기능 안내", JOptionPane.INFORMATION_MESSAGE);
        });

        btnRoomManagement.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "객실 및 가격 관리 화면은 준비 중입니다.", "기능 안내", JOptionPane.INFORMATION_MESSAGE);
        });

        btnReport.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "매출 및 예약 보고서 화면은 준비 중입니다.", "기능 안내", JOptionPane.INFORMATION_MESSAGE);
        });


        gridPanel.add(btnReservation);
        gridPanel.add(btnReservationCheck);
        gridPanel.add(btnRoomService);
        gridPanel.add(btnCheckInOut);
        gridPanel.add(btnRoomManagement);
        gridPanel.add(btnReport);

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- 3. 푸터 ---
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(31, 41, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel footerLabel = new JLabel("호텔 관리 시스템 © 2025");
        footerLabel.setForeground(Color.WHITE);
        panel.add(footerLabel);
        return panel;
    }

    // --- 4. 헬퍼 메소드 (버튼 스타일) ---
    private JButton createMenuButton(String title) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 10));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(150, 0, 0), 2));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        button.add(titleLabel, BorderLayout.CENTER);
        return button;
    }
}