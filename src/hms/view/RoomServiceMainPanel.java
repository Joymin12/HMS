package hms.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * [관리자] 룸서비스 관리 창이 열렸을 때 가장 먼저 보이는 메인 선택 패널.
 */
public class RoomServiceMainPanel extends JPanel {

    private final RoomServiceOrderFrame parentFrame;

    // ⭐ [NEW] 새로운 뷰 이름 상수 정의 (RoomServiceOrderFrame에 정의된 것을 가져와 사용)
    private static final String REQUESTS_VIEW = "RequestsView";
    private static final String MENU_MANAGE_VIEW = "MenuManageView";
    private static final String ADD_REQUEST_VIEW = "AddRequestView"; // ⭐ 요청 추가 뷰 상수

    public RoomServiceMainPanel(RoomServiceOrderFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new GridBagLayout()); // 중앙 정렬 및 배치를 위해 GridBagLayout 사용

        JLabel titleLabel = new JLabel("🍽️ 룸서비스 관리 메인");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        JButton requestButton = createMenuButton("📋 요청 목록 확인 및 처리", REQUESTS_VIEW);
        JButton menuManageButton = createMenuButton("📝 메뉴 항목 추가/수정/삭제", MENU_MANAGE_VIEW);

        // ⭐ [CRITICAL] 요청 추가 버튼 생성
        JButton addRequestButton = createMenuButton("➕ 고객 요청 추가 (관리용)", ADD_REQUEST_VIEW);

        // GridBagConstraints 설정
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 타이틀 (gridy=0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // 2. 요청 목록 버튼 (gridy=1, gridx=0)
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.ipadx = 50; // 버튼 폭 확장
        gbc.ipady = 30; // 버튼 높이 확장
        add(requestButton, gbc);

        // 3. 메뉴 관리 버튼 (gridy=1, gridx=1)
        gbc.gridx = 1;
        add(menuManageButton, gbc);

        // ⭐ 4. 요청 추가 버튼 (gridy=2, gridx=0, 2칸 차지)
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // 2칸 폭 사용
        gbc.ipady = 30;
        add(addRequestButton, gbc);


        // 5. 메인 화면으로 돌아가기 버튼 (gridy=3)
        JButton backToAdminButton = new JButton("관리자 메인으로 돌아가기");
        backToAdminButton.addActionListener(e -> parentFrame.returnToAdminMain());

        gbc.gridx = 0;
        gbc.gridy = 3; // ⭐ gridy 증가
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