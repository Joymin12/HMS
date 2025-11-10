package hms.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Locale;
import java.text.SimpleDateFormat; // ★ SimpleDateFormat import 추가

/**
 * [예약 4단계] 고객 정보 입력 패널
 */
public class Reservation_InfoPanel extends JPanel {

    private final ReservationManagerPanel manager;
    private final JTextArea summaryTextArea;
    private final JTextField customerNameField;
    private final JTextField phoneNumberField;
    private final JPanel cardInputPanel;
    private final JLabel noticeLabel;

    private String paymentMethod = "immediate";

    private final String NAME_PLACEHOLDER = "예약자 성함 (예: 홍길동)";
    private final String PHONE_PLACEHOLDER = "전화번호 (예: 010-1234-5678)";
    private final String CARD_PLACEHOLDER = "카드 번호 16자리 (저장되지 않음)";

    public Reservation_InfoPanel(ReservationManagerPanel manager) {
        this.manager = manager;
        setLayout(new BorderLayout(10, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // (1. 요약 정보)
        summaryTextArea = new JTextArea(8, 30);
        summaryTextArea.setEditable(false);
        summaryTextArea.setBackground(new Color(245, 245, 245));
        summaryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        summaryTextArea.setBorder(BorderFactory.createTitledBorder("예약 정보 요약"));
        add(summaryTextArea, BorderLayout.NORTH);

        // (2. 입력 폼)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("예약자 이름"), gbc);
        gbc.gridx = 1;
        customerNameField = createPlaceholderField(NAME_PLACEHOLDER);
        formPanel.add(customerNameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("전화번호"), gbc);
        gbc.gridx = 1;
        phoneNumberField = createPlaceholderField(PHONE_PLACEHOLDER);
        formPanel.add(phoneNumberField, gbc);

        // (3. 예약 방식)
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("예약 방식"), gbc);
        JRadioButton immediateRadio = new JRadioButton("지금 결제 (보장 예약)");
        immediateRadio.setSelected(true);
        immediateRadio.setOpaque(false);
        JRadioButton onsiteRadio = new JRadioButton("현장 결제 (미보장)");
        onsiteRadio.setOpaque(false);
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(immediateRadio); radioGroup.add(onsiteRadio);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.setOpaque(false);
        radioPanel.add(immediateRadio); radioPanel.add(onsiteRadio);
        gbc.gridx = 1;
        formPanel.add(radioPanel, gbc);

        // (4. 카드 입력 패널)
        cardInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cardInputPanel.setOpaque(false);
        cardInputPanel.add(new JLabel("카드 번호:"));
        JTextField cardField = createPlaceholderField(CARD_PLACEHOLDER);
        cardInputPanel.add(cardField);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        formPanel.add(cardInputPanel, gbc);

        // (5. 18시 자동 취소 안내 라벨)
        noticeLabel = new JLabel("<html><font color='red'>※ 당일 18시까지 체크인하지 않으시면 예약이 자동 취소됩니다.</font></html>");
        noticeLabel.setVisible(false);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        formPanel.add(noticeLabel, gbc);
        add(formPanel, BorderLayout.CENTER);

        // (6. 라디오 버튼 액션)
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

        // (7. 버튼 패널)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton prevButton = new JButton("이전으로 (방 다시 선택)");
        prevButton.addActionListener(e -> manager.showStep("roomShow"));

        JButton finalButton = new JButton("최종 예약 확정");
        finalButton.setBackground(new Color(37, 99, 235));
        finalButton.setForeground(Color.WHITE);

        finalButton.addActionListener(e -> {
            String customerName = getActualInput(customerNameField, NAME_PLACEHOLDER);
            String phoneNumber = getActualInput(phoneNumberField, PHONE_PLACEHOLDER);

            if (customerName == null || phoneNumber == null) {
                JOptionPane.showMessageDialog(this, "이름과 전화번호를 입력해주세요.");
                return;
            }

            String confirmMsg = paymentMethod.equals("immediate")
                    ? "보장 예약으로 진행됩니다. 지금 결제하시겠습니까?"
                    : "<html>현장 결제로 진행됩니다.<br><font color='red'><b>당일 18시까지 미 체크인 시 자동 취소</b></font>됩니다.<br>진행하시겠습니까?</html>";

            int result = JOptionPane.showConfirmDialog(this, confirmMsg, "예약 확인", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                // Manager의 최종 저장 메소드 호출
                manager.finalSaveReservation(customerName, phoneNumber, paymentMethod);

                // 이 중복 호출은 제거되었으므로 그대로 둡니다.
            }
        });

        buttonPanel.add(prevButton);
        buttonPanel.add(finalButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // =================================================================
    // ★ 요약 정보 업데이트 메소드 (Getter 이름 수정으로 오류 해결)
    // =================================================================

    /**
     * 이 패널이 보여질 때마다 Manager가 이 메서드를 호출해서
     * 요약 정보를 업데이트합니다.
     */
    public void updateSummary() {

        // 1. 총 요금(long)을 콤마가 포함된 문자열로 미리 포맷합니다.
        String priceFormatted = String.format(Locale.US, "%,d", manager.getTotalPrice());

        // 2. 최종 출력 시에는 포맷팅 플래그 대신 일반 문자열(%s)로 대체하여 오류를 방지합니다.
        summaryTextArea.setText(
                String.format(" --- 예약 정보 요약 --- \n\n" +
                                " 체크인: \t%s\n" +
                                " 체크아웃:\t%s (%d박)\n" +
                                " 인원: \t%d명\n" +
                                " 등급: \t%s\n" +
                                " 객실: \t%s호\n\n" +
                                " 총 요금: \t%s원",
                        // ★★★ 오류 수정: getCheckInDate(), getCheckOutDate(), getGuestCount() 사용 ★★★
                        new SimpleDateFormat("yyyy-MM-dd").format(manager.getCheckInDate()), // Date 객체를 문자열로 포맷
                        new SimpleDateFormat("yyyy-MM-dd").format(manager.getCheckOutDate()), // Date 객체를 문자열로 포맷
                        manager.getNights(),
                        manager.getGuestCount(), // getGuests() 대신 getGuestCount() 사용
                        manager.getSelectedGrade(),
                        manager.getSelectedRoom(),
                        priceFormatted
                ));
    }

    // (플레이스홀더 헬퍼 메소드)
    private JTextField createPlaceholderField(String placeholder) {
        JTextField textField = new JTextField(20);
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });
        return textField;
    }

    // (플레이스홀더 입력값 가져오는 헬퍼 메소드)
    private String getActualInput(JTextField textField, String placeholder) {
        String text = textField.getText().trim();
        if (text.isEmpty() || text.equals(placeholder)) {
            return null;
        }
        return text;
    }
}