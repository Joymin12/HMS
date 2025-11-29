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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HMSServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println(">>> HMS 서버(Port:5000)가 시작되었습니다.");

            // 1. 모든 데이터 매니저 생성
            UserDataManager userMgr = new UserDataManager();
            ReservationDataManager resMgr = new ReservationDataManager();
            RoomServiceDataManager rsMgr = new RoomServiceDataManager();
            RoomDataManager roomMgr = new RoomDataManager();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> 클라이언트 접속: " + clientSocket.getInetAddress());
                // 동시 처리를 위해 별도 스레드 생성
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
                        // [1] 회원 관리
                        case "LOGIN":
                            String[] login = ((String) req.getData()).split(",");
                            User user = userMgr.findUserById(login[0]);
                            res = (user != null && user.getPassword().equals(login[1])) ? new NetworkMessage(true, "성공", user) : new NetworkMessage(false, "실패", null);
                            break;
                        case "SIGNUP":
                            User newUser = (User) req.getData();
                            if (userMgr.isUserIdExists(newUser.getId())) res = new NetworkMessage(false, "ID중복", 1);
                            else if (userMgr.addUser(newUser)) res = new NetworkMessage(true, "가입성공", 0);
                            else res = new NetworkMessage(false, "저장실패", 2);
                            break;
                        case "DELETE_USER":
                            res = new NetworkMessage(userMgr.deleteUser((String)req.getData()), "탈퇴", null);
                            break;
                        case "USER_GET_BY_ID":
                            User targetUser = userMgr.findUserById((String)req.getData());
                            res = (targetUser != null) ? new NetworkMessage(true, "조회성공", targetUser) : new NetworkMessage(false, "조회실패", null);
                            break;
                        case "USER_GET_ALL":
                            res = new NetworkMessage(true, "전체사용자", userMgr.readAllUsers());
                            break;
                        case "USER_ADD_ADMIN":
                            User userToAdd = (User) req.getData();
                            if (userMgr.isUserIdExists(userToAdd.getId())) res = new NetworkMessage(false, "ID중복", 1);
                            else if (userMgr.addUser(userToAdd)) res = new NetworkMessage(true, "추가성공", 0);
                            else res = new NetworkMessage(false, "저장실패", 2);
                            break;
                        case "USER_UPDATE_ADMIN":
                            User userToUpdate = (User) req.getData();
                            res = new NetworkMessage(userMgr.updateUser(userToUpdate), "수정성공", 0);
                            break;

                        // [2] 예약 관리
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
                            String[] coData = ((String)req.getData()).split(",");
                            String coRoom = coData[0];
                            int coLateFee = (coData.length > 1) ? Integer.parseInt(coData[1]) : 0;
                            res = new NetworkMessage(resMgr.processCheckoutByRoom(coRoom, coLateFee), "체크아웃", null);
                            break;
                        case "RES_VALIDATE_CHECKIN":
                            String[] vd = ((String)req.getData()).split(",");
                            String inputCode = vd[0];
                            String inputRoom = vd[1];

                            // [수정] validateReservationAndCheckIn 대신 validateRoomServiceAccess 호출
                            // (상태 변경 없이, 투숙 중인지 확인만 하는 메서드)
                            res = new NetworkMessage(resMgr.validateRoomServiceAccess(inputCode, inputRoom), "검증", null);
                            break;
                        case "RES_GET_ALL":
                            // ReservationDataManager에 구현된 readAllReservations() 메서드를 호출합니다.
                            res = new NetworkMessage(true, "전체조회", resMgr.readAllReservations());
                            break;

                        // [3] 룸서비스 관리
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
                        // [4] 보고서 생성 (지연료 분리 로직 수정 완료)
                        // ========================================================
                        case "REPORT_GENERATE":
                            String[] rdates = ((String) req.getData()).split(",");
                            String start = rdates[0], end = rdates[1];

                            List<String[]> resList = resMgr.getReservationsByPeriod(start, end);
                            List<String[]> rsList = rsMgr.getPaidRequestsByPeriod(start, end);

                            long roomRev = 0;
                            long fnbRev = 0;
                            long lateFeeRev = 0;
                            long occNights = 0;

                            int totalRooms = 24;
                            LocalDate sDate = LocalDate.parse(start), eDate = LocalDate.parse(end);
                            long days = ChronoUnit.DAYS.DAYS.between(sDate, eDate) + 1;
                            long capacity = totalRooms * days;

                            for (String[] r : resList) {
                                try {
                                    if (r.length > 12) {
                                        String status = r[12];
                                        String paymentMethod = r[11]; // 인덱스 11: 결제 방식
                                        boolean shouldCountRevenue = false;

                                        // ⭐ [최종 매출 집계 로직]
                                        // Rule 1: CHECKED_IN/OUT은 무조건 매출로 인정
                                        if (status.equals("CHECKED_IN") || status.equals("CHECKED_OUT")) {
                                            shouldCountRevenue = true;
                                        }
                                        // Rule 2: PENDING 상태는 '카드결제'인 경우만 잠정 매출로 인정
                                        else if (status.equals("PENDING")) {
                                            if (paymentMethod.equals("카드결제")) {
                                                shouldCountRevenue = true;
                                            }
                                        }

                                        if (shouldCountRevenue) {
                                            // 1. 객실 기본료만 더함 (인덱스 10)
                                            try {
                                                roomRev += Long.parseLong(r[10]);
                                            } catch (NumberFormatException e) {}
                                        }

                                        // 2. 지연료는 'CHECKED_OUT'일 때만 계산
                                        if (status.equals("CHECKED_OUT") && r.length > 15) {
                                            String lfStr = r[15].trim();
                                            if (!lfStr.isEmpty()) {
                                                try {
                                                    lateFeeRev += Long.parseLong(lfStr);
                                                } catch (NumberFormatException nfe) {}
                                            }
                                        }
                                    }

                                    // [점유율 계산 로직] (기존 로직 유지)
                                    LocalDate rIn = LocalDate.parse(r[3]);
                                    LocalDate rOut = LocalDate.parse(r[4]);
                                    LocalDate os = rIn.isAfter(sDate) ? rIn : sDate;
                                    LocalDate oe = rOut.isBefore(eDate) ? rOut : eDate;

                                    if (!os.isAfter(oe)) {
                                        long n = ChronoUnit.DAYS.between(os, oe);
                                        if (oe.isBefore(rOut)) {
                                            n += 1;
                                        }
                                        occNights += Math.max(0, n);
                                    }
                                } catch(Exception e) {
                                    continue;
                                }
                            }

                            // 룸서비스 합산
                            for (String[] rs : rsList) { try{ fnbRev += Long.parseLong(rs[3]); }catch(Exception e){} }

                            double occRate = (capacity>0) ? ((double)occNights/capacity)*100 : 0;

                            Map<String, Object> rpt = new HashMap<>();
                            rpt.put("RoomRevenue", roomRev);
                            rpt.put("FNBRevenue", fnbRev);
                            rpt.put("LateFeeRevenue", lateFeeRev);
                            rpt.put("TotalRevenue", roomRev + fnbRev + lateFeeRev);

                            rpt.put("OccupancyRate", occRate);
                            rpt.put("TotalCapacity", capacity);
                            rpt.put("OccupiedNights", occNights);

                            res = new NetworkMessage(true, "보고서", rpt);
                            break;

                        // ========================================================
                        // [5] 객실 및 가격 관리
                        // ========================================================
                        case "ROOM_GET_ALL":
                            res = new NetworkMessage(true, "조회", roomMgr.getAllRooms());
                            break;

                        // ⭐ [NEW] 예약 화면에서 가격 물어볼 때 사용
                        case "ROOM_GET_PRICE":
                            String targetRoom = (String) req.getData();
                            int currentPrice = roomMgr.getRoomPrice(targetRoom);
                            res = new NetworkMessage(true, "가격확인", currentPrice);
                            break;

                        case "ROOM_ADD":
                            Map<String, Object> ra = (Map<String, Object>) req.getData();
                            res = new NetworkMessage(roomMgr.addRoom((String)ra.get("roomNum"), (String)ra.get("grade"), (Integer)ra.get("price")), "추가", null);
                            break;

                        // ⭐ [수정] 사유(reason)까지 받아서 처리하도록 변경
                        case "ROOM_UPDATE":
                            Map<String, Object> ru = (Map<String, Object>) req.getData();
                            String rNum = (String) ru.get("roomNum");
                            String rGrade = (String) ru.get("grade");
                            int rPrice = (Integer) ru.get("price");
                            String rReason = (String) ru.get("reason");

                            res = new NetworkMessage(roomMgr.updateRoom(rNum, rGrade, rPrice, rReason), "수정", null);
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