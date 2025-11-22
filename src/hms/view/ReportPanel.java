// 파일 경로: hms/view/ReportPanel.java
package hms.view;

import hms.controller.ReportController;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;

/**
 * [관리자] 매출 보고서 기간 조회 및 결과 출력 패널 (SFR-503).
 */
public class ReportPanel extends JPanel {

    private final ReportController controller;
    // ⭐ [추가] ReportFrame 부모 프레임 필드
    private final ReportFrame parentFrame;

    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JTextArea summaryArea;

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public ReportPanel(ReportFrame parentFrame) { // ⭐ 생성자가 ReportFrame을 받도록 수정
        this.controller = new ReportController();
        this.parentFrame = parentFrame; // 부모 프레임 저장

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. 헤더 (타이틀)
        JLabel titleLabel = new JLabel("📊 기간별 매출 및 수익 보고서", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // 2. 중앙 패널 (입력 + 요약)
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.add(createInputPanel(), BorderLayout.NORTH);
        centerPanel.add(createSummaryPanel(), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. 하단 (버튼)
        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.setBorder(BorderFactory.createTitledBorder("조회 기간 설정"));

        startDateChooser = new JDateChooser(new Date());
        endDateChooser = new JDateChooser(new Date());

        panel.add(new JLabel("시작 날짜:"));
        panel.add(startDateChooser);
        panel.add(new JLabel("종료 날짜:"));
        panel.add(endDateChooser);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        summaryArea = new JTextArea(15, 40);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                null, "매출 요약 및 상세 내역", TitledBorder.CENTER, TitledBorder.TOP));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());

        JButton runButton = new JButton("▶️ 보고서 생성 및 조회");
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD, 14f));
        runButton.addActionListener(e -> generateReport());

        // ⭐ [추가] 나가기 버튼 (UX 개선)
        JButton exitButton = new JButton("⬅️ 메인 화면으로 돌아가기");
        exitButton.setFont(exitButton.getFont().deriveFont(Font.BOLD, 14f));
        exitButton.addActionListener(e -> parentFrame.returnToAdminMainWithConfirmation());

        // ⭐ [추가] 버튼 크기 통일을 위한 GridLayout 사용
        JPanel buttonSizingPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonSizingPanel.add(runButton);
        buttonSizingPanel.add(exitButton);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.add(buttonSizingPanel);

        southPanel.add(buttonGroup, BorderLayout.EAST);
        return southPanel;
    }


    private void generateReport() {
        Date startDate = startDateChooser.getDate();
        Date endDate = endDateChooser.getDate();

        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this, "시작일과 종료일을 모두 선택해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "시작일은 종료일보다 빨라야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String startDateStr = formatter.format(startDate);
            String endDateStr = formatter.format(endDate);

            // Controller 호출 및 결과 받기
            Map<String, Long> reportData = controller.generateTotalReport(startDateStr, endDateStr);

            displayReport(reportData, startDateStr, endDateStr);

        } catch (Exception e) {
            summaryArea.setText("보고서 생성 중 심각한 오류 발생. 콘솔을 확인하세요.");
            e.printStackTrace();
        }
    }

    private void displayReport(Map<String, Long> data, String start, String end) {
        long total = data.getOrDefault("TotalRevenue", 0L);
        long room = data.getOrDefault("RoomRevenue", 0L);
        long fnb = data.getOrDefault("FNBRevenue", 0L);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

        String report = String.format(
                "===============================================\n" +
                        "  기간별 매출 보고서 (%s ~ %s)\n" +
                        "===============================================\n\n" +
                        " [1] 총 객실 매출 (Room Revenue):\t %s 원\n" + // ⭐ [수정] 포맷팅 플래그 제거, %s만 사용
                        " [2] 총 식음료 매출 (F&B Revenue):\t %s 원\n" + // ⭐ [수정] 포맷팅 플래그 제거, %s만 사용
                        "-----------------------------------------------\n" +
                        " 최종 총 수익 (Total Revenue):\t %s 원\n" +     // ⭐ [수정] 포맷팅 플래그 제거, %s만 사용
                        "===============================================\n",
                start, end,
                nf.format(room), // nf.format()은 이미 문자열을 반환합니다.
                nf.format(fnb),
                nf.format(total)
        );

        summaryArea.setText(report);
    }
}