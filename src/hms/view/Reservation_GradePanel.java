package hms.view;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Reservation_GradePanel extends JPanel {

    private final ReservationManagerPanel manager;
    private final JPanel gradesPanel; // ⭐ [수정] 갱신을 위해 필드로 선언됨
    private final String[] grades = {"스탠다드", "디럭스", "스위트"}; // 등급 배열 고정

    public Reservation_GradePanel(ReservationManagerPanel manager) {
        this.manager = manager;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("객실 등급 선택");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // ⭐ [수정] 필드로 선언된 gradesPanel 초기화
        gradesPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        gradesPanel.setOpaque(false);
        add(gradesPanel, BorderLayout.CENTER);

        // 생성 시점에는 버튼을 그리지 않고, showStep("step2_grade") 호출 시 그립니다.

        JButton prevButton = new JButton("이전으로 (날짜 다시 선택)");
        prevButton.addActionListener(e -> manager.showStep("search"));
        add(prevButton, BorderLayout.SOUTH);
    }

    /**
     * ⭐ [핵심 추가] 패널이 화면에 표시될 때마다 매진 상태를 계산하고 화면을 갱신합니다.
     */
    public void updateGradeStatus() {
        // 1. 실시간 매진 상태 조회 (날짜 선택 정보 기반)
        Map<String, Boolean> soldOut = manager.getGradeSoldOutStatus();

        gradesPanel.removeAll(); // 기존 버튼 모두 제거

        // 2. 갱신된 상태로 버튼 다시 그리기
        for (String gradeName : grades) {
            int price = 0; // 가격은 다음 단계에서 조회되므로 0으로 설정

            // ⭐ 조회된 실시간 매진 상태를 사용
            boolean isSoldOut = soldOut.getOrDefault(gradeName, false);

            JButton gradeButton = createGradeButton(gradeName, price, isSoldOut);

            gradeButton.addActionListener(e -> {
                if (!isSoldOut) {
                    manager.setStep2Data(gradeName);
                    manager.showStep("roomShow"); // 3단계(방 선택) 호출
                }
            });
            gradesPanel.add(gradeButton);
        }

        gradesPanel.revalidate();
        gradesPanel.repaint(); // 화면 갱신
    }


    private JButton createGradeButton(String name, int price, boolean soldOut) {
        // 가격이 0으로 넘어올 경우, '요금 확정' 문구로 대체
        String priceText = (price > 0)
                ? String.format("1박 기본 가격: %,d원", price)
                : "객실 선택 후 실시간 요금 확정";

        String text = "<html><div style='padding: 10px;'>" +
                "<h3 style='margin:0;'>" + name + "</h3>" +
                "<p>" + priceText + "</p>" +
                (soldOut ? "<span style='color:red; font-weight:bold;'>매진</span>" : "") +
                "</div></html>";
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setEnabled(!soldOut);

        // 매진 시 비활성화 및 배경색 변경 (UX 개선)
        if (soldOut) {
            button.setBackground(Color.LIGHT_GRAY);
        }

        return button;
    }
}