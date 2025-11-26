package hms.server;

import hms.model.ReservationDataManager;
import hms.model.RoomDataManager;
import hms.model.RoomServiceDataManager;
import hms.model.User;
import hms.model.UserDataManager;
import hms.network.NetworkMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HMSServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println(">>> HMS 서버(Port:5000)가 시작되었습니다.");

            // 1. 모든 데이터 매니저 생성 (서버 메모리에 로드)
            UserDataManager userMgr = new UserDataManager();
            ReservationDataManager resMgr = new ReservationDataManager();
            RoomServiceDataManager rsMgr = new RoomServiceDataManager();
            RoomDataManager roomMgr = new RoomDataManager();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> 클라이언트 접속: " + clientSocket.getInetAddress());

                // 2. 클라이언트 요청 처리 스레드 시작 (모든 매니저 전달)
                new Thread(() -> handleClient(clientSocket, userMgr, resMgr, rsMgr, roomMgr)).start();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void handleClient(Socket socket, UserDataManager userMgr, ReservationDataManager resMgr, RoomServiceDataManager rsMgr, RoomDataManager roomMgr) {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            while (true) {
                try {
                    NetworkMessage req = (NetworkMessage) in.readObject();
                    NetworkMessage res = null;
                    String cmd = req.getCommand();
                    System.out.println("[요청] " + cmd);

                    switch (cmd) {
                        // ========================================================
                        // [1] 회원 관리 (UserController 대응) - [최종 수정]
                        // ========================================================
                        case "LOGIN":
                            String[] login = ((String) req.getData()).split(",");
                            User user = userMgr.findUserById(login[0]);
                            res = (user != null && user.getPassword().equals(login[1])) ? new NetworkMessage(true, "성공", user) : new NetworkMessage(false, "실패", null);
                            break;
                        case "SIGNUP":
                            User newUser = (User) req.getData();
                            // ⭐⭐ [최종 수정]: isUserIdExists로 ID 중복 체크 (오류 해결)
                            if (userMgr.isUserIdExists(newUser.getId())) res = new NetworkMessage(false, "ID중복", 1);
                            else if (userMgr.addUser(newUser)) res = new NetworkMessage(true, "가입성공", 0);
                            else res = new NetworkMessage(false, "저장실패", 2);
                            break;
                        case "DELETE_USER":
                            // ⭐⭐ [최종 수정]: deleteUser로 사용자 삭제 (오류 해결)
                            res = new NetworkMessage(userMgr.deleteUser((String)req.getData()), "탈퇴", null);
                            break;

                        // ⭐⭐ [추가]: 단일 사용자 조회 (UserModifyDialog 대응)
                        case "USER_GET_BY_ID":
                            User targetUser = userMgr.findUserById((String)req.getData());
                            res = (targetUser != null) ? new NetworkMessage(true, "조회성공", targetUser) : new NetworkMessage(false, "조회실패", null);
                            break;

                        // ⭐⭐ [추가]: 관리자용 전체 사용자 조회 (UserController.getAllUsersForAdmin 대응)
                        case "USER_GET_ALL":
                            // readAllUsers 대신 readAll()이 맞을 수 있지만, 현재 UserController에서 사용하도록 요청했으므로 그대로 유지
                            // ⭐ 만약 userMgr.readAllUsers()에서 오류가 난다면, userMgr.readAll()로 변경해야 함.
                            res = new NetworkMessage(true, "전체사용자", userMgr.readAllUsers());
                            break;

                        // ⭐⭐ [추가]: 관리자용 사용자 추가 (UserController.addUserByAdmin 대응)
                        case "USER_ADD_ADMIN":
                            User userToAdd = (User) req.getData();
                            if (userMgr.isUserIdExists(userToAdd.getId())) res = new NetworkMessage(false, "ID중복", 1);
                            else if (userMgr.addUser(userToAdd)) res = new NetworkMessage(true, "추가성공", 0);
                            else res = new NetworkMessage(false, "저장실패", 2);
                            break;

                        // ⭐⭐ [추가]: 관리자용 사용자 수정 (UserController.updateUserByAdmin 대응)
                        case "USER_UPDATE_ADMIN":
                            User userToUpdate = (User) req.getData();
                            // UserDataManager의 updateUser는 boolean을 반환한다고 가정
                            res = new NetworkMessage(userMgr.updateUser(userToUpdate), "수정성공", 0);
                            break;


                        // ========================================================
                        // [2] 예약 관리 (ReservationController 대응)
                        // ... (생략)
                        // ========================================================
                        case "RES_SAVE":
                            res = new NetworkMessage(resMgr.saveReservation((Map<String,Object>)req.getData()), "예약저장", null);
                            break;
                        case "RES_SEARCH":
                            String[] search = ((String)req.getData()).split(",");
                            res = new NetworkMessage(true, "검색", resMgr.searchReservation(search[0], search[1]));
                            break;
                        case "RES_GET_BY_ID":
                            res = new NetworkMessage(true, "조회", resMgr.getReservationById((String)req.getData()));
                            break;
                        case "RES_UPDATE_STATUS":
                            String[] us = ((String)req.getData()).split(",");
                            res = new NetworkMessage(resMgr.updateStatus(us[0], us[1]), "상태변경", null);
                            break;
                        case "RES_GET_BOOKED":
                            String[] bd = ((String)req.getData()).split(",");
                            res = new NetworkMessage(true, "방목록", resMgr.getBookedRooms(bd[0], bd[1]));
                            break;
                        case "RES_CHECKOUT":
                            res = new NetworkMessage(resMgr.processCheckoutByRoom((String)req.getData()), "체크아웃", null);
                            break;
                        case "RES_VALIDATE_CHECKIN":
                            String[] vd = ((String)req.getData()).split(",");
                            String inputCode = vd[0];
                            String inputRoom = vd[1];
                            res = new NetworkMessage(resMgr.validateReservationAndCheckIn(inputCode, inputRoom), "검증", null);
                            break;

                        // ========================================================
                        // [3] 룸서비스 관리 (RoomServiceController 대응)
                        // ... (생략)
                        // ========================================================
                        case "RS_GET_ALL_MENU": res = new NetworkMessage(true, "메뉴", rsMgr.getAllMenu()); break;
                        case "RS_GET_CATEGORIES": res = new NetworkMessage(true, "카테고리", rsMgr.getAllCategories()); break;
                        case "RS_GET_MENU_BY_CAT": res = new NetworkMessage(true, "메뉴", rsMgr.getMenuByCategory((String)req.getData())); break;
                        case "RS_ADD_MENU":
                            Map<String,Object> am = (Map<String,Object>)req.getData();
                            res = new NetworkMessage(true, "추가", rsMgr.addMenuItem((String)am.get("name"),(Integer)am.get("price"),(String)am.get("cat")));
                            break;
                        case "RS_UPDATE_MENU":
                            Map<String,Object> um = (Map<String,Object>)req.getData();
                            res = new NetworkMessage(rsMgr.updateMenuItem((String)um.get("id"),(String)um.get("name"),(Integer)um.get("price"),(String)um.get("cat")), "수정", null);
                            break;
                        case "RS_DELETE_MENU": res = new NetworkMessage(rsMgr.deleteMenuItem((String)req.getData()), "삭제", null); break;
                        case "RS_ADD_REQUEST":
                            Map<String,Object> ar = (Map<String,Object>)req.getData();
                            res = new NetworkMessage(true, "주문", rsMgr.addServiceRequest((String)ar.get("room"),(String)ar.get("items"),(Long)ar.get("price")));
                            break;
                        case "RS_GET_ALL_REQUESTS": res = new NetworkMessage(true, "요청목록", rsMgr.getAllRequests()); break;
                        case "RS_GET_REQ_BY_STATUS": res = new NetworkMessage(true, "상태별", rsMgr.getRequestsByStatus((String)req.getData())); break;
                        case "RS_UPDATE_REQ_STATUS":
                            String[] urs = ((String)req.getData()).split(",");
                            res = new NetworkMessage(rsMgr.updateRequestStatus(urs[0], urs[1]), "상태변경", null);
                            break;
                        case "RS_UPDATE_STATUS_BY_ROOM":
                            String[] rstat = ((String)req.getData()).split(",");
                            res = new NetworkMessage(rsMgr.updateStatusByRoomAndStatus(rstat[0], rstat[1], rstat[2]), "일괄변경", null);
                            break;

                        // ========================================================
                        // [4] 보고서 생성 (ReportController 대응)
                        // ... (생략)
                        // ========================================================
                        case "REPORT_GENERATE":
                            String[] rdates = ((String) req.getData()).split(",");
                            String start = rdates[0], end = rdates[1];

                            List<String[]> resList = resMgr.getReservationsByPeriod(start, end);
                            List<String[]> rsList = rsMgr.getPaidRequestsByPeriod(start, end);

                            long roomRev = 0, fnbRev = 0, occNights = 0;
                            int totalRooms = 24;
                            LocalDate sDate = LocalDate.parse(start), eDate = LocalDate.parse(end);
                            long days = ChronoUnit.DAYS.between(sDate, eDate) + 1;
                            long capacity = totalRooms * days;

                            for (String[] r : resList) {
                                try {
                                    if(r[12].equals("CHECKED_OUT")) roomRev += Long.parseLong(r[10]);
                                    LocalDate rIn = LocalDate.parse(r[3]), rOut = LocalDate.parse(r[4]);
                                    LocalDate os = rIn.isAfter(sDate) ? rIn : sDate;
                                    LocalDate oe = rOut.isBefore(eDate) ? rOut : eDate;
                                    if(!os.isAfter(oe)) {
                                        long n = ChronoUnit.DAYS.between(os, oe);
                                        if(!os.equals(rOut)) n+=1;
                                        occNights += Math.max(0, n);
                                    }
                                } catch(Exception e){}
                            }
                            for (String[] rs : rsList) { try{ fnbRev += Long.parseLong(rs[3]); }catch(Exception e){} }

                            double occRate = (capacity>0) ? ((double)occNights/capacity)*100 : 0;
                            Map<String, Object> rpt = new HashMap<>();
                            rpt.put("RoomRevenue", roomRev); rpt.put("FNBRevenue", fnbRev);
                            rpt.put("TotalRevenue", roomRev+fnbRev); rpt.put("OccupancyRate", occRate);
                            rpt.put("TotalCapacity", capacity); rpt.put("OccupiedNights", occNights);

                            res = new NetworkMessage(true, "보고서", rpt);
                            break;

                        // ========================================================
                        // [5] 객실 및 가격 관리 (RoomController 대응)
                        // ... (생략)
                        // ========================================================
                        case "ROOM_GET_ALL":
                            res = new NetworkMessage(true, "조회", roomMgr.getAllRooms());
                            break;
                        case "ROOM_ADD":
                            Map<String, Object> ra = (Map<String, Object>) req.getData();
                            res = new NetworkMessage(roomMgr.addRoom((String)ra.get("roomNum"), (String)ra.get("grade"), (Integer)ra.get("price")), "추가", null);
                            break;
                        case "ROOM_UPDATE":
                            Map<String, Object> ru = (Map<String, Object>) req.getData();
                            res = new NetworkMessage(roomMgr.updateRoom((String)ru.get("roomNum"), (String)ru.get("grade"), (Integer)ru.get("price")), "수정", null);
                            break;
                        case "ROOM_DELETE":
                            res = new NetworkMessage(roomMgr.deleteRoom((String)req.getData()), "삭제", null);
                            break;

                        default: res = new NetworkMessage(false, "알수없는명령", null);
                    }
                    out.writeObject(res); out.flush();
                } catch (EOFException e) { break; }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}