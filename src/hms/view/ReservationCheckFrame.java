package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ReservationCheckFrame extends JFrame {

    private final JFrame parentFrame;
    private final ReservationController controller; // ⭐ [수정 1] 필드로 Controller 추가

    /**
     * [수정] 생성자를 2개의 인수로 변경합니다.
     */
    public ReservationCheckFrame(JFrame parentFrame, ReservationController controller) {
        this.parentFrame = parentFrame;
        this.controller = controller; // ⭐ [수정 2] Controller 객체를 필드에 저장

        setTitle("호텔 예약 조회 및 확인");
        setSize(550, 450);

        // 이전 코드에서 Controller를 내부적으로 생성하는 로직은 제거합니다.
        // Controller는 이미 인수로 주입받았습니다.

        // ----------------------------------------------------
        // ★★★ 핵심 수정: ReservationCheckPanel 생성 시 Controller 전달 ★★★
        // ----------------------------------------------------
        try {
            // ⭐ [수정 3] ReservationCheckPanel 생성자에 controller 객체를 인자로 전달합니다.
            // ReservationCheckPanel 생성자가 Controller를 인자로 받도록 가정하고 수정합니다.
            ReservationCheckPanel checkPanel = new ReservationCheckPanel(this.controller);
            add(checkPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("패널 초기화 오류: 콘솔 확인 필요.", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();

            // 오류 발생 시 부모 프레임을 다시 보이게 하고 창을 닫습니다.
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            }
            dispose();
            return;
        }
        // ----------------------------------------------------

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 창이 닫힐 때 부모 프레임을 다시 보이게 하는 리스너 추가
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                }
            }
        });

        setVisible(true);
    }
}