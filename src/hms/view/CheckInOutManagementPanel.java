package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent; // WindowListener 사용을 위해 import 추가

public class CheckInOutManagementPanel extends JPanel {

    private final JFrame parentFrame; // AdminMainFrame에서 호출된 임시 JFrame (CheckInOutFrame 역할)
    private final ReservationController controller;
    private JTextField reservationIdField;
    private JButton checkInButton;
    private JButton checkOutButton;
    private JButton backButton;

    public CheckInOutManagementPanel(JFrame parentFrame, ReservationController controller) {
        this.parentFrame = parentFrame;
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- 1. Header ---
        JLabel headerLabel = new JLabel("<html><h2>🚪 체크인/아웃 관리</h2><p>예약 번호를 입력하고 진행할 작업을 선택하세요.</p></html>", SwingConstants.CENTER);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // --- 2. Input Panel ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("예약 번호 입력 (ID):"), gbc);

        reservationIdField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        inputPanel.add(reservationIdField, gbc);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        checkInButton = new JButton("✅ 체크인 시작");
        checkOutButton = new JButton("💳 체크아웃 시작");

        buttonGroup.add(checkInButton);
        buttonGroup.add(checkOutButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        inputPanel.add(buttonGroup, gbc);

        add(inputPanel, BorderLayout.CENTER);

        // --- 3. Footer (Back Button) ---
        backButton = new JButton("메인 화면으로 돌아가기");
        // 부모 JFrame을 닫아 AdminMainFrame으로 복귀하도록 처리
        backButton.addActionListener(e -> parentFrame.dispose());

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.add(backButton);
        add(footerPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        checkInButton.addActionListener(e -> handleCheckInOutStart("CHECK_IN"));
        checkOutButton.addActionListener(e -> handleCheckInOutStart("CHECK_OUT"));
    }

    private void handleCheckInOutStart(String actionType) {
        String reservationId = reservationIdField.getText().trim();

        if (reservationId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "예약 번호를 입력해 주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Controller를 통해 예약 상세 정보 조회
        String[] reservationDetails = controller.getReservationDetailsById(reservationId);

        if (reservationDetails != null) {

            // Index 12는 상태 필드 (ReservationController에서 PENDING으로 보장)
            String currentStatus = reservationDetails.length > 12 ? reservationDetails[12] : "PENDING";

            if (actionType.equals("CHECK_IN")) {
                if (currentStatus.equals("CHECKED_IN")) {
                    JOptionPane.showMessageDialog(this, "이미 체크인된 예약입니다.", "안내", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // ⭐⭐ [로직 활성화] CheckInProcessPanel을 새 창에 담아 호출합니다. ⭐⭐

                // 1. 현재 창 숨기기
                parentFrame.setVisible(false);

                // 2. 새 CheckInProcess 창 생성 (인라인 JFrame)
                JFrame checkInFrame = new JFrame("체크인 프로세스: " + reservationId);

                // 3. 창 닫힐 때 현재 CheckInOutManagementPanel이 포함된 프레임을 다시 보이게 설정
                checkInFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        parentFrame.setVisible(true); // 관리 화면 복귀
                    }
                });

                // 4. CheckInProcessPanel을 새 프레임에 추가 (3개 인자 호출)
                CheckInProcessPanel processPanel = new CheckInProcessPanel(checkInFrame, controller, reservationDetails);
                checkInFrame.add(processPanel);

                checkInFrame.setSize(600, 450);
                checkInFrame.setLocationRelativeTo(parentFrame);
                checkInFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                checkInFrame.setVisible(true); // 새 창 표시

            } else if (actionType.equals("CHECK_OUT")) {
                // ... (체크아웃 로직 유지)
                if (currentStatus.equals("CHECKED_OUT")) {
                    JOptionPane.showMessageDialog(this, "이미 체크아웃이 완료된 예약입니다.", "안내", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!currentStatus.equals("CHECKED_IN")) {
                    JOptionPane.showMessageDialog(this, "체크아웃은 체크인된 상태에서만 가능합니다.", "안내", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(this, "체크아웃 정산 기능은 준비 중입니다.", "안내", JOptionPane.INFORMATION_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "해당 예약 번호로 검색되는 내역이 없습니다. 번호를 확인해 주세요.",
                    "조회 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
}