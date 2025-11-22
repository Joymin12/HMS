package hms.view;

import hms.controller.ReservationController;
import hms.controller.RoomServiceController; // 변경
import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutProcessPanel extends JPanel {

    private final CheckInOutFrame parentFrame;
    private final ReservationController controller;
    private final RoomServiceController serviceController; // 변경
    private final String[] reservationData;

    private JTextArea billArea;
    private long finalTotalBill = 0;

    private static final int RES_IDX_ROOM_NUM = 9;
    private static final int RES_IDX_CHECK_IN_DATE = 3;

    public CheckoutProcessPanel(CheckInOutFrame parentFrame, ReservationController controller, String[] reservationData) {
        this.parentFrame = parentFrame;
        this.controller = controller;
        this.reservationData = reservationData;
        this.serviceController = new RoomServiceController(); // 변경

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.setBorder(BorderFactory.createTitledBorder("💰 최종 청구서"));
        billPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);
        add(billPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton payBtn = new JButton("✅ 결제 및 체크아웃");
        JButton cancelBtn = new JButton("취소");
        payBtn.addActionListener(e -> handleCheckoutComplete());
        cancelBtn.addActionListener(e -> parentFrame.switchPanel(CheckInOutFrame.MANAGEMENT_VIEW, null));
        btnPanel.add(cancelBtn); btnPanel.add(payBtn);
        add(btnPanel, BorderLayout.SOUTH);

        loadAndDisplayBill();
    }

    private void loadAndDisplayBill() {
        String roomNumber = reservationData[RES_IDX_ROOM_NUM];
        long serviceCost = calculateRoomServiceCost(roomNumber);
        long roomCharge = controller.getRoomCharge(reservationData);
        finalTotalBill = roomCharge + serviceCost;

        String checkIn = reservationData[RES_IDX_CHECK_IN_DATE];
        String now = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(java.time.LocalDateTime.now());
        String serviceDetails = getServiceDetails(roomNumber);

        billArea.setText(String.format(
                "=== 체크아웃 청구서 ===\n객실: %s\n체크인: %s\n체크아웃: %s\n----------------------\n숙박료: %,d원\n룸서비스: %,d원\n----------------------\n[룸서비스 상세]\n%s\n----------------------\n총 결제액: %,d원",
                roomNumber, checkIn, now, roomCharge, serviceCost, serviceDetails, finalTotalBill));
    }

    private long calculateRoomServiceCost(String roomNumber) {
        long total = 0;
        // Controller 호출
        List<String[]> reqs = serviceController.getRequestsByStatus("완료");
        for (String[] req : reqs) {
            if (req[1].equals(roomNumber)) {
                try { total += Long.parseLong(req[3]); } catch (Exception e) {}
            }
        }
        return total;
    }

    private String getServiceDetails(String roomNumber) {
        StringBuilder sb = new StringBuilder();
        List<String[]> reqs = serviceController.getRequestsByStatus("완료");
        for (String[] req : reqs) {
            if (req[1].equals(roomNumber)) {
                sb.append(String.format("- %s (%,d원)\n", req[2], Long.parseLong(req[3])));
            }
        }
        return sb.length() > 0 ? sb.toString() : "(내역 없음)";
    }

    private void handleCheckoutComplete() {
        String room = reservationData[RES_IDX_ROOM_NUM];
        if (JOptionPane.showConfirmDialog(this, "결제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (controller.processCheckout(room)) {
                // Controller 호출 (일괄 결제 처리)
                serviceController.updateStatusByRoomAndStatus(room, "완료", "결제완료");
                JOptionPane.showMessageDialog(this, "체크아웃 완료");
                parentFrame.switchPanel(CheckInOutFrame.MANAGEMENT_VIEW, null);
            } else {
                JOptionPane.showMessageDialog(this, "체크아웃 실패");
            }
        }
    }
}