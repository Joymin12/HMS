package hms.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class RoomServiceDataManager {

    private static final String MENU_FILE_PATH = "data/room_service_menu.txt";
    private static final String REQUEST_FILE_PATH = "data/room_service_requests.txt";
    private AtomicLong menuIdCounter = new AtomicLong(0);
    private AtomicLong requestIdCounter = new AtomicLong(0);

    public static final String STATUS_PENDING = "대기중";
    public static final String STATUS_PROCESSING = "처리중";
    public static final String STATUS_COMPLETED = "완료";
    public static final String STATUS_PAID = "결제완료";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter ID_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    public RoomServiceDataManager() {
        initializeFile(MENU_FILE_PATH, null, menuIdCounter);
        initializeFile(REQUEST_FILE_PATH, null, requestIdCounter);
    }

    private void initializeFile(String filePath, Runnable dummyDataInitializer, AtomicLong idCounter) {
        Path path = Paths.get(filePath);
        try {
            if (!Files.exists(path)) {
                if (path.getParent() != null) Files.createDirectories(path.getParent());
                Files.createFile(path);
                if (dummyDataInitializer != null) dummyDataInitializer.run();
            }
            loadInitialIdCounter(filePath, idCounter);
        } catch (IOException e) {}
    }

    private void loadInitialIdCounter(String fp, AtomicLong cnt) {
        long max = 0;
        try(BufferedReader br=new BufferedReader(new FileReader(fp))){
            String l; while((l=br.readLine())!=null){
                String[] p=l.split(","); if(p.length>0){
                    try{ long c=Long.parseLong(p[0].replaceAll("[^0-9]","")); if(c>max)max=c; }catch(Exception e){}
                }
            }
        }catch(Exception e){} cnt.set(max+1);
    }

    public List<String> getAllCategories() {
        Set<String> c = new HashSet<>();
        try(BufferedReader br=new BufferedReader(new FileReader(MENU_FILE_PATH))){
            String l; while((l=br.readLine())!=null) { String[] p=l.split(","); if(p.length>3) c.add(p[3].trim()); }
        }catch(Exception e){} return new ArrayList<>(c);
    }

    public List<String[]> getMenuByCategory(String c) {
        List<String[]> list = new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new FileReader(MENU_FILE_PATH))){
            String l; while((l=br.readLine())!=null){
                String[] p=l.split(","); if(p.length>=4){ if(c==null || p[3].trim().equals(c)) list.add(p); }
            }
        }catch(Exception e){} return list;
    }

    public List<String[]> getAllMenu() { return getMenuByCategory(null); }

    public String addMenuItem(String n, int p, String c) {
        String id = String.valueOf(menuIdCounter.getAndIncrement());
        String line = String.join(",", id, n, String.valueOf(p), c);
        try(PrintWriter pw=new PrintWriter(new FileWriter(MENU_FILE_PATH, true))){ pw.println(line); return id; }catch(Exception e){return null;}
    }

    public boolean updateMenuItem(String id, String n, int p, String c) {
        List<String> lines = new ArrayList<>(); boolean u = false;
        try(BufferedReader br=new BufferedReader(new FileReader(MENU_FILE_PATH))){
            String l; while((l=br.readLine())!=null){
                String[] part=l.split(","); if(part.length>0 && part[0].equals(id)) { lines.add(String.join(",", id,n,String.valueOf(p),c)); u=true; }
                else lines.add(l);
            }
        }catch(Exception e){return false;}
        if(u) try(PrintWriter pw=new PrintWriter(new FileWriter(MENU_FILE_PATH))){ for(String l:lines)pw.println(l); return true; }catch(Exception e){}
        return false;
    }

    public boolean deleteMenuItem(String id) {
        List<String> lines = new ArrayList<>(); boolean d = false;
        try(BufferedReader br=new BufferedReader(new FileReader(MENU_FILE_PATH))){
            String l; while((l=br.readLine())!=null){
                String[] part=l.split(","); if(part.length>0 && part[0].equals(id)) d=true; else lines.add(l);
            }
        }catch(Exception e){return false;}
        if(d) try(PrintWriter pw=new PrintWriter(new FileWriter(MENU_FILE_PATH))){ for(String l:lines)pw.println(l); return true; }catch(Exception e){}
        return false;
    }

    public String addServiceRequest(String r, String i, long t) {
        String id = "R" + ID_DATE_FORMATTER.format(java.time.LocalDateTime.now()) + "-" + String.format("%03d", requestIdCounter.getAndIncrement());
        String time = java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String line = String.join(",", id, r, i, String.valueOf(t), STATUS_PENDING, time);
        try(PrintWriter pw=new PrintWriter(new FileWriter(REQUEST_FILE_PATH, true))){ pw.println(line); return id; }catch(Exception e){return null;}
    }

    public List<String[]> getAllRequests() {
        List<String[]> l = new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new FileReader(REQUEST_FILE_PATH))){
            String line; while((line=br.readLine())!=null){ String[] p=line.split(",",-1); if(p.length>=6) l.add(p); }
        }catch(Exception e){} return l;
    }

    public List<String[]> getRequestsByStatus(String s) {
        List<String[]> l = new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new FileReader(REQUEST_FILE_PATH))){
            String line; while((line=br.readLine())!=null){ String[] p=line.split(",",-1); if(p.length>=5 && p[4].trim().equals(s)) l.add(p); }
        }catch(Exception e){} return l;
    }

    public boolean updateRequestStatus(String id, String s) {
        List<String> lines = new ArrayList<>(); boolean u = false;
        try(BufferedReader br=new BufferedReader(new FileReader(REQUEST_FILE_PATH))){
            String l; while((l=br.readLine())!=null){
                String[] p=l.split(","); if(p.length>=6 && p[0].equals(id)) { p[4]=s; lines.add(String.join(",", p)); u=true; }
                else lines.add(l);
            }
        }catch(Exception e){return false;}
        if(u) try(PrintWriter pw=new PrintWriter(new FileWriter(REQUEST_FILE_PATH))){ for(String l:lines)pw.println(l); return true; }catch(Exception e){}
        return false;
    }

    public boolean updateStatusByRoomAndStatus(String r, String ts, String ns) {
        List<String> lines = new ArrayList<>(); boolean u = false;
        try(BufferedReader br=new BufferedReader(new FileReader(REQUEST_FILE_PATH))){
            String l; while((l=br.readLine())!=null){
                String[] p=l.split(","); if(p.length>=5 && p[1].equals(r) && p[4].equals(ts)) { p[4]=ns; lines.add(String.join(",", p)); u=true; }
                else lines.add(l);
            }
        }catch(Exception e){return false;}
        if(u) try(PrintWriter pw=new PrintWriter(new FileWriter(REQUEST_FILE_PATH))){ for(String l:lines)pw.println(l); return true; }catch(Exception e){}
        return false;
    }

    // ⭐⭐⭐ [추가된 메소드] 기간별 결제된 룸서비스 조회 (보고서용) ⭐⭐⭐
    public List<String[]> getPaidRequestsByPeriod(String startStr, String endStr) {
        List<String[]> list = new ArrayList<>();
        // 날짜 포맷 변환 (YYYY-MM-DD -> YYYYMMDD000000)
        long startInt = Long.parseLong(startStr.replace("-", "") + "000000");
        long endInt = Long.parseLong(endStr.replace("-", "") + "235959");

        try (BufferedReader br = new BufferedReader(new FileReader(REQUEST_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 6) continue;

                // 결제완료(STATUS_PAID) 상태인 주문만 필터링
                if (parts[4].trim().equals(STATUS_PAID)) {
                    try {
                        long timestamp = Long.parseLong(parts[5].trim());
                        if (timestamp >= startInt && timestamp <= endInt) {
                            list.add(parts);
                        }
                    } catch (NumberFormatException e) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }
}