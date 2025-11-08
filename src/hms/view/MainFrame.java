package hms.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import hms.controller.UserController;

/**
 * 메인 메뉴 화면
 * (★수정: '관리' 버튼이 isCurrentUserAdmin()을 호출)
 */
public class MainFrame extends JFrame {

    private JLabel welcomeLabel;
    private UserController userController;
    private String loggedInUserName;

    public MainFrame(String userName, UserController controller) {
        this.loggedInUserName = userName;
        this.userController = controller;

        setTitle("호텔 관리 시스템 (메인 메뉴)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = createMainPanel(this.loggedInUserName);
        add(mainPanel, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * <header> 생성 (회원탈퇴 버튼 포함, 변경 없음)
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(37, 99, 235));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("호텔 관리 시스템");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        // --- 로그아웃 버튼 ---
        JButton logoutButton = new JButton("로그아웃");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(37, 99, 235));
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "로그아웃 되었습니다.");
                dispose();
                new LoginFrame();
            }
        });

        // --- 회원탈퇴 버튼 ---
        JButton deleteAccountButton = new JButton("회원탈퇴");
        deleteAccountButton.setBackground(Color.RED);
        deleteAccountButton.setForeground(Color.WHITE);

        deleteAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        null,
                        "정말로 회원탈퇴 하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
                        "회원탈퇴 확인",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (result == JOptionPane.YES_OPTION) {
                    boolean deleteSuccess = userController.deleteAccount();
                    if (deleteSuccess) {
                        JOptionPane.showMessageDialog(null, "회원탈퇴가 완료되었습니다.");
                        dispose();
                        new LoginFrame();
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "회원탈퇴 중 오류가 발생했습니다.",
                                "오류",
                                JOptionPane.ERROR_MESSAGE);
                    }
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

    /**
     * <main> 생성
     */
    private JPanel createMainPanel(String userName) {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(new Color(243, 244, 246));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // ... ('환영합니다' 박스, 변경 없음) ...
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        String welcomeText = "<html><h2 style='margin-bottom: 4px;'>안녕하세요! " + userName + "님!</h2><p>호텔 관리 시스템에 오신 것을 환영합니다.</p></html>";
        welcomeLabel = new JLabel(welcomeText);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        welcomePanel.add(welcomeLabel);
        panel.add(welcomePanel, BorderLayout.NORTH);

        // --- 6개 버튼 그리드 ---
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setOpaque(false);

        JButton btnReservation = createMenuButton("예약");
        JButton btnReservationCheck = createMenuButton("예약조회");
        JButton btnRoomService = createMenuButton("룸서비스");
        JButton btnCheckIn = createMenuButton("체크인");
        JButton btnCheckOut = createMenuButton("체크아웃");
        JButton btnManagement = createMenuButton("관리");

        // ... (다른 버튼 5개는 변경 없음) ...
        btnReservation.addActionListener(e -> JOptionPane.showMessageDialog(this, "예약 화면으로 이동"));
        btnReservationCheck.addActionListener(e -> JOptionPane.showMessageDialog(this, "예약조회 화면으로 이동"));
        btnRoomService.addActionListener(e -> JOptionPane.showMessageDialog(this, "룸서비스 화면으로 이동"));
        btnCheckIn.addActionListener(e -> JOptionPane.showMessageDialog(this, "체크인 화면으로 이동"));
        btnCheckOut.addActionListener(e -> JOptionPane.showMessageDialog(this, "체크아웃 화면으로 이동"));


        // (★★★ '관리' 버튼 로직 수정 ★★★)
        btnManagement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // (수정) 임시 팝업 대신 '컨트롤러'에게 물어봅니다.
                if (userController.isCurrentUserAdmin()) {
                    // 관리자가 맞으면
                    JOptionPane.showMessageDialog(null, "관리 화면으로 이동합니다.");
                    // TODO: new ManagementFrame().setVisible(true);
                    // (나중에 ManagementFrame을 만들고 이 팝업 대신 띄우면 됩니다)
                } else {
                    // 일반 사용자면
                    JOptionPane.showMessageDialog(null,
                            "관리자가 아닌 다른 사용자는 사용이 제한됩니다.",
                            "접근 거부",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        gridPanel.add(btnReservation);
        gridPanel.add(btnReservationCheck);
        gridPanel.add(btnRoomService);
        gridPanel.add(btnCheckIn);
        gridPanel.add(btnCheckOut);
        gridPanel.add(btnManagement);

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    // ... (footerPanel 및 createMenuButton 헬퍼 메서드는 변경 없음) ...

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(31, 41, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel footerLabel = new JLabel("호텔 관리 시스템 © 2025");
        footerLabel.setForeground(Color.WHITE);
        panel.add(footerLabel);
        return panel;
    }

    private JButton createMenuButton(String title) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 10));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        button.add(titleLabel, BorderLayout.CENTER);
        return button;
    }
}