package hms.controller;

import hms.network.NetworkMessage;
import hms.util.LateFeeCalculator;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationController {

    // [중요] 파일 데이터 구조에 맞춘 인덱스 정의
    public static final int RES_IDX_ID = 0;
    public static final int RES_IDX_CHECKIN_DATE = 3;
    public static final int RES_IDX_SCHED_CHECKOUT_DATE = 4;
    public static final int RES_IDX_ROOM_NUM = 9;
    public static final int RES_IDX_TOTAL_PRICE = 10;
    public static final int RES_IDX_STATUS = 12;
    public static final int RES_IDX_CHECKOUT_TIME = 13;     // 실제 체크아웃 시간

    // [NEW] 지연 요금이 저장될 인덱스 (14번째 칸)
    // 데이터 예시: ..., CHECKED_OUT, 2025-11-27 12:00:00, 20000
    public static final int RES_IDX_LATE_FEE = 14;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT";

    private String serverIp = "127.0.0.1";
    private int serverPort = 5000;

    private NetworkMessage sendRequest(String command, Object data) {
        try (Socket socket = new Socket(serverIp, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new NetworkMessage(command, data));
            out.flush();
            return (NetworkMessage) in.readObject();

        } catch (Exception e) {
            System.err.println("네트워크 오류: " + e.getMessage());
            return new NetworkMessage(false, "통신 오류", null);
        }
    }

    // ... 기존 조회/검증 메서드들은 그대로 유지 ...
    public boolean saveReservationToFile(Map<String, Object> data) {
        return sendRequest("RES_SAVE", data).isSuccess();
    }

    public String[] searchReservation(String name, String phoneNumber) {
        NetworkMessage res = sendRequest("RES_SEARCH", name + "," + phoneNumber);
        if (res.isSuccess() && res.getData() instanceof String[]) {
            return (String[]) res.getData();
        }
        return null;
    }

    public String[] getReservationDetailsById(String reservationId) {
        NetworkMessage res = sendRequest("RES_GET_BY_ID", reservationId);
        if (res.isSuccess()) return (String[]) res.getData();
        return null;
    }

    public boolean updateReservationStatus(String reservationId, String newStatus) {
        return sendRequest("RES_UPDATE_STATUS", reservationId + "," + newStatus).isSuccess();
    }

    public List<String> getBookedRooms(String checkInStr, String checkOutStr) {
        NetworkMessage res = sendRequest("RES_GET_BOOKED", checkInStr + "," + checkOutStr);
        if (res.isSuccess()) return (List<String>) res.getData();
        return new ArrayList<>();
    }

    public boolean validateReservationAndCheckIn(String lastSixDigits, String inputRoomNumber) {
        return sendRequest("RES_VALIDATE_CHECKIN", lastSixDigits + "," + inputRoomNumber).isSuccess();
    }

    public long getRoomCharge(String[] reservationData) {
        if (reservationData.length > RES_IDX_TOTAL_PRICE) {
            try {
                return Long.parseLong(reservationData[RES_IDX_TOTAL_PRICE].replaceAll("[^0-9]", ""));
            } catch (Exception e) { return 0; }
        }
        return 0;
    }

    // ==========================================================
    // [NEW] 파일에 저장된 지연 요금을 가져오는 메서드 (보고서용)
    // ==========================================================
    public int getSavedLateFee(String[] reservationData) {
        // 데이터 길이가 인덱스보다 커야만 읽을 수 있음 (옛날 데이터나 체크인 상태 대비)
        if (reservationData.length > RES_IDX_LATE_FEE) {
            try {
                return Integer.parseInt(reservationData[RES_IDX_LATE_FEE].trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0; // 지연료 정보가 없으면 0원
    }

    // ==========================================================
    // 체크아웃 프로세스
    // ==========================================================
    public boolean processCheckout(String roomNumber) {
        return processCheckout(roomNumber, 0);
    }

    public boolean processCheckout(String roomNumber, int lateFee) {
        // 서버에게 "객실번호,지연요금"을 보냄 -> 서버가 파일 맨 뒤에 지연요금을 붙여서 저장해야 함
        return sendRequest("RES_CHECKOUT", roomNumber + "," + lateFee).isSuccess();
    }

    // ==========================================================
    // 청구서 텍스트 생성
    // ==========================================================
    public String generateCheckoutBillText(String[] resData, int roomServiceTotal) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String roomNumber = resData[RES_IDX_ROOM_NUM];
        String checkInDateStr = resData[RES_IDX_CHECKIN_DATE];
        String scheduledCheckoutStr = resData[RES_IDX_SCHED_CHECKOUT_DATE];

        long roomPrice = 0;
        try {
            roomPrice = Long.parseLong(resData[RES_IDX_TOTAL_PRICE].replaceAll("[^0-9]", ""));
        } catch (Exception e) { roomPrice = 0; }

        // 현재 시간(실제 체크아웃)
        LocalDateTime now = LocalDateTime.now();

        // 지연 요금 계산 (서버 저장 전, 미리보기용 계산)
        int lateFee = calculateLateFee(scheduledCheckoutStr); // 아래 메서드 활용

        long finalPayAmount = lateFee + roomServiceTotal;

        sb.append("=== 체크아웃 청구서 ===\n");
        sb.append("객실: ").append(roomNumber).append("\n");
        sb.append("체크인: ").append(checkInDateStr).append("\n");
        sb.append("체크아웃: ").append(now.format(formatter)).append("\n");
        sb.append("--------------------\n");

        sb.append("숙박료: ").append(String.format("%,d", roomPrice)).append("원 (기결제)\n");

        if (lateFee > 0) {
            sb.append("지연료: ").append(String.format("%,d", lateFee)).append("원 (시간 초과)\n");
        } else {
            sb.append("지연료: 0원\n");
        }

        sb.append("룸서비스: ").append(String.format("%,d", roomServiceTotal)).append("원\n");
        sb.append("--------------------\n");

        sb.append("[룸서비스 상세]\n");
        if (roomServiceTotal == 0) sb.append("(내역 없음)\n");

        sb.append("--------------------\n");
        sb.append("총 결제액: ").append(String.format("%,d", finalPayAmount)).append("원");

        return sb.toString();
    }

    // 외부에서 호출하기 쉽게 메서드 분리
    public int calculateLateFee(String scheduledCheckoutStr) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // 예정 날짜의 00:00을 기준으로 잡거나, 로직에 따라 11:00은 Calculator 내부에서 처리
            LocalDateTime scheduledDate = java.time.LocalDate.parse(scheduledCheckoutStr, dateFormatter).atStartOfDay();
            return LateFeeCalculator.calculateLateFee(scheduledDate, LocalDateTime.now());
        } catch (Exception e) {
            return 0;
        }
    }
}