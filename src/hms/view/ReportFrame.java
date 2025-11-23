package hms.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ReportFrame extends JFrame {

    private final JFrame adminMainFrame;

    public ReportFrame(JFrame adminMainFrame) {
        this.adminMainFrame = adminMainFrame;
        setTitle("호텔 관리 시스템 - 매출 및 점유율 보고서");

        // ReportPanel을 메인으로 사용
        ReportPanel reportPanel = new ReportPanel(this);
        add(reportPanel, BorderLayout.CENTER);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnToAdminMainWithConfirmation();
            }
        });

        setVisible(true);
    }

    public void returnToAdminMainWithConfirmation() {
        int result = JOptionPane.showConfirmDialog(this, "창을 닫고 메인으로 돌아가시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            this.dispose();
            if (adminMainFrame != null) adminMainFrame.setVisible(true);
        }
    }
}