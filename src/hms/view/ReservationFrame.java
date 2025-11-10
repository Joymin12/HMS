// hms/view/ReservationFrame.java
package hms.view;

import javax.swing.*;
import java.awt.*;

public class ReservationFrame extends JFrame {

    // 부모 창 타입은 JFrame으로 일반화합니다.
    private final JFrame parentFrame;
    private ReservationManagerPanel managerPanel;

    /**
     * ReservationFrame 생성자: UserMainFrame 또는 AdminMainFrame을 부모로 받습니다.
     */
    public ReservationFrame(JFrame parentFrame) { // ★ 부모 프레임은 JFrame 타입으로 받습니다.
        this.parentFrame = parentFrame;

        setTitle("호텔 관리 시스템 - 새 예약");
        setSize(800, 600);
        // 창 닫기 버튼(X)을 눌러도 바로 종료되지 않도록 설정합니다.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // -------------------------------------------------------------------------
        // ★★★ 핵심 연결: ReservationManagerPanel에 2개의 인수를 전달합니다. ★★★
        // -------------------------------------------------------------------------
        try {
            // this: ReservationFrame 자신 (ManagerPanel의 direct parent)
            // parentFrame: UserMainFrame/AdminMainFrame (ManagerPanel의 ultimate parent)
            managerPanel = new ReservationManagerPanel(this, parentFrame);
            add(managerPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("예약 관리 패널 초기화 오류 발생. 콘솔 확인 필요.", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        // 윈도우 닫기 버튼(X) 클릭 시 ManagerPanel의 취소 로직을 호출합니다.
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // ManagerPanel이 '메인으로 돌아갈지' 확인 후 창을 닫습니다.
                managerPanel.goBackToMain();
            }
        });

        setVisible(true);
    }
}