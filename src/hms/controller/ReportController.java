// 파일 경로: hms/controller/ReportController.java
package hms.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * ReportController (매출 및 통계 분석 담당)
 * [SFR-503: 객실 매출 및 F&B 매출을 집계하여 보고서를 생성한다.]
 */
public class ReportController {

    // --- 파일 경로 상수 (프로젝트 구조에 맞게 가정) ---
    private final String RES_FILE_PATH = "data/reservation_info.txt";
    private final String FNB_FILE_PATH = "data/room_service_requests.txt";

    // --- 파일 데이터 인덱스 (ReservationController 및 RoomServiceDataManager 구조를 따름) ---
    private static final int RES_IDX_ROOM_PRICE = 10;       // 예약 파일 총 요금 인덱스
    private static final int RES_IDX_CHECKOUT_DATE = 4;     // 예약 파일 체크아웃 날짜 인덱스
    private static final int RES_IDX_STATUS = 12;           // 예약 파일 상태 인덱스

    private static final int REQ_IDX_TOTAL_PRICE = 3;       // 룸서비스 파일 총 금액 인덱스
    private static final int REQ_IDX_STATUS = 4;            // 룸서비스 파일 상태 인덱스
    private static final int REQ_IDX_TIMESTAMP = 5;         // 룸서비스 파일 요청 시간 인덱스

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * View에서 호출하여 기간별 총 매출 보고서를 생성합니다.
     * @param startDateStr 시작 날짜 (YYYY-MM-DD)
     * @param endDateStr 종료 날짜 (YYYY-MM-DD)
     * @return 매출 지표 Map (TotalRevenue, RoomRevenue, FNBRevenue)
     */
    public Map<String, Long> generateTotalReport(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        long roomRevenue = calculateRoomRevenue(startDate, endDate);
        long fnbRevenue = calculateFNBRevenue(startDate, endDate);
        long totalRevenue = roomRevenue + fnbRevenue;

        Map<String, Long> reportData = new HashMap<>();
        reportData.put("RoomRevenue", roomRevenue);
        reportData.put("FNBRevenue", fnbRevenue);
        reportData.put("TotalRevenue", totalRevenue);

        return reportData;
    }

    /**
     * 기간 내 체크아웃 완료된 예약의 객실 매출을 계산합니다.
     */
    private long calculateRoomRevenue(LocalDate startDate, LocalDate endDate) {
        long revenue = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(RES_FILE_PATH))) {
            String line;

            // 헤더 스킵 로직 (파일의 첫 줄이 헤더일 경우)
            if ((line = reader.readLine()) != null) { /* 헤더 스킵 */ }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // ⭐ [강화] 빈 라인 스킵

                String[] data = line.split(",", -1);

                // ⭐ [강화] 필수 필드 (가격과 상태)가 존재하는지 확인
                if (data.length <= RES_IDX_ROOM_PRICE || data.length <= RES_IDX_STATUS) continue;

                String status = data[RES_IDX_STATUS].trim();

                // 1. 체크아웃 완료된 건만 집계 ("CHECKED_OUT" 상태 사용 가정)
                if (status.equals("CHECKED_OUT")) {
                    try {
                        String checkoutDateStr = data[RES_IDX_CHECKOUT_DATE].trim();
                        LocalDate checkoutDate = LocalDate.parse(checkoutDateStr, formatter);

                        // 2. 보고 기간 내 포함되는지 확인
                        if (!checkoutDate.isBefore(startDate) && !checkoutDate.isAfter(endDate)) {
                            // 가격 파싱 로직 강화
                            String priceStr = data[RES_IDX_ROOM_PRICE].replaceAll("[^0-9]", "");
                            if (priceStr.isEmpty()) priceStr = "0"; // 빈 값이면 0으로 처리

                            revenue += Long.parseLong(priceStr);
                        }
                    } catch (Exception ignored) {
                        // 날짜 또는 가격 파싱 오류 발생 시, 이 데이터 행은 무시하고 다음 줄로 이동
                        System.err.println("경고: 예약 파일 파싱 오류 발생. 해당 라인 무시됨.");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("예약 정보 파일을 읽는 중 오류 발생: " + e.getMessage());
        }
        return revenue;
    }

    /**
     * 기간 내 '결제완료' 상태인 룸서비스 주문의 매출을 계산합니다.
     */
    private long calculateFNBRevenue(LocalDate startDate, LocalDate endDate) {
        long revenue = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(FNB_FILE_PATH))) {
            String line;
            if ((line = reader.readLine()) != null) { /* 헤더 스킵 */ } // 헤더 스킵 로직

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // ⭐ [강화] 빈 라인 스킵

                String[] data = line.split(",");
                // 필수 필드 길이 확인
                if (data.length <= REQ_IDX_TIMESTAMP || data.length <= REQ_IDX_TOTAL_PRICE) continue;

                String status = data[REQ_IDX_STATUS].trim();

                // 1. '결제완료' 상태의 주문만 집계 ("결제완료" 상태 사용 가정)
                if (status.equals("결제완료")) {
                    try {
                        // 룸서비스 파일의 시간 형식은 YYYYMMDDHHmmss이므로, YYYYMMDD 부분만 추출
                        String paidDateStr = data[REQ_IDX_TIMESTAMP].substring(0, 8);
                        LocalDate paidDate = LocalDate.parse(paidDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));

                        // 2. 보고 기간 내 포함되는지 확인
                        if (!paidDate.isBefore(startDate) && !paidDate.isAfter(endDate)) {
                            // 가격 파싱 로직 강화
                            String priceStr = data[REQ_IDX_TOTAL_PRICE].replaceAll("[^0-9]", "");
                            if (priceStr.isEmpty()) priceStr = "0"; // 빈 값이면 0으로 처리

                            revenue += Long.parseLong(priceStr);
                        }
                    } catch (Exception ignored) {
                        System.err.println("경고: 룸서비스 파일 파싱 오류 발생. 해당 라인 무시됨.");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("룸서비스 요청 파일을 읽는 중 오류 발생: " + e.getMessage());
        }
        return revenue;
    }
}