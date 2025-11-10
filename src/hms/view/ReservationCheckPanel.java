package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Locale; // ★ 추가: Locale import

/**
 * =================================================================
 * [핵심 역할: 예약 조회 사용자 인터페이스 (View)]
 * 이 클래스는 예약 조회 창의 '내용물'을 구성하는 JPanel입니다.
 * 1. 예약자 이름과 전화번호를 입력받는 폼을 제공합니다.
 * 2. '조회하기' 버튼 액션을 처리하며, ReservationController를 호출합니다.
 * 3. Controller로부터 받은 결과를 사용자에게 보기 좋게 형식화하여 표시합니다.
 * =================================================================
 */
public class ReservationCheckPanel extends JPanel {

    private final ReservationController controller;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextArea resultArea;

    public ReservationCheckPanel() {
        this.controller = new ReservationController();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- 1. 검색 폼 (입력) ---
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("예약 정보 입력"));

        nameField = new JTextField(15);
        phoneField = new JTextField(15);

        JButton searchButton = new JButton("예약 조회하기");
        searchButton.setBackground(new Color(37, 99, 235));
        searchButton.setForeground(Color.WHITE);

        inputPanel.add(new JLabel("예약자 이름:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("전화번호:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(searchButton);

        add(inputPanel, BorderLayout.NORTH);

        // --- 2. 결과 표시 영역 ---
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultArea.setBorder(BorderFactory.createTitledBorder("조회 결과"));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // --- 3. 버튼 리스너 연결 ---
        searchButton.addActionListener(this::handleSearchAction);
    }

    private void handleSearchAction(ActionEvent e) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름과 전화번호를 모두 입력해주세요.");
            return;
        }

        // Controller를 통해 파일 검색 요청
        String[] reservationData = controller.searchReservation(name, phone);

        if (reservationData != null) {
            displayReservation(reservationData);
        } else {
            resultArea.setText("\n\n" + name + " (" + phone + ")로 검색된 예약 내역이 없습니다.");
        }
    }

    /**
     * Controller로부터 받은 예약 데이터를 형식화하여 JTextArea에 표시합니다.
     */
    public void displayReservation(String[] reservationData) {
        // handleSearchAction에서 null 체크를 했으므로 첫 줄은 제거함.

        // ReservationController의 반환 순서 (0:ID, 1:Name, 2:Phone, 3:CheckIn, 4:CheckOut, 5:Guests, 6:Grade, 7:Room, 8:Price, 9:Payment)

        // 1. 가격 데이터를 문자열에서 long 타입으로 변환
        long totalPrice = Long.parseLong(reservationData[8].trim());

        // 2. 출력 포맷 (Locale.US를 사용하여 포맷 오류 방지)
        String summary = String.format(
                Locale.US, // ★ 수정: 로케일 명시
                "--- 예약 상세 정보 ---\n\n" +
                        "예약 번호: %s\n" +
                        "예약자: %s (%s)\n" +
                        "체크인: %s\n" +
                        "체크아웃: %s\n" +
                        "객실 등급: %s (%s호)\n" +
                        "총 요금: %,d원\n" + // ★ 수정: 별표(**) 제거 및 %,d 사용
                        "결제 방식: %s",
                reservationData[0], reservationData[1], reservationData[2],
                reservationData[3], reservationData[4],
                reservationData[6], reservationData[7],
                totalPrice, // 변환된 long 값 전달
                reservationData[9]
        );

        resultArea.setText(summary);

        // ★ 수정: data 변수 대신 reservationData 사용
        if (reservationData[9].equals("onsite")) {
            resultArea.append("\n\n[주의] 현장 결제 예약은 당일 18시까지 미 체크인 시 자동 취소됩니다.");
        }
    }
}