package hms.view;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.Calendar;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;

/**
 * [예약 1단계] 날짜와 인원을 입력받는 패널.
 */
public class Reservation_SearchPanel extends JPanel {

    private final ReservationManagerPanel manager;
    private JDateChooser checkInChooser;
    private JDateChooser checkOutChooser;
    private JTextField guestsField;
    private JButton nextButton;
    private JButton backButton;

    // ⭐ 오늘 날짜의 시작 시점을 저장할 필드
    private final Date todayStart;

    /**
     * 생성자
     */
    public Reservation_SearchPanel(ReservationManagerPanel manager) {
        this.manager = manager;

        // ⭐ 1. 오늘 날짜의 시작 시간 (00:00:00)을 계산 및 저장
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

        // --- 체크인 날짜 선택 ---
        gbc.gridwidth = 1; gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("체크인 날짜:"), gbc);
        gbc.gridx = 1;
        checkInChooser = new JDateChooser();
        checkInChooser.setPreferredSize(new Dimension(200, 30));

        // ⭐ 2. 체크인 달력에 '오늘 날짜 이후'만 선택 가능하도록 제한
        checkInChooser.setMinSelectableDate(todayStart);

        add(checkInChooser, gbc);

        // --- 체크아웃 날짜 선택 ---
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("체크아웃 날짜:"), gbc);
        gbc.gridx = 1;
        checkOutChooser = new JDateChooser();
        checkOutChooser.setPreferredSize(new Dimension(200, 30));
        add(checkOutChooser, gbc);

        // --- 인원 수, 버튼 추가 ---
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("인원 수:"), gbc);
        gbc.gridx = 1;
        guestsField = new JTextField(20);
        add(guestsField, gbc);

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


        // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
        // ★ (기존 로직) 체크인 날짜 선택 시, 체크아웃 날짜 제한 ★
        // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

        checkInChooser.getDateEditor().addPropertyChangeListener(
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("date".equals(evt.getPropertyName())) {
                            Date selectedCheckIn = (Date) evt.getNewValue();

                            if (selectedCheckIn != null) {
                                // 2. 체크아웃 최소 날짜 = (체크인 날짜 + 1일)
                                Calendar c = Calendar.getInstance();
                                c.setTime(selectedCheckIn);
                                c.add(Calendar.DAY_OF_MONTH, 1);
                                Date minCheckOutDate = c.getTime();

                                // 3. 체크아웃 캘린더에 '최소 선택 가능 날짜'를 설정
                                checkOutChooser.setMinSelectableDate(minCheckOutDate);

                                // 4. 기존 체크아웃 날짜가 최소 날짜보다 빠르면 초기화
                                Date currentCheckOut = checkOutChooser.getDate();
                                if (currentCheckOut != null && currentCheckOut.before(minCheckOutDate)) {
                                    checkOutChooser.setDate(null);
                                }
                            } else {
                                // 5. 체크인 날짜가 지워졌으면, 체크아웃 제한도 풂
                                checkOutChooser.setMinSelectableDate(null);
                            }
                        }
                    }
                });

        // --- '다음' 버튼 클릭 시 ---
        nextButton.addActionListener(e -> {
            Date checkIn = checkInChooser.getDate();
            Date checkOut = checkOutChooser.getDate();
            String guestsStr = guestsField.getText().trim();

            if (checkIn == null || checkOut == null || guestsStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 항목을 입력해주세요.");
                return;
            }

            // ⭐ 3. 최종 유효성 검사: 오늘 날짜 이전 선택 시 막기 (추가 안전장치)
            if (checkIn.before(todayStart)) {
                // 이 코드는 minSelectableDate 설정 때문에 발생할 확률은 낮지만, 안전장치로 유지
                JOptionPane.showMessageDialog(this, "체크인 날짜는 오늘 이후여야 합니다.", "날짜 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (checkIn.after(checkOut)) {
                JOptionPane.showMessageDialog(this, "체크아웃 날짜는 체크인 날짜 이후여야 합니다.");
                return;
            }

            int guests;
            try {
                guests = Integer.parseInt(guestsStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "인원 수는 숫자로 입력해주세요.");
                return;
            }

            // 날짜 객체를 String으로 변환하여 Controller에 전달 (매니저에서 처리한다고 가정)
            // ReservationManagerPanel의 setStep1Data가 Date 객체를 받으므로 그대로 전달합니다.
            manager.setStep1Data(checkIn, checkOut, guests);
            manager.showStep("step2_grade");
        });

        // --- '취소' 버튼 클릭 시 ---
        backButton.addActionListener(e -> {
            manager.goBackToMain();
        });
    }
}