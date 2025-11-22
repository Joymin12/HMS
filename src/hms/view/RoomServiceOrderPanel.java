package hms.view;

import hms.controller.RoomServiceController; // 변경
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RoomServiceOrderPanel extends JPanel {

    private final UserMainFrame parentFrame;
    private final RoomServiceController controller; // 변경

    private JTable menuTable;
    private DefaultTableModel menuTableModel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JTextField quantityField;
    private JLabel totalLabel;
    private JComboBox<String> categoryFilter;

    public RoomServiceOrderPanel(UserMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.controller = new RoomServiceController(); // 변경

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel menuListPanel = createMenuListPanel();
        add(menuListPanel, BorderLayout.NORTH);

        JPanel orderCartPanel = createOrderCartPanel();
        add(orderCartPanel, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        loadCategories();
        loadMenuData();
    }

    private JPanel createMenuListPanel() {
        String[] menuColumns = {"ID", "메뉴 이름", "가격 (원)", "카테고리"};
        menuTableModel = new DefaultTableModel(menuColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        menuTable = new JTable(menuTableModel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("메뉴 목록"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryFilter = new JComboBox<>();
        filterPanel.add(new JLabel("카테고리 필터:"));
        filterPanel.add(categoryFilter);
        categoryFilter.addActionListener(this::handleCategoryFilterChange);

        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setPreferredSize(new Dimension(700, 150));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(filterPanel, BorderLayout.NORTH);
        northPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(northPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOrderCartPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        quantityField = new JTextField("1", 5);
        JButton addButton = new JButton("장바구니에 담기");
        inputPanel.add(new JLabel("수량:"));
        inputPanel.add(quantityField);
        inputPanel.add(addButton);

        String[] cartColumns = {"ID", "메뉴", "수량", "금액"};
        cartTableModel = new DefaultTableModel(cartColumns, 0);
        cartTable = new JTable(cartTableModel);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setPreferredSize(new Dimension(350, 150));

        JButton removeButton = new JButton("🗑️ 선택 항목 제거");
        JPanel cartButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cartButtonsPanel.add(removeButton);

        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("장바구니"));
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);
        cartPanel.add(inputPanel, BorderLayout.NORTH);
        cartPanel.add(cartButtonsPanel, BorderLayout.SOUTH);

        mainPanel.add(cartPanel);
        addButton.addActionListener(this::handleAddToCart);
        removeButton.addActionListener(this::handleRemoveFromCart);
        return mainPanel;
    }

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

    private void handleCategoryFilterChange(ActionEvent e) {
        if (categoryFilter.getSelectedItem() != null) loadMenuData();
    }

    private void loadCategories() {
        // Controller 호출
        List<String> categories = controller.getAllCategories();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("전체 메뉴");
        for (String category : categories) categoryFilter.addItem(category);
    }

    private void loadMenuData() {
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        if (selectedCategory != null && selectedCategory.equals("전체 메뉴")) selectedCategory = null;

        menuTableModel.setRowCount(0);
        // Controller 호출
        List<String[]> menuItems = controller.getMenuByCategory(selectedCategory);
        for (String[] item : menuItems) {
            menuTableModel.addRow(new Object[]{item[0], item[1], item[2], item[3]});
        }
        menuTable.repaint();
    }

    private void handleAddToCart(ActionEvent e) {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "먼저 주문할 메뉴를 선택해주세요.");
            return;
        }
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();

            String id = (String) menuTableModel.getValueAt(selectedRow, 0);
            String name = (String) menuTableModel.getValueAt(selectedRow, 1);
            String priceStr = (String) menuTableModel.getValueAt(selectedRow, 2);
            long price = Long.parseLong(priceStr.replaceAll("[^0-9]", ""));

            cartTableModel.addRow(new Object[]{id, name, quantity, String.format("%,d", price * quantity)});
            updateTotal();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "수량은 1 이상의 숫자여야 합니다.");
        }
    }

    private void handleRemoveFromCart(ActionEvent e) {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) return;
        cartTableModel.removeRow(selectedRow);
        updateTotal();
    }

    private void handleConfirmOrder(ActionEvent e) {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.");
            return;
        }
        String roomNumber = parentFrame.getAuthenticatedRoomNumber();
        if (roomNumber == null || roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "객실 인증 정보가 없습니다.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, roomNumber + "호실로 주문하시겠습니까?", "주문 확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            StringBuilder itemSummary = new StringBuilder();
            long totalAmount = 0;
            for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                String name = (String) cartTableModel.getValueAt(i, 1);
                int quantity = (Integer) cartTableModel.getValueAt(i, 2);
                String formattedPrice = (String) cartTableModel.getValueAt(i, 3);
                totalAmount += Long.parseLong(formattedPrice.replaceAll("[^0-9]", ""));
                itemSummary.append(name).append(" x ").append(quantity);
                if (i < cartTableModel.getRowCount() - 1) itemSummary.append("; ");
            }

            // Controller 호출
            String newId = controller.addServiceRequest(roomNumber, itemSummary.toString(), totalAmount);
            if (newId != null) {
                JOptionPane.showMessageDialog(this, "주문이 완료되었습니다. (ID: " + newId + ")");
                SwingUtilities.getWindowAncestor(this).dispose();
            } else {
                JOptionPane.showMessageDialog(this, "주문 실패 (서버 오류)");
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