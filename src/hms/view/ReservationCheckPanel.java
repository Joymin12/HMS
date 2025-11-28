package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

/**
 * [예약 조회/확인 패널] 고객 이름과 전화번호로 예약 정보를 검색하고 표시합니다.
 * 관리자 모드에서는 초기 로드 시 모든 예약 정보를 표시합니다.
 */
public class ReservationCheckPanel extends JPanel {

    // 예약 데이터 배열의 인덱스 상수를 명시합니다. (HMSServer와 동일)
    private static final int RES_IDX_ID = 0;
    private static final int RES_IDX_NAME = 1;
    private static final int RES_IDX_PHONE = 2;
    private static final int RES_IDX_IN_DATE = 3;
    private static final int RES_IDX_OUT_DATE = 4;
    private static final int RES_IDX_IN_TIME = 5;
    private static final int RES_IDX_OUT_TIME = 6;
    private static final int RES_IDX_GUESTS = 7;
    private static final int RES_IDX_GRADE = 8;
    private static final int RES_IDX_ROOM_NUM = 9;
    private static final int RES_IDX_TOTAL_PRICE = 10;
    private static final int RES_IDX_PAYMENT_METHOD = 11;
    private static final int RES_IDX_STATUS = 12;

    private static final String STATUS_OBLIGATORY_CHARGE = "OBLIGATORY_CHARGE";

    private final ReservationController controller;
    private final boolean isAdmin; // ⭐ 관리자 여부 플래그
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextArea resultArea;

    /**
     * ⭐ [핵심 수정] 생성자가 isAdmin 플래그를 받아 이중 모드를 설정합니다.
     */
    public ReservationCheckPanel(ReservationController controller, boolean isAdmin) {
        this.controller = controller;
        this.isAdmin = isAdmin;

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
        // 타이틀을 역할에 따라 변경
        resultArea.setBorder(BorderFactory.createTitledBorder(isAdmin ? "전체 예약 목록" : "조회 결과"));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // --- 3. 버튼 리스너 연결 ---
        searchButton.addActionListener(this::handleSearchAction);

        // ⭐ [핵심 로직] 관리자일 경우, 초기 로드 시 모든 데이터를 가져와 표시합니다.
        if (this.isAdmin) {
            loadAllReservations();
        } else {
            resultArea.setText("\n\n이름과 전화번호를 입력하여 예약을 조회하세요.");
        }
    }

    // --- (handleSearchAction 메서드: 검색 버튼 클릭) ---
    private void handleSearchAction(ActionEvent e) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름과 전화번호를 모두 입력해주세요.");
            return;
        }

        // 관리자 모드여도, 검색 버튼은 단일 검색을 수행합니다.
        String[] reservationData = controller.searchReservation(name, phone);

        if (reservationData != null) {
            // 단일 조회 결과를 JTextArea에 표시
            resultArea.setText(formatReservationSummary(reservationData));

            // 일반 사용자의 경우에만 노쇼 경고 메시지 추가
            if (!isAdmin) {
                String paymentMethod = reservationData[RES_IDX_PAYMENT_METHOD].trim();
                if (paymentMethod.equalsIgnoreCase("현장결제") || paymentMethod.equalsIgnoreCase("onsite")) {
                    resultArea.append("\n\n[주의] 현장 결제 (미보장) 예약은 당일 18시까지 미 체크인 시 자동 취소됩니다.");
                }
            }
        } else {
            resultArea.setText("\n\n" + name + " (" + phone + ")로 검색된 예약 내역이 없습니다.");
        }
    }


    // ⭐ [NEW] 관리자 전용: 모든 예약 데이터를 불러와 표시합니다.
    public void loadAllReservations() {
        // ReservationController의 getAllReservations 메서드 (HMSServer -> DataManager 호출)
        List<String[]> allData = controller.getAllReservations();

        if (allData.isEmpty()) {
            resultArea.setText("\n\n [조회 결과] 현재 시스템에 저장된 예약 내역이 없습니다.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 전 체 예 약 상 세 정 보 ===\n");
        sb.append("총 ").append(allData.size()).append("건의 예약 내역이 있습니다.\n\n");

        // 가져온 모든 데이터를 순회하며 JTextArea에 표시
        for (String[] reservationData : allData) {
            String summary = formatReservationSummary(reservationData); // 포맷팅 재활용
            sb.append(summary).append("\n");
            sb.append("--------------------------------------------------\n");
        }

        resultArea.setText(sb.toString());
    }

    /**
     * Controller로부터 받은 예약 데이터를 형식화하여 String으로 반환합니다.
     * (JTextArea 출력을 위해 HTML 태그를 사용하지 않습니다.)
     */
    private String formatReservationSummary(String[] data) {
        long totalPriceValue;

        try {
            totalPriceValue = Long.parseLong(data[RES_IDX_TOTAL_PRICE].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            totalPriceValue = 0;
        }

        String priceFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalPriceValue);

        // 1. 예약 상태 필드 처리 (순수 텍스트로 변경)
        String status = (data.length > RES_IDX_STATUS) ? data[RES_IDX_STATUS] : "UNDEFINED";
        String displayStatusText;
        String extraInfo = "";

        if (status.equals(STATUS_OBLIGATORY_CHARGE)) {
            // ⭐ [최종 경고] 관리자 조치 필요 상태 (HTML 태그 대신 텍스트 경고 사용)
            displayStatusText = "‼️ 위약금 청구 대상 (NO-SHOW)";
            extraInfo = "\n[조치 필요] 위약금 청구 후 상태 변경을 진행해야 합니다.";
        } else if (status.equals("CHECKED_OUT")) {
            displayStatusText = "✅ 체크아웃 완료";
        } else if (status.equals("PENDING") && data[RES_IDX_PAYMENT_METHOD].trim().equalsIgnoreCase("현장결제")) {
            displayStatusText = "⚠️ 현장 결제 대기 (미보장)";
        } else {
            displayStatusText = status;
        }

        // 2. 출력 구성 (HTML 태그 없이 순수 텍스트로 구성)
        String summary = String.format(
                Locale.US,
                "=== 예약 상세 정보 ===\n" +
                        "예약 번호: \t%s\n" +
                        "예약자: \t%s (%s)\n" +
                        "예상 체크인/아웃: \t%s %s ~ %s %s\n" +
                        "객실 정보: \t%s (%s호, %s명)\n" +
                        "총 요금: \t%s원\n" +
                        "결제 방식: \t%s\n" +
                        "예약 상태: \t%s" + // ⭐ PLAIN TEXT 변수 사용
                        "%s",

                data[RES_IDX_ID],
                data[RES_IDX_NAME], data[RES_IDX_PHONE],
                data[RES_IDX_IN_DATE], data[RES_IDX_IN_TIME],
                data[RES_IDX_OUT_DATE], data[RES_IDX_OUT_TIME],
                data[RES_IDX_GRADE], data[RES_IDX_ROOM_NUM], data[RES_IDX_GUESTS],
                priceFormatted,
                data[RES_IDX_PAYMENT_METHOD],
                displayStatusText, // 순수 텍스트 사용
                extraInfo // 조치 필요 메시지
        );

        return summary;
    }

    // ⭐ [OLD] 기존 displayReservation 메서드는 formatReservationSummary를 호출하도록 리팩토링되었으므로 유지.
    public void displayReservation(String[] data) {
        String summary = formatReservationSummary(data);
        resultArea.setText(summary);

        // 일반 사용자 모드일 때만 노쇼 경고 메시지 추가
        if (!isAdmin) {
            String paymentMethod = data[RES_IDX_PAYMENT_METHOD].trim();
            if (paymentMethod.equalsIgnoreCase("현장결제") || paymentMethod.equalsIgnoreCase("onsite")) {
                resultArea.append("\n\n[주의] 현장 결제 (미보장) 예약은 당일 18시까지 미 체크인 시 자동 취소됩니다.");
            }
        }
    }
}