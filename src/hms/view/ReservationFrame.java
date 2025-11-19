// hms.view.ReservationFrame.java (수정 완료)
package hms.view;

import hms.controller.ReservationController;
import hms.controller.UserController; // User Controller 추가
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ReservationFrame extends JFrame {

    private final JFrame parentFrame;
    private final ReservationController reservationController; // 필드 추가
    private final UserController userController;             // 필드 추가
    private ReservationManagerPanel managerPanel;

    /**
     * ReservationFrame 생성자: UserMainFrame/AdminMainFrame 호출에 맞춰 3개의 인수를 받습니다.
     */
    public ReservationFrame(JFrame parentFrame,
                            ReservationController reservationController,
                            UserController userController) { // ★ 3개 인수로 변경
        this.parentFrame = parentFrame;
        this.reservationController = reservationController;
        this.userController = userController;

        setTitle("호텔 관리 시스템 - 새 예약");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            // ReservationManagerPanel에 4개 인수를 전달하여 Controller 의존성을 주입
            managerPanel = new ReservationManagerPanel(this, parentFrame,
                    reservationController, userController);
            add(managerPanel, BorderLayout.CENTER);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("예약 관리 패널 초기화 오류 발생. 콘솔 확인 필요.", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
            // 오류 발생 시 부모 프레임 복귀 로직 추가
            parentFrame.setVisible(true);
            dispose();
            return;
        }

        // 윈도우 닫기 버튼(X) 클릭 시 ManagerPanel의 취소 로직을 호출합니다.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (managerPanel != null) {
                    managerPanel.goBackToMain(); // ManagerPanel이 취소 확인 후 닫음
                } else {
                    parentFrame.setVisible(true); // 만약 패널 생성 실패 시 바로 부모 복귀
                    dispose();
                }
            }
        });

        setVisible(true);
    }
}