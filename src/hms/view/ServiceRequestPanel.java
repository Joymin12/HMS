package hms.view;

import hms.controller.ReservationController;
import hms.controller.RoomServiceController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class ServiceRequestPanel extends JPanel {

    private final RoomServiceOrderFrame parentFrame;
    // ⭐ [수정] 필드 타입을 RoomServiceController로 변경
    private final RoomServiceController roomServiceController;

    private JTable requestTable;
    private DefaultTableModel tableModel;
    private JButton processButton, completeButton, refreshButton;
    private JComboBox<String> statusFilter;

    // ⭐ [NEW] 서버 데이터와 일치하는 상태 상수 정의
    public static final String STATUS_PENDING_KR = "대기중";
    public static final String STATUS_PROCESSING_KR = "처리중";
    public static final String STATUS_COMPLETED_KR = "완료";
    public static final String STATUS_PAID_KR = "결제완료";
    public static final String STATUS_ALL = "전체";


    // ⭐ [CRITICAL] 생성자 인수를 RoomServiceController로 변경
    public ServiceRequestPanel(RoomServiceOrderFrame parentFrame, RoomServiceController roomServiceController) {
        this.parentFrame = parentFrame;
        this.roomServiceController = roomServiceController;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"주문 ID", "객실 번호", "청구 항목", "금액", "상태", "요청 시간"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        requestTable = new JTable(tableModel);
        add(new JScrollPane(requestTable), BorderLayout.CENTER);

        requestTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        requestTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        requestTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showDetailsPopup();
            }
        });

        add(createSouthPanel(), BorderLayout.SOUTH);
        add(createNorthPanel(), BorderLayout.NORTH);

        loadRequestData();
    }

    private JPanel createNorthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("상태 필터:"));

        // ⭐ [수정] 상수 사용
        statusFilter = new JComboBox<>(new String[]{
                STATUS_PENDING_KR, STATUS_PROCESSING_KR, STATUS_COMPLETED_KR,
                STATUS_PAID_KR, STATUS_ALL
        });
        statusFilter.setSelectedItem(STATUS_PENDING_KR); // 기본값 설정

        // ⭐ [NEW] 필터 변경 시 데이터 로드
        statusFilter.addActionListener(e -> loadRequestData());

        panel.add(statusFilter);
        return panel;
    }


    private void showDetailsPopup() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) return;
        String itemSummary = (String) tableModel.getValueAt(selectedRow, 2);
        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        JTextArea textArea = new JTextArea(itemSummary.replace(";", "\n"));
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "주문 상세 (ID: " + orderId + ")", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton backToMainButton = new JButton("⬅️ 메인으로");
        backToMainButton.addActionListener(e -> parentFrame.switchPanel(RoomServiceOrderFrame.MAIN_VIEW));
        panel.add(backToMainButton, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        processButton = new JButton("처리 중");
        completeButton = new JButton("완료");
        refreshButton = new JButton("새로고침");

        // Controller 상태 업데이트 호출
        processButton.addActionListener(e -> updateStatus(STATUS_PROCESSING_KR)); // ⭐ [수정] 상수 사용
        completeButton.addActionListener(e -> updateStatus(STATUS_COMPLETED_KR)); // ⭐ [수정] 상수 사용
        refreshButton.addActionListener(e -> loadRequestData());

        actionPanel.add(refreshButton);
        actionPanel.add(processButton);
        actionPanel.add(completeButton);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadRequestData() {
        tableModel.setRowCount(0);

        String selectedStatus = (String) statusFilter.getSelectedItem();
        List<String[]> requests;

        // ⭐ [핵심 수정] 필터링 로직 적용
        if (STATUS_ALL.equals(selectedStatus)) {
            requests = roomServiceController.getAllRequests();
        } else {
            // 서버에 필터링을 요청할 때, 서버가 인식하는 문자열을 보냅니다.
            requests = roomServiceController.getRequestsByStatus(selectedStatus);
        }

        // ⭐ [CRITICAL] 요청이 비어있지 않은지 확인하고, 비어있으면 초기 로드 시 오류 처리 방지
        if (requests == null || requests.isEmpty()) {
            tableModel.addRow(new Object[]{"N/A", "N/A", "요청 없음", "0", "N/A", "N/A"});
            requestTable.repaint();
            return;
        }


        for (String[] req : requests) {
            String priceFormatted;
            try {
                // req[3]은 금액
                priceFormatted = NumberFormat.getNumberInstance(Locale.US).format(Long.parseLong(req[3]));
            } catch (Exception e) { priceFormatted = req[3]; }

            // req[2]는 청구 항목
            String itemSummaryFormatted = req[2].replace(";", ",");

            // req[4]는 상태 (대기중, 처리중, 완료 등)
            // req[5]는 요청 시간

            // 데이터 배열: {ID, 객실 번호, 청구 항목, 금액, 상태, 요청 시간}
            tableModel.addRow(new Object[]{req[0], req[1], itemSummaryFormatted, priceFormatted, req[4], req[5]});
        }

        requestTable.repaint();
    }

    private void updateStatus(String newStatus) {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "요청을 선택해주세요.");
            return;
        }
        String orderId = (String) tableModel.getValueAt(selectedRow, 0);

        // Controller 호출
        if (roomServiceController.updateRequestStatus(orderId, newStatus)) {
            JOptionPane.showMessageDialog(this, "상태 변경 완료: " + newStatus);
            loadRequestData(); // 갱신된 데이터를 다시 로드하여 테이블을 업데이트
        } else {
            JOptionPane.showMessageDialog(this, "상태 변경 실패");
        }
    }
}