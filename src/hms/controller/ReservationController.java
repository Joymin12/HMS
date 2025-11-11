package hms.controller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate; // ★ 추가
import java.util.Date;
import java.util.Map;
import java.util.ArrayList; // ★ 추가
import java.util.List; // ★ 추가

public class ReservationController {

    private static final String RESERVATION_FILE = "data/reservation_info.txt";

    /**
     * (예약 생성) 최종 예약 정보를 받아 파일에 저장합니다. (기존 코드 유지)
     */
    public boolean saveReservationToFile(Map<String, Object> data) {
        // 1. 고유 번호 생성
        String datePart = new SimpleDateFormat("yyMMdd").format(new Date());
        int randomPart = (int)(Math.random() * 900000) + 100000;
        String confirmationId = datePart + "-" + randomPart;

        // 2. CSV 라인 구성
        String line = String.join(",",
                confirmationId,
                (String) data.get("customerName"),
                (String) data.get("phoneNumber"),
                (String) data.get("checkIn"),
                (String) data.get("checkOut"),
                String.valueOf(data.get("guests")),
                (String) data.get("grade"),
                (String) data.get("room"),
                String.valueOf(data.get("totalPrice")),
                (String) data.get("paymentMethod")
        );

        // 디버깅 코드
        System.out.println("---[DEBUG: Controller Received]---");
        System.out.println("생성된 CSV 라인: " + line);
        System.out.println("----------------------------------");

        // 3. 디렉토리 생성
        try {
            File file = new File(RESERVATION_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                System.out.println("[DEBUG] 'data' 디렉토리가 없어 생성합니다.");
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
            System.err.println("파일 쓰기 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * (예약 조회) 파일에서 예약 내역을 검색합니다. (기존 코드 유지)
     */
    public String[] searchReservation(String name, String phoneNumber) {
        // ... (기존 검색 로직 생략) ...
        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length < 3) continue;

                if (parts[1].trim().equals(name) && parts[2].trim().equals(phoneNumber)) {
                    return parts;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("예약 파일이 존재하지 않습니다: " + RESERVATION_FILE);
            return null;
        } catch (IOException e) {
            System.err.println("파일 읽기 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    // =================================================================
    // ★★★ 새로 추가: 예약된 방 목록 검색 기능 ★★★
    // =================================================================

    /**
     * 특정 기간(checkIn ~ checkOut) 동안 이미 예약된 객실 번호 목록을 반환합니다.
     */
    public List<String> getBookedRooms(String checkInStr, String checkOutStr) {
        List<String> bookedRooms = new ArrayList<>();
        // String을 LocalDate 객체로 변환하여 날짜 계산에 용이하게 합니다.
        LocalDate checkIn = LocalDate.parse(checkInStr);
        LocalDate checkOut = LocalDate.parse(checkOutStr);

        try (BufferedReader br = new BufferedReader(new FileReader(RESERVATION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                // 데이터 유효성 검사 (최소한의 필수 필드 확인)
                if (parts.length < 8) continue;

                // 파일에 저장된 예약 정보 추출
                LocalDate fileCheckIn = LocalDate.parse(parts[3].trim()); // 파일의 예약 시작일
                LocalDate fileCheckOut = LocalDate.parse(parts[4].trim()); // 파일의 예약 종료일
                String roomNumber = parts[7].trim(); // 예약된 객실 번호

                // 날짜 겹침 판정 로직:
                // 요청 기간(checkIn ~ checkOut)과 파일 예약 기간(fileCheckIn ~ fileCheckOut)이 겹치는지 확인
                if (checkIn.isBefore(fileCheckOut) && checkOut.isAfter(fileCheckIn)) {
                    // 예약이 겹칩니다.
                    bookedRooms.add(roomNumber);
                }
            }
        } catch (FileNotFoundException e) {
            // 파일이 없어도 오류는 아니므로 콘솔 출력만 하고 진행
            System.out.println("[ERROR] 예약 파일이 존재하지 않습니다. 빈 목록을 반환합니다.");
        } catch (Exception e) {
            System.err.println("[ERROR] 예약된 방 검색 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return bookedRooms;
    }
}