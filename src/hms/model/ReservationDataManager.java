package hms.model;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.text.SimpleDateFormat;

public class ReservationDataManager {

    private static final String RESERVATION_FILE = "data/reservation_info.txt";

    // ⭐ 메모리 캐시
    private final List<String[]> reservationCache = Collections.synchronizedList(new ArrayList<>());

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
    public static final int RES_IDX_USER_ID = 14;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT";

    public ReservationDataManager() {
        File file = new File(RESERVATION_FILE);
        if (!file.exists()) {
            try {
                if (file.getParentFile() != null) file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) { e.printStackTrace(); }
        }
        loadDataToMemory();
    }

    // --- 로드 ---
    private void loadDataToMemory() {
        reservationCache.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > RES_IDX_STATUS) {
                    reservationCache.add(parts);
                }
            }
            System.out.println(">>> [예약] 데이터 로드 완료: " + reservationCache.size() + "건");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- 저장 ---
    private boolean saveMemoryToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(RESERVATION_FILE))) {
            synchronized (reservationCache) {
                for (String[] parts : reservationCache) {
                    pw.println(String.join(",", parts));
                }
            }
            return true;
        } catch (IOException e) { return false; }
    }

    // 1. 예약 저장
    public boolean saveReservation(Map<String, Object> data) {
        String datePart = new SimpleDateFormat("yyMMdd").format(new Date());
        int randomPart = (int)(Math.random() * 900000) + 100000;
        String confirmationId = datePart + "-" + randomPart;

        String[] newRes = new String[] {
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
                "", // checkout time
                (String) data.get("userId")
        };

        synchronized (reservationCache) {
            reservationCache.add(newRes);
            return saveMemoryToFile();
        }
    }

    // 8. 예약 코드 검증 및 체크인
    public boolean validateReservationAndCheckIn(String reservationCode, String roomNumber) {
        synchronized (reservationCache) {
            boolean updated = false;
            for (int i = 0; i < reservationCache.size(); i++) {
                String[] parts = reservationCache.get(i);
                if (parts.length > RES_IDX_STATUS && parts[RES_IDX_ID].equals(reservationCode)) {

                    if (parts[RES_IDX_ROOM_NUM].equals(roomNumber) && parts[RES_IDX_STATUS].equals(STATUS_PENDING)) {
                        // 배열 확장 필요 시 처리
                        if (parts.length <= RES_IDX_CHECKOUT_TIME) {
                            String[] newParts = new String[RES_IDX_USER_ID + 1];
                            System.arraycopy(parts, 0, newParts, 0, parts.length);
                            for(int k=parts.length; k<newParts.length; k++) newParts[k] = "";
                            parts = newParts;
                        }

                        parts[RES_IDX_STATUS] = STATUS_CHECKED_IN;
                        reservationCache.set(i, parts);
                        updated = true;
                        break;
                    }
                }
            }
            if (updated) return saveMemoryToFile();
        }
        return false;
    }

    // 2. 예약 검색
    public String[] searchReservation(String name, String phoneNumber) {
        synchronized (reservationCache) {
            for (String[] parts : reservationCache) {
                if (parts.length < 3) continue;
                if (parts[RES_IDX_NAME].trim().equals(name) && parts[RES_IDX_PHONE].trim().equals(phoneNumber)) {
                    return parts;
                }
            }
        }
        return null;
    }

    // 3. ID로 조회
    public String[] getReservationById(String id) {
        synchronized (reservationCache) {
            for (String[] parts : reservationCache) {
                if (parts.length > 0 && parts[RES_IDX_ID].trim().equals(id)) {
                    return parts;
                }
            }
        }
        return null;
    }

    // 4. 상태 업데이트
    public boolean updateStatus(String id, String newStatus) {
        String checkoutTime = newStatus.equals(STATUS_CHECKED_OUT)
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) : "";

        synchronized (reservationCache) {
            boolean updated = false;
            for (int i = 0; i < reservationCache.size(); i++) {
                String[] parts = reservationCache.get(i);
                if (parts.length > RES_IDX_STATUS && parts[RES_IDX_ID].equals(id)) {

                    if (parts.length <= RES_IDX_CHECKOUT_TIME) {
                        String[] newParts = new String[RES_IDX_USER_ID + 1];
                        System.arraycopy(parts, 0, newParts, 0, parts.length);
                        for(int k=parts.length; k<newParts.length; k++) newParts[k] = "";
                        parts = newParts;
                    }

                    parts[RES_IDX_STATUS] = newStatus;
                    if (newStatus.equals(STATUS_CHECKED_OUT)) {
                        parts[RES_IDX_CHECKOUT_TIME] = checkoutTime;
                    }
                    reservationCache.set(i, parts);
                    updated = true;
                    break;
                }
            }
            if (updated) return saveMemoryToFile();
        }
        return false;
    }

    // 5. 예약된 방 목록
    public List<String> getBookedRooms(String inStr, String outStr) {
        List<String> booked = new ArrayList<>();
        LocalDate checkIn = LocalDate.parse(inStr);
        LocalDate checkOut = LocalDate.parse(outStr);

        synchronized (reservationCache) {
            for (String[] parts : reservationCache) {
                if (parts.length < 12) continue;
                if (parts.length > RES_IDX_STATUS && parts[RES_IDX_STATUS].equals(STATUS_CHECKED_OUT)) continue;

                try {
                    LocalDate rIn = LocalDate.parse(parts[RES_IDX_CHECK_IN_DATE]);
                    LocalDate rOut = LocalDate.parse(parts[RES_IDX_CHECKOUT_DATE]);
                    if (checkIn.isBefore(rOut) && checkOut.isAfter(rIn)) {
                        booked.add(parts[RES_IDX_ROOM_NUM]);
                    }
                } catch (Exception e) {}
            }
        }
        return booked;
    }

    // 6. 체크아웃 처리
    public boolean processCheckoutByRoom(String roomNumber) {
        String targetId = null;
        synchronized (reservationCache) {
            for (String[] parts : reservationCache) {
                if (parts.length > RES_IDX_STATUS) {
                    if (parts[RES_IDX_ROOM_NUM].trim().equals(roomNumber) &&
                            parts[RES_IDX_STATUS].trim().equals(STATUS_CHECKED_IN)) {
                        targetId = parts[RES_IDX_ID];
                        break;
                    }
                }
            }
        }
        return targetId != null && updateStatus(targetId, STATUS_CHECKED_OUT);
    }

    // 7. 보고서용 조회
    public List<String[]> getReservationsByPeriod(String startStr, String endStr) {
        List<String[]> list = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(startStr);
        LocalDate endDate = LocalDate.parse(endStr);

        synchronized (reservationCache) {
            for (String[] parts : reservationCache) {
                if (parts.length < 12) continue;
                try {
                    LocalDate checkIn = LocalDate.parse(parts[RES_IDX_CHECK_IN_DATE]);
                    LocalDate checkOut = LocalDate.parse(parts[RES_IDX_CHECKOUT_DATE]);
                    if (!startDate.isAfter(checkOut) && !endDate.isBefore(checkIn)) {
                        list.add(parts);
                    }
                } catch (Exception e) {}
            }
        }
        return list;
    }
}