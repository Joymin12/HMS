package hms.controller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ReservationController {

    private static final String RESERVATION_FILE = "data/reservation_info.txt";
    // ⭐ 상태 상수 추가
    public static final String STATUS_PENDING = "PENDING"; // 예약은 완료했지만 미체크인 상태
    public static final String STATUS_CHECKED_IN = "CHECKED_IN"; // 체크인 완료 상태
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT"; // 체크아웃 완료 상태
    public static final int RES_IDX_ID = 0; // 예약 ID 인덱스
    public static final int RES_IDX_ROOM_NUM = 9; // 객실 번호 인덱스
    public static final int RES_IDX_TOTAL_PRICE = 10; // ⭐ [상수 추가] 총 요금 인덱스
    public static final int RES_IDX_STATUS = 12; // 상태 인덱스
    public static final int RES_IDX_CHECKOUT_TIME = 13; // ⭐ [상수 추가] 체크아웃 시간 인덱스 (14번째 필드)

    // ---------------------------------------------------------------------
    // 1. 예약 저장 (Save Reservation)
    // ---------------------------------------------------------------------
    public boolean saveReservationToFile(Map<String, Object> data) {
        String datePart = new SimpleDateFormat("yyMMdd").format(new Date());
        int randomPart = (int)(Math.random() * 900000) + 100000;
        String confirmationId = datePart + "-" + randomPart;
        // 2. CSV 라인 구성 (총 14개 필드로 맞추기 위해 마지막에 콤마(,)를 추가하여 빈 필드 확보)
        String line = String.join(",",
                confirmationId,                              // 0. 예약 번호
                (String) data.get("customerName"),           // 1. 고객 이름
                (String) data.get("phoneNumber"),            // 2. 전화번호
                (String) data.get("checkIn"),                // 3. 체크인 날짜
                (String) data.get("checkOut"),               // 4. 체크아웃 날짜
                (String) data.get("estimatedInTime"),        // 5. 예상 IN 시간
                (String) data.get("estimatedOutTime"),       // 6. 예상 OUT 시간
                String.valueOf(data.get("guests")),          // 7. 인원 수
                (String) data.get("grade"),                  // 8. 등급
                (String) data.get("room"),                   // 9. 객실 번호
                String.valueOf(data.get("totalPrice")),      // 10. 총 요금
                (String) data.get("paymentMethod"),           // 11. 결제 방식
                STATUS_PENDING,                              // 12. 예약 상태
                ""                                           // ⭐ 13. 체크아웃 시간 (초기 빈 값)
        );

        // 3. 디렉토리 생성 및 권한 처리
        try {
            File file = new File(RESERVATION_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        } catch (Exception e) {
            System.err.println("디렉토리 생성 오류 발생: " + e.getMessage());
            return false;
        }

        // 4. 파일에 기록
        try (FileWriter fw = new FileWriter(RESERVATION_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println(line);
            System.out.println("예약 저장 성공: " + confirmationId);
            return true;

        } catch (IOException e) {
            System.err.println("[ERROR] 파일 쓰기 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------------------------------------------------
    // 2. 예약 검색 (Search Reservation by Name/Phone)
    // ---------------------------------------------------------------------
    public String[] searchReservation(String name, String phoneNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length < 3) continue;

                if (parts[1].trim().equals(name) && parts[2].trim().equals(phoneNumber)) {
                    // ⭐ [수정] 14개 필드 기준으로 확장하여 반환 (구형 데이터 호환성 유지)
                    if (parts.length < RES_IDX_CHECKOUT_TIME + 1) {
                        String[] newParts = new String[RES_IDX_CHECKOUT_TIME + 1];
                        System.arraycopy(parts, 0, newParts, 0, parts.length);

                        // 상태 필드와 체크아웃 시간 필드가 없는 경우 빈 값 및 PENDING으로 채움
                        if (parts.length <= RES_IDX_STATUS) {
                            newParts[RES_IDX_STATUS] = STATUS_PENDING;
                        }
                        if (parts.length <= RES_IDX_CHECKOUT_TIME) {
                            newParts[RES_IDX_CHECKOUT_TIME] = "";
                        }
                        return newParts;
                    }
                    return parts;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[ERROR] 예약 파일이 존재하지 않습니다: " + RESERVATION_FILE);
            return null;
        } catch (IOException e) {
            System.err.println("[ERROR] 파일 읽기 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // 3. 예약 상세 정보 조회 (by ID)
    // ---------------------------------------------------------------------
    public String[] getReservationDetailsById(String reservationId) {
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length > 0 && parts[RES_IDX_ID].trim().equals(reservationId)) {
                    if (parts.length == 12) {
                        String[] newParts = new String[13];
                        System.arraycopy(parts, 0, newParts, 0, 12);
                        newParts[RES_IDX_STATUS] = STATUS_PENDING;
                        return newParts;
                    }
                    return parts;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[ERROR] 예약 파일이 존재하지 않습니다: " + RESERVATION_FILE);
            return null;
        } catch (IOException e) {
            System.err.println("[ERROR] 파일 읽기 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // 4. 예약 상태 업데이트 (Update Status)
    // ---------------------------------------------------------------------

    public boolean updateReservationStatus(String reservationId, String newStatus) {
        List<String> updatedLines = new ArrayList<>();
        boolean updated = false;

        // ⭐ [추가] 체크아웃 상태로 변경될 때 현재 시간을 기록합니다.
        String checkoutTime = newStatus.equals(STATUS_CHECKED_OUT)
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                : "";

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length > 0 && parts[RES_IDX_ID].trim().equals(reservationId)) {

                    // ⭐ [수정] 최소 14개 필드를 갖도록 배열을 확장하여 상태와 시간을 안전하게 업데이트
                    String[] currentParts = parts;
                    if (currentParts.length < RES_IDX_CHECKOUT_TIME + 1) {
                        currentParts = new String[RES_IDX_CHECKOUT_TIME + 1];
                        System.arraycopy(parts, 0, currentParts, 0, parts.length);
                        // 새로 추가된 필드는 빈 값으로 초기화
                        for(int i = parts.length; i <= RES_IDX_CHECKOUT_TIME; i++) {
                            currentParts[i] = "";
                        }
                    }

                    // 1. 상태(Index 12) 업데이트
                    currentParts[RES_IDX_STATUS] = newStatus;

                    // 2. 체크아웃 상태일 때만 시간(Index 13) 업데이트
                    if (newStatus.equals(STATUS_CHECKED_OUT)) {
                        currentParts[RES_IDX_CHECKOUT_TIME] = checkoutTime;
                    }

                    updatedLines.add(String.join(",", currentParts));
                    updated = true;
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] 상태 변경 중 파일 읽기 오류: " + e.getMessage());
            return false;
        }

        if (updated) {
            try (FileWriter fw = new FileWriter(RESERVATION_FILE);
                 PrintWriter pw = new PrintWriter(fw)) {
                for (String newLine : updatedLines) {
                    pw.println(newLine);
                }
                System.out.println("예약 ID " + reservationId + " 상태를 " + newStatus + "로 업데이트 완료.");
                return true;
            } catch (IOException e) {
                System.err.println("[ERROR] 상태 변경 중 파일 쓰기 오류: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // 5. 예약된 방 목록 검색 (Get Booked Rooms - 날짜 겹침 확인)
    // ---------------------------------------------------------------------
    public List<String> getBookedRooms(String checkInStr, String checkOutStr) {
        List<String> bookedRooms = new ArrayList<>();
        LocalDate checkIn = LocalDate.parse(checkInStr);
        LocalDate checkOut = LocalDate.parse(checkOutStr);

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length < 12) continue;

                LocalDate fileCheckIn = LocalDate.parse(parts[3].trim());
                LocalDate fileCheckOut = LocalDate.parse(parts[4].trim());
                String roomNumber = parts[RES_IDX_ROOM_NUM].trim();

                if (checkIn.isBefore(fileCheckOut) && checkOut.isAfter(fileCheckIn)) {
                    bookedRooms.add(roomNumber);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[ERROR] 예약 파일이 존재하지 않습니다. 빈 목록을 반환합니다.");
        } catch (Exception e) {
            System.err.println("[ERROR] 예약된 방 검색 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return bookedRooms;
    }

    // ---------------------------------------------------------------------
    // ⭐ [추가] 6. 총 숙박 요금 조회
    // ---------------------------------------------------------------------
    /**
     * 특정 예약 데이터에서 총 숙박 요금(TotalPrice)을 숫자로 추출합니다.
     * @param reservationData 예약 상세 정보 배열 (Total Price는 Index 10)
     * @return long 타입의 숙박 비용. 파싱 오류 시 0을 반환.
     */
    public long getRoomCharge(String[] reservationData) {
        if (reservationData.length > RES_IDX_TOTAL_PRICE) {
            try {
                // 숫자 외 문자 제거 후 파싱 (예: 쉼표 등)
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
    // 7. 체크아웃 처리 (Process Checkout) - CheckoutProcessPanel 요구 사항
    // ---------------------------------------------------------------------
    /**
     * 특정 객실의 현재 체크인 상태 예약을 찾아 'CHECKED_OUT' 상태로 변경합니다.
     * (실제 구현에서는 객실 상태 관리 로직도 포함되어야 합니다.)
     * @param roomNumber 체크아웃할 객실 번호
     * @return 체크아웃 처리 성공 여부
     */
    public boolean processCheckout(String roomNumber) {
        // 1. 해당 객실 번호로 'CHECKED_IN' 상태의 예약을 찾습니다.
        String reservationIdToCheckout = null;

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                // 예약 ID, 객실 번호, 상태 필드가 있는지 확인
                if (parts.length > RES_IDX_STATUS) {
                    String currentRoom = parts[RES_IDX_ROOM_NUM].trim();
                    String currentStatus = parts[RES_IDX_STATUS].trim();

                    // 해당 객실이 CHECKED_IN 상태인지 확인
                    if (currentRoom.equals(roomNumber) && currentStatus.equals(STATUS_CHECKED_IN)) {
                        reservationIdToCheckout = parts[RES_IDX_ID];
                        break; // 가장 최근/유효한 예약 하나만 처리
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] 체크아웃 대상 예약 검색 중 오류: " + e.getMessage());
            return false;
        }

        if (reservationIdToCheckout != null) {
            // 2. 해당 예약 ID의 상태를 CHECKED_OUT으로 업데이트
            boolean success = updateReservationStatus(reservationIdToCheckout, STATUS_CHECKED_OUT);

            // 3. (추가 구현 필요: RoomDataManager를 호출하여 객실 상태를 '공실'로 변경)

            if (success) {
                System.out.println("DEBUG: 객실 " + roomNumber + " 예약 ID " + reservationIdToCheckout + " 체크아웃 완료.");
                return true;
            }
        } else {
            System.out.println("DEBUG: 객실 " + roomNumber + " 에 대한 현재 CHECKED_IN 상태의 예약을 찾을 수 없습니다.");
        }
        return false;
    }
}