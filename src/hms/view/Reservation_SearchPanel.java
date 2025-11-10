package hms.view;

// 1. import 문 (JCalendar 및 이벤트 리스너)
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar; // ★ (1) import 추가
import java.beans.PropertyChangeEvent; // ★ (2) import 추가
import java.beans.PropertyChangeListener; // ★ (3) import 추가


/**
 * [예약 1단계] 날짜와 인원을 입력받는 패널.
 * (★ 수정: 체크인 날짜 선택 시 체크아웃 날짜 제한 기능 추가)
 */
public class Reservation_SearchPanel extends JPanel {

    // (멤버 변수 동일)
    private final ReservationManagerPanel manager;
    private JDateChooser checkInChooser;
    private JDateChooser checkOutChooser;
    private JTextField guestsField;
    private JButton nextButton;
    private JButton backButton;

    /**
     * 생성자
     * @param manager 이 패널을 제어할 ReservationManagerPanel
     */
    public Reservation_SearchPanel(ReservationManagerPanel manager) {
        this.manager = manager;

        // (레이아웃, 디자인, 컴포넌트 추가 ... 모두 동일)

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

        gbc.gridwidth = 1; gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("체크인 날짜:"), gbc);
        gbc.gridx = 1;
        checkInChooser = new JDateChooser();
        checkInChooser.setPreferredSize(new Dimension(200, 30));
        add(checkInChooser, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("체크아웃 날짜:"), gbc);
        gbc.gridx = 1;
        checkOutChooser = new JDateChooser();
        checkOutChooser.setPreferredSize(new Dimension(200, 30));
        add(checkOutChooser, gbc);

        // --- (인원 수, 버튼 추가 ... 모두 동일) ---
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
        // ★ (추가) 체크인 날짜 선택 시, 체크아웃 날짜 제한 ★
        // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

        // checkInChooser의 날짜 속성('date')이 변경될 때마다 이 리스너가 실행됩니다.
        checkInChooser.getDateEditor().addPropertyChangeListener(
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        // 'date' 속성이 변경되었을 때만 실행
                        if ("date".equals(evt.getPropertyName())) {

                            // 1. 선택된 새 체크인 날짜 가져오기
                            Date selectedCheckIn = (Date) evt.getNewValue();

                            if (selectedCheckIn != null) {
                                // 2. 체크아웃 최소 날짜 = (체크인 날짜 + 1일)
                                Calendar c = Calendar.getInstance();
                                c.setTime(selectedCheckIn);
                                c.add(Calendar.DAY_OF_MONTH, 1); // 날짜에 1을 더함
                                Date minCheckOutDate = c.getTime();

                                // 3. 체크아웃 캘린더에 '최소 선택 가능 날짜'를 설정
                                // (이 설정으로 12일을 선택했다면, 12일 및 그 이전은 비활성화됩니다)
                                checkOutChooser.setMinSelectableDate(minCheckOutDate);

                                // 4. (개선) 만약 기존에 선택된 체크아웃 날짜가
                                //    새로운 최소 날짜보다 빠르다면, 체크아웃 날짜를 초기화(null)
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

        // (기존 '다음' 버튼, '취소' 버튼 리스너는 그대로 둡니다)
        // --- '다음' 버튼 클릭 시 ---
        nextButton.addActionListener(e -> {
            Date checkIn = checkInChooser.getDate();
            Date checkOut = checkOutChooser.getDate();
            String guestsStr = guestsField.getText().trim();

            if (checkIn == null || checkOut == null || guestsStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 항목을 입력해주세요.");
                return;
            }
            if (checkIn.after(checkOut)) {
                // (이 로직은 사실상 위 리스너 때문에 발생하기 어려워졌지만,
                //  혹시 모를 상황을 대비해 남겨둡니다.)
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

            manager.setStep1Data(checkIn, checkOut, guests);
            manager.showStep("step2_grade");
        });

        // --- '취소' 버튼 클릭 시 ---
        backButton.addActionListener(e -> {
            manager.goBackToMain();
        });
    }
}