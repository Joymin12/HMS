package hms.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDataManager {
    // 데이터 저장 경로
    private static final String ROOM_FILE = "data/rooms.txt";

    public RoomDataManager() {
        // 파일이 없으면 생성하고 기본 데이터 추가
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
    }

    private void initDummyData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            pw.println("101,스탠다드,100000");
            pw.println("102,스탠다드,100000");
            pw.println("201,디럭스,150000");
            pw.println("202,디럭스,150000");
            pw.println("301,스위트,300000");
        } catch (IOException e) {}
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

    // 객실 추가
    public boolean addRoom(String roomNum, String grade, int price) {
        // 중복 확인
        if (getRoom(roomNum) != null) return false;

        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE, true))) {
            pw.println(roomNum + "," + grade + "," + price);
            return true;
        } catch (IOException e) { return false; }
    }

    // 객실 수정 (방 번호 기준)
    public boolean updateRoom(String roomNum, String newGrade, int newPrice) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ROOM_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                // 방 번호가 같으면 내용 교체
                if (parts.length >= 3 && parts[0].equals(roomNum)) {
                    lines.add(roomNum + "," + newGrade + "," + newPrice);
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { return false; }

        if (updated) return rewriteFile(lines);
        return false;
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
                    deleted = true; // 리스트에 추가하지 않음 (삭제)
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