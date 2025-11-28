package hms.view;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Date; // Date import needs to be present based on user's original code

public class Reservation_InfoPanel extends JPanel {

    private final ReservationManagerPanel manager;
    private final JTextArea summaryTextArea;
    private final JTextField customerNameField;
    private final JTextField phoneNumberField;
    private final JPanel cardInputPanel;
    private final JLabel noticeLabel;

    private JComboBox<String> checkInTimeCombo;
    private JComboBox<String> checkOutTimeCombo;
    private String paymentMethod = "immediate"; // immediate: 카드결제, onsite: 현장결제

    public Reservation_InfoPanel(ReservationManagerPanel manager) {
        this.manager = manager;
        setLayout(new BorderLayout(10, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        summaryTextArea = new JTextArea(8, 30);
        summaryTextArea.setEditable(false);
        summaryTextArea.setBackground(new Color(245, 245, 245));
        summaryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        summaryTextArea.setBorder(BorderFactory.createTitledBorder("예약 정보 요약"));
        add(summaryTextArea, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- 1. 예약자 이름 (자동 채움) ---
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("예약자 이름"), gbc);
        gbc.gridx = 1;
        customerNameField = new JTextField(20);
        formPanel.add(customerNameField, gbc);

        // --- 2. 전화번호 (자동 채움) ---
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("전화번호"), gbc);
        gbc.gridx = 1;
        phoneNumberField = new JTextField(20);
        formPanel.add(phoneNumberField, gbc);

        // --- 3. 시간 ---
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("체크인 예정 시간"), gbc);
        gbc.gridx = 1;
        checkInTimeCombo = new JComboBox<>(generateTimeOptions(15, 22, 30));
        formPanel.add(checkInTimeCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("체크아웃 예정 시간"), gbc);
        gbc.gridx = 1;
        checkOutTimeCombo = new JComboBox<>(generateTimeOptions(11, 14, 30));
        checkOutTimeCombo.setSelectedItem("11:00");
        formPanel.add(checkOutTimeCombo, gbc);

        // --- 4. 결제 방식 ---
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("예약 방식"), gbc);
        JRadioButton immediateRadio = new JRadioButton("지금 결제 (보장 예약)");
        immediateRadio.setSelected(true);
        JRadioButton onsiteRadio = new JRadioButton("현장 결제 (미보장)");
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(immediateRadio); radioGroup.add(onsiteRadio);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.setOpaque(false);
        radioPanel.add(immediateRadio); radioPanel.add(onsiteRadio);
        gbc.gridx = 1;
        formPanel.add(radioPanel, gbc);

        // --- 5. 카드 ---
        cardInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cardInputPanel.setOpaque(false);
        cardInputPanel.add(new JLabel("카드 번호:"));
        cardInputPanel.add(new JTextField("카드 번호 16자리", 16));
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        formPanel.add(cardInputPanel, gbc);

        // 현장 결제 경고 (18시 이후 자동 취소)
        noticeLabel = new JLabel("<html><font color='red'>※ 현장 결제 예약은 당일 18시까지 미입실 시 자동 취소됩니다.</font></html>");
        noticeLabel.setVisible(false);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(noticeLabel, gbc);
        add(formPanel, BorderLayout.CENTER);

        // 이벤트 리스너
        immediateRadio.addActionListener(e -> {
            paymentMethod = "immediate";
            cardInputPanel.setVisible(true);
            noticeLabel.setVisible(false);
        });
        onsiteRadio.addActionListener(e -> {
            paymentMethod = "onsite";
            cardInputPanel.setVisible(false);
            noticeLabel.setVisible(true);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton prevButton = new JButton("이전");
        prevButton.addActionListener(e -> manager.showStep("roomShow"));
        JButton finalButton = new JButton("최종 예약 확정");
        finalButton.setBackground(new Color(37, 99, 235));
        finalButton.setForeground(Color.WHITE);

        finalButton.addActionListener(e -> {
            String name = customerNameField.getText().trim();
            String phone = phoneNumberField.getText().trim();
            String inTime = (String) checkInTimeCombo.getSelectedItem();
            String outTime = (String) checkOutTimeCombo.getSelectedItem();

            if(name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "예약자 정보 누락: 이름과 전화번호를 입력해주세요.");
                return;
            }

            // ⭐ [핵심 수정] 뷰의 변수(immediate/onsite)를 서버의 기준 문자열로 변환
            String finalPaymentMethod;
            if (paymentMethod.equals("onsite")) {
                finalPaymentMethod = "현장결제"; // 서버 로직에서 사용하는 문자열
            } else {
                finalPaymentMethod = "카드결제"; // 서버 로직에서 사용하는 문자열
            }

            int res = JOptionPane.showConfirmDialog(this, "예약을 확정하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                // ⭐ 변환된 문자열을 Manager로 전달하여 저장 (매출 로직에 영향)
                manager.finalSaveReservation(name, phone, finalPaymentMethod, inTime, outTime);
            }
        });

        buttonPanel.add(prevButton);
        buttonPanel.add(finalButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void updateSummary() {
        String priceFormatted = String.format(Locale.US, "%,d", manager.getTotalPrice());

        // ⭐ [핵심] 1단계에서 입력한 이름/전화번호를 자동으로 채워줌
        customerNameField.setText(manager.getCustomerName());
        phoneNumberField.setText(manager.getPhoneNumber());

        summaryTextArea.setText(String.format(
                " [예약 정보 요약]\n 날짜: %s ~ %s (%d박)\n 인원: %d명\n 객실: %s / %s호\n 총액: %s원",
                new SimpleDateFormat("yyyy-MM-dd").format(manager.getCheckInDate()),
                new SimpleDateFormat("yyyy-MM-dd").format(manager.getCheckOutDate()),
                manager.getNights(),
                manager.getGuestCount(),
                manager.getSelectedGrade(),
                manager.getSelectedRoom(),
                priceFormatted
        ));
    }

    private String[] generateTimeOptions(int start, int end, int interval) {
        List<String> t = new ArrayList<>();
        for(int h=start; h<=end; h++) {
            for(int m=0; m<60; m+=interval) {
                if(h==end && m>0) break;
                t.add(String.format("%02d:%02d", h, m));
            }
        }
        return t.toArray(new String[0]);
    }
}