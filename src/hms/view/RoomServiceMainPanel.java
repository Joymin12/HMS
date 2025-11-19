package hms.view;

import javax.swing.*;
import java.awt.*;

/**
 * [관리자] 룸서비스 관리 창이 열렸을 때 가장 먼저 보이는 메인 선택 패널.
 */
public class RoomServiceMainPanel extends JPanel {

    private final RoomServiceOrderFrame parentFrame;

    public RoomServiceMainPanel(RoomServiceOrderFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new GridBagLayout()); // 중앙 정렬 및 배치를 위해 GridBagLayout 사용

        JLabel titleLabel = new JLabel("🍽️ 룸서비스 관리 메인");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        JButton requestButton = createMenuButton("📋 요청 목록 확인 및 처리", RoomServiceOrderFrame.REQUESTS_VIEW);
        JButton menuManageButton = createMenuButton("📝 메뉴 항목 추가/수정/삭제", RoomServiceOrderFrame.MENU_MANAGE_VIEW);

        // GridBagConstraints 설정
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 타이틀
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // 요청 목록 버튼
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.ipadx = 50; // 버튼 폭 확장
        gbc.ipady = 30; // 버튼 높이 확장
        add(requestButton, gbc);

        // 메뉴 관리 버튼
        gbc.gridx = 1;
        add(menuManageButton, gbc);

        // 메인 화면으로 돌아가기 버튼 (옵션)
        JButton backToAdminButton = new JButton("관리자 메인으로 돌아가기");
        backToAdminButton.addActionListener(e -> parentFrame.returnToAdminMain());

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.ipadx = 0;
        gbc.ipady = 10;
        gbc.anchor = GridBagConstraints.SOUTH;
        add(backToAdminButton, gbc);
    }

    private JButton createMenuButton(String text, String viewName) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 16));
        button.setBackground(new Color(240, 240, 240));
        button.addActionListener(e -> parentFrame.switchPanel(viewName));
        return button;
    }
}