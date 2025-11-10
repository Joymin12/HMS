// hms/view/ReservationCheckFrame.java

package hms.view;

import javax.swing.*;
import java.awt.*;

public class ReservationCheckFrame extends JFrame {

    private final JFrame parentFrame;

    public ReservationCheckFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        setTitle("호텔 예약 조회 및 확인");
        setSize(550, 450); // 적당한 크기 설정

        // ----------------------------------------------------
        // ★★★ 핵심 수정: ReservationCheckPanel을 생성하여 프레임에 추가 ★★★
        // ----------------------------------------------------
        try {
            ReservationCheckPanel checkPanel = new ReservationCheckPanel();
            add(checkPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("패널 초기화 오류: 콘솔 확인 필요.", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }
        // ----------------------------------------------------

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 창이 닫힐 때 부모 프레임을 다시 보이게 하는 리스너 추가
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                }
            }
        });

        setVisible(true);
    }
}