package hms.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import hms.controller.UserController;
import hms.model.User; // (★ 임포트 필요)
import java.util.List; // (★ 임포트 필요)
import java.util.ArrayList; // (★ 임포트 필요)

/**
 * 메인 메뉴 화면
 * (★수정: '관리' 버튼이 '사용자 목록'을 보여주고 선택하여 삭제)
 */
public class MainFrame extends JFrame {

    // ▼▼▼ (오류 원인: 이 부분이 누락되었습니다) ▼▼▼
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
    // ▲▲▲ (여기까지) ▲▲▲

    /**
     * <header> 생성 (회원탈퇴 버튼 포함)
     */
    private JPanel createHeaderPanel() {
        // ▼▼▼ (오류 원인: 이 부분이 누락되었습니다) ▼▼▼
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
        // ▲▲▲ (여기까지) ▲▲▲

        // (님께서 붙여넣으신 코드의 시작 부분)
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
        // (님이 붙여넣으신 옛날 코드가 아닌, 최종 기능이 적용된 코드입니다)
        btnManagement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // 1. 관리자인지 확인
                if (userController.isCurrentUserAdmin()) {

                    // 2. '사용자 추가' / '사용자 삭제' 선택창 띄우기
                    String[] options = {"사용자 추가", "사용자 삭제", "취소"};
                    int choice = JOptionPane.showOptionDialog(
                            null,
                            "어떤 작업을 수행하시겠습니까?",
                            "사용자 관리",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

                    if (choice == 0) { // 0: 사용자 추가
                        // '회원가입' 창을 재사용합니다.
                        new SignUpFrame();

                    } else if (choice == 1) { // 1: 사용자 삭제
                        // '사용자 삭제' 로직 시작

                        // 3. 컨트롤러로부터 모든 사용자 목록을 가져옴
                        List<User> userList = userController.getAllUsersList();

                        // 4. 목록에서 'admin'과 '현재 로그인한 관리자'는 제거
                        List<String> deletableUserList = new ArrayList<>();
                        String currentAdminId = userController.getCurrentlyLoggedInUser().getId();

                        for (User user : userList) {
                            // 'admin' 계정이 아니고, '현재 로그인한 나'도 아니어야 함
                            if (!user.getId().equals("admin") && !user.getId().equals(currentAdminId)) {
                                deletableUserList.add(user.getId() + " (" + user.getName() + ")");
                            }
                        }

                        // 5. 삭제할 유저가 없으면 알림
                        if (deletableUserList.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "삭제할 수 있는 사용자가 없습니다.");
                            return; // 작업 종료
                        }

                        // 6. 선택 가능한 목록(드롭다운)으로 변환
                        Object[] selectionValues = deletableUserList.toArray();

                        // 7. 드롭다운 팝업창 띄우기
                        String selectedValue = (String) JOptionPane.showInputDialog(
                                null,
                                "삭제할 사용자를 선택하세요:",
                                "사용자 삭제",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                selectionValues, // (선택 목록)
                                selectionValues[0] // (기본 선택값)
                        );

                        // 8. 사용자가 '취소'가 아닌 '확인'을 눌렀다면
                        if (selectedValue != null) {
                            // "korea (한국인)" -> "korea" (ID만 추출)
                            String idToDelete = selectedValue.split(" ")[0];

                            // 9. 삭제 실행
                            boolean deleteSuccess = userController.deleteUserByAdmin(idToDelete);

                            if (deleteSuccess) {
                                JOptionPane.showMessageDialog(null, idToDelete + " 사용자가 삭제되었습니다.");
                            } else {
                                JOptionPane.showMessageDialog(null, "삭제에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                    // choice == 2 (취소)는 아무 작업도 하지 않음

                } else {
                    // 관리자가 아닐 때
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