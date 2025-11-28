package hms.view;

import hms.controller.ReservationController;
import hms.controller.UserController;
import hms.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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

        // ⭐ [수정] 등급 선택 단계로 돌아올 때마다 매진 상태를 다시 계산하여 화면 갱신
        if (stepName.equals("step2_grade")) {
            if (step2_grade != null) {
                step2_grade.updateGradeStatus();
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

    // ❌ 하드코딩된 가격을 리턴하던 getRoomPrices() 메서드는 제거되었음.

    public void setStep2Data(String grade) {
        this.selectedGrade = grade;
        // ❌ 기존 하드코딩된 가격을 설정하던 this.basePricePerNight = ... 줄은 제거됨.
        // 가격은 이제 Step3에서 실시간으로 조회됨.
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

    /**
     * ⭐ [필수 로직 추가] 선택된 날짜를 기준으로 등급별 매진 여부를 확인합니다.
     * (Reservation_GradePanel에서 호출됨)
     */
    public Map<String, Boolean> getGradeSoldOutStatus() {
        Map<String, Boolean> status = new HashMap<>();

        if (checkInDate == null || checkOutDate == null) {
            status.put("스탠다드", false);
            status.put("디럭스", false);
            status.put("스위트", false);
            return status;
        }

        // 1. 이미 예약된 객실 목록을 가져옴 (Controller 호출)
        List<String> bookedRooms = getBookedRooms();

        // 2. 등급별 객실 번호 목록 정의 (RoomDataManager에 있어야 이상적이지만, 현재 View/Manager에서 처리)
        Map<String, List<String>> gradeRooms = new HashMap<>();
        gradeRooms.put("스탠다드", Arrays.asList("101", "102", "103", "104", "105", "106", "107", "108"));
        gradeRooms.put("디럭스", Arrays.asList("201", "202", "203", "204", "205", "206", "207", "208"));
        gradeRooms.put("스위트", Arrays.asList("301", "302", "303", "304", "305", "306", "307", "308"));

        // 3. 등급별로 매진 여부 확인
        for (Map.Entry<String, List<String>> entry : gradeRooms.entrySet()) {
            List<String> totalRooms = entry.getValue();
            int totalRoomCount = totalRooms.size();
            int bookedCount = 0;

            // 해당 등급의 객실 중 예약된 객실 수를 카운트
            for (String room : totalRooms) {
                if (bookedRooms.contains(room)) {
                    bookedCount++;
                }
            }

            // 전체 객실 수 == 예약된 객실 수 이면 매진 (Sold Out)
            boolean isSoldOut = (bookedCount == totalRoomCount);
            status.put(entry.getKey(), isSoldOut);
        }

        return status;
    }


    public void setStep3Data(String roomNumber) {
        this.selectedRoom = roomNumber;
        calculatePrice(); // ⭐ 객실 확정 시 가격 계산 트리거
    }

    /**
     * ⭐ [수정됨] 하드코딩 대신 ReservationController를 통해 서버에 가격을 조회합니다.
     */
    private void calculatePrice() {
        // 1. Controller의 새 메서드를 호출하여 서버에게 실시간 가격을 요청합니다.
        this.totalPrice = reservationController.calculateFinalPrice(
                this.selectedRoom,
                (int)this.nights // nights는 long이므로 int로 변환
        );

        // 2. 가격 조회 실패 시 경고 메시지 표시
        if (this.totalPrice <= 0) {
            JOptionPane.showMessageDialog(reservationFrame,
                    "경고: 객실 가격 조회에 실패했습니다. 관리자에게 문의하세요.",
                    "가격 오류", JOptionPane.WARNING_MESSAGE);
        }
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
        finalData.put("totalPrice", totalPrice); // ⭐ 계산된 실시간 가격 사용!

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