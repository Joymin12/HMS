package hms.model;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class RoomServiceDataManager {

    private static final String MENU_FILE_PATH = "data/room_service_menu.txt";
    private static final String REQUEST_FILE_PATH = "data/room_service_requests.txt";

    // ⭐ 두 개의 메모리 캐시
    private final List<String[]> menuCache = Collections.synchronizedList(new ArrayList<>());
    private final List<String[]> requestCache = Collections.synchronizedList(new ArrayList<>());

    private AtomicLong menuIdCounter = new AtomicLong(0);
    private AtomicLong requestIdCounter = new AtomicLong(0);

    public static final String STATUS_PENDING = "대기중";
    public static final String STATUS_PROCESSING = "처리중";
    public static final String STATUS_COMPLETED = "완료";
    public static final String STATUS_PAID = "결제완료";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter ID_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    public RoomServiceDataManager() {
        initializeFile(MENU_FILE_PATH);
        initializeFile(REQUEST_FILE_PATH);

        loadMenuCache();
        loadRequestCache();
    }

    private void initializeFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            if (!Files.exists(path)) {
                if (path.getParent() != null) Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
        } catch (IOException e) {}
    }

    // --- 로드 & ID 카운터 설정 ---
    private void loadMenuCache() {
        menuCache.clear();
        long max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(MENU_FILE_PATH))) {
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(",");
                if (p.length > 0) {
                    menuCache.add(p);
                    try {
                        long c = Long.parseLong(p[0].replaceAll("[^0-9]", ""));
                        if (c > max) max = c;
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {}
        menuIdCounter.set(max + 1);
        System.out.println(">>> [룸서비스] 메뉴 로드 완료: " + menuCache.size() + "개");
    }

    private void loadRequestCache() {
        requestCache.clear();
        long max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(REQUEST_FILE_PATH))) {
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(",", -1); // -1 중요
                if (p.length >= 6) {
                    requestCache.add(p);
                    // ID 형식: R241127-001 -> 뒷부분 숫자 추출
                    try {
                        String[] idParts = p[0].split("-");
                        if(idParts.length > 1) {
                            long c = Long.parseLong(idParts[1]);
                            if (c > max) max = c;
                        }
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {}
        requestIdCounter.set(max + 1);
        System.out.println(">>> [룸서비스] 주문 로드 완료: " + requestCache.size() + "건");
    }

    // --- 저장 ---
    private boolean saveMenuToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(MENU_FILE_PATH))) {
            synchronized (menuCache) {
                for (String[] p : menuCache) pw.println(String.join(",", p));
            }
            return true;
        } catch (Exception e) { return false; }
    }

    private boolean saveRequestsToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(REQUEST_FILE_PATH))) {
            synchronized (requestCache) {
                for (String[] p : requestCache) pw.println(String.join(",", p));
            }
            return true;
        } catch (Exception e) { return false; }
    }

    // --- 메뉴 관련 메서드 ---
    public List<String> getAllCategories() {
        Set<String> c = new HashSet<>();
        synchronized (menuCache) {
            for(String[] p : menuCache) {
                if(p.length > 3) c.add(p[3].trim());
            }
        }
        return new ArrayList<>(c);
    }

    public List<String[]> getMenuByCategory(String c) {
        List<String[]> list = new ArrayList<>();
        synchronized (menuCache) {
            for(String[] p : menuCache) {
                if(p.length >= 4) {
                    if(c == null || p[3].trim().equals(c)) list.add(p);
                }
            }
        }
        return list;
    }
    public List<String[]> getAllMenu() { return getMenuByCategory(null); }

    public String addMenuItem(String n, int p, String c) {
        String id = String.valueOf(menuIdCounter.getAndIncrement());
        synchronized (menuCache) {
            menuCache.add(new String[]{id, n, String.valueOf(p), c});
            saveMenuToFile();
        }
        return id;
    }

    public boolean updateMenuItem(String id, String n, int p, String c) {
        synchronized (menuCache) {
            for(int i=0; i<menuCache.size(); i++) {
                if(menuCache.get(i)[0].equals(id)) {
                    menuCache.set(i, new String[]{id, n, String.valueOf(p), c});
                    return saveMenuToFile();
                }
            }
        }
        return false;
    }

    public boolean deleteMenuItem(String id) {
        synchronized (menuCache) {
            boolean removed = menuCache.removeIf(p -> p[0].equals(id));
            if(removed) return saveMenuToFile();
        }
        return false;
    }

    // --- 주문(Request) 관련 메서드 ---
    public String addServiceRequest(String r, String i, long t) {
        String id = "R" + ID_DATE_FORMATTER.format(java.time.LocalDateTime.now()) + "-" + String.format("%03d", requestIdCounter.getAndIncrement());
        String time = java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        synchronized (requestCache) {
            requestCache.add(new String[]{id, r, i, String.valueOf(t), STATUS_PENDING, time});
            saveRequestsToFile();
        }
        return id;
    }

    public List<String[]> getAllRequests() {
        synchronized (requestCache) {
            return new ArrayList<>(requestCache);
        }
    }

    public List<String[]> getRequestsByStatus(String s) {
        List<String[]> list = new ArrayList<>();
        synchronized (requestCache) {
            for(String[] p : requestCache) {
                if(p.length >= 5 && p[4].trim().equals(s)) list.add(p);
            }
        }
        return list;
    }

    public boolean updateRequestStatus(String id, String s) {
        synchronized (requestCache) {
            for(int k=0; k<requestCache.size(); k++) {
                String[] p = requestCache.get(k);
                if(p.length >= 6 && p[0].equals(id)) {
                    p[4] = s;
                    requestCache.set(k, p);
                    return saveRequestsToFile();
                }
            }
        }
        return false;
    }

    public boolean updateStatusByRoomAndStatus(String r, String ts, String ns) {
        synchronized (requestCache) {
            boolean updated = false;
            for(int k=0; k<requestCache.size(); k++) {
                String[] p = requestCache.get(k);
                if(p.length >= 5 && p[1].equals(r) && p[4].equals(ts)) {
                    p[4] = ns;
                    requestCache.set(k, p);
                    updated = true;
                }
            }
            if(updated) return saveRequestsToFile();
        }
        return false;
    }

    // 보고서용
    public List<String[]> getPaidRequestsByPeriod(String startStr, String endStr) {
        List<String[]> list = new ArrayList<>();
        long startInt = Long.parseLong(startStr.replace("-", "") + "000000");
        long endInt = Long.parseLong(endStr.replace("-", "") + "235959");

        synchronized (requestCache) {
            for(String[] parts : requestCache) {
                if (parts.length < 6) continue;
                if (parts[4].trim().equals(STATUS_PAID)) {
                    try {
                        long timestamp = Long.parseLong(parts[5].trim());
                        if (timestamp >= startInt && timestamp <= endInt) {
                            list.add(parts);
                        }
                    } catch (NumberFormatException e) {}
                }
            }
        }
        return list;
    }
}