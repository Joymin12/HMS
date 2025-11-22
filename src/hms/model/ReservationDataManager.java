// 파일 경로: hms/model/ReservationDataManager.java (최종 수정)
package hms.model;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReservationDataManager {

    private static final String RESERVATION_FILE = "data/reservation_info.txt";
    // 인덱스 상수
    public static final int RES_IDX_ID = 0;
    public static final int RES_IDX_NAME = 1;
    public static final int RES_IDX_PHONE = 2;
    public static final int RES_IDX_CHECK_IN_DATE = 3;
    public static final int RES_IDX_CHECKOUT_DATE = 4;
    public static final int RES_IDX_ROOM_NUM = 9;
    public static final int RES_IDX_TOTAL_PRICE = 10;
    public static final int RES_IDX_PAYMENT = 11;
    public static final int RES_IDX_STATUS = 12;
    public static final int RES_IDX_CHECKOUT_TIME = 13;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT";

    public ReservationDataManager() {
        // 데이터 폴더 확인 및 생성
        File file = new File(RESERVATION_FILE);
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
        }
    }

    // 1. 예약 저장 (Save Reservation)
    public boolean saveReservation(Map<String, Object> data) {
        String datePart = new SimpleDateFormat("yyMMdd").format(new Date());
        int randomPart = (int)(Math.random() * 900000) + 100000;
        String confirmationId = datePart + "-" + randomPart;

        String line = String.join(",",
                confirmationId,
                (String) data.get("customerName"),
                (String) data.get("phoneNumber"),
                (String) data.get("checkIn"),
                (String) data.get("checkOut"),
                (String) data.get("estimatedInTime"),
                (String) data.get("estimatedOutTime"),
                String.valueOf(data.get("guests")),
                (String) data.get("grade"),
                (String) data.get("room"),
                String.valueOf(data.get("totalPrice")),
                (String) data.get("paymentMethod"),
                STATUS_PENDING,
                (String) data.get("userId") // 14번째 필드 (예약자 ID)
        );

        try (FileWriter fw = new FileWriter(RESERVATION_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. 예약 검색 (Search Reservation)
    public String[] searchReservation(String name, String phoneNumber) {
        // ... (기존 로직 유지) ...
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                // 최소 필드 길이 확인
                if (parts.length < 3) continue;
                // 이름(Index 1)과 전화번호(Index 2)로 검색
                if (parts[RES_IDX_NAME].trim().equals(name) && parts[RES_IDX_PHONE].trim().equals(phoneNumber)) {
                    // ⭐ [수정] 14개 필드 기준에 맞게 배열을 확장하여 반환 (Client/Server 통신 시 인덱스 오류 방지)
                    if (parts.length < RES_IDX_CHECKOUT_TIME + 1) {
                        String[] newParts = new String[RES_IDX_CHECKOUT_TIME + 1];
                        System.arraycopy(parts, 0, newParts, 0, parts.length);
                        // 누락된 상태 필드 채우기
                        if (parts.length <= RES_IDX_STATUS) newParts[RES_IDX_STATUS] = STATUS_PENDING;
                        if (parts.length <= RES_IDX_CHECKOUT_TIME) newParts[RES_IDX_CHECKOUT_TIME] = "";
                        return newParts;
                    }
                    return parts;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    // 3. ID로 조회 (Get Reservation by ID)
    public String[] getReservationById(String id) {
        // ... (기존 로직 유지, 배열 확장 로직 추가) ...
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && parts[RES_IDX_ID].trim().equals(id)) {
                    if (parts.length < RES_IDX_CHECKOUT_TIME + 1) {
                        String[] newParts = new String[RES_IDX_CHECKOUT_TIME + 1];
                        System.arraycopy(parts, 0, newParts, 0, parts.length);
                        if (parts.length <= RES_IDX_STATUS) newParts[RES_IDX_STATUS] = STATUS_PENDING;
                        if (parts.length <= RES_IDX_CHECKOUT_TIME) newParts[RES_IDX_CHECKOUT_TIME] = "";
                        return newParts;
                    }
                    return parts;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    // 4. 상태 업데이트 (Update Status)
    public boolean updateStatus(String id, String newStatus) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        String checkoutTime = newStatus.equals(STATUS_CHECKED_OUT)
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) : "";

        // ... (기존 로직 유지) ...
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > RES_IDX_STATUS && parts[RES_IDX_ID].equals(id)) {

                    String[] currentParts = parts;
                    // ⭐ 배열 확장 로직 (데이터 무결성 확보)
                    if (currentParts.length <= RES_IDX_CHECKOUT_TIME) {
                        currentParts = new String[RES_IDX_CHECKOUT_TIME + 1];
                        System.arraycopy(parts, 0, currentParts, 0, parts.length);
                        for(int i = parts.length; i <= RES_IDX_CHECKOUT_TIME; i++) {
                            currentParts[i] = "";
                        }
                    }

                    currentParts[RES_IDX_STATUS] = newStatus;
                    if (newStatus.equals(STATUS_CHECKED_OUT)) {
                        currentParts[RES_IDX_CHECKOUT_TIME] = checkoutTime;
                    }
                    lines.add(String.join(",", currentParts));
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { return false; }

        if (updated) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(RESERVATION_FILE))) {
                for (String l : lines) pw.println(l);
                return true;
            } catch (IOException e) { return false; }
        }
        return false;
    }

    // 5. 예약된 방 목록
    public List<String> getBookedRooms(String inStr, String outStr) {

        List<String> booked = new ArrayList<>();

        // ⭐ [수정 1] 메서드 시작 시점에 한 번만 날짜 객체로 파싱합니다.
        LocalDate checkIn = LocalDate.parse(inStr);
        LocalDate checkOut = LocalDate.parse(outStr);

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 12) continue;

                try {
                    LocalDate rIn = LocalDate.parse(parts[RES_IDX_CHECK_IN_DATE]);
                    LocalDate rOut = LocalDate.parse(parts[RES_IDX_CHECKOUT_DATE]);

                    // ⭐ [수정 2] 중복된 파싱 로직을 제거하고, 이미 선언된 checkIn/checkOut 객체를 사용합니다.
                    if (checkIn.isBefore(rOut) && checkOut.isAfter(rIn)) {
                        booked.add(parts[RES_IDX_ROOM_NUM]); // 방 번호
                    }
                } catch (Exception e) {
                    // 데이터 파싱 오류가 발생한 라인은 건너뜁니다.
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return booked;
    }

    // 6. 예약 데이터 삭제 (SFR-207 지원)
    /**
     * 특정 고객의 예약 정보를 삭제합니다. (UserController에서 호출됨)
     * @param name 삭제할 고객 이름
     * @param phoneNumber 삭제할 고객 전화번호
     * @return 삭제 성공 여부
     */
    public boolean deleteReservationsByCustomer(String name, String phoneNumber) {
        List<String> allLines = new ArrayList<>();
        boolean deleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length > RES_IDX_PHONE &&
                        parts[RES_IDX_NAME].trim().equals(name) &&
                        parts[RES_IDX_PHONE].trim().equals(phoneNumber)) {

                    deleted = true;
                    continue; // 삭제 대상이므로 건너뜁니다.
                }
                allLines.add(line);
            }
        } catch (IOException e) {
            return false;
        }

        if (deleted) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(RESERVATION_FILE))) {
                for (String newLine : allLines) {
                    pw.println(newLine);
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return true; // 삭제할 대상이 없었거나, 삭제에 성공했거나
    }

    // 7. 체크아웃 최종 처리 로직 (SFR-315)
    /**
     * 특정 객실 번호에 대해 CHECKED_IN 상태의 예약을 찾아 CHECKED_OUT으로 변경합니다.
     * @param roomNumber 체크아웃할 객실 번호
     * @return 상태 변경 성공 여부
     */
    public boolean processCheckoutByRoom(String roomNumber) {
        String reservationIdToCheckout = null;

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length > RES_IDX_STATUS) {
                    String currentRoom = parts[RES_IDX_ROOM_NUM].trim();
                    String currentStatus = parts[RES_IDX_STATUS].trim();

                    if (currentRoom.equals(roomNumber) && currentStatus.equals(STATUS_CHECKED_IN)) {
                        reservationIdToCheckout = parts[RES_IDX_ID];
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("체크아웃 대상 예약 검색 중 오류: " + e.getMessage());
            return false;
        }

        if (reservationIdToCheckout != null) {
            // updateStatus 메서드를 호출하여 상태를 변경합니다.
            return updateStatus(reservationIdToCheckout, STATUS_CHECKED_OUT);
        }

        System.out.println("DEBUG: 객실 " + roomNumber + "에 대한 체크인 상태의 예약을 찾을 수 없습니다.");
        return false;
    }
}