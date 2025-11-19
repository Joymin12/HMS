package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 🚪 체크인/아웃 관리를 위한 최상위 프레임입니다.
 * CardLayout을 사용하여 관리 패널과 프로세스 패널 사이의 전환을 관리합니다.
 */
public class CheckInOutFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);
    private final JFrame parentFrame; // AdminMainFrame
    private final ReservationController controller;

    // ⭐ 내부 패널들 (이 필드들은 CheckInProcessPanel에서 사용되지 않으므로, 주입받은 Controller를 사용합니다.)

    // ⭐⭐ [필수 상수] 내부 패널들이 전환 시 사용하는 view 이름 ⭐⭐
    public static final String MANAGEMENT_VIEW = "ManagementView";
    public static final String CHECK_IN_PROCESS_VIEW = "CheckInProcessView";
    public static final String CHECK_OUT_PROCESS_VIEW = "CheckOutProcessView"; // 체크아웃 기능 확장 시 사용

    /**
     * 생성자: AdminMainFrame에서 호출 시 2개의 인수를 받습니다.
     */
    public CheckInOutFrame(JFrame parentFrame, ReservationController controller) {
        this.parentFrame = parentFrame;
        this.controller = controller;

        setTitle("🚪 체크인/아웃 관리");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parentFrame);

        // 부모 창 숨기기
        parentFrame.setVisible(false);

        // 1. 관리 패널 추가 (ManagementPanel은 2개의 인수를 받음)
        CheckInOutManagementPanel managementPanel = new CheckInOutManagementPanel(this, controller);
        cardsPanel.add(managementPanel, MANAGEMENT_VIEW);

        // *주의: CheckInProcessPanel은 예약 ID를 받아야 하므로, switchPanel에서 동적으로 생성합니다.

        cardLayout.show(cardsPanel, MANAGEMENT_VIEW); // 기본 화면 설정

        add(cardsPanel); // cardsPanel을 프레임에 추가

        // 창이 닫힐 때 부모 프레임을 다시 보이게 하는 리스너 추가
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                if (CheckInOutFrame.this.parentFrame != null) {
                    CheckInOutFrame.this.parentFrame.setVisible(true);
                }
            }
        });

        setVisible(true);
    }

    /**
     * 메인 패널을 전환합니다. (CheckInOutManagementPanel에서 호출됨)
     * @param viewName 전환할 뷰의 상수 이름
     * @param data CheckInProcessPanel에 전달할 예약 상세 정보 (String 배열)
     */
    public void switchPanel(String viewName, String[] data) {
        if (viewName.equals(CHECK_IN_PROCESS_VIEW) && data != null) {
            // ⭐ [핵심] CheckInProcessPanel을 동적으로 생성하여 CardLayout에 추가
            CheckInProcessPanel processPanel = new CheckInProcessPanel(this, controller, data);

            // 이전에 같은 이름의 컴포넌트가 있을 경우를 대비하여 제거 후 추가 (안전한 동적 추가)
            cardsPanel.add(processPanel, CHECK_IN_PROCESS_VIEW);
            cardLayout.show(cardsPanel, CHECK_IN_PROCESS_VIEW);
        } else if (viewName.equals(MANAGEMENT_VIEW)) {
            // 관리 화면으로 돌아갈 때
            cardLayout.show(cardsPanel, MANAGEMENT_VIEW);
            // 메모리 정리: 프로세스 뷰를 제거하여 메모리를 확보할 수 있습니다.
            // cardsPanel.remove(cardsPanel.getComponent(cardsPanel.getComponentCount() - 1));
        }
        // TODO: 체크아웃 로직 추가 시 CHECK_OUT_PROCESS_VIEW 처리
        // ⭐⭐ [수정] 체크아웃 로직 추가 시 CHECK_OUT_PROCESS_VIEW 처리 ⭐⭐
        else if (viewName.equals(CHECK_OUT_PROCESS_VIEW) && data != null) {
            // CheckoutProcessPanel을 동적으로 생성하고 추가
            CheckoutProcessPanel checkoutPanel = new CheckoutProcessPanel(this, controller, data);

            // 기존 뷰를 제거하고 새 뷰를 추가하여 메모리 효율성을 높일 수 있습니다.
            cardsPanel.add(checkoutPanel, CHECK_OUT_PROCESS_VIEW);
            cardLayout.show(cardsPanel, CHECK_OUT_PROCESS_VIEW);
        }
    }

    /**
     * 현재 프레임을 닫고 AdminMainFrame을 다시 보이게 합니다.
     * (내부 패널에서 메인으로 완전히 복귀할 때 사용)
     */
    public void returnToAdminMain() {
        this.dispose();
    }
}