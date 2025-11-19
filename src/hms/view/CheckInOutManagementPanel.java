package hms.view;

import hms.controller.ReservationController;
import javax.swing.*;
import java.awt.*;

public class CheckInOutManagementPanel extends JPanel {

    // ⭐ [수정] JFrame 대신 CheckInOutFrame 타입을 명시하여 switchPanel 접근 가능
    private final CheckInOutFrame parentFrame;
    private final ReservationController controller;
    private JTextField reservationIdField;
    private JButton checkInButton;
    private JButton checkOutButton;
    private JButton backButton;

    // ⭐ [수정] 생성자 시그니처를 CheckInOutFrame으로 변경
    public CheckInOutManagementPanel(CheckInOutFrame parentFrame, ReservationController controller) {
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
        // ⭐ [수정] CheckInOutFrame의 returnToAdminMain() 메서드 호출
        backButton.addActionListener(e -> parentFrame.returnToAdminMain());

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

            // 상태 필드의 인덱스를 Controller 상수에서 가져옴
            final int STATUS_IDX = ReservationController.RES_IDX_STATUS;
            String currentStatus = reservationDetails.length > STATUS_IDX ? reservationDetails[STATUS_IDX] : ReservationController.STATUS_PENDING;

            if (actionType.equals("CHECK_IN")) {
                if (currentStatus.equals(ReservationController.STATUS_CHECKED_IN)) {
                    JOptionPane.showMessageDialog(this, "이미 체크인된 예약입니다.", "안내", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // ⭐ [수정] switchPanel 호출로 변경하여 CheckInProcessPanel로 전환
                parentFrame.switchPanel(CheckInOutFrame.CHECK_IN_PROCESS_VIEW, reservationDetails);


            } else if (actionType.equals("CHECK_OUT")) {
                // ⭐⭐ [활성화 및 수정] 체크아웃 로직 ⭐⭐

                if (currentStatus.equals(ReservationController.STATUS_CHECKED_OUT)) {
                    JOptionPane.showMessageDialog(this, "이미 체크아웃이 완료된 예약입니다.", "안내", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!currentStatus.equals(ReservationController.STATUS_CHECKED_IN)) {
                    JOptionPane.showMessageDialog(this, "체크아웃은 체크인된 상태에서만 가능합니다.", "안내", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ⭐ [핵심 호출] CheckoutProcessPanel로 전환
                parentFrame.switchPanel(CheckInOutFrame.CHECK_OUT_PROCESS_VIEW, reservationDetails);
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "해당 예약 번호로 검색되는 내역이 없습니다. 번호를 확인해 주세요.",
                    "조회 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
}