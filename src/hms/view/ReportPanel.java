package hms.view;

import hms.controller.ReportController;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ReportPanel extends JPanel {

    private final ReportController controller;
    private final ReportFrame parentFrame;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JTextArea summaryArea;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public ReportPanel(ReportFrame parentFrame) {
        this.controller = new ReportController();
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📊 호텔 통합 성과 보고서 (매출/점유율/예측)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.add(createInputPanel(), BorderLayout.NORTH);
        centerPanel.add(createSummaryPanel(), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.setBorder(BorderFactory.createTitledBorder("조회 기간 설정"));
        startDateChooser = new JDateChooser(new Date());
        endDateChooser = new JDateChooser(new Date());
        panel.add(new JLabel("시작 날짜:")); panel.add(startDateChooser);
        panel.add(new JLabel("종료 날짜:")); panel.add(endDateChooser);
        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        summaryArea = new JTextArea(15, 40);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("분석 결과"));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSouthPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runBtn = new JButton("▶️ 분석 실행");
        runBtn.addActionListener(e -> generateReport());
        JButton exitBtn = new JButton("나가기");
        exitBtn.addActionListener(e -> parentFrame.returnToAdminMainWithConfirmation());
        p.add(runBtn); p.add(exitBtn);
        return p;
    }

    private void generateReport() {
        Date start = startDateChooser.getDate();
        Date end = endDateChooser.getDate();
        if (start == null || end == null) { JOptionPane.showMessageDialog(this, "날짜를 선택하세요."); return; }
        if (start.after(end)) { JOptionPane.showMessageDialog(this, "시작일이 종료일보다 큽니다."); return; }

        String sStr = formatter.format(start);
        String eStr = formatter.format(end);

        Map<String, Object> data = controller.generateReport(sStr, eStr);

        long roomRev = ((Number) data.getOrDefault("RoomRevenue", 0)).longValue();
        long fnbRev = ((Number) data.getOrDefault("FNBRevenue", 0)).longValue();
        long totalRev = ((Number) data.getOrDefault("TotalRevenue", 0)).longValue();
        double occRate = ((Number) data.getOrDefault("OccupancyRate", 0.0)).doubleValue();
        long occNights = ((Number) data.getOrDefault("OccupiedNights", 0)).longValue();
        long capacity = ((Number) data.getOrDefault("TotalCapacity", 0)).longValue();

        String reportType = start.after(new Date()) ? "[미래 예측 보고서]" : "[실적 보고서]";

        String txt = String.format(
                "===============================================\n" +
                        "  %s (%s ~ %s)\n" +
                        "===============================================\n\n" +
                        " [1] 객실 점유율 (Occupancy)\n" +
                        "     - 총 가용 객실 수: %d 박\n" +
                        "     - 예약된 객실 수:  %d 박\n" +
                        "     ➤ 점유율: %.2f%%\n\n" +
                        " [2] 매출 현황 (Revenue)\n" +
                        "     - 객실 매출: \t%,d 원\n" +
                        "     - 룸서비스: \t%,d 원\n" +
                        "     ➤ 총 매출: \t%,d 원\n" +
                        "===============================================\n",
                reportType, sStr, eStr, capacity, occNights, occRate, roomRev, fnbRev, totalRev
        );
        summaryArea.setText(txt);
    }
}