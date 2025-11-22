package hms.view;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.Calendar;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Reservation_SearchPanel extends JPanel {

    private final ReservationManagerPanel manager;
    private JDateChooser checkInChooser;
    private JDateChooser checkOutChooser;
    private JTextField guestsField;
    private JTextField nameField;  // ⭐ 추가됨
    private JTextField phoneField; // ⭐ 추가됨
    private JButton nextButton;
    private JButton backButton;
    private final Date todayStart;

    public Reservation_SearchPanel(ReservationManagerPanel manager) {
        this.manager = manager;

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        this.todayStart = todayCalendar.getTime();

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("예약 정보 입력");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, gbc);

        // --- 1. 예약자 정보 (신규 추가) ---
        gbc.gridwidth = 1; gbc.gridy++;
        gbc.gridx = 0; add(new JLabel("예약자 이름:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        add(nameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0; add(new JLabel("전화번호:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(15);
        add(phoneField, gbc);

        // --- 2. 날짜 정보 ---
        gbc.gridy++;
        gbc.gridx = 0; add(new JLabel("체크인 날짜:"), gbc);
        gbc.gridx = 1;
        checkInChooser = new JDateChooser();
        checkInChooser.setPreferredSize(new Dimension(200, 30));
        checkInChooser.setMinSelectableDate(todayStart);
        add(checkInChooser, gbc);

        gbc.gridy++;
        gbc.gridx = 0; add(new JLabel("체크아웃 날짜:"), gbc);
        gbc.gridx = 1;
        checkOutChooser = new JDateChooser();
        checkOutChooser.setPreferredSize(new Dimension(200, 30));
        add(checkOutChooser, gbc);

        // --- 3. 인원 수 ---
        gbc.gridy++;
        gbc.gridx = 0; add(new JLabel("인원 수:"), gbc);
        gbc.gridx = 1;
        guestsField = new JTextField(20);
        add(guestsField, gbc);

        // --- 버튼 ---
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        nextButton = new JButton("객실 등급 선택하기 (다음)");
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(nextButton, gbc);

        gbc.gridy++;
        backButton = new JButton("메인으로 돌아가기");
        backButton.setForeground(Color.GRAY);
        add(backButton, gbc);

        // 날짜 자동 조정 로직
        checkInChooser.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                Date selectedCheckIn = (Date) evt.getNewValue();
                if (selectedCheckIn != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(selectedCheckIn);
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    checkOutChooser.setMinSelectableDate(c.getTime());
                } else {
                    checkOutChooser.setMinSelectableDate(null);
                }
            }
        });

        nextButton.addActionListener(e -> {
            Date checkIn = checkInChooser.getDate();
            Date checkOut = checkOutChooser.getDate();
            String guestsStr = guestsField.getText().trim();
            String name = nameField.getText().trim(); // ⭐ 값 가져오기
            String phone = phoneField.getText().trim(); // ⭐ 값 가져오기

            if (checkIn == null || checkOut == null || guestsStr.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 정보(이름, 전화번호 포함)를 입력해주세요.");
                return;
            }

            if (checkIn.before(todayStart)) {
                JOptionPane.showMessageDialog(this, "체크인 날짜는 오늘 이후여야 합니다.");
                return;
            }
            if (checkIn.after(checkOut)) {
                JOptionPane.showMessageDialog(this, "체크아웃 날짜 오류.");
                return;
            }

            int guests;
            try {
                guests = Integer.parseInt(guestsStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "인원 수는 숫자만 입력.");
                return;
            }

            // ⭐ 매니저에게 이름/전화번호까지 모두 전달
            manager.setStep1Data(checkIn, checkOut, guests, name, phone);
            manager.showStep("step2_grade");
        });

        backButton.addActionListener(e -> manager.goBackToMain());
    }
}