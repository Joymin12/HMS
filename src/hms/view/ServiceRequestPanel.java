package hms.view;

import hms.model.RoomServiceDataManager;
import hms.controller.ReservationController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter; // ⭐ MouseAdapter import
import java.awt.event.MouseEvent;   // ⭐ MouseEvent import
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * [관리자] 고객 룸서비스 요청 목록을 표시하고 처리하는 패널 (SFR-404).
 */
public class ServiceRequestPanel extends JPanel {

    private final RoomServiceOrderFrame parentFrame;
    private final RoomServiceDataManager dataManager;

    private JTable requestTable;
    private DefaultTableModel tableModel;

    private JButton processButton;
    private JButton completeButton;
    private JButton refreshButton;

    public ServiceRequestPanel(RoomServiceOrderFrame parentFrame, ReservationController controller) {
        this.parentFrame = parentFrame;
        this.dataManager = new RoomServiceDataManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. 테이블 모델 및 UI
        String[] columnNames = {"주문 ID", "객실 번호", "청구 항목", "금액", "상태", "요청 시간"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(requestTable);
        add(scrollPane, BorderLayout.CENTER);

        // ⭐ [수정 1] 칼럼 너비 조정 및 더블 클릭 리스너 추가
        adjustTableColumnSettings();
        requestTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 더블 클릭 감지
                    showDetailsPopup();
                }
            }
        });

        // 2. 하단 버튼 및 액션 패널
        add(createSouthPanel(), BorderLayout.SOUTH);

        loadRequestData();
    }

    // --- 테이블 칼럼 설정 메서드 ---
    private void adjustTableColumnSettings() {
        javax.swing.table.TableColumnModel columnModel = requestTable.getColumnModel();

        // "청구 항목" (Index 2)에 충분한 너비 할당 (잘림 방지)
        columnModel.getColumn(2).setPreferredWidth(300);
        // "요청 시간" (Index 5)
        columnModel.getColumn(5).setPreferredWidth(150);
    }

    // --- 팝업 상세 보기 메서드 ---
    private void showDetailsPopup() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) return;

        // 테이블에서 '청구 항목'의 포맷팅된 문자열을 가져옵니다.
        String itemSummary = (String) tableModel.getValueAt(selectedRow, 2);
        String orderId = (String) tableModel.getValueAt(selectedRow, 0);

        // UI에 표시된 쉼표(,)를 다시 줄바꿈 태그로 바꿔서 가독성을 높입니다.
        String displayMessage = itemSummary.replace(", ", "\n");

        JTextArea textArea = new JTextArea(displayMessage);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        JOptionPane.showMessageDialog(this, scrollPane,
                "주문 상세 내역 (ID: " + orderId + ")", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // ⭐ 상단: 메인 선택 화면으로 돌아가기 버튼
        JButton backToMainButton = new JButton("⬅️ 룸서비스 메인 화면으로 돌아가기");
        backToMainButton.addActionListener(e -> parentFrame.switchPanel(RoomServiceOrderFrame.MAIN_VIEW));
        panel.add(backToMainButton, BorderLayout.NORTH);

        // 하단: 처리 버튼
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        processButton = new JButton("처리 중으로 변경");
        completeButton = new JButton("완료 처리");
        refreshButton = new JButton("새로고침");

        // ⭐ [액션 수정] DataManager의 상수 사용 및 상태 업데이트 연결
        processButton.addActionListener(e -> updateStatus(RoomServiceDataManager.STATUS_PROCESSING));
        completeButton.addActionListener(e -> updateStatus(RoomServiceDataManager.STATUS_COMPLETED));
        refreshButton.addActionListener(e -> loadRequestData());

        actionPanel.add(refreshButton);
        actionPanel.add(processButton);
        actionPanel.add(completeButton);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------------------
    // 1. READ: 실제 데이터 로드 로직 (room_service_requests.txt에서 읽기)
    // ---------------------------------------------------------------------
    private void loadRequestData() {
        tableModel.setRowCount(0);

        // ⭐ [핵심 수정] DataManager를 통해 파일에서 요청 목록을 읽어옵니다.
        List<String[]> requests = dataManager.getAllRequests();

        for (String[] req : requests) {

            // 금액 포맷팅 (Index 3)
            String priceFormatted;
            try {
                long price = Long.parseLong(req[3]);
                priceFormatted = NumberFormat.getNumberInstance(Locale.US).format(price);
            } catch (Exception e) {
                priceFormatted = req[3]; // 파싱 오류 시 원본 값 사용
            }

            // ⭐⭐⭐ [핵심 수정] ItemSummary (Index 2)의 세미콜론을 쉼표로 치환 ⭐⭐⭐
            // 파일에 저장된 '햄버거 x 1; 콜라 x 1' 형태를 '햄버거 x 1, 콜라 x 1'로 변환합니다.
            String itemSummaryFormatted = req[2].replace(";", ",");

            // ⭐ [매핑] 파일 인덱스에 맞춰 테이블에 추가
            tableModel.addRow(new Object[]{
                    req[0], // 주문 ID
                    req[1], // 객실 번호
                    itemSummaryFormatted, // 🚨 수정된 포맷의 청구 항목 사용
                    priceFormatted, // 금액
                    req[4], // 상태
                    req[5]  // 요청 시간 (Timestamp)
            });
        }

        // 요청 데이터가 없는 경우 안내 메시지 추가
        if (requests.isEmpty()) {
            System.out.println("DEBUG: 룸서비스 요청 파일이 비어 있습니다.");
            tableModel.addRow(new Object[]{"N/A", "N/A", "요청 없음", "0원", "N/A", "N/A"});
        }

        requestTable.repaint();
    }

    // ---------------------------------------------------------------------
    // 2. UPDATE: 상태 변경 로직 (파일에 반영)
    // ---------------------------------------------------------------------
    private void updateStatus(String newStatus) {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "먼저 처리할 요청을 선택해주세요.");
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);

        // DataManager를 호출하여 파일에 상태를 업데이트합니다.
        if (dataManager.updateRequestStatus(orderId, newStatus)) {
            // 파일 업데이트 성공 시에만 UI 업데이트
            tableModel.setValueAt(newStatus, selectedRow, 4); // Column 4는 '상태'

            // ⭐ [수정 1] 성공 메시지에 4번째 인자 (INFORMATION_MESSAGE) 추가
            JOptionPane.showMessageDialog(this,
                    "주문 ID " + orderId + " 상태가 " + newStatus + "로 변경되었습니다.",
                    "성공",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            // ⭐ [수정 2] 오류 메시지에 4번째 인자 (ERROR_MESSAGE) 추가
            JOptionPane.showMessageDialog(this,
                    "상태 변경 중 파일 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}