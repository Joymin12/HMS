package hms.view;

import hms.controller.UserController;
import hms.controller.ReservationController;
import hms.controller.RoomController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import hms.view.LoginFrame;
import hms.view.ReservationFrame;
import hms.view.ReservationCheckFrame;
import hms.view.RoomServiceOrderFrame;
import hms.view.CheckInOutFrame;
import hms.view.RoomManagementFrame;
import hms.view.ReportFrame;
import hms.view.AdminUserManagementFrame;

public class AdminMainFrame extends JFrame {

    private final String TITLE = "HMS - 호텔 예약 시스템 (관리자용)";
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private final UserController userController;

    // LoginController에서 전달받는 필드
    private final ReservationController reservationController;
    private final RoomController roomController;
    private final String userRole;
    private final String userName;

    /**
     * 생성자가 4개의 인자를 받습니다.
     */
    public AdminMainFrame(UserController userController,
                          ReservationController resController,
                          RoomController roomController,
                          String role) {

        this.userName = userController.getCurrentlyLoggedInUser().getName();
        this.userController = userController;
        this.reservationController = resController;
        this.roomController = roomController;
        this.userRole = role;

        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

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

    /**
     * ⭐ [핵심 수정] role에 따라 메뉴 접근을 제어하고, 리스너를 올바른 Controller에 연결합니다.
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(new Color(255, 230, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createLineBorder(new Color(178, 34, 34), 2));
        String welcomeText = "<html><h2 style='margin-bottom: 4px; color:#b22222;'>환영합니다, "
                + userName + " 관리자님!</h2><p>호텔 운영 및 관리를 시작하세요.</p></html>";
        JLabel welcomeLabel = new JLabel(welcomeText);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        welcomePanel.add(welcomeLabel);
        panel.add(welcomePanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        gridPanel.setOpaque(false);

        JButton btnReservation = createMenuButton("예약 (고객 대리)");
        JButton btnReservationCheck = createMenuButton("예약 전체 조회");
        JButton btnRoomService = createMenuButton("🍽️ 룸서비스 관리");
        JButton btnCheckInOut = createMenuButton("🚪 체크인/아웃 관리");
        JButton btnRoomManagement = createMenuButton("🔑 객실/가격 관리");
        JButton btnReport = createMenuButton("📊 매출 보고서");
        JButton btnUserManagement = createMenuButton("☑ 사용자 관리");

        // =========================================================
        // ⭐ [핵심 로직] ROLE 기반 버튼 접근 제어
        // =========================================================

        // CSR 역할 확인 (CSR은 '객실/가격 관리', '사용자 관리', '매출 보고서'에 접근 불가)
        boolean isCSR = this.userRole.equals("csr");

        // [이벤트 연결 - ADMIN/CSR 공통 Operational 기능]
        btnReservation.addActionListener(e -> {
            this.setVisible(false);
            new ReservationFrame(this, this.reservationController, this.userController);
        });

        btnReservationCheck.addActionListener(e -> {
            this.setVisible(false);
            new ReservationCheckFrame(this, this.reservationController, true);
        });

        btnRoomService.addActionListener(e -> {
            this.setVisible(false);
            new RoomServiceOrderFrame(this, this.reservationController);
        });

        btnCheckInOut.addActionListener(e -> {
            this.setVisible(false);
            new CheckInOutFrame(this, this.reservationController);
        });

        // 1. 매출 보고서 (ADMIN ONLY) - ⭐ [NEW] 추가된 로직
        if (isCSR) {
            btnReport.setEnabled(false);
            btnReport.setBackground(Color.LIGHT_GRAY);
            btnReport.setText("📊 매출 보고서 (CSR 접근 불가)");
        } else {
            btnReport.addActionListener(e -> {
                this.setVisible(false);
                new ReportFrame(this);
            });
        }

        // 2. 객실/가격 관리 (ADMIN ONLY)
        if (isCSR) {
            btnRoomManagement.setEnabled(false);
            btnRoomManagement.setBackground(Color.LIGHT_GRAY);
            btnRoomManagement.setText("🔑 객실/가격 관리 (CSR 접근 불가)");
        } else {
            btnRoomManagement.addActionListener(e -> {
                this.setVisible(false);
                new RoomManagementFrame(this, this.roomController);
            });
        }

        // 3. 사용자 관리 (ADMIN ONLY)
        if (isCSR) {
            btnUserManagement.setEnabled(false);
            btnUserManagement.setBackground(Color.LIGHT_GRAY);
            btnUserManagement.setText("☑ 사용자 관리 (CSR 접근 불가)");
        } else {
            btnUserManagement.addActionListener(e -> {
                this.setVisible(false);
                new AdminUserManagementFrame(this, this.userController);
            });
        }

        // 모든 버튼을 gridPanel에 추가 (순서는 그대로 유지)
        gridPanel.add(btnReservation);
        gridPanel.add(btnReservationCheck);
        gridPanel.add(btnRoomService);
        gridPanel.add(btnCheckInOut);
        gridPanel.add(btnRoomManagement);
        gridPanel.add(btnReport);
        gridPanel.add(btnUserManagement);

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(240, 240, 240));
        JLabel footerLabel = new JLabel("© 2025 Hotel Management System. All Rights Reserved.");
        footerLabel.setForeground(Color.GRAY);
        panel.add(footerLabel);
        return panel;
    }

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