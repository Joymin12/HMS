package hms.model;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomDataManager {
    // 데이터 저장 경로
    private static final String ROOM_FILE = "data/rooms.txt";

    // ⭐ 메모리 캐시 리스트
    private final List<String[]> roomCache = Collections.synchronizedList(new ArrayList<>());

    public RoomDataManager() {
        File file = new File(ROOM_FILE);
        if (!file.exists()) {
            try {
                if (file.getParentFile() != null) file.getParentFile().mkdirs();
                file.createNewFile();
                initDummyData(); // 초기 데이터 생성
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 서버 시작 시 파일 -> 메모리 로드
        loadDataToMemory();
    }

    private void initDummyData() {
        // 더미 데이터 생성 시에는 직접 파일에 씀 (로드 전이므로)
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            pw.println("101,스탠다드,100000");
            pw.println("102,스탠다드,100000");
            pw.println("201,디럭스,150000");
            pw.println("202,디럭스,150000");
            pw.println("301,스위트,300000");
        } catch (IOException e) {}
    }

    // --- [내부 메서드] 파일 -> 메모리 로드 ---
    private void loadDataToMemory() {
        roomCache.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(ROOM_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    roomCache.add(parts);
                }
            }
            System.out.println(">>> [객실] 데이터 로드 완료: " + roomCache.size() + "개");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- [내부 메서드] 메모리 -> 파일 저장 ---
    private boolean saveMemoryToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            synchronized (roomCache) {
                for (String[] parts : roomCache) {
                    pw.println(String.join(",", parts));
                }
            }
            return true;
        } catch (IOException e) { return false; }
    }

    // 모든 객실 조회 (메모리 조회)
    public List<String[]> getAllRooms() {
        synchronized (roomCache) {
            return new ArrayList<>(roomCache); // 안전하게 복사본 반환
        }
    }

    // 객실 추가
    public boolean addRoom(String roomNum, String grade, int price) {
        synchronized (roomCache) {
            // 중복 확인
            for (String[] r : roomCache) {
                if (r[0].equals(roomNum)) return false;
            }
            // 메모리에 추가
            roomCache.add(new String[]{roomNum, grade, String.valueOf(price)});
            // 파일 저장
            return saveMemoryToFile();
        }
    }

    // 객실 수정 (방 번호 기준)
    public boolean updateRoom(String roomNum, String newGrade, int newPrice) {
        synchronized (roomCache) {
            boolean updated = false;
            for (int i = 0; i < roomCache.size(); i++) {
                if (roomCache.get(i)[0].equals(roomNum)) {
                    roomCache.set(i, new String[]{roomNum, newGrade, String.valueOf(newPrice)});
                    updated = true;
                    break;
                }
            }
            if (updated) return saveMemoryToFile();
        }
        return false;
    }

    // 객실 삭제
    public boolean deleteRoom(String roomNum) {
        synchronized (roomCache) {
            boolean removed = roomCache.removeIf(r -> r[0].equals(roomNum));
            if (removed) return saveMemoryToFile();
        }
        return false;
    }

    // 내부 헬퍼: 단일 객실 찾기 (필요 시 사용)
    public String[] getRoom(String roomNum) {
        synchronized (roomCache) {
            for (String[] r : roomCache) {
                if (r[0].equals(roomNum)) return r;
            }
        }
        return null;
    }
}