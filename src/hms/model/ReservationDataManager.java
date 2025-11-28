package hms.model;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

public class ReservationDataManager {

    private static final String RESERVATION_FILE = "data/reservation_info.txt";

    // 인덱스 상수 (최종 구조에 맞춤)
    public static final int RES_IDX_ID = 0;
    public static final int RES_IDX_NAME = 1;
    public static final int RES_IDX_PHONE = 2;
    public static final int RES_IDX_CHECK_IN_DATE = 3;
    public static final int RES_IDX_CHECKOUT_DATE = 4;
    public static final int RES_IDX_ROOM_NUM = 9;
    public static final int RES_IDX_TOTAL_PRICE = 10;
    public static final int RES_IDX_PAYMENT_METHOD = 11; // ⭐ [NEW] 결제 방식 인덱스 (위치 11)
    public static final int RES_IDX_STATUS = 12;
    public static final int RES_IDX_CHECKOUT_TIME = 13;
    public static final int RES_IDX_USER_ID = 14;
    public static final int RES_IDX_LATE_FEE = 15; // 지연료 인덱스

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
        int randomPart = (int) (Math.random() * 900000) + 100000;
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
                (String) data.get("paymentMethod"), // ⭐ 인덱스 11
                STATUS_PENDING, // 인덱스 12
                "",
                (String) data.get("userId")
        );

        try (FileWriter fw = new FileWriter(RESERVATION_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // 2. 예약 검색 (기존 유지)
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
        } catch (IOException e) {
        }
        return null;
    }

    // 3. ID로 조회 (기존 유지)
    public String[] getReservationById(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && parts[RES_IDX_ID].trim().equals(id)) {
                    return parts;
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    // 4. 상태 업데이트 (기존 유지)
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
                    if (parts.length <= RES_IDX_LATE_FEE) {
                        String[] newParts = new String[RES_IDX_LATE_FEE + 1];
                        System.arraycopy(parts, 0, newParts, 0, parts.length);
                        for (int i = parts.length; i < newParts.length; i++) newParts[i] = "";
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
        } catch (IOException e) {
            return false;
        }

        if (updated) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(RESERVATION_FILE))) {
                for (String l : lines) pw.println(l);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    // 5. 예약된 방 목록
    public List<String> getBookedRooms(String inStr, String outStr) {
        List<String> booked = new ArrayList<>();
        LocalDate checkIn = LocalDate.parse(inStr);
        LocalDate checkOut = LocalDate.parse(outStr);

        //  [NEW] 자동 취소 로직에 필요한 시간 및 날짜 설정 (KST 기준)
        final LocalTime cancelTime = LocalTime.of(18, 0);
        final ZoneId kstZone = ZoneId.of("Asia/Seoul");
        final LocalDate today = LocalDate.now(kstZone);
        final LocalTime currentTime = LocalTime.now(kstZone);

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length < RES_IDX_STATUS) continue;

                String status = parts[RES_IDX_STATUS];
                String paymentMethod = parts[RES_IDX_PAYMENT_METHOD]; // 현장결제/카드결제

                // 1. 체크아웃된 방은 무시합니다. (재판매 가능)
                if (status.equals(STATUS_CHECKED_OUT)) continue;

                // ⭐ 2. [핵심 로직] PENDING 현장결제 예약에 대한 자동 취소 로직
                // 상태가 PENDING이고, 결제 방식이 현장결제인 경우
                if (status.equals(STATUS_PENDING) && paymentMethod.equals("현장결제")) {
                    try {
                        LocalDate rIn = LocalDate.parse(parts[RES_IDX_CHECK_IN_DATE]);

                        // a) 체크인 날짜가 이미 지났거나 (rIn.isBefore(today))
                        // b) 체크인 날짜가 오늘이고, 현재 시간이 18시를 넘겼다면 (rIn.isEqual(today) && currentTime.isAfter(cancelTime))
                        if (rIn.isBefore(today) || (rIn.isEqual(today) && currentTime.isAfter(cancelTime))) {
                            // 자동 취소된 것으로 간주하고, 예약된 방 목록에 추가하지 않음 (continue)
                            continue;
                        }
                    } catch (Exception e) {
                        // 날짜 파싱 오류 발생 시, 안전하게 계속 유효한 것으로 간주 (아래 3번 로직으로 진행)
                    }
                }
                // ⭐ -----------------------------------------------------


                // 3. 날짜 겹침 확인 (자동 취소되지 않은 유효한 예약 건만 진행)
                try {
                    LocalDate rIn = LocalDate.parse(parts[RES_IDX_CHECK_IN_DATE]);
                    LocalDate rOut = LocalDate.parse(parts[RES_IDX_CHECKOUT_DATE]);

                    if (checkIn.isBefore(rOut) && checkOut.isAfter(rIn)) {
                        booked.add(parts[RES_IDX_ROOM_NUM]);
                    }
                } catch (Exception e) { /* 날짜 파싱 오류 무시 */ }
            }
        } catch (IOException e) {
        }
        return booked;
    }

    // 6. 체크아웃 처리 (지연료 포함) (기존 유지)
    public boolean processCheckoutByRoom(String roomNumber, int lateFee) {
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
        } catch (IOException e) {
            return false;
        }

        if (targetId == null) return false;

        List<String> lines = new ArrayList<>();
        boolean updated = false;
        String checkoutTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > RES_IDX_STATUS && parts[RES_IDX_ID].equals(targetId)) {
                    if (parts.length <= RES_IDX_LATE_FEE) {
                        String[] newParts = new String[RES_IDX_LATE_FEE + 1];
                        System.arraycopy(parts, 0, newParts, 0, parts.length);
                        for (int i = parts.length; i < newParts.length; i++) newParts[i] = "";
                        parts = newParts;
                    }

                    parts[RES_IDX_STATUS] = STATUS_CHECKED_OUT;
                    parts[RES_IDX_CHECKOUT_TIME] = checkoutTime;
                    parts[RES_IDX_LATE_FEE] = String.valueOf(lateFee);

                    lines.add(String.join(",", parts));
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (updated) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(RESERVATION_FILE))) {
                for (String l : lines) pw.println(l);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    // 7. 보고서용 조회 (기존 유지)
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
                    if (!startDate.isAfter(checkOut) && !endDate.isBefore(checkIn)) {
                        list.add(parts);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    // 예약 전체조회 admin
    public List<String[]> readAllReservations() {
        List<String[]> allReservations = new ArrayList<>();

        // ⭐ [주의] 파일 경로 상수(RESERVATION_FILE)를 사용합니다.
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                // 데이터가 비어있지 않다면 리스트에 추가
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    allReservations.add(parts);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading all reservations: " + e.getMessage());
        }
        return allReservations;
    }

    // ⭐ [수정됨] 예약 검증 (체크인 및 룸서비스 인증용) (기존 유지)
    public boolean validateRoomServiceAccess(String reservationCode, String roomNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length > RES_IDX_STATUS) {
                    boolean isIdMatch = parts[RES_IDX_ID].equals(reservationCode) ||
                            parts[RES_IDX_ID].endsWith("-" + reservationCode);

                    boolean isRoomMatch = parts[RES_IDX_ROOM_NUM].equals(roomNumber);

                    if (isIdMatch && isRoomMatch) {
                        String currentStatus = parts[RES_IDX_STATUS];

                        if (currentStatus.equals(STATUS_CHECKED_IN)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}