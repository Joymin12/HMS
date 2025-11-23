package hms.view;

import hms.controller.RoomController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RoomManagementFrame extends JFrame {
    private final JFrame parentFrame;
    private final RoomController controller;
    private JTable roomTable;
    private DefaultTableModel tableModel;

    public RoomManagementFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.controller = new RoomController();

        setTitle("🔑 객실 및 가격 관리");
        setSize(600, 500);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. 타이틀
        JLabel title = new JLabel("객실 정보 관리", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(title, BorderLayout.NORTH);

        // 2. 테이블 (목록 표시)
        String[] cols = {"객실 번호", "등급", "1박 가격 (원)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        roomTable = new JTable(tableModel);
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        // 3. 버튼 패널
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("추가");
        JButton editBtn = new JButton("수정");
        JButton delBtn = new JButton("삭제");
        JButton closeBtn = new JButton("닫기");

        addBtn.addActionListener(this::handleAdd);
        editBtn.addActionListener(this::handleEdit);
        delBtn.addActionListener(this::handleDelete);
        closeBtn.addActionListener(e -> returnToMain());

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(new JSeparator(SwingConstants.VERTICAL));
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 창 닫기 시 이벤트
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                returnToMain();
            }
        });

        // 초기 데이터 로드
        loadData();
        setVisible(true);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<String[]> rooms = controller.getAllRooms();
        for (String[] r : rooms) {
            try {
                int price = Integer.parseInt(r[2]);
                tableModel.addRow(new Object[]{r[0], r[1], String.format("%,d", price)});
            } catch (Exception e) {
                tableModel.addRow(new Object[]{r[0], r[1], r[2]});
            }
        }
    }

    private void handleAdd(ActionEvent e) {
        JTextField numF = new JTextField();
        String[] grades = {"스탠다드", "디럭스", "스위트"};
        JComboBox<String> gradeBox = new JComboBox<>(grades);
        JTextField priceF = new JTextField();

        Object[] message = {
                "객실 번호 (예: 101):", numF,
                "등급:", gradeBox,
                "가격 (숫자만):", priceF
        };

        int option = JOptionPane.showConfirmDialog(this, message, "객실 추가", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String num = numF.getText().trim();
                String grade = (String) gradeBox.getSelectedItem();
                int price = Integer.parseInt(priceF.getText().trim());

                if(num.isEmpty()) throw new Exception();

                if(controller.addRoom(num, grade, price)) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "추가되었습니다.");
                } else {
                    JOptionPane.showMessageDialog(this, "실패: 이미 존재하는 객실 번호이거나 오류 발생.");
                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "입력 오류: 정보를 정확히 입력해주세요.");
            }
        }
    }

    private void handleEdit(ActionEvent e) {
        int row = roomTable.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "수정할 객실을 선택하세요."); return; }

        String curNum = (String) tableModel.getValueAt(row, 0);
        String curGrade = (String) tableModel.getValueAt(row, 1);
        String curPriceStr = ((String) tableModel.getValueAt(row, 2)).replaceAll("[^0-9]", "");

        JTextField numF = new JTextField(curNum);
        numF.setEditable(false); // ID는 수정 불가
        String[] grades = {"스탠다드", "디럭스", "스위트"};
        JComboBox<String> gradeBox = new JComboBox<>(grades);
        gradeBox.setSelectedItem(curGrade);
        JTextField priceF = new JTextField(curPriceStr);

        Object[] message = { "객실 번호 (수정불가):", numF, "등급:", gradeBox, "가격:", priceF };

        if(JOptionPane.showConfirmDialog(this, message, "객실 수정", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int price = Integer.parseInt(priceF.getText().trim());
                if(controller.updateRoom(curNum, (String)gradeBox.getSelectedItem(), price)) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "수정되었습니다.");
                }
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "가격은 숫자여야 합니다."); }
        }
    }

    private void handleDelete(ActionEvent e) {
        int row = roomTable.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "삭제할 객실을 선택하세요."); return; }
        String num = (String) tableModel.getValueAt(row, 0);

        if(JOptionPane.showConfirmDialog(this, num + "호 객실을 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if(controller.deleteRoom(num)) {
                loadData();
                JOptionPane.showMessageDialog(this, "삭제되었습니다.");
            }
        }
    }

    private void returnToMain() {
        dispose();
        if(parentFrame != null) parentFrame.setVisible(true);
    }
}