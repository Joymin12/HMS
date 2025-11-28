package hms.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LateFeeCalculator {

    // 시간당 요금 (10,000원)
    private static final int FEE_PER_HOUR = 10000;
    // 체크아웃 기준 시간 (11:00)
    private static final LocalTime STANDARD_CHECKOUT_TIME = LocalTime.of(11, 0);

    /**
     * 지연 요금을 계산합니다.
     * @param scheduledCheckoutDate 예약된 체크아웃 날짜 (예: 2025-11-25 00:00)
     * @param actualCheckoutTime 실제 체크아웃하는 현재 시간
     * @return 계산된 지연 요금
     */
    public static int calculateLateFee(LocalDateTime scheduledCheckoutDate, LocalDateTime actualCheckoutTime) {
        // 예약된 날짜의 11:00를 기준 시간으로 설정
        LocalDateTime standardTime = scheduledCheckoutDate.with(STANDARD_CHECKOUT_TIME);

        // 실제 시간이 기준 시간보다 이전이거나 같으면 요금 없음 (예: 10:59 체크아웃)
        if (!actualCheckoutTime.isAfter(standardTime)) {
            return 0;
        }

        // 차이 계산 (분 단위)
        Duration diff = Duration.between(standardTime, actualCheckoutTime);
        long diffMinutes = diff.toMinutes();

        if (diffMinutes <= 0) return 0;

        // 시간 계산 (1분이라도 지나면 1시간 요금 추가)
        long hours = diffMinutes / 60;
        if (diffMinutes % 60 > 0) {
            hours++;
        }

        return (int) (hours * FEE_PER_HOUR);
    }
}