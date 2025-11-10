package hms.view;

import hms.controller.ReservationController;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

/**
 * 예약의 4단계를 모두 관리하는 최종 메인 패널 (Central Mediator).
 */
public class ReservationManagerPanel extends JPanel {

    // --- 1. 멤버 변수 ---
    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private ReservationFrame reservationFrame;
    private JFrame ultimateParentFrame; // ★ 최종 부모 프레임 필드 추가 (UserMainFrame/AdminMainFrame)
    private ReservationController reservationController;

    // --- 2. 단계별 패널 (멤버 변수) ---
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

    // ----------------------------------------------------
    // ★★★ 생성자: 2개의 인수를 받도록 수정 ★★★
    // ----------------------------------------------------
    public ReservationManagerPanel(ReservationFrame reservationFrame, JFrame ultimateParentFrame) { // ★ 인수 2개로 수정
        this.reservationFrame = reservationFrame;
        this.ultimateParentFrame = ultimateParentFrame; // ★ 최종 부모 저장
        this.reservationController = new ReservationController();

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // 단계별 패널 생성
        step1_search = new Reservation_SearchPanel(this);
        step2_grade = new Reservation_GradePanel(this);
        step3_roomShow = new Reservation_RoomShowPanel(this);
        step4_info = new Reservation_InfoPanel(this);

        // cardsPanel에 추가
        cardsPanel.add(step1_search, "search");
        cardsPanel.add(step2_grade, "step2_grade");
        cardsPanel.add(step3_roomShow, "roomShow");
        cardsPanel.add(step4_info, "info");

        setLayout(new BorderLayout());
        add(cardsPanel, BorderLayout.CENTER);

        cardLayout.show(cardsPanel, "search");
    }


    // =================================================================
    // ★★★ 4개 패널과 통신하는 핵심 위임 메소드들 (모든 오류 해결) ★★★
    // =================================================================

    /**
     * 단계별 패널 전환 및 데이터 갱신을 처리합니다.
     */
    public void showStep(String stepName) {
        if (stepName.equals("roomShow")) {
            step3_roomShow.updateRoomGrid();
        } else if (stepName.equals("info")) {
            step4_info.updateSummary(); // 4단계 정보 업데이트 호출
        }
        cardLayout.show(cardsPanel, stepName);
    }

    /**
     * [Step 1] 날짜와 인원 데이터를 받아서 멤버 변수에 저장합니다.
     */
    public void setStep1Data(Date checkIn, Date checkOut, int guests) {
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.guestCount = guests;

        long diffInMillies = Math.abs(checkOut.getTime() - checkIn.getTime());
        this.nights = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * [Step 1/2] 객실 등급별 기본 가격 정보를 제공합니다.
     */
    public Map<String, Integer> getRoomPrices() {
        Map<String, Integer> prices = new HashMap<>();
        prices.put("스탠다드", 100000);
        prices.put("디럭스", 150000);
        prices.put("스위트", 300000);
        return prices;
    }

    /**
     * [Step 2] 선택된 등급을 저장합니다.
     */
    public void setStep2Data(String grade) {
        this.selectedGrade = grade;
        this.basePricePerNight = getRoomPrices().getOrDefault(grade, 0);
    }

    /**
     * [Step 2/3] 현재 선택된 등급을 반환합니다.
     */
    public String getSelectedGrade() {
        return selectedGrade;
    }

    /**
     * [Step 3] Controller를 통해 예약된 객실 목록을 반환합니다.
     */
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
     * [Step 3] 선택된 객실 번호를 저장하고 4단계 가격 계산을 호출합니다.
     */
    public void setStep3Data(String roomNumber) {
        this.selectedRoom = roomNumber;
        calculatePrice();
    }

    /**
     * [내부 로직] 최종 총액을 계산합니다.
     */
    private void calculatePrice() {
        this.totalPrice = (long) basePricePerNight * nights;
    }

    // --- Getter 메소드 (Step 4에서 사용) ---
    public long getNights() { return nights; }
    public long getTotalPrice() { return totalPrice; }
    public String getSelectedRoom() { return selectedRoom; }
    public Date getCheckInDate() { return checkInDate; }
    public Date getCheckOutDate() { return checkOutDate; }
    public int getGuestCount() { return guestCount; }


    // =================================================================
    // 🏠 메인 복귀 메소드 (오류 해결 및 복귀 흐름 제어)
    // =================================================================

    /**
     * 메인으로 돌아가는 메소드 (취소 확인용)
     */
    public void goBackToMain() {
        int result = JOptionPane.showConfirmDialog(
                reservationFrame,
                "예약을 취소하고 메인 메뉴로 돌아가시겠습니까?",
                "예약 취소",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            reservationFrame.dispose();
            // 취소 시에도 최종 부모 창이 다시 보이게 합니다.
            if (ultimateParentFrame != null) {
                ultimateParentFrame.setVisible(true);
            }
        }
    }

    /**
     * 예약 성공 후 확인 질문 없이 메인으로 돌아갑니다.
     */
    public void goBackToMain(boolean reservationCompleted) {
        if (reservationCompleted) {
            reservationFrame.dispose();
            // 성공 시 최종 부모 창이 다시 보이게 합니다.
            if (ultimateParentFrame != null) {
                ultimateParentFrame.setVisible(true);
            }
        } else {
            goBackToMain();
        }
    }

    // =================================================================
    // 💾 최종 저장 메소드 (finalSaveReservation 메소드)
    // =================================================================

    public void finalSaveReservation(String customerName, String phoneNumber, String paymentMethod) {
        // 1. 최종 데이터 맵 구성
        Map<String, Object> finalData = new HashMap<>();
        finalData.put("customerName", customerName);
        finalData.put("phoneNumber", phoneNumber);
        finalData.put("checkIn", new SimpleDateFormat("yyyy-MM-dd").format(checkInDate));
        finalData.put("checkOut", new SimpleDateFormat("yyyy-MM-dd").format(checkOutDate));
        finalData.put("guests", guestCount);
        finalData.put("grade", selectedGrade);
        finalData.put("room", selectedRoom);
        finalData.put("totalPrice", totalPrice);
        finalData.put("paymentMethod", paymentMethod);

        // 2. Controller를 통해 파일 저장 요청
        boolean success = reservationController.saveReservationToFile(finalData);

        if (success) {
            JOptionPane.showMessageDialog(reservationFrame, "예약이 성공적으로 완료되었습니다!", "예약 완료", JOptionPane.INFORMATION_MESSAGE);
            goBackToMain(true); // 성공했으니 확인 없이 메인으로 복귀
        } else {
            JOptionPane.showMessageDialog(reservationFrame, "예약 저장 중 오류가 발생했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}