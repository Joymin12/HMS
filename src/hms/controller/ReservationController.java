package hms.controller;

import hms.network.NetworkMessage; // 네트워크 통신을 위한 import
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class ReservationController {

    // ---------------------------------------------------------------------
    // ⭐ 1. 상수 정의 (Constants)
    // ---------------------------------------------------------------------

    // 파일 경로 및 인덱스 상수 (⭐ 파일 I/O 로직을 유지하기 위해 경로 상수는 남겨둡니다.)
    private static final String RESERVATION_FILE = "data/reservation_info.txt";

    // 🚨 Public으로 수정: 외부 클래스(View)에서 접근 가능한 인덱스 상수
    public static final int RES_IDX_ID = 0;              // 예약 번호 인덱스
    public static final int RES_IDX_ROOM_NUM = 9;        // 객실 번호 인덱스
    public static final int RES_IDX_TOTAL_PRICE = 10;    // 총 요금 인덱스
    public static final int RES_IDX_STATUS = 12;         // 상태 인덱스
    public static final int RES_IDX_CHECKOUT_TIME = 13;  // 체크아웃 시간 인덱스

    // 🚨 Public으로 수정: 외부 클래스(View)에서 접근 가능한 예약 상태 상수
    public static final String STATUS_PENDING = "PENDING";       // 예약 대기 (초기값)
    public static final String STATUS_CHECKED_IN = "CHECKED_IN"; // 체크인 완료
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT"; // 체크아웃 완료

    // ⭐ 서버 IP (서버 컴퓨터의 IP로 변경하세요)
    private String serverIp = "192.168.0.2";
    private int serverPort = 5000;

    // --- 공통 통신 헬퍼 ---
    private NetworkMessage sendRequest(String command, Object data) {
        try (Socket socket = new Socket(serverIp, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new NetworkMessage(command, data));
            out.flush();
            return (NetworkMessage) in.readObject();

        } catch (Exception e) {
            // 통신 오류 발생 시 실패 응답 반환
            System.err.println("네트워크 통신 오류 발생: " + e.getMessage());
            return new NetworkMessage(false, "통신 오류", null);
        }
    }


    // ---------------------------------------------------------------------
    // 1. 예약 저장 (Save Reservation) - Network 로직 사용
    // ---------------------------------------------------------------------
    public boolean saveReservationToFile(Map<String, Object> data) {
        // 네트워크 요청에 필요한 데이터 구성 및 전송 (파일 I/O 로직은 서버 측에 존재)
        NetworkMessage res = sendRequest("RES_SAVE", data);
        return res.isSuccess();

        /* 🚨 파일 I/O 로직 (구 버전)은 서버 측으로 이동하거나 제거해야 합니다.
           현재는 Network 로직만 호출하도록 유지합니다.
           (만약 파일 I/O 로직이 여전히 필요한 상황이라면, 이 코드를 Network 통신 전에 구현해야 합니다.)
        */
    }

    // ---------------------------------------------------------------------
    // 2. 예약 검색 (Search Reservation by Name/Phone) - Network 로직 사용
    // ---------------------------------------------------------------------
    public String[] searchReservation(String name, String phoneNumber) {
        // NetworkMessage는 String[]을 반환한다고 가정
        NetworkMessage res = sendRequest("RES_SEARCH", name + "," + phoneNumber);
        if (res.isSuccess() && res.getData() instanceof String[]) {
            return (String[]) res.getData();
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // 3. 예약 상세 정보 조회 (by ID) - Network 로직 사용
    // ---------------------------------------------------------------------
    public String[] getReservationDetailsById(String reservationId) {
        NetworkMessage res = sendRequest("RES_GET_BY_ID", reservationId);
        if (res.isSuccess() && res.getData() instanceof String[]) {
            return (String[]) res.getData();
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // 4. 예약 상태 업데이트 (Update Status) - Network 로직 사용
    // ---------------------------------------------------------------------
    public boolean updateReservationStatus(String reservationId, String newStatus) {
        NetworkMessage res = sendRequest("RES_UPDATE_STATUS", reservationId + "," + newStatus);
        return res.isSuccess();
    }


    // ---------------------------------------------------------------------
    // 5. 예약된 방 목록 검색 (Get Booked Rooms - 날짜 겹침 확인) - Network 로직 사용
    // ---------------------------------------------------------------------
    public List<String> getBookedRooms(String checkInStr, String checkOutStr) {
        NetworkMessage res = sendRequest("RES_GET_BOOKED", checkInStr + "," + checkOutStr);
        if (res.isSuccess() && res.getData() instanceof List) {
            return (List<String>) res.getData();
        }
        return new ArrayList<>();
    }

    // ---------------------------------------------------------------------
    // 6. 총 숙박 요금 조회
    // ---------------------------------------------------------------------
    public long getRoomCharge(String[] reservationData) {
        if (reservationData.length > RES_IDX_TOTAL_PRICE) {
            try {
                // 숫자가 아닌 문자(쉼표 등) 제거 후 파싱
                String priceStr = reservationData[RES_IDX_TOTAL_PRICE].replaceAll("[^0-9]", "");
                return Long.parseLong(priceStr);
            } catch (NumberFormatException e) {
                System.err.println("ERROR: 숙박 비용 파싱 오류 - " + reservationData[RES_IDX_TOTAL_PRICE]);
                return 0;
            }
        }
        return 0;
    }

    // ---------------------------------------------------------------------
    // ⭐ 7. 예약 유효성 검증 및 체크인 처리 (UserMainFrame 요구 사항)
    // ---------------------------------------------------------------------
    /**
     * 예약 ID를 검증하고, 예약 상태가 PENDING인 경우 체크인 상태로 변경합니다.
     * @param reservationId 검증할 예약 ID
     * @param roomNumber 배정/확인된 객실 번호
     * @return 성공 시 true, 예약 정보가 없거나 상태가 PENDING이 아니거나 객실 번호가 일치하지 않으면 false
     */
    public boolean validateReservationAndCheckIn(String reservationId, String roomNumber) {
        // 서버 측에서 이 복잡한 검증 로직을 수행하도록 Network 요청을 보냅니다.
        NetworkMessage res = sendRequest("RES_VALIDATE_CHECKIN", reservationId + "," + roomNumber);

        // 서버 응답이 성공이고, 메시지가 긍정적이면 true
        if (res.isSuccess()) {
            System.out.println("[SUCCESS] 체크인 처리 완료: ID " + reservationId);
            return true;
        } else {
            // 실패 메시지를 출력하여 디버깅에 도움
            System.out.println("[FAIL] 체크인 실패: " + res.getMessage());
            return false;
        }

        /* 🚨 Note: 원본 파일 I/O 로직은 팀원의 Network 로직과 중복되어 제거되었습니다.
           만약 Network 통신 없이 파일 I/O로만 처리해야 한다면, 아래 로직을 복구해야 합니다.

           // 1. 예약 정보 조회 및 검증 로직
           String[] reservationDetails = getReservationDetailsById(reservationId);
           // ... (나머지 로직)
        */
    }

    // ---------------------------------------------------------------------
    // ⭐ [추가] 8. 룸서비스 객실 인증 (Authentication) - Network 로직 사용
    // ---------------------------------------------------------------------
    /**
     * 예약 ID 뒷 6자리와 객실 번호를 받아, 해당 예약이 CHECKED_IN 상태이며
     * 입력된 객실 번호와 일치하는지 검증합니다.
     * @param lastSixDigits 예약 ID의 뒷 6자리
     * @param inputRoomNumber 사용자가 입력한 객실 번호
     * @return 인증 및 체크인 상태가 유효하면 true
     */
    public boolean authenticateRoomService(String lastSixDigits, String inputRoomNumber) {
        String data = lastSixDigits + "," + inputRoomNumber;
        NetworkMessage res = sendRequest("AUTH_ROOM_SERVICE", data);

        if (res.isSuccess()) {
            System.out.println("[SUCCESS] 룸서비스 객실 인증 성공.");
            return true;
        } else {
            System.out.println("[FAIL] 룸서비스 객실 인증 실패: " + res.getMessage());
            return false;
        }
    }


    // ---------------------------------------------------------------------
    // 9. 체크아웃 처리 (Process Checkout) - Network 로직 사용
    // ---------------------------------------------------------------------
    public boolean processCheckout(String roomNumber) {
        NetworkMessage res = sendRequest("RES_CHECKOUT", roomNumber);
        if (res.isSuccess()) {
            System.out.println("DEBUG: 객실 " + roomNumber + " 체크아웃 완료.");
            return true;
        } else {
            System.out.println("[FAIL] 체크아웃 실패: " + res.getMessage());
            return false;
        }
    }
}