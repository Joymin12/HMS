package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ReservationCheckFrame extends JFrame {

    private final ReservationController controller;
    private final boolean isAdmin; // ⭐ 관리자 여부 플래그
    private final JFrame parentFrame;

    /**
     * [수정] 생성자가 isAdmin 플래그를 받도록 변경
     * @param parentFrame 돌아갈 메인 화면
     * @param controller 예약 컨트롤러
     * @param isAdmin 관리자 모드 여부 (true = 전체 조회 모드 활성화)
     */
    public ReservationCheckFrame(JFrame parentFrame,
                                 ReservationController controller,
                                 boolean isAdmin) {

        this.controller = controller;
        this.isAdmin = isAdmin; // 플래그 저장
        this.parentFrame = parentFrame;

        // 타이틀에 모드 표시 추가
        setTitle("호텔 예약 조회 및 확인" + (isAdmin ? " (관리자 모드)" : ""));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());

        // ⭐ [핵심] isAdmin 플래그를 ReservationCheckPanel로 전달
        ReservationCheckPanel checkPanel = new ReservationCheckPanel(controller, isAdmin);
        add(checkPanel, BorderLayout.CENTER);

        // 창 닫을 때 메인으로 복귀
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentFrame.setVisible(true);
            }
        });

        setVisible(true);
    }
}