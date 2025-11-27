package hms.view;

import hms.controller.ReservationController;
import hms.controller.RoomServiceController;
import javax.swing.*;
import java.awt.*;
import java.util.List;

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

    public CheckoutProcessPanel(CheckInOutFrame parentFrame, ReservationController controller, String[] reservationData) {
        this.parentFrame = parentFrame;
        this.controller = controller;
        this.reservationData = reservationData;
        this.serviceController = new RoomServiceController();

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

        // 1. 룸서비스 총액 계산 (기존 로직 유지)
        long serviceCost = calculateRoomServiceCost(roomNumber);

        // 2. [핵심 변경] 컨트롤러에게 청구서 텍스트 생성 요청
        // (내부적으로 지연료 계산, 숙박료 제외 총액 합산, 텍스트 포맷팅을 수행함)
        String billText = controller.generateCheckoutBillText(reservationData, (int)serviceCost);

        // 3. 룸서비스 상세 내역을 추가하고 싶다면, 컨트롤러 텍스트 뒤에 붙이거나
        // 컨트롤러 텍스트 생성 로직을 수정해야 하지만, 일단은 깔끔하게 컨트롤러 결과 사용
        // (만약 상세 내역이 꼭 필요하면 billText 뒤에 getServiceDetails() 결과를 append 할 수 있음)

        billArea.setText(billText);

        // 커서를 맨 위로 (내용이 길 경우 스크롤 위로)
        billArea.setCaretPosition(0);
    }

    private long calculateRoomServiceCost(String roomNumber) {
        long total = 0;
        List<String[]> reqs = serviceController.getRequestsByStatus("완료");
        for (String[] req : reqs) {
            // req[1]: roomNumber, req[3]: price
            if (req[1].equals(roomNumber)) {
                try { total += Long.parseLong(req[3]); } catch (Exception e) {}
            }
        }
        return total;
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