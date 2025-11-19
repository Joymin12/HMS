package hms.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RoomServiceDataManager (룸서비스 메뉴 및 요청 파일 관리)
 * - 메뉴 파일 구조: [ID, Name, Price, Category] (4필드)
 * - 요청 파일 구조: [ReqID, RoomNum, ItemSummary, TotalPrice, Status, Timestamp] (6필드)
 */
public class RoomServiceDataManager {

    // --- 파일 경로 및 카운터 ---
    private static final String MENU_FILE_PATH = "data/room_service_menu.txt";
    private static final String REQUEST_FILE_PATH = "data/room_service_requests.txt";
    private AtomicLong menuIdCounter = new AtomicLong(0);
    private AtomicLong requestIdCounter = new AtomicLong(0);

    // --- 상태 및 포맷 상수 ---
    public static final String STATUS_PENDING = "대기중";
    public static final String STATUS_PROCESSING = "처리중";
    public static final String STATUS_COMPLETED = "완료";
    public static final String STATUS_PAID = "결제완료"; // ⭐ [추가] 결제 완료 상태 상수
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter ID_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd"); // YYMMDD 형식 사용
    // --- 요청 데이터 인덱스 상수 ---
    private static final int REQ_IDX_ID = 0;
    private static final int REQ_IDX_ROOM_NUM = 1; // ⭐ [추가] 객실 번호 인덱스
    private static final int REQ_IDX_STATUS = 4; // 상태 필드는 Index 4에 위치
    // --- 메뉴 데이터 인덱스 상수 ---
    private static final int MENU_IDX_CATEGORY = 3; // 카테고리 필드는 Index 3에 위치

    public RoomServiceDataManager() {
        initializeFile(MENU_FILE_PATH, null, menuIdCounter);
        initializeFile(REQUEST_FILE_PATH, null, requestIdCounter);
    }

    // ---------------------------------------------------------------------
    // 초기화 및 ID 관리 헬퍼
    // ---------------------------------------------------------------------
    private void initializeFile(String filePath, Runnable dummyDataInitializer, AtomicLong idCounter) {
        Path path = Paths.get(filePath);
        try {
            if (!Files.exists(path)) {
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
                if (dummyDataInitializer != null) {
                    dummyDataInitializer.run();
                }
            }
            loadInitialIdCounter(filePath, idCounter);
        } catch (IOException e) {
            System.err.println(filePath + " 파일 초기화 오류: " + e.getMessage());
        }
    }


    private void loadInitialIdCounter(String filePath, AtomicLong idCounter) {
        long maxId = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > 0) {
                    try {
                        long currentId = 0;

                        if (filePath.equals(REQUEST_FILE_PATH)) {
                            String idPart = parts[0].trim();
                            if (idPart.contains("-")) {
                                String sequenceStr = idPart.substring(idPart.lastIndexOf('-') + 1);
                                currentId = Long.parseLong(sequenceStr.replaceAll("[^0-9]", ""));
                            }
                        } else {
                            currentId = Long.parseLong(parts[0].trim().replaceAll("[^0-9]", ""));
                        }

                        if (currentId > maxId) {
                            maxId = currentId;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        idCounter.set(maxId + 1);
    }

    // =================================================================
    // ★ 룸서비스 메뉴 관리 (CRUD - 4개 필드)
    // =================================================================

    public List<String> getAllCategories() {
        Set<String> categories = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(MENU_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > MENU_IDX_CATEGORY) {
                    categories.add(parts[MENU_IDX_CATEGORY].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("카테고리 조회 중 오류 발생: " + e.getMessage());
        }
        return new ArrayList<>(categories);
    }

    public List<String[]> getMenuByCategory(String category) {
        List<String[]> menuList = new ArrayList<>();
        boolean filterActive = category != null && !category.trim().isEmpty();

        try (BufferedReader reader = new BufferedReader(new FileReader(MENU_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    if (!filterActive || parts[MENU_IDX_CATEGORY].trim().equals(category)) {
                        menuList.add(parts);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("메뉴 조회 중 오류 발생: " + e.getMessage());
        }
        return menuList;
    }

    public List<String[]> getAllMenu() {
        return getMenuByCategory(null);
    }

    public String addMenuItem(String name, int price, String category) {
        String newId = String.valueOf(menuIdCounter.getAndIncrement());
        String csvLine = String.join(",",
                newId,
                name,
                String.valueOf(price),
                category
        );

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(MENU_FILE_PATH, true)))) {
            out.println(csvLine);
            return newId;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateMenuItem(String id, String name, int price, String category) {
        List<String> allLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(MENU_FILE_PATH))) {
            String line;
            String newLine = String.join(",", id, name, String.valueOf(price), category);

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && parts[0].trim().equals(id)) {
                    allLines.add(newLine);
                    updated = true;
                } else {
                    allLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (updated) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(MENU_FILE_PATH, false)))) {
                for (String newLine : allLines) {
                    out.println(newLine);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean deleteMenuItem(String id) {
        List<String> allLines = new ArrayList<>();
        boolean deleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(MENU_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].trim().equals(id)) {
                    deleted = true;
                } else {
                    allLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (deleted) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(MENU_FILE_PATH, false)))) {
                for (String newLine : allLines) {
                    out.println(newLine);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    // =================================================================
    // ★ 룸서비스 요청 관리 (room_service_requests.txt)
    // =================================================================

    public String addServiceRequest(String roomNumber, String itemSummary, long totalPrice) {
        String datePart = ID_DATE_FORMATTER.format(LocalDateTime.now());
        String sequencePart = String.format("%03d", requestIdCounter.getAndIncrement());
        String newId = "R" + datePart + "-" + sequencePart;
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        String csvLine = String.join(",",
                newId,
                roomNumber,
                itemSummary,
                String.valueOf(totalPrice),
                STATUS_PENDING,
                timestamp
        );

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(REQUEST_FILE_PATH, true)))) {
            out.println(csvLine);
            return newId;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String[]> getAllRequests() {
        List<String[]> requestList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(REQUEST_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    requestList.add(parts);
                }
            }
        } catch (IOException e) {
            System.err.println("요청 조회 중 오류 발생: " + e.getMessage());
        }
        return requestList;
    }

    // ⭐ [추가] 특정 상태를 가진 모든 서비스 요청 목록을 조회합니다. (청구서 조회 용도)
    public List<String[]> getRequestsByStatus(String status) {
        List<String[]> requestList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(REQUEST_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length > REQ_IDX_STATUS && parts[REQ_IDX_STATUS].trim().equals(status)) {
                    requestList.add(parts);
                }
            }
        } catch (IOException e) {
            System.err.println("상태별 요청 조회 중 오류 발생: " + e.getMessage());
        }
        return requestList;
    }

    public boolean updateRequestStatus(String id, String newStatus) {
        List<String> allLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(REQUEST_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 6 && parts[REQ_IDX_ID].trim().equals(id)) {

                    parts[REQ_IDX_STATUS] = newStatus;
                    allLines.add(String.join(",", parts));
                    updated = true;
                } else {
                    allLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (updated) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(REQUEST_FILE_PATH, false)))) {
                for (String newLine : allLines) {
                    out.println(newLine);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    // ⭐ [추가] 객실 번호와 상태를 기준으로, 해당 요청들을 새로운 상태로 일괄 업데이트합니다. (결제 완료 처리 용도)
    public boolean updateStatusByRoomAndStatus(String roomNumber, String targetStatus, String newStatus) {
        List<String> allLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(REQUEST_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length > REQ_IDX_STATUS) {
                    if (parts[REQ_IDX_ROOM_NUM].trim().equals(roomNumber) &&
                            parts[REQ_IDX_STATUS].trim().equals(targetStatus)) {

                        parts[REQ_IDX_STATUS] = newStatus;
                        allLines.add(String.join(",", parts));
                        updated = true;
                    } else {
                        allLines.add(line);
                    }
                } else {
                    allLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (updated) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(REQUEST_FILE_PATH, false)))) {
                for (String newLine : allLines) {
                    out.println(newLine);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}