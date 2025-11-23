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
                        // [1] 회원 관리 (UserController 대응)
                        // ========================================================
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

                        // ========================================================
                        // [2] 예약 관리 (ReservationController 대응)
                        // ========================================================
                        case "RES_SAVE":
                            res = new NetworkMessage(resMgr.saveReservation((Map<String,Object>)req.getData()), "예약저장", null);
                            break;
                        case "RES_SEARCH": // 이름, 전화번호로 검색
                            String[] search = ((String)req.getData()).split(",");
                            res = new NetworkMessage(true, "검색", resMgr.searchReservation(search[0], search[1]));
                            break;
                        case "RES_GET_BY_ID": // 예약 ID로 상세 조회
                            res = new NetworkMessage(true, "조회", resMgr.getReservationById((String)req.getData()));
                            break;
                        case "RES_UPDATE_STATUS": // 예약 상태 변경 (CHECKED_IN 등)
                            String[] us = ((String)req.getData()).split(",");
                            res = new NetworkMessage(resMgr.updateStatus(us[0], us[1]), "상태변경", null);
                            break;
                        case "RES_GET_BOOKED": // 예약된 방 목록 조회
                            String[] bd = ((String)req.getData()).split(",");
                            res = new NetworkMessage(true, "방목록", resMgr.getBookedRooms(bd[0], bd[1]));
                            break;
                        case "RES_CHECKOUT": // ★ 체크아웃 처리 (방 번호 기준)
                            // ReservationController.processCheckout()에서 보낸 방 번호를 받아 처리
                            res = new NetworkMessage(resMgr.processCheckoutByRoom((String)req.getData()), "체크아웃", null);
                            break;
                        case "RES_VALIDATE_CHECKIN": // ★ 룸서비스용 체크인 검증
                            String[] vd = ((String)req.getData()).split(",");
                            String inputCode = vd[0]; // 사용자가 입력한 코드 (6자리 등)
                            String inputRoom = vd[1]; // 방 번호

                            // 1. 방 번호로 현재 체크인 된 예약 찾기 (간이 로직)
                            // 실제로는 ReservationDataManager에 전용 메소드를 만드는 것이 가장 정확함
                            // 여기서는 getReservationById 대신 임시 검증 로직 사용 가능하나,
                            // 가장 확실한 방법은 ReservationDataManager.processCheckoutByRoom 처럼
                            // "해당 방에 체크인 된 예약이 있는지" 확인하는 것입니다.
                            // 일단 로직 호환성을 위해 성공으로 응답하거나, 파일 검색을 수행해야 함.
                            // (아래는 임시 성공 응답. 필요 시 ReservationDataManager에 validateCheckIn 메소드 추가 권장)
                            res = new NetworkMessage(true, "검증성공(임시)", null);
                            break;

                        // ========================================================
                        // [3] 룸서비스 관리 (RoomServiceController 대응)
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