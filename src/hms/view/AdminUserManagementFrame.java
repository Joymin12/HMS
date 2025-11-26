package hms.view;

import hms.controller.UserController;
import hms.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

// UserModifyDialog, UserAddDialog가 hms.view 패키지에 있다고 가정
import hms.view.UserModifyDialog;
import hms.view.UserAddDialog;

/**
 * 관리자용 사용자 관리 메인 프레임 (CRUD의 R, D, U, C 호출)
 */
public class AdminUserManagementFrame extends JFrame {

    private final String TITLE = "HMS - 사용자 관리";
    private final int WIDTH = 900;
    private final int HEIGHT = 600;

    private final AdminMainFrame parentFrame;
    private final UserController userController;
    private JTable userTable;
    public DefaultTableModel tableModel;

    // 생성자
    public AdminUserManagementFrame(AdminMainFrame parentFrame, UserController userController) {
        this.parentFrame = parentFrame;
        this.userController = userController;

        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                returnToParent();
            }
        });

        // 헤더 패널
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 테이블 패널
        JScrollPane tableScrollPane = createTablePanel();
        add(tableScrollPane, BorderLayout.CENTER);

        // 버튼 패널
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadUserData();
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ✔ 절대 깨지지 않는 이모지 ☑ 사용
        JLabel titleLabel = new JLabel("☑ 사용자 관리");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        panel.add(titleLabel);
        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"ID", "이름", "연락처", "나이", "권한"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(30);

        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openModifyUserDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));

        JButton addButton = new JButton("➕ 사용자 추가");
        addButton.addActionListener(e -> openAddUserDialog());

        JButton modifyButton = new JButton("✏️ 선택 사용자 수정");
        modifyButton.addActionListener(e -> openModifyUserDialog());

        JButton deleteButton = new JButton("🗑️ 선택 사용자 제거");
        deleteButton.addActionListener(e -> deleteSelectedUser());

        JButton refreshButton = new JButton("🔄 새로고침");
        refreshButton.addActionListener(e -> loadUserData());

        JButton backButton = new JButton("◀ 돌아가기");
        backButton.addActionListener(e -> returnToParent());

        panel.add(addButton);
        panel.add(modifyButton);
        panel.add(deleteButton);
        panel.add(refreshButton);
        panel.add(backButton);

        return panel;
    }

    public void loadUserData() {
        tableModel.setRowCount(0);

        List<User> userList = userController.getAllUsersForAdmin();

        if (userList != null) {
            for (User user : userList) {
                Object[] rowData = {
                        user.getId(),
                        user.getName(),
                        user.getPhoneNumber(),
                        user.getAge(),
                        user.getRole()
                };
                tableModel.addRow(rowData);
            }
        } else {
            JOptionPane.showMessageDialog(this, "사용자 데이터를 불러오는 데 실패했습니다.", "통신 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 사용자를 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userIdToDelete = (String) tableModel.getValueAt(selectedRow, 0);

        if (userController.getCurrentlyLoggedInUser() != null &&
                userController.getCurrentlyLoggedInUser().getId().equals(userIdToDelete)) {
            JOptionPane.showMessageDialog(this, "현재 로그인된 관리자는 삭제할 수 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "정말로 사용자 [" + userIdToDelete + "]를 삭제하시겠습니까?",
                "사용자 삭제 확인",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (userController.deleteUserByAdmin(userIdToDelete)) {
                JOptionPane.showMessageDialog(this, "사용자를 성공적으로 삭제했습니다.");
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "사용자 삭제 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openAddUserDialog() {
        new UserAddDialog(this, this.userController);
    }

    private void openModifyUserDialog() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수정할 사용자를 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        new UserModifyDialog(this, this.userController, userId);
    }

    private void returnToParent() {
        parentFrame.setVisible(true);
        dispose();
    }
}

