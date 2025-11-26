package hms.model;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

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
    public static final int RES_IDX_STATUS = 12;
    public static final int RES_IDX_CHECKOUT_TIME = 13;
    public static final int RES_IDX_USER_ID = 14; // 예약 시 저장되는 User ID (기존 데이터와 길이 맞춤)

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT";

    public ReservationDataManager() {
        File file = new File(RESERVATION_FILE);
        if (!file.exists()) {
            try {
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 1. 예약 저장
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
                "", // RES_IDX_CHECKOUT_TIME (예약 시에는 비워둡니다)
                (String) data.get("userId") // RES_IDX_USER_ID
        );

        try (FileWriter fw = new FileWriter(RESERVATION_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // (기존 메서드들: searchReservation, getReservationById, updateStatus, getBookedRooms, processCheckoutByRoom, getReservationsByPeriod는 생략)
    // ...

    // 8. ⭐ [추가됨] 예약 코드 검증 및 체크인 상태로 변경 (HMSServer 오류 해결)
    public boolean validateReservationAndCheckIn(String reservationCode, String roomNumber) {
        List<String> lines = new ArrayList<>();
        boolean validatedAndCheckedIn = false;

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                // 데이터 길이가 최소한 status까지는 있는지 확인
                if (parts.length > RES_IDX_STATUS && parts[RES_IDX_ID].equals(reservationCode)) {

                    // 1. 예약 코드가 일치하는지 확인
                    // 2. 객실 번호가 일치하는지 확인
                    // 3. 현재 상태가 PENDING인지 확인
                    if (parts[RES_IDX_ROOM_NUM].equals(roomNumber) && parts[RES_IDX_STATUS].equals(STATUS_PENDING)) {

                        // 상태를 CHECKED_IN으로 변경
                        parts[RES_IDX_STATUS] = STATUS_CHECKED_IN;

                        // 체크아웃 시간 필드가 없는 경우 배열 확장 (안전성 확보)
                        if (parts.length <= RES_IDX_CHECKOUT_TIME) {
                            String[] newParts = new String[RES_IDX_USER_ID + 1];
                            System.arraycopy(parts, 0, newParts, 0, parts.length);
                            for (int i = parts.length; i < newParts.length; i++) newParts[i] = "";
                            parts = newParts;
                        }

                        // 현재 라인을 수정된 상태로 리스트에 추가
                        lines.add(String.join(",", parts));
                        validatedAndCheckedIn = true;

                    } else {
                        // 조건 불일치 (방 번호 불일치, 이미 체크인 상태 등)
                        lines.add(line);
                    }
                } else {
                    // ID 불일치
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // 파일 덮어쓰기
        if (validatedAndCheckedIn) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(RESERVATION_FILE))) {
                for (String l : lines) pw.println(l);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    // 2. 예약 검색
    public String[] searchReservation(String name, String phoneNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;
                if (parts[RES_IDX_NAME].trim().equals(name) && parts[RES_IDX_PHONE].trim().equals(phoneNumber)) {
                    return parts;
                }
            }
        } catch (IOException e) { }
        return null;
    }

    // 3. ID로 조회
    public String[] getReservationById(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && parts[RES_IDX_ID].trim().equals(id)) {
                    return parts;
                }
            }
        } catch (IOException e) { }
        return null;
    }

    // 4. 상태 업데이트
    public boolean updateStatus(String id, String newStatus) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        String checkoutTime = newStatus.equals(STATUS_CHECKED_OUT)
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) : "";

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > RES_IDX_STATUS && parts[RES_IDX_ID].equals(id)) {
                    // 기존 데이터 길이가 짧으면 확장 (체크아웃 시간 필드 확보)
                    if (parts.length <= RES_IDX_CHECKOUT_TIME) {
                        String[] newParts = new String[RES_IDX_USER_ID + 1];
                        System.arraycopy(parts, 0, newParts, 0, parts.length);
                        // 빈 공간 채우기
                        for(int i=parts.length; i<newParts.length; i++) newParts[i] = "";
                        parts = newParts;
                    }

                    parts[RES_IDX_STATUS] = newStatus;
                    if (newStatus.equals(STATUS_CHECKED_OUT)) {
                        parts[RES_IDX_CHECKOUT_TIME] = checkoutTime;
                    }
                    lines.add(String.join(",", parts));
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
        LocalDate checkIn = LocalDate.parse(inStr);
        LocalDate checkOut = LocalDate.parse(outStr);

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 12) continue;
                // 체크아웃된 방은 예약 목록에서 제외 (즉시 예약 가능하도록)
                if (parts.length > RES_IDX_STATUS && parts[RES_IDX_STATUS].equals(STATUS_CHECKED_OUT)) continue;

                try {
                    LocalDate rIn = LocalDate.parse(parts[RES_IDX_CHECK_IN_DATE]);
                    LocalDate rOut = LocalDate.parse(parts[RES_IDX_CHECKOUT_DATE]);
                    if (checkIn.isBefore(rOut) && checkOut.isAfter(rIn)) {
                        booked.add(parts[RES_IDX_ROOM_NUM]);
                    }
                } catch (Exception e) { }
            }
        } catch (IOException e) { }
        return booked;
    }

    // 6. 체크아웃 처리 (방 번호로)
    public boolean processCheckoutByRoom(String roomNumber) {
        String targetId = null;
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > RES_IDX_STATUS) {
                    // 방 번호가 같고, 아직 체크인 상태인 예약을 찾음
                    if (parts[RES_IDX_ROOM_NUM].trim().equals(roomNumber) &&
                            parts[RES_IDX_STATUS].trim().equals(STATUS_CHECKED_IN)) {
                        targetId = parts[RES_IDX_ID];
                        break;
                    }
                }
            }
        } catch (IOException e) { return false; }

        // 찾은 예약 ID로 상태 업데이트(체크아웃) 진행
        return targetId != null && updateStatus(targetId, STATUS_CHECKED_OUT);
    }

    // 7. [보고서용] 기간별 예약 목록 조회
    public List<String[]> getReservationsByPeriod(String startStr, String endStr) {
        List<String[]> list = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(startStr);
        LocalDate endDate = LocalDate.parse(endStr);

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 12) continue;

                try {
                    LocalDate checkIn = LocalDate.parse(parts[RES_IDX_CHECK_IN_DATE]);
                    LocalDate checkOut = LocalDate.parse(parts[RES_IDX_CHECKOUT_DATE]);

                    // 기간 겹침 확인 (Overlap)
                    if (!startDate.isAfter(checkOut) && !endDate.isBefore(checkIn)) {
                        list.add(parts);
                    }
                } catch (Exception e) { continue; }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }
}