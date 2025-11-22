package hms.model;

import java.io.*;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReservationDataManager {

    private static final String RESERVATION_FILE = "data/reservation_info.txt";
    // 인덱스 상수
    public static final int RES_IDX_ID = 0;
    public static final int RES_IDX_ROOM_NUM = 9;
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

    // 2. 예약 검색
    public String[] searchReservation(String name, String phoneNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;
                if (parts[1].trim().equals(name) && parts[2].trim().equals(phoneNumber)) {
                    return parts;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
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
        } catch (IOException e) { e.printStackTrace(); }
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
                    // 배열 크기가 작으면 확장
                    if (parts.length <= RES_IDX_CHECKOUT_TIME) {
                        String[] newParts = new String[RES_IDX_CHECKOUT_TIME + 1];
                        System.arraycopy(parts, 0, newParts, 0, parts.length);
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

                try {
                    LocalDate rIn = LocalDate.parse(parts[3]);
                    LocalDate rOut = LocalDate.parse(parts[4]);
                    if (checkIn.isBefore(rOut) && checkOut.isAfter(rIn)) {
                        booked.add(parts[9]); // 방 번호
                    }
                } catch (Exception e) { continue; }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return booked;
    }
}