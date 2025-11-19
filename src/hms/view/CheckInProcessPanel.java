package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener; // ActionListener import 추가

public class CheckInProcessPanel extends JPanel {

    private final JFrame parentFrame;
    private final ReservationController controller;
    private final String[] reservationDetails;

    public CheckInProcessPanel(JFrame parentFrame, ReservationController controller, String[] reservationDetails) {
        this.parentFrame = parentFrame;
        this.controller = controller;
        this.reservationDetails = reservationDetails;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- 1. Header ---
        JLabel headerLabel = new JLabel("<html><h2>✅ 체크인 확정 및 정보 확인</h2></html>", SwingConstants.CENTER);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // --- 2. Details Display Panel ---
        JPanel detailPanel = createDetailPanel(reservationDetails);
        add(detailPanel, BorderLayout.CENTER);

        // --- 3. Footer (Confirm Button) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));

        JButton confirmButton = new JButton("⭐ 체크인 최종 확정");
        confirmButton.setBackground(new Color(50, 200, 50));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.addActionListener(e -> handleCheckInConfirm());

        JButton cancelButton = new JButton("취소/이전 화면으로");

        // 부모 창(CheckInFrame 역할)을 닫고 AdminMainFrame으로 복귀하도록 처리합니다.
        cancelButton.addActionListener(e -> {
            parentFrame.dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createDetailPanel(String[] details) {
        // ⭐ [수정] GridLayout의 수평 여백(hgap)을 10에서 20으로 늘려 공간 확보
        JPanel panel = new JPanel(new GridLayout(0, 2, 20, 5));
        panel.setBorder(BorderFactory.createTitledBorder("예약 상세 정보"));

        String[] labels = {
                "예약 번호", "고객 이름", "전화번호", "체크인 날짜", "체크아웃 날짜",
                "예상 IN 시간", "예상 OUT 시간", "인원 수", "등급", "객실 번호",
                "총 요금", "결제 방식", "현재 상태"
        };

        // 정보 표시 (13개 필드)
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i] + ":"));
            String value = (i < details.length && details[i] != null) ? details[i] : "정보 없음";

            JLabel valueLabel = new JLabel(value);
            // 객실 번호, 상태 강조
            if (i == 9 || i == 12) {
                valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 14f));
                if (i == 12 && value.equals("예약확인")) {
                    valueLabel.setForeground(Color.BLUE);
                }
            }
            panel.add(valueLabel);
        }
        return panel;
    }

    private void handleCheckInConfirm() {
        String reservationId = reservationDetails[0];

        boolean success = controller.updateReservationStatus(reservationId, "CHECKED_IN");

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "예약 번호 " + reservationId + "의 체크인이 성공적으로 확정되었습니다.\n객실: " + reservationDetails[9],
                    "체크인 성공", JOptionPane.INFORMATION_MESSAGE);

            // 체크인 성공 후, 현재 창을 닫고 AdminMainFrame으로 돌아가도록 처리합니다.
            if (parentFrame != null) {
                parentFrame.dispose();
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "체크인 처리 중 오류가 발생했습니다. 파일을 확인해 주세요.",
                    "체크인 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
}