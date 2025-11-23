package hms.server;

import hms.network.NetworkMessage;
import hms.model.User;
import hms.model.UserDataManager;
import hms.model.ReservationDataManager;
import hms.model.RoomServiceDataManager;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HMSServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println(">>> HMS 서버(Port:5000)가 시작되었습니다.");

            UserDataManager userMgr = new UserDataManager();
            ReservationDataManager resMgr = new ReservationDataManager();
            RoomServiceDataManager rsMgr = new RoomServiceDataManager();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> 클라이언트 접속: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket, userMgr, resMgr, rsMgr)).start();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void handleClient(Socket socket, UserDataManager userMgr, ReservationDataManager resMgr, RoomServiceDataManager rsMgr) {
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
                        // [회원]
                        case "LOGIN":
                            String[] login = ((String) req.getData()).split(",");
                            User user = userMgr.findUserById(login[0]);
                            res = (user != null && user.getPassword().equals(login[1])) ? new NetworkMessage(true, "성공", user) : new NetworkMessage(false, "실패", null);
                            break;
                        case "SIGNUP":
                            User newUser = (User) req.getData();
                            if (userMgr.checkIfUserExists(newUser.getId())) res = new NetworkMessage(false, "ID중복", 1);
                            else if (userMgr.addUser(newUser)) res = new NetworkMessage(true, "가입성공", 0);
                            else res = new NetworkMessage(false, "저장실패", 2);
                            break;
                        case "DELETE_USER":
                            res = new NetworkMessage(userMgr.deleteUserById((String)req.getData()), "탈퇴", null);
                            break;

                        // [예약]
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
                            String[] ri = resMgr.getReservationById(vd[0]);
                            boolean vOk = (ri!=null && ri.length>12 && ri[0].equals(vd[0]) && ri[9].equals(vd[1]) && ri[12].equals("CHECKED_IN"));
                            res = new NetworkMessage(vOk, "검증", null); // 로직 보완
                            break;
                        case "AUTH_ROOM_SERVICE": // 룸서비스 인증
                            String[] authData = ((String) req.getData()).split(",");
                            String sixDigits = authData[0];
                            String roomNum = authData[1];
                            // 간단 인증 로직 (실제로는 파일 전체 검색 필요)
                            res = new NetworkMessage(true, "인증성공(임시)", null);
                            break;

                        // [룸서비스]
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

                        // ⭐⭐⭐ [보고서 생성] ⭐⭐⭐
                        case "REPORT_GENERATE":
                            String[] rdates = ((String) req.getData()).split(",");
                            String start = rdates[0], end = rdates[1];

                            List<String[]> resList = resMgr.getReservationsByPeriod(start, end);
                            List<String[]> rsList = rsMgr.getPaidRequestsByPeriod(start, end);

                            long roomRev = 0, fnbRev = 0, occNights = 0;
                            int totalRooms = 24; // 전체 객실 수 (임의 설정)
                            LocalDate sDate = LocalDate.parse(start), eDate = LocalDate.parse(end);
                            long days = ChronoUnit.DAYS.between(sDate, eDate) + 1;
                            long capacity = totalRooms * days;

                            for (String[] r : resList) {
                                try {
                                    // 매출: 체크아웃 완료건
                                    if(r[12].equals("CHECKED_OUT")) roomRev += Long.parseLong(r[10]);

                                    // 점유율: 기간 겹침 계산
                                    LocalDate rIn = LocalDate.parse(r[3]), rOut = LocalDate.parse(r[4]);
                                    LocalDate os = rIn.isAfter(sDate) ? rIn : sDate;
                                    LocalDate oe = rOut.isBefore(eDate) ? rOut : eDate;
                                    if(!os.isAfter(oe)) {
                                        long n = ChronoUnit.DAYS.between(os, oe);
                                        if(!os.equals(rOut)) n+=1; // 마지막날 포함 여부 보정 (단순화)
                                        occNights += Math.max(0, n);
                                    }
                                } catch(Exception e){}
                            }
                            for (String[] rs : rsList) { try{ fnbRev += Long.parseLong(rs[3]); }catch(Exception e){} }

                            double occRate = (capacity>0) ? ((double)occNights/capacity)*100 : 0;

                            Map<String, Object> rpt = new HashMap<>();
                            rpt.put("RoomRevenue", roomRev);
                            rpt.put("FNBRevenue", fnbRev);
                            rpt.put("TotalRevenue", roomRev+fnbRev);
                            rpt.put("OccupancyRate", occRate);
                            rpt.put("TotalCapacity", capacity);
                            rpt.put("OccupiedNights", occNights);

                            res = new NetworkMessage(true, "보고서", rpt);
                            break;

                        default: res = new NetworkMessage(false, "알수없는명령", null);
                    }
                    out.writeObject(res); out.flush();
                } catch (EOFException e) { break; }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}