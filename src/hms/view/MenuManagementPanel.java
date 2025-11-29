package hms.view;

import hms.controller.ReservationController;
import hms.controller.RoomServiceController; // ⭐ [NEW] RoomServiceController 임포트
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MenuManagementPanel extends JPanel {

    private final RoomServiceOrderFrame parentFrame;
    // ⭐ [CRITICAL] 필드 타입 변경: Menu 관리는 RoomServiceController의 책임입니다.
    private final RoomServiceController roomServiceController;
    private JTable menuTable;
    private DefaultTableModel tableModel;

    // ⭐ [CRITICAL] 생성자 인자 변경: ReservationController 대신 RoomServiceController를 받습니다.
    public MenuManagementPanel(RoomServiceOrderFrame parentFrame, RoomServiceController roomServiceController) {
        this.parentFrame = parentFrame;
        // ⭐ [수정] 외부에서 주입받은 Controller를 사용합니다.
        this.roomServiceController = roomServiceController;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"ID", "메뉴 이름", "가격 (원)", "카테고리"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        menuTable = new JTable(tableModel);
        add(new JScrollPane(menuTable), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        loadMenuData();
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton addButton = new JButton("추가");
        JButton editButton = new JButton("수정");
        JButton deleteButton = new JButton("삭제");
        JButton backButton = new JButton("⬅️ 돌아가기");

        addButton.addActionListener(this::handleAddAction);
        editButton.addActionListener(this::handleEditAction);
        deleteButton.addActionListener(this::handleDeleteAction);
        backButton.addActionListener(e -> parentFrame.switchPanel(RoomServiceOrderFrame.MAIN_VIEW));

        panel.add(addButton); panel.add(editButton); panel.add(deleteButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(backButton);
        return panel;
    }

    private void loadMenuData() {
        tableModel.setRowCount(0);
        // Controller 호출
        List<String[]> menuItems = roomServiceController.getAllMenu();
        for (String[] item : menuItems) {
            try {
                long price = Long.parseLong(item[2].trim());
                tableModel.addRow(new Object[]{item[0], item[1], String.format("%,d", price), item[3]});
            } catch (Exception e) {
                tableModel.addRow(new Object[]{item[0], item[1], item[2], item[3]});
            }
        }
        menuTable.repaint();
    }

    private void handleAddAction(ActionEvent e) {
        String name = JOptionPane.showInputDialog(this, "이름:");
        if (name == null || name.trim().isEmpty()) return;
        String priceStr = JOptionPane.showInputDialog(this, "가격:");
        if (priceStr == null) return;
        int price;
        try { price = Integer.parseInt(priceStr.trim()); } catch (Exception ex) { return; }
        String category = JOptionPane.showInputDialog(this, "카테고리:");
        if (category == null) return;

        // Controller 호출
        if (roomServiceController.addMenuItem(name, price, category) != null) {
            JOptionPane.showMessageDialog(this, "추가되었습니다.");
            loadMenuData();
        }
    }

    private void handleEditAction(ActionEvent e) {
        int row = menuTable.getSelectedRow();
        if (row == -1) return;
        String id = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String priceStr = ((String) tableModel.getValueAt(row, 2)).replaceAll("[^0-9]", "");
        String cat = (String) tableModel.getValueAt(row, 3);

        String newName = JOptionPane.showInputDialog(this, "이름:", name);
        if (newName == null) return;
        String newPriceStr = JOptionPane.showInputDialog(this, "가격:", priceStr);
        int newPrice;
        try { newPrice = Integer.parseInt(newPriceStr); } catch (Exception ex) { return; }
        String newCat = JOptionPane.showInputDialog(this, "카테고리:", cat);

        // Controller 호출
        if (roomServiceController.updateMenuItem(id, newName, newPrice, newCat)) {
            JOptionPane.showMessageDialog(this, "수정되었습니다.");
            loadMenuData();
        }
    }

    private void handleDeleteAction(ActionEvent e) {
        int row = menuTable.getSelectedRow();
        if (row == -1) return;
        String id = (String) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // Controller 호출
            if (roomServiceController.deleteMenuItem(id)) {
                JOptionPane.showMessageDialog(this, "삭제되었습니다.");
                loadMenuData();
            }
        }
    }
}