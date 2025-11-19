// hms/view/Reservation_GradePanel.java
package hms.view;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Reservation_GradePanel extends JPanel {

    private final ReservationManagerPanel manager;

    public Reservation_GradePanel(ReservationManagerPanel manager) {
        this.manager = manager;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("객실 등급 선택");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel gradesPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        gradesPanel.setOpaque(false);

        Map<String, Integer> prices = manager.getRoomPrices();

        // (임시 데이터 - 매진 여부)
        Map<String, Boolean> soldOut = new HashMap<>();
        soldOut.put("스탠다드", false);
        soldOut.put("디럭스", false);
        soldOut.put("스위트", false);

        for (String gradeName : prices.keySet()) {
            int price = prices.get(gradeName);
            boolean isSoldOut = soldOut.getOrDefault(gradeName, false);

            JButton gradeButton = createGradeButton(gradeName, price, isSoldOut);

            gradeButton.addActionListener(e -> {
                if (!isSoldOut) {
                    manager.setStep2Data(gradeName);
                    manager.showStep("roomShow"); // ★ 3단계 이름표 호출
                }
            });
            gradesPanel.add(gradeButton);
        }
        add(gradesPanel, BorderLayout.CENTER);

        JButton prevButton = new JButton("이전으로 (날짜 다시 선택)");
        prevButton.addActionListener(e -> manager.showStep("search")); // ★ 1단계 이름표 호출
        add(prevButton, BorderLayout.SOUTH);
    }

    private JButton createGradeButton(String name, int price, boolean soldOut) {
        String text = "<html><div style='padding: 10px;'>" +
                "<h3 style='margin:0;'>" + name + "</h3>" +
                "<p>1박 기본 가격: " + price + "원</p>" +
                (soldOut ? "<span style='color:red; font-weight:bold;'>매진</span>" : "") +
                "</div></html>";
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setEnabled(!soldOut);
        return button;
    }
}