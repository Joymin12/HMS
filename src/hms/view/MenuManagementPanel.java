package hms.view;

import hms.controller.ReservationController;
import hms.model.RoomServiceDataManager; // ⭐ DataManager import
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * [관리자] 룸서비스 메뉴 항목을 추가, 수정, 삭제하는 패널 (SFR-505~507).
 */
public class MenuManagementPanel extends JPanel {

    private final RoomServiceOrderFrame parentFrame;
    private final ReservationController controller;
    private final RoomServiceDataManager dataManager; // ⭐ DataManager 필드 추가

    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton backButton;

    public MenuManagementPanel(RoomServiceOrderFrame parentFrame, ReservationController controller) {
        this.parentFrame = parentFrame;
        this.controller = controller;
        this.dataManager = new RoomServiceDataManager(); // ⭐ DataManager 인스턴스화

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. 테이블 구성
        String[] columnNames = {"ID", "메뉴 이름", "가격 (원)", "카테고리"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 셀 편집 불가
            }
        };
        menuTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(menuTable);
        add(scrollPane, BorderLayout.CENTER);

        // 2. 버튼 패널 (CRUD 액션)
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        loadMenuData(); // ⭐ 초기 데이터 로드
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        // CRUD 버튼
        addButton = new JButton("⭐ 메뉴 추가");
        editButton = new JButton("메뉴 수정");
        deleteButton = new JButton("메뉴 삭제");
        backButton = new JButton("⬅️ 메인으로 돌아가기");

        addButton.addActionListener(this::handleAddAction);
        editButton.addActionListener(this::handleEditAction);
        deleteButton.addActionListener(this::handleDeleteAction);
        backButton.addActionListener(e -> parentFrame.switchPanel(RoomServiceOrderFrame.MAIN_VIEW));

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL)); // 구분선
        panel.add(backButton);

        return panel;
    }

    // ---------------------------------------------------------------------
    // 1. READ: 데이터 로드
    // ---------------------------------------------------------------------
    private void loadMenuData() {
        tableModel.setRowCount(0); // 기존 테이블 내용 초기화
        List<String[]> menuItems = dataManager.getAllMenu();

        for (String[] item : menuItems) {
            // [ID, Name, Price, Category]
            // 가격을 포맷팅하여 보기 좋게 표시
            try {
                long price = Long.parseLong(item[2].trim());
                tableModel.addRow(new Object[]{
                        item[0],
                        item[1],
                        String.format("%,d", price),
                        item[3]
                });
            } catch (NumberFormatException ignored) {
                // 가격 파싱 오류 시 원본 데이터로 추가
                tableModel.addRow(new Object[]{item[0], item[1], item[2], item[3]});
            }
        }
        // 테이블 UI 업데이트
        menuTable.repaint();
    }

    // ---------------------------------------------------------------------
    // 2. CREATE: 메뉴 추가
    // ---------------------------------------------------------------------
    private void handleAddAction(ActionEvent e) {
        String name = JOptionPane.showInputDialog(this, "추가할 메뉴 이름을 입력하세요:", "새 메뉴 추가", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        String priceStr = JOptionPane.showInputDialog(this, "가격 (숫자)을 입력하세요:", "새 메뉴 추가", JOptionPane.PLAIN_MESSAGE);
        if (priceStr == null || priceStr.trim().isEmpty()) return;

        int price;
        try {
            price = Integer.parseInt(priceStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "가격은 숫자로만 입력해야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String category = JOptionPane.showInputDialog(this, "카테고리 (예: 식품, 음료)를 입력하세요:", "새 메뉴 추가", JOptionPane.PLAIN_MESSAGE);
        if (category == null || category.trim().isEmpty()) return;

        // DataManager 호출 및 파일에 저장
        String newId = dataManager.addMenuItem(name, price, category);

        if (newId != null) {
            JOptionPane.showMessageDialog(this, name + " 메뉴가 추가되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
            loadMenuData(); // 테이블 새로고침
        } else {
            JOptionPane.showMessageDialog(this, "메뉴 저장 중 파일 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------------
    // 3. UPDATE: 메뉴 수정
    // ---------------------------------------------------------------------
    private void handleEditAction(ActionEvent e) {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수정할 메뉴를 테이블에서 선택해주세요.", "선택 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentPriceStr = ((String) tableModel.getValueAt(selectedRow, 2)).replaceAll("[^0-9]", ""); // 콤마 제거
        String currentCategory = (String) tableModel.getValueAt(selectedRow, 3);

        // 새 이름 입력
        String newName = JOptionPane.showInputDialog(this, "새 메뉴 이름:", "메뉴 수정", JOptionPane.PLAIN_MESSAGE, null, null, currentName).toString();
        if (newName == null || newName.trim().isEmpty()) return;

        // 새 가격 입력
        String newPriceStr = JOptionPane.showInputDialog(this, "새 가격 (원):", "메뉴 수정", JOptionPane.PLAIN_MESSAGE, null, null, currentPriceStr).toString();

        int newPrice;
        try {
            newPrice = Integer.parseInt(newPriceStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "가격은 숫자로만 입력해야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 새 카테고리 입력
        String newCategory = JOptionPane.showInputDialog(this, "새 카테고리:", "메뉴 수정", JOptionPane.PLAIN_MESSAGE, null, null, currentCategory).toString();
        if (newCategory == null || newCategory.trim().isEmpty()) return;

        // DataManager 호출 및 파일에 저장
        if (dataManager.updateMenuItem(id, newName, newPrice, newCategory)) {
            JOptionPane.showMessageDialog(this, "메뉴가 성공적으로 수정되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
            loadMenuData(); // 테이블 새로고침
        } else {
            JOptionPane.showMessageDialog(this, "메뉴 수정 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------------
    // 4. DELETE: 메뉴 삭제
    // ---------------------------------------------------------------------
    private void handleDeleteAction(ActionEvent e) {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 메뉴를 테이블에서 선택해주세요.", "선택 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "정말로 [" + name + "] 메뉴를 삭제하시겠습니까?\n(삭제하면 복구할 수 없습니다)",
                "삭제 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dataManager.deleteMenuItem(id)) {
                JOptionPane.showMessageDialog(this, name + " 메뉴가 삭제되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                loadMenuData(); // 테이블 새로고침
            } else {
                JOptionPane.showMessageDialog(this, "메뉴 삭제 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}