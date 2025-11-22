package hms.view;

import hms.controller.ReservationController;
import hms.controller.UserController;
import hms.model.User;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 예약의 4단계를 모두 관리하는 최종 메인 패널 (Central Mediator).
 */
public class ReservationManagerPanel extends JPanel {

    // ----------------------------------------------------------------
    // 1. 필드 선언 영역
    // ----------------------------------------------------------------
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardsPanel = new JPanel(cardLayout);

    private ReservationFrame reservationFrame;
    private JFrame ultimateParentFrame;
    private ReservationController reservationController;
    private UserController userController;

    private Reservation_SearchPanel step1_search;
    private Reservation_GradePanel  step2_grade;
    private Reservation_RoomShowPanel step3_roomShow;
    private Reservation_InfoPanel     step4_info;

    // --- 3. 예약 정보 저장용 변수 (상태) ---
    private Date checkInDate;
    private Date checkOutDate;
    private int guestCount;
    private String selectedGrade;
    private int basePricePerNight;
    private String selectedRoom;
    private long nights = 0;
    private long totalPrice = 0;

    // ⭐ [추가] 예약자 이름과 전화번호를 저장할 필드
    private String customerName;
    private String phoneNumber;


    // ----------------------------------------------------
    // 2. 생성자
    // ----------------------------------------------------
    public ReservationManagerPanel(ReservationFrame reservationFrame,
                                   JFrame ultimateParentFrame,
                                   ReservationController reservationController,
                                   UserController userController) {
        this.reservationFrame = reservationFrame;
        this.ultimateParentFrame = ultimateParentFrame;
        this.reservationController = reservationController;
        this.userController = userController;

        // ⭐ 실제 패널 객체 생성 (this를 넘겨서 Manager와 연결)
        step1_search = new Reservation_SearchPanel(this);
        step2_grade = new Reservation_GradePanel(this);
        step3_roomShow = new Reservation_RoomShowPanel(this);
        step4_info = new Reservation_InfoPanel(this);

        cardsPanel.add(step1_search, "search");
        cardsPanel.add(step2_grade, "step2_grade");
        cardsPanel.add(step3_roomShow, "roomShow");
        cardsPanel.add(step4_info, "info");

        setLayout(new BorderLayout());
        add(cardsPanel, BorderLayout.CENTER);
        cardLayout.show(cardsPanel, "search");
    }


    // =================================================================
    // 3. 핵심 위임 메소드들 (단계 전환 및 데이터 처리)
    // =================================================================

    public void showStep(String stepName) {
        if (stepName.equals("roomShow")) {
            if (step3_roomShow != null) {
                step3_roomShow.updateRoomGrid();
            }
        } else if (stepName.equals("info")) {
            if (step4_info != null) {
                step4_info.updateSummary(); // 4단계 정보 업데이트 호출
            }
        }
        cardLayout.show(cardsPanel, stepName);
    }

    /**
     * [Step 1] 날짜, 인원 + ⭐이름, 전화번호 데이터를 받아서 저장합니다.
     * (Reservation_SearchPanel에서 호출됨)
     */
    public void setStep1Data(Date checkIn, Date checkOut, int guests, String name, String phone) {
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.guestCount = guests;

        // ⭐ 전달받은 이름과 전화번호 저장
        this.customerName = name;
        this.phoneNumber = phone;

        long diffInMillies = Math.abs(checkOut.getTime() - checkIn.getTime());
        this.nights = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public Map<String, Integer> getRoomPrices() {
        Map<String, Integer> prices = new HashMap<>();
        prices.put("스탠다드", 100000);
        prices.put("디럭스", 150000);
        prices.put("스위트", 300000);
        return prices;
    }

    public void setStep2Data(String grade) {
        this.selectedGrade = grade;
        this.basePricePerNight = getRoomPrices().getOrDefault(grade, 0);
    }

    public List<String> getBookedRooms() {
        if (checkInDate == null || checkOutDate == null) {
            return Arrays.asList();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String checkInStr = sdf.format(checkInDate);
        String checkOutStr = sdf.format(checkOutDate);

        return reservationController.getBookedRooms(checkInStr, checkOutStr);
    }

    public void setStep3Data(String roomNumber) {
        this.selectedRoom = roomNumber;
        calculatePrice();
    }

    private void calculatePrice() {
        this.totalPrice = (long) basePricePerNight * nights;
    }

    // --- Getter 메소드 ---
    public long getTotalPrice() { return this.totalPrice; }
    public Date getCheckInDate() { return this.checkInDate; }
    public Date getCheckOutDate() { return this.checkOutDate; }
    public long getNights() { return this.nights; }
    public int getGuestCount() { return this.guestCount; }
    public String getSelectedGrade() { return selectedGrade; }
    public String getSelectedRoom() { return this.selectedRoom; }

    // ⭐ [추가] Reservation_InfoPanel에서 호출할 Getter 메소드들
    public String getCustomerName() { return this.customerName; }
    public String getPhoneNumber() { return this.phoneNumber; }


    // =================================================================
    // 4. 복귀 및 저장 메소드
    // =================================================================

    public void goBackToMain() {
        int result = JOptionPane.showConfirmDialog(
                reservationFrame,
                "예약을 취소하고 메인 메뉴로 돌아가시겠습니까?",
                "예약 취소",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            reservationFrame.dispose();
            if (ultimateParentFrame != null) {
                ultimateParentFrame.setVisible(true);
            }
        }
    }

    public void goBackToMain(boolean reservationCompleted) {
        if (reservationCompleted) {
            reservationFrame.dispose();
            if (ultimateParentFrame != null) {
                ultimateParentFrame.setVisible(true);
            }
        } else {
            goBackToMain();
        }
    }

    public String getCurrentUserId() {
        if (userController != null) {
            User currentUser = userController.getCurrentlyLoggedInUser();
            if (currentUser != null) {
                return currentUser.getId();
            }
        }
        return "GUEST";
    }

    /**
     * 최종 예약 저장 메소드 (4단계 패널에서 호출)
     */
    public void finalSaveReservation(
            String customerName,
            String phoneNumber,
            String paymentMethod,
            String estimatedInTime,
            String estimatedOutTime
    ) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Map<String, Object> finalData = new HashMap<>();

        // 저장해둔 정보들 사용
        finalData.put("checkIn", dateFormat.format(checkInDate));
        finalData.put("checkOut", dateFormat.format(checkOutDate));
        finalData.put("guests", guestCount);
        finalData.put("grade", selectedGrade);
        finalData.put("room", selectedRoom);
        finalData.put("totalPrice", totalPrice);

        // 매개변수로 받은 정보들 사용
        finalData.put("customerName", customerName);
        finalData.put("phoneNumber", phoneNumber);
        finalData.put("paymentMethod", paymentMethod);
        finalData.put("estimatedInTime", estimatedInTime);
        finalData.put("estimatedOutTime", estimatedOutTime);
        finalData.put("userId", getCurrentUserId());

        boolean success = reservationController.saveReservationToFile(finalData);

        if (success) {
            JOptionPane.showMessageDialog(reservationFrame, "예약이 성공적으로 완료되었습니다!", "예약 완료", JOptionPane.INFORMATION_MESSAGE);
            goBackToMain(true);
        } else {
            JOptionPane.showMessageDialog(reservationFrame, "예약 저장 중 오류가 발생했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}