// hms/view/Reservation_RoomShowPanel.java
package hms.view;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class Reservation_RoomShowPanel extends JPanel {

    private final ReservationManagerPanel manager;
    private final JPanel roomGridPanel;
    private final JLabel titleLabel;

    public Reservation_RoomShowPanel(ReservationManagerPanel manager) {
        this.manager = manager;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        titleLabel = new JLabel("객실 선택");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 2x4 그리드 -> 4x2 (4줄 2칸)
        roomGridPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        roomGridPanel.setOpaque(false);
        add(roomGridPanel, BorderLayout.CENTER);

        JButton prevButton = new JButton("이전으로 (등급 다시 선택)");
        prevButton.addActionListener(e -> manager.showStep("step2_grade")); // ★ 이름표 수정
        add(prevButton, BorderLayout.SOUTH);
    }

    public void updateRoomGrid() {
        String grade = manager.getSelectedGrade();
        if (grade == null) {
            titleLabel.setText("오류: 등급이 선택되지 않았습니다.");
            return;
        }

        titleLabel.setText(grade + " 객실 선택");
        roomGridPanel.removeAll();

        // (설명대로 스탠다드 101~108)
        int startRoom = grade.equals("스탠다드") ? 101 : (grade.equals("디럭스") ? 201 : 301);
        List<String> bookedRooms = manager.getBookedRooms(); // 매니저에게 예약 목록 물어봄

        for (int i = 0; i < 8; i++) {
            final String roomNumber = String.valueOf(startRoom + i);
            JButton roomButton = new JButton(roomNumber + "호");
            roomButton.setFont(new Font("SansSerif", Font.BOLD, 16));

            if (bookedRooms.contains(roomNumber)) {
                roomButton.setText(roomNumber + "호 (예약불가)");
                roomButton.setEnabled(false);
                roomButton.setBackground(Color.LIGHT_GRAY);
            }

            roomButton.addActionListener(e -> {
                manager.setStep3Data(roomNumber);
                manager.showStep("info"); // ★ 4단계 이름표 호출
            });
            roomGridPanel.add(roomButton);
        }

        roomGridPanel.revalidate();
        roomGridPanel.repaint();
    }
}