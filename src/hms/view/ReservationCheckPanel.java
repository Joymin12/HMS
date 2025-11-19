package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Locale;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter; // WindowAdapter import 추가 (필요 시)

/**
 * [예약 조회/확인 패널] 고객 이름과 전화번호로 예약 정보를 검색하고 표시합니다.
 */
public class ReservationCheckPanel extends JPanel {

    private final ReservationController controller;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextArea resultArea;

    // ⭐ [수정 1] 생성자가 ReservationController 객체를 주입받도록 변경
    public ReservationCheckPanel(ReservationController controller) {
        this.controller = controller; // ⭐ [수정 2] 주입받은 Controller 객체를 필드에 저장

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
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

    // --- (handleSearchAction 메서드) ---
    private void handleSearchAction(ActionEvent e) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름과 전화번호를 모두 입력해주세요.");
            return;
        }

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
    public void displayReservation(String[] data) {
        // ... (총 13개 필드 구조에 따른 인덱스 정의 기반 로직 유지) ...
        // Index 12는 상태 필드이며, 표시 로직에 따라 추가할 수 있습니다.
        // 현재 코드는 12개 필드 (Index 11까지)를 기준으로 출력합니다.

        long totalPriceValue;

        // 1. 가격 데이터 안전하게 변환 (Index 10 사용)
        try {
            totalPriceValue = Long.parseLong(data[10].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            totalPriceValue = 0;
            System.err.println("경고: 총 요금 필드 데이터 오류. 값: " + (data.length > 10 ? data[10] : "없음"));
        }

        // 2. 출력 포맷
        String priceFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalPriceValue);

        // 3. 예약 상태 필드 처리 (Index 12)
        String status = (data.length > 12) ? data[12] : "UNDEFINED"; // 13번째 필드(상태) 가져오기

        // 4. JTextArea 출력 구성 (인덱스 맞춤)
        String summary = String.format(
                Locale.US,
                "=== 예약 상세 정보 ===\n\n" +
                        "예약 번호: \t%s\n" +
                        "예약자: \t%s (%s)\n" +
                        "예상 체크인 시간: \t%s (%s)\n" +
                        "예상 체크아웃 시간: \t%s (%s)\n" +
                        "객실 등급: \t%s (%s호)\n" +
                        "인원: \t%s명\n" +
                        "총 요금: \t%s원\n" +
                        "결제 방식: \t%s\n" +
                        "예약 상태: \t%s", // ⭐ 상태 필드 추가

                data[0], // ID
                data[1], data[2], // 이름, 전화번호
                data[3], data[5], // IN 날짜, IN 시간
                data[4], data[6], // OUT 날짜, OUT 시간
                data[8], data[9], // 등급, 객실 번호
                data[7], // 인원 수
                priceFormatted, // 포맷팅된 총 요금
                data[11], // 결제 방식
                status // 예약 상태
        );

        resultArea.setText(summary);

        // ★ 미보장 예약 경고 추가
        if (data[11].trim().equalsIgnoreCase("onsite")) {
            resultArea.append("\n\n[주의] 현장 결제 (미보장) 예약은 당일 18시까지 미 체크인 시 자동 취소됩니다.");
        }
    }
}