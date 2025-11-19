package hms.view;

import hms.controller.ReservationController;
import hms.model.RoomServiceDataManager;
import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * 💸 [관리자] 체크아웃 과정에서 최종 청구서를 표시하고 결제를 완료하는 패널입니다.
 */
public class CheckoutProcessPanel extends JPanel {

    private final CheckInOutFrame parentFrame;
    private final ReservationController controller;
    private final RoomServiceDataManager serviceManager;
    private final String[] reservationData; // 예약 상세 정보 (예: [예약ID, 객실번호, ...])

    private JTextArea billArea;
    private JButton completeCheckoutButton;
    private long totalServiceCost = 0;
    private long finalTotalBill = 0;

    // ⭐ 예약 데이터 인덱스 상수 (DataManager와 맞춰야 함)
    private static final int RES_IDX_ROOM_NUM = 1; // 객실 번호가 인덱스 1에 있다고 가정

    public CheckoutProcessPanel(CheckInOutFrame parentFrame, ReservationController controller, String[] reservationData) {
        this.parentFrame = parentFrame;
        this.controller = controller;
        this.reservationData = reservationData;
        this.serviceManager = new RoomServiceDataManager();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // 1. 청구서 표시 영역
        add(createBillPanel(), BorderLayout.CENTER);

        // 2. 버튼 영역 (결제 및 취소)
        add(createButtonPanel(), BorderLayout.SOUTH);

        // 청구서 데이터 로드 및 계산
        loadAndDisplayBill();
    }

    private JPanel createBillPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("💰 최종 청구서 및 룸서비스 정산"));

        billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        panel.add(new JScrollPane(billArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        completeCheckoutButton = new JButton("✅ 결제 확인 및 체크아웃 완료");
        JButton cancelButton = new JButton("취소 및 관리 화면으로 복귀");

        completeCheckoutButton.addActionListener(e -> handleCheckoutComplete());
        cancelButton.addActionListener(e -> parentFrame.switchPanel(CheckInOutFrame.MANAGEMENT_VIEW, null));

        panel.add(cancelButton);
        panel.add(completeCheckoutButton);
        return panel;
    }

    // =================================================================
    // ★ 핵심 로직: 룸서비스 비용 계산 및 청구서 표시 ★
    // =================================================================

    private void loadAndDisplayBill() {
        String roomNumber = reservationData[RES_IDX_ROOM_NUM];

        // 1. 룸서비스 비용 계산
        totalServiceCost = calculateRoomServiceCost(roomNumber);

        // 2. 숙박비 계산 (임시값 또는 Controller에서 조회)
        // 실제로는 controller.calculateRoomCharge(reservationData) 등을 사용
        long roomCharge = 200000;

        finalTotalBill = roomCharge + totalServiceCost;

        // 3. UI에 청구서 내용 포맷팅
        String billDetails = formatBillContent(roomNumber, roomCharge, totalServiceCost, finalTotalBill);
        billArea.setText(billDetails);
    }

    private String formatBillContent(String roomNumber, long roomCharge, long serviceCost, long total) {
        // ⭐ [수정] 체크인 날짜 가져오기
        String checkInDate = reservationData[RES_IDX_CHECK_IN_DATE];
        // ⭐ [수정] 현재 체크아웃 시간 포맷
        String now = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(java.time.LocalDateTime.now());

        // 룸서비스 상세 내역을 가져와서 청구서에 포함
        String serviceDetails = getServiceDetails(roomNumber);

        return String.format(
                "================================================\n" +
                        "            🏨 객실 체크아웃 청구서 💸\n" +
                        "================================================\n" +
                        " 객실 번호: %s\n" +
                        " 체크인: %s\n" + // ⭐ [수정] 실제 데이터 사용
                        " 체크아웃: %s\n" + // ⭐ [수정] 실제 시간 사용
                        "------------------------------------------------\n" +
                        " [1] 숙박 비용: \t\t%,15d 원\n" +
                        " [2] 룸서비스 비용: \t%,15d 원\n" +
                        "------------------------------------------------\n" +
                        " 룸서비스 상세 내역:\n" +
                        "%s\n" +
                        "------------------------------------------------\n" +
                        " 최종 결제 금액: \t%,15d 원\n" +
                        "================================================\n",
                roomNumber, checkInDate, now, roomCharge, serviceCost, serviceDetails, total
        );
    }

    /**
     * 완료된 룸서비스 요청의 총 비용을 계산합니다.
     */
    private long calculateRoomServiceCost(String roomNumber) {
        long totalCost = 0;

        // DataManager에 getRequestsByRoomAndStatus(String, String) 메서드가 있다고 가정
        List<String[]> completedRequests = serviceManager.getRequestsByStatus(RoomServiceDataManager.STATUS_COMPLETED);

        for (String[] request : completedRequests) {
            String reqRoomNum = request[1]; // 객실 번호는 Index 1

            if (reqRoomNum.equals(roomNumber)) {
                try {
                    totalCost += Long.parseLong(request[3]); // 금액은 Index 3
                } catch (NumberFormatException ignored) {
                    // 오류 무시
                }
            }
        }
        return totalCost;
    }

    /**
     * 룸서비스 상세 내역 문자열을 만듭니다. (청구서에 표시용)
     */
    private String getServiceDetails(String roomNumber) {
        StringBuilder details = new StringBuilder();
        List<String[]> completedRequests = serviceManager.getRequestsByStatus(RoomServiceDataManager.STATUS_COMPLETED);

        for (String[] request : completedRequests) {
            String reqRoomNum = request[1];
            if (reqRoomNum.equals(roomNumber)) {
                // req[2] = ItemSummary (예: 샌드위치 x 1; 콜라 x 1)
                // req[3] = TotalPrice

                String items = request[2].replace(";", ", "); // 구분자 변경
                String price = NumberFormat.getNumberInstance(Locale.US).format(Long.parseLong(request[3]));

                details.append(String.format(" - %s (%,s원)\n", items, price));
            }
        }
        return details.length() > 0 ? details.toString() : " (청구된 룸서비스 내역이 없습니다)\n";
    }

    // =================================================================
    // ★ 결제 및 상태 업데이트 핸들러 ★
    // =================================================================

    private void handleCheckoutComplete() {
        String roomNumber = reservationData[RES_IDX_ROOM_NUM];

        int confirm = JOptionPane.showConfirmDialog(this,
                roomNumber + " 객실의 최종 금액 " + NumberFormat.getNumberInstance(Locale.US).format(finalTotalBill) + " 원 결제를 완료하시겠습니까?",
                "결제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {

            // 1. 예약 상태를 '체크아웃 완료'로 변경 (Controller 호출)
            if (controller.processCheckout(roomNumber)) {

                // 2. 룸서비스 요청 상태를 '결제 완료(Paid)' 상태로 변경
                // 🚨 이 기능은 DataManager에 새로운 메서드 (예: updateStatusToPaidByRoom)와 상수 (STATUS_PAID)가 필요합니다.

                // 가상의 결제 완료 상태 업데이트 호출 (다음 작업 요청 시 DataManager에 추가할 수 있습니다)
                // serviceManager.markRoomServiceAsPaid(roomNumber, RoomServiceDataManager.STATUS_COMPLETED);

                JOptionPane.showMessageDialog(this,
                        "체크아웃이 완료되고 청구서가 정산되었습니다.",
                        "체크아웃 성공",
                        JOptionPane.INFORMATION_MESSAGE);

                parentFrame.switchPanel(CheckInOutFrame.MANAGEMENT_VIEW, null); // 관리 화면으로 복귀
            } else {
                JOptionPane.showMessageDialog(this,
                        "예약 또는 체크아웃 처리 중 오류가 발생했습니다.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}