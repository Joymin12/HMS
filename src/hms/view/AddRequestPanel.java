package hms.view;

import hms.controller.ReservationController;
import hms.controller.RoomServiceController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * [관리자] 고객 요청(룸서비스)을 직접 추가하는 패널.
 * - 방 번호 지정
 * - 메뉴 목록에서 선택 및 수량 지정
 * - 최종적으로 서버에 요청 추가 (RS_ADD_REQUEST)
 */
public class AddRequestPanel extends JPanel {

    private final RoomServiceOrderFrame parentFrame;
    private final ReservationController resController; // 객실 유효성 검사용
    private final RoomServiceController controller; // 룸서비스 요청용

    private JTable menuTable;
    private DefaultTableModel menuModel;
    private JTextField roomNumberField;
    private JTextArea orderSummaryArea;
    private JButton addToOrderButton;
    private JButton submitRequestButton;

    private final Map<String, Integer> currentOrderItems = new HashMap<>(); // 현재 주문 품목: 메뉴이름 -> 수량
    private long totalOrderPrice = 0;

    // ⭐ 생성자를 3개의 인수를 받도록 최종 확정
    public AddRequestPanel(RoomServiceOrderFrame parentFrame,
                           ReservationController resController,
                           RoomServiceController controller) {
        this.parentFrame = parentFrame;
        this.resController = resController; // 예약 컨트롤러 저장
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. 상단: 방 번호 입력 및 주문 요약
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. 중앙: 메뉴 테이블
        add(createMenuPanel(), BorderLayout.CENTER);

        // 3. 하단: 주문 버튼
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadMenuData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("요청 정보"));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));

        // 방 번호 입력
        inputPanel.add(new JLabel("방 번호 (필수):"));
        roomNumberField = new JTextField(10);
        inputPanel.add(roomNumberField);

        panel.add(inputPanel, BorderLayout.NORTH);

        // 주문 요약 (장바구니 역할)
        orderSummaryArea = new JTextArea(3, 40);
        orderSummaryArea.setEditable(false);
        orderSummaryArea.setBorder(BorderFactory.createTitledBorder("현재 주문 내역 (총액: 0원)"));
        orderSummaryArea.setLineWrap(true);
        panel.add(new JScrollPane(orderSummaryArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("메뉴 선택"));

        String[] columnNames = {"ID", "메뉴명", "가격", "카테고리"};
        menuModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 테이블 편집 불가능
            }
        };
        menuTable = new JTable(menuModel);

        // 메뉴 더블클릭 이벤트 리스너
        menuTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && menuTable.getSelectedRow() != -1) {
                    addSelectedItemToOrder();
                }
            }
        });

        panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        // 메뉴 추가 버튼 (선택한 항목을 주문에 추가)
        addToOrderButton = new JButton("주문에 추가 (수량 1)");
        addToOrderButton.addActionListener(e -> addSelectedItemToOrder());

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(addToOrderButton);
        panel.add(buttonWrapper, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton backButton = new JButton("취소 및 돌아가기");
        backButton.addActionListener(e -> parentFrame.switchPanel(RoomServiceOrderFrame.MAIN_VIEW));

        // 주문 초기화 버튼
        JButton resetButton = new JButton("🗑️ 현재 주문 초기화");
        resetButton.setBackground(new Color(255, 230, 230));
        resetButton.addActionListener(e -> resetOrder());

        submitRequestButton = new JButton("✅ 요청 확정 및 서버 전송");
        submitRequestButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitRequestButton.addActionListener(e -> handleSubmitRequest());

        panel.add(backButton);
        panel.add(resetButton);
        panel.add(submitRequestButton);

        return panel;
    }

    // --- [데이터 및 로직 메서드] ---

    private void loadMenuData() {
        menuModel.setRowCount(0); // 테이블 초기화
        // controller는 RoomServiceController입니다.
        List<String[]> menuList = controller.getAllMenu();

        if (menuList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "룸서비스 메뉴 데이터 로드에 실패했거나 메뉴가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (String[] menu : menuList) {
            // menu: [ID, 메뉴명, 가격, 카테고리] 구조를 가정
            menuModel.addRow(menu);
        }
    }

    private void addSelectedItemToOrder() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "주문에 추가할 메뉴를 선택해주세요.", "선택 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String menuId = (String) menuModel.getValueAt(selectedRow, 0);
        String menuName = (String) menuModel.getValueAt(selectedRow, 1);
        String priceStr = (String) menuModel.getValueAt(selectedRow, 2);

        try {
            long itemPrice = Long.parseLong(priceStr);

            // 현재 주문 품목 맵 업데이트
            currentOrderItems.put(menuName, currentOrderItems.getOrDefault(menuName, 0) + 1);
            totalOrderPrice += itemPrice;

            updateOrderSummary();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "메뉴 가격 형식이 올바르지 않습니다.", "데이터 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOrderSummary() {
        StringBuilder sb = new StringBuilder();

        if (currentOrderItems.isEmpty()) {
            sb.append("주문 내역이 비어있습니다.");
        } else {
            for (Map.Entry<String, Integer> entry : currentOrderItems.entrySet()) {
                sb.append(entry.getKey())
                        .append(" x ")
                        .append(entry.getValue())
                        .append("\n");
            }
        }

        orderSummaryArea.setText(sb.toString());
        orderSummaryArea.setBorder(BorderFactory.createTitledBorder(
                String.format("현재 주문 내역 (총액: %,d원)", totalOrderPrice)
        ));
    }

    private void resetOrder() {
        if (currentOrderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "초기화할 주문 내역이 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "현재까지의 주문 내역을 모두 취소하고 초기화하시겠습니까?",
                "주문 초기화 확인",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            currentOrderItems.clear();
            totalOrderPrice = 0;
            roomNumberField.setText("");
            updateOrderSummary();
            JOptionPane.showMessageDialog(this, "주문 내역이 초기화되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleSubmitRequest() {
        String roomNumber = roomNumberField.getText().trim();

        if (roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "룸서비스를 요청할 방 번호를 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentOrderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "주문할 메뉴를 하나 이상 선택해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 🚨 [핵심 수정] 객실 유효성 검사 (ReservationController 사용)
        boolean isRoomValid;
        try {
            // ⭐ [활성화] ReservationController의 isRoomCheckedIn 호출
            isRoomValid = resController.isRoomCheckedIn(roomNumber);
        } catch (Exception e) {
            // 통신 오류 발생 시
            JOptionPane.showMessageDialog(this, "객실 유효성 검사 중 통신 오류가 발생했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isRoomValid) {
            JOptionPane.showMessageDialog(this, "❌ 해당 방은 현재 투숙 중이 아니거나 유효한 예약 상태가 아닙니다.", "객실 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // 주문 품목을 서버로 보낼 CSV 포맷 문자열로 변환
        List<String> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : currentOrderItems.entrySet()) {
            items.add(entry.getKey() + " x " + entry.getValue());
        }
        String itemsString = String.join(", ", items);

        // 서버로 요청 전송 (RS_ADD_REQUEST)
        // RoomServiceController.addServiceRequest(String room, String items, long price) 호출
        String responseId = controller.addServiceRequest(roomNumber, itemsString, totalOrderPrice);

        if (responseId != null) {
            JOptionPane.showMessageDialog(this,
                    String.format("✅ 룸서비스 요청이 성공적으로 추가되었습니다!\n방 번호: %s\n주문 번호: %s\n총액: %,d원", roomNumber, responseId, totalOrderPrice),
                    "요청 성공",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // 성공 후 주문 초기화 및 메인 화면으로 복귀
            currentOrderItems.clear(); // 초기화
            totalOrderPrice = 0; // 초기화
            roomNumberField.setText("");
            updateOrderSummary(); // 요약 패널 초기화
            parentFrame.switchPanel(RoomServiceOrderFrame.MAIN_VIEW);

        } else {
            JOptionPane.showMessageDialog(this, "❌ 요청 추가에 실패했습니다. 서버 로그를 확인하세요.", "통신 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}