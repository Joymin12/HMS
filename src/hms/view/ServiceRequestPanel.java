package hms.view;

import hms.controller.ReservationController;
import hms.controller.RoomServiceController; // 변경
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
    private final RoomServiceController roomServiceController; // 변경

    private JTable requestTable;
    private DefaultTableModel tableModel;
    private JButton processButton, completeButton, refreshButton;

    public ServiceRequestPanel(RoomServiceOrderFrame parentFrame, ReservationController resController) {
        this.parentFrame = parentFrame;
        this.roomServiceController = new RoomServiceController(); // 변경

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
        loadRequestData();
    }

    private void showDetailsPopup() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) return;
        String itemSummary = (String) tableModel.getValueAt(selectedRow, 2);
        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        JTextArea textArea = new JTextArea(itemSummary.replace(", ", "\n"));
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
        processButton.addActionListener(e -> updateStatus("처리중"));
        completeButton.addActionListener(e -> updateStatus("완료"));
        refreshButton.addActionListener(e -> loadRequestData());

        actionPanel.add(refreshButton);
        actionPanel.add(processButton);
        actionPanel.add(completeButton);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadRequestData() {
        tableModel.setRowCount(0);
        // Controller 호출
        List<String[]> requests = roomServiceController.getAllRequests();

        for (String[] req : requests) {
            String priceFormatted;
            try {
                priceFormatted = NumberFormat.getNumberInstance(Locale.US).format(Long.parseLong(req[3]));
            } catch (Exception e) { priceFormatted = req[3]; }
            String itemSummaryFormatted = req[2].replace(";", ",");
            tableModel.addRow(new Object[]{req[0], req[1], itemSummaryFormatted, priceFormatted, req[4], req[5]});
        }
        if (requests.isEmpty()) {
            tableModel.addRow(new Object[]{"N/A", "N/A", "요청 없음", "0", "N/A", "N/A"});
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
            tableModel.setValueAt(newStatus, selectedRow, 4);
            JOptionPane.showMessageDialog(this, "상태 변경 완료");
        } else {
            JOptionPane.showMessageDialog(this, "상태 변경 실패");
        }
    }
}