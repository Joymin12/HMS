package hms.view;

import hms.model.RoomServiceDataManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Window;
import java.awt.event.WindowAdapter;

/**
 * 🍴 [사용자] 룸서비스 메뉴를 표시하고 주문을 받는 패널.
 */
public class RoomServiceOrderPanel extends JPanel {

    private final UserMainFrame parentFrame;
    private final RoomServiceDataManager dataManager;

    // UI 컴포넌트
    private JTable menuTable;
    private DefaultTableModel menuTableModel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JTextField quantityField;
    private JLabel totalLabel;

    // ⭐ [수정] 생성자 시그니처를 UserMainFrame으로 변경 (연결 오류 해결)
    public RoomServiceOrderPanel(UserMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.dataManager = new RoomServiceDataManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- 1. 메뉴 목록 표시 영역 ---
        JPanel menuListPanel = createMenuListPanel();
        add(menuListPanel, BorderLayout.NORTH);

        // --- 2. 주문/장바구니 영역 ---
        JPanel orderCartPanel = createOrderCartPanel();
        add(orderCartPanel, BorderLayout.CENTER);

        // --- 3. 푸터 (총액 및 주문 버튼) ---
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        loadMenuData();
    }

    // --- 메뉴 목록 UI 생성 ---
    private JPanel createMenuListPanel() {
        String[] menuColumns = {"ID", "메뉴 이름", "가격 (원)", "카테고리"};
        menuTableModel = new DefaultTableModel(menuColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(menuTableModel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("메뉴 목록"));
        panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        return panel;
    }

    // --- 주문/장바구니 UI 생성 ---
    private JPanel createOrderCartPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // 2-1. 수량 입력 및 추가 버튼
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        quantityField = new JTextField("1", 5);
        JButton addButton = new JButton("장바구니에 담기");

        inputPanel.add(new JLabel("수량:"));
        inputPanel.add(quantityField);
        inputPanel.add(addButton);

        // 2-2. 장바구니 테이블
        String[] cartColumns = {"ID", "메뉴", "수량", "금액"};
        cartTableModel = new DefaultTableModel(cartColumns, 0);
        cartTable = new JTable(cartTableModel);

        // ⭐ [추가] 장바구니 제거 버튼 생성 및 패널
        JButton removeButton = new JButton("🗑️ 선택 항목 제거");
        JPanel cartButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cartButtonsPanel.add(removeButton);

        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("장바구니"));
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        cartPanel.add(inputPanel, BorderLayout.NORTH);
        // ⭐ [추가] 장바구니 하단에 제거 버튼 패널 추가
        cartPanel.add(cartButtonsPanel, BorderLayout.SOUTH);

        mainPanel.add(cartPanel);

        // --- 액션 리스너 ---
        addButton.addActionListener(this::handleAddToCart);
        // ⭐ [추가] 제거 버튼 액션 리스너 추가
        removeButton.addActionListener(this::handleRemoveFromCart);

        return mainPanel;
    }

    // --- 푸터 UI 생성 (총액 및 주문 확정) ---
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        totalLabel = new JLabel("총 주문 금액: 0원", SwingConstants.RIGHT);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(totalLabel, BorderLayout.CENTER);

        JButton confirmButton = new JButton("⭐ 주문 확정 및 요청");
        confirmButton.addActionListener(this::handleConfirmOrder);
        panel.add(confirmButton, BorderLayout.EAST);

        return panel;
    }

    // =================================================================
    // ★ 데이터 로드 및 액션 로직 ★
    // =================================================================

    private void loadMenuData() {
        menuTableModel.setRowCount(0);
        List<String[]> menuItems = dataManager.getAllMenu();

        for (String[] item : menuItems) {
            // [ID, Name, Price, Category] (4개 필드 기준)
            menuTableModel.addRow(new Object[]{
                    item[0], item[1], item[2], item[3]
            });
        }
        menuTable.repaint();
    }

    private void handleAddToCart(ActionEvent e) {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "먼저 주문할 메뉴를 목록에서 선택해주세요.", "선택 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "수량은 1 이상이어야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 메뉴 데이터 가져오기
            String id = (String) menuTableModel.getValueAt(selectedRow, 0);
            String name = (String) menuTableModel.getValueAt(selectedRow, 1);
            String priceStr = (String) menuTableModel.getValueAt(selectedRow, 2);
            // Long.parseLong을 위해 숫자 외 문자 제거
            long price = Long.parseLong(priceStr.replaceAll("[^0-9]", ""));

            // 장바구니에 추가
            cartTableModel.addRow(new Object[]{
                    id,
                    name,
                    quantity,
                    String.format("%,d", price * quantity) // 금액 포맷팅
            });

            updateTotal();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "수량을 숫자로 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * ⭐ [추가] 장바구니에서 선택된 항목을 제거하는 로직
     */
    private void handleRemoveFromCart(ActionEvent e) {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "장바구니에서 제거할 항목을 선택해주세요.", "선택 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 선택된 행을 모델에서 제거
        cartTableModel.removeRow(selectedRow);

        // 총액 업데이트
        updateTotal();
    }

    private void handleConfirmOrder(ActionEvent e) {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.", "주문 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                totalLabel.getText() + "로 주문을 확정하시겠습니까?",
                "주문 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {

            // ⭐ 1. 주문 데이터 수집 (구분자 충돌 해결)
            StringBuilder itemSummary = new StringBuilder();
            long totalAmount = 0;

            for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                String name = (String) cartTableModel.getValueAt(i, 1);
                // 장바구니에 수량이 Integer 타입으로 들어갔다고 가정
                int quantity = (Integer) cartTableModel.getValueAt(i, 2);
                String formattedPrice = (String) cartTableModel.getValueAt(i, 3);

                totalAmount += Long.parseLong(formattedPrice.replaceAll("[^0-9]", ""));

                itemSummary.append(name).append(" x ").append(quantity);
                if (i < cartTableModel.getRowCount() - 1) {
                    itemSummary.append("; "); // ⭐ [핵심 수정] 항목 간의 구분자를 세미콜론(; )으로 변경
                }
            }

            // ⭐ 2. 현재 사용자 객실 번호 가져오기 (임시값 사용)
            String roomNumber = "101";

            // 3. DataManager 호출 및 저장
            String newId = dataManager.addServiceRequest(roomNumber, itemSummary.toString(), totalAmount);

            if (newId != null) {
                JOptionPane.showMessageDialog(this, "룸서비스 주문이 요청되었습니다! (ID: " + newId + ")", "주문 완료", JOptionPane.INFORMATION_MESSAGE);

                // 주문 완료 후 창을 닫아 UserMainFrame으로 복귀
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) {
                    w.dispose();
                }

            } else {
                JOptionPane.showMessageDialog(this, "주문 저장 중 시스템 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTotal() {
        long total = 0;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            String formattedPrice = (String) cartTableModel.getValueAt(i, 3);
            total += Long.parseLong(formattedPrice.replaceAll("[^0-9]", ""));
        }
        totalLabel.setText("총 주문 금액: " + String.format("%,d", total) + "원");
    }
}