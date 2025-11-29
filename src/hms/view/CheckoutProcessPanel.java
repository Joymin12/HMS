package hms.view;

import hms.controller.ReservationController;
import hms.controller.RoomServiceController;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.text.NumberFormat; // ⭐ 금액 포맷용 임포트
import java.util.Locale;       // ⭐ 로케일 임포트

public class CheckoutProcessPanel extends JPanel {

    private final CheckInOutFrame parentFrame;
    private final ReservationController controller;
    private final RoomServiceController serviceController;
    private final String[] reservationData;

    private JTextArea billArea;

    // 데이터 인덱스 상수
    private static final int RES_IDX_ROOM_NUM = 9;
    // [추가] 지연료 계산을 위해 체크아웃 예정 날짜 인덱스(4)가 필요함
    private static final int RES_IDX_SCHED_CHECKOUT_DATE = 4;

    public CheckoutProcessPanel(CheckInOutFrame parentFrame, ReservationController controller, RoomServiceController serviceController, String[] reservationData) {
        this.parentFrame = parentFrame;
        this.controller = controller;
        this.reservationData = reservationData;
        // ⭐ [수정] 외부에서 주입받은 serviceController를 사용
        this.serviceController = serviceController;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // 1. 청구서 텍스트 영역
        billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.setBorder(BorderFactory.createTitledBorder("💰 최종 청구서"));
        billPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);
        add(billPanel, BorderLayout.CENTER);

        // 2. 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton payBtn = new JButton("✅ 결제 및 체크아웃");
        JButton cancelBtn = new JButton("취소");

        // 이벤트 연결
        payBtn.addActionListener(e -> handleCheckoutComplete());
        cancelBtn.addActionListener(e -> parentFrame.switchPanel(CheckInOutFrame.MANAGEMENT_VIEW, null));

        btnPanel.add(cancelBtn);
        btnPanel.add(payBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 3. 청구서 내용 로드
        loadAndDisplayBill();
    }

    private void loadAndDisplayBill() {
        String roomNumber = reservationData[RES_IDX_ROOM_NUM];

        // 1. 룸서비스 총액 계산
        long serviceCost = calculateRoomServiceCost(roomNumber);

        // ⭐ [NEW] 룸서비스 상세 내역 텍스트 생성
        String serviceDetailsText = getRoomServiceDetailsText(roomNumber);

        // 2. [핵심 변경] 컨트롤러에게 기본 청구서 텍스트 생성 요청
        String baseBillText = controller.generateCheckoutBillText(reservationData, (int)serviceCost);

        // 3. ⭐ [CRITICAL FIX] 최종 텍스트 합치기: "[룸서비스 상세]" 태그를 실제 상세 내역으로 대체
        String finalBillText = baseBillText.replace("[룸서비스 상세]", serviceDetailsText);

        billArea.setText(finalBillText);

        // 커서를 맨 위로 (내용이 길 경우 스크롤 위로)
        billArea.setCaretPosition(0);
    }

    private long calculateRoomServiceCost(String roomNumber) {
        long total = 0;
        // 완료 상태인 요청만 가져옵니다.
        List<String[]> reqs = serviceController.getRequestsByStatus("완료");
        for (String[] req : reqs) {
            // req[1]: roomNumber, req[3]: price
            if (req[1].equals(roomNumber)) {
                try { total += Long.parseLong(req[3]); } catch (Exception e) {}
            }
        }
        return total;
    }

    /**
     * ⭐ [NEW HELPER] 룸서비스 상세 내역 텍스트를 생성합니다.
     */
    private String getRoomServiceDetailsText(String roomNumber) {
        StringBuilder sb = new StringBuilder();
        // 완료 상태인 요청 목록을 가져옵니다.
        List<String[]> reqs = serviceController.getRequestsByStatus("완료");

        boolean foundItems = false;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);

        for (String[] req : reqs) {
            // req[0]: ID, req[1]: roomNumber, req[2]: items list, req[3]: price, req[5]: time
            if (req[1].equals(roomNumber)) {
                foundItems = true;

                String requestTime = req[5]; // 요청 시간 (timestamp)
                String itemsList = req[2]; // ex: "커피 x 2, 샌드위치 x 1"

                // 시간 포맷을 간단하게 변경 (YYYYMMDDHHmmss -> HH:mm)
                String formattedTime = requestTime.length() >= 12 ? requestTime.substring(8, 10) + ":" + requestTime.substring(10, 12) : requestTime;

                // 줄 바꿈 및 포맷팅: [시간] - 품목 목록 (총액)
                long price = 0;
                try { price = Long.parseLong(req[3]); } catch (Exception e) {}

                sb.append(" [")
                        .append(formattedTime)
                        .append("] ")
                        .append(itemsList.replace(";", ", ")) // ";"을 공백이 있는 ","로 변경
                        // ⭐ [CRITICAL FIX] String.format 대신 문자열 연결 사용
                        .append(" (")
                        .append(nf.format(price))
                        .append("원)")
                        .append("\n");
            }
        }

        if (!foundItems) {
            sb.append("(상세 내역 없음)\n");
        }

        return sb.toString();
    }


    private void handleCheckoutComplete() {
        String room = reservationData[RES_IDX_ROOM_NUM];

        // 1. 예정된 체크아웃 날짜 가져오기 (인덱스 4)
        String scheduledDate = "";
        if (reservationData.length > RES_IDX_SCHED_CHECKOUT_DATE) {
            scheduledDate = reservationData[RES_IDX_SCHED_CHECKOUT_DATE];
        }

        // 2. [핵심] 지연료 계산 (컨트롤러 유틸 사용)
        int lateFee = controller.calculateLateFee(scheduledDate);

        // 3. 메시지 생성
        String message = "결제 및 체크아웃을 진행하시겠습니까?";
        if (lateFee > 0) {
            message = String.format("⚠️ 지연 체크아웃 요금 %,d원이 발생했습니다.\n합산하여 결제하시겠습니까?", lateFee);
        }

        // 4. 사용자 확인 및 처리
        if (JOptionPane.showConfirmDialog(this, message, "체크아웃 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            // 5. [핵심] 서버로 요청 (지연료 포함)
            if (controller.processCheckout(room, lateFee)) {

                // 룸서비스 상태 일괄 '결제완료' 처리
                serviceController.updateStatusByRoomAndStatus(room, "완료", "결제완료");

                JOptionPane.showMessageDialog(this, "체크아웃이 완료되었습니다.");
                parentFrame.switchPanel(CheckInOutFrame.MANAGEMENT_VIEW, null);

            } else {
                JOptionPane.showMessageDialog(this, "체크아웃 처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}