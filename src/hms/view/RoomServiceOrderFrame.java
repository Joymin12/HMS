package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
/**
 * =================================================================
 * [호텔 관리 시스템 - 룸서비스 관리 최상위 창]
 * 이 프레임은 관리자용 룸서비스 기능을 위한 메인 창이며,
 * CardLayout을 사용하여 요청 목록, 메뉴 관리 등 다양한 서브 패널의 전환을 관리합니다.
 * =================================================================
 */
public class RoomServiceOrderFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);
    private final JFrame adminMainFrame;
    private final ReservationController controller;

    // 상수명을 public static final로 수정하여 외부 접근 오류를 해결했습니다.
    public static final String MAIN_VIEW = "MainView"; // ⭐ 새롭게 추가된 메인 뷰
    public static final String REQUESTS_VIEW = "RequestsView";
    public static final String MENU_MANAGE_VIEW = "MenuManageView";

    public RoomServiceOrderFrame(JFrame adminMainFrame, ReservationController controller) {
        this.adminMainFrame = adminMainFrame;
        this.controller = controller;

        setTitle("🍽️ 룸서비스 관리");
        setSize(800, 600);
        setLocationRelativeTo(adminMainFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 1. 패널 생성
        RoomServiceMainPanel mainPanel = new RoomServiceMainPanel(this); // ⭐ 메인 선택 패널
        ServiceRequestPanel requestPanel = new ServiceRequestPanel(this, controller);
        MenuManagementPanel menuPanel = new MenuManagementPanel(this, controller);

        // 2. CardLayout에 추가
        cardsPanel.add(mainPanel, MAIN_VIEW); // ⭐ 메인 패널을 가장 먼저 추가
        cardsPanel.add(requestPanel, REQUESTS_VIEW);
        cardsPanel.add(menuPanel, MENU_MANAGE_VIEW);

        add(cardsPanel, BorderLayout.CENTER);

        // 초기 화면 설정: 메인 선택 화면이 먼저 보이도록 변경
        cardLayout.show(cardsPanel, MAIN_VIEW);

        setVisible(true);
    }

    /**
     * 패널 전환 메서드
     */
    public void switchPanel(String viewName) {
        cardLayout.show(cardsPanel, viewName);
    }

    /**
     * 창 닫기 시 AdminMainFrame으로 복귀 (혹은 프레임 종료)
     */
    public void returnToAdminMain() {
        this.dispose(); // 현재 창 닫기

        // ⭐ [필수 로직] 부모 창을 다시 보이게 하여 프로그램 종료를 막습니다.
        if (adminMainFrame != null) {
            adminMainFrame.setVisible(true);
        }
    }
}