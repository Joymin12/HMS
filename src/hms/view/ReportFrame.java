// 파일 경로: hms/view/ReportFrame.java
package hms.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * [관리자] 매출 보고서 Frame 컨테이너
 */
public class ReportFrame extends JFrame {

    private final JFrame adminMainFrame; // ⭐ 부모 프레임 저장 필드

    public ReportFrame(JFrame adminMainFrame) {
        this.adminMainFrame = adminMainFrame;

        setTitle("호텔 관리 시스템 - 매출 보고서");

        // ⭐ ReportPanel 인스턴스 생성 및 프레임에 추가 (Panel은 이 프레임(this)을 부모로 가짐)
        ReportPanel reportPanel = new ReportPanel(this);
        add(reportPanel, BorderLayout.CENTER);

        // 프레임 설정
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // X 버튼 클릭 시 바로 종료 방지
        setLocationRelativeTo(null);

        // 윈도우 닫기(X) 버튼 클릭 시 확인 메시지 출력
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 부모 창 복귀 로직 호출
                returnToAdminMainWithConfirmation();
            }
        });

        setVisible(true);
    }

    /**
     * View 패널에서 호출하는 메인 화면 복귀 로직 (확인 메시지 포함)
     */
    public void returnToAdminMainWithConfirmation() {
        int result = JOptionPane.showConfirmDialog(this,
                "매출 보고서 창을 닫고 메인 화면으로 돌아가시겠습니까?",
                "창 닫기 확인",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            this.dispose();
            // 부모 프레임 다시 활성화
            if (adminMainFrame != null) {
                adminMainFrame.setVisible(true);
            }
        }
    }
}