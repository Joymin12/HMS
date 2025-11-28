package hms.model;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RoomDataManager {
    // 경로를 src/hms/files/ 로 통일했습니다.
    private static final String ROOM_FILE = "data/rooms.txt";
    private static final String HISTORY_FILE = "data/room_history.txt";

    public RoomDataManager() {
        File file = new File(ROOM_FILE);
        if (!file.exists()) {
            // try-catch 지우고 바로 실행!
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            initDummyData(); // 초기 데이터 생성
        }
    }

    // [1단계 핵심] 초기 데이터 생성 로직 수정 (1층:스탠다드, 2층:디럭스, 3층:스위트)
    private void initDummyData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            for (int floor = 1; floor <= 3; floor++) {
                for (int room = 1; room <= 8; room++) {
                    // 방 번호 포맷 (예: 101, 205, 308)
                    String roomNum = floor + String.format("%02d", room);
                    String grade = "";
                    int price = 0;

                    if (floor == 1) {
                        grade = "스탠다드";
                        price = 100000;
                    } else if (floor == 2) {
                        grade = "디럭스";
                        price = 150000;
                    } else if (floor == 3) {
                        grade = "스위트";
                        price = 300000;
                    }
                    pw.println(roomNum + "," + grade + "," + price);
                }
            }
            System.out.println(">>> rooms.txt 파일이 자동으로 생성되었습니다. (총 24개 객실)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 모든 객실 조회
    public List<String[]> getAllRooms() {
        List<String[]> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ROOM_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) list.add(parts);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    // [예약 시스템 연동용] 특정 객실 가격 조회 메서드 추가
    public int getRoomPrice(String roomNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(ROOM_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                // 방 번호 일치 시 가격 리턴
                if (parts.length >= 3 && parts[0].equals(roomNumber)) {
                    return Integer.parseInt(parts[2].trim());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0; // 못 찾으면 0
    }

    // 객실 추가
    public boolean addRoom(String roomNum, String grade, int price) {
        if (getRoom(roomNum) != null) return false; // 중복 방지

        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE, true))) {
            pw.println(roomNum + "," + grade + "," + price);
            return true;
        } catch (IOException e) { return false; }
    }

    // [핵심 수정] 객실 수정: 사유(reason)를 받아서 이력 파일에 기록
    public boolean updateRoom(String roomNum, String newGrade, int newPrice, String reason) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        String oldPrice = "0"; // 기록용 예전 가격

        try (BufferedReader br = new BufferedReader(new FileReader(ROOM_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3 && parts[0].equals(roomNum)) {
                    oldPrice = parts[2]; // 수정 전 가격 저장
                    lines.add(roomNum + "," + newGrade + "," + newPrice); // 새 정보로 교체
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { return false; }

        if (updated) {
            boolean success = rewriteFile(lines);
            if (success) {
                // 수정 성공 시 히스토리 저장
                saveHistory(roomNum, oldPrice, String.valueOf(newPrice), reason);
            }
            return success;
        }
        return false;
    }

    // [신규 기능] 이력 저장 메서드 (room_history.txt)
    private void saveHistory(String roomNum, String oldPrice, String newPrice, String reason) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String log = String.format("[%s] %s호 | %s원 -> %s원 | 사유: %s",
                    now, roomNum, oldPrice, newPrice, reason);
            bw.write(log);
            bw.newLine();
            System.out.println("LOG SAVED: " + log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 객실 삭제
    public boolean deleteRoom(String roomNum) {
        List<String> lines = new ArrayList<>();
        boolean deleted = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ROOM_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3 && parts[0].equals(roomNum)) {
                    deleted = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { return false; }

        if (deleted) return rewriteFile(lines);
        return false;
    }

    // 내부 헬퍼: 단일 객실 찾기
    private String[] getRoom(String roomNum) {
        try (BufferedReader br = new BufferedReader(new FileReader(ROOM_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3 && parts[0].equals(roomNum)) return parts;
            }
        } catch (IOException e) {}
        return null;
    }

    // 내부 헬퍼: 파일 덮어쓰기
    private boolean rewriteFile(List<String> lines) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            for (String l : lines) pw.println(l);
            return true;
        } catch (IOException e) { return false; }
    }
}