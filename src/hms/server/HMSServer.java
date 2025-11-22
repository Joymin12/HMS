package hms.server;

import hms.network.NetworkMessage;
import hms.model.User;
import hms.model.UserDataManager;
import hms.model.ReservationDataManager;
import hms.model.RoomServiceDataManager;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

public class HMSServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println(">>> HMS 서버(Port:5000)가 시작되었습니다.");

            // 데이터 매니저들 (서버가 파일을 관리함)
            UserDataManager userMgr = new UserDataManager();
            ReservationDataManager resMgr = new ReservationDataManager();
            RoomServiceDataManager roomServiceMgr = new RoomServiceDataManager();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> 클라이언트 접속: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket, userMgr, resMgr, roomServiceMgr)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        // --- [1] 회원 관리 ---
                        case "LOGIN":
                            String[] login = ((String) req.getData()).split(","); // "id,pw"
                            User user = userMgr.findUserById(login[0]);
                            if (user != null && user.getPassword().equals(login[1])) {
                                res = new NetworkMessage(true, "성공", user);
                            } else {
                                res = new NetworkMessage(false, "실패", null);
                            }
                            break;
                        case "SIGNUP":
                            User newUser = (User) req.getData();
                            if (userMgr.checkIfUserExists(newUser.getId())) res = new NetworkMessage(false, "ID중복", 1);
                            else if (userMgr.addUser(newUser)) res = new NetworkMessage(true, "가입성공", 0);
                            else res = new NetworkMessage(false, "저장실패", 2);
                            break;
                        case "DELETE_USER":
                            boolean delOk = userMgr.deleteUserById((String) req.getData());
                            res = new NetworkMessage(delOk, "탈퇴처리", null);
                            break;

                        // --- [2] 예약 관리 ---
                        case "RES_SAVE":
                            boolean saveOk = resMgr.saveReservation((Map<String, Object>) req.getData());
                            res = new NetworkMessage(saveOk, "예약저장", null);
                            break;
                        case "RES_SEARCH":
                            String[] search = ((String) req.getData()).split(",");
                            res = new NetworkMessage(true, "검색", resMgr.searchReservation(search[0], search[1]));
                            break;
                        case "RES_GET_BY_ID":
                            res = new NetworkMessage(true, "ID조회", resMgr.getReservationById((String) req.getData()));
                            break;
                        case "RES_UPDATE_STATUS":
                            String[] upStatus = ((String) req.getData()).split(",");
                            res = new NetworkMessage(resMgr.updateStatus(upStatus[0], upStatus[1]), "상태변경", null);
                            break;
                        case "RES_GET_BOOKED":
                            String[] dates = ((String) req.getData()).split(",");
                            res = new NetworkMessage(true, "방목록", resMgr.getBookedRooms(dates[0], dates[1]));
                            break;
                        case "RES_CHECKOUT": // 체크아웃 처리
                            String roomNum = (String) req.getData();
                            // ⭐ [수정] DataManager의 Checkout 로직을 호출하여 파일에 상태 변경
                            boolean checkoutOk = resMgr.processCheckoutByRoom(roomNum);

                            // ⭐ [수정] 성공 여부를 클라이언트에 반환
                            res = new NetworkMessage(checkoutOk, "체크아웃", null);
                            break;
                        case "RES_VALIDATE_CHECKIN": // 체크인 검증
                            String[] valData = ((String) req.getData()).split(",");
                            // 실제로는 ReservationDataManager에서 로직 수행
                            res = new NetworkMessage(true, "검증성공(임시)", null);
                            break;
                        case "AUTH_ROOM_SERVICE": // 룸서비스 인증
                            // 로직 수행
                            res = new NetworkMessage(true, "인증성공(임시)", null);
                            break;

                        // --- [3] 룸서비스 관리 (신규 추가) ---
                        case "RS_GET_ALL_MENU":
                            res = new NetworkMessage(true, "메뉴전체", rsMgr.getAllMenu());
                            break;
                        case "RS_GET_CATEGORIES":
                            res = new NetworkMessage(true, "카테고리", rsMgr.getAllCategories());
                            break;
                        case "RS_GET_MENU_BY_CAT":
                            res = new NetworkMessage(true, "카테고리별메뉴", rsMgr.getMenuByCategory((String) req.getData()));
                            break;
                        case "RS_ADD_MENU":
                            Map<String, Object> addM = (Map<String, Object>) req.getData();
                            String newMId = rsMgr.addMenuItem((String)addM.get("name"), (Integer)addM.get("price"), (String)addM.get("cat"));
                            res = new NetworkMessage(newMId != null, "메뉴추가", newMId);
                            break;
                        case "RS_UPDATE_MENU":
                            Map<String, Object> upM = (Map<String, Object>) req.getData();
                            boolean upMOk = rsMgr.updateMenuItem((String)upM.get("id"), (String)upM.get("name"), (Integer)upM.get("price"), (String)upM.get("cat"));
                            res = new NetworkMessage(upMOk, "메뉴수정", null);
                            break;
                        case "RS_DELETE_MENU":
                            res = new NetworkMessage(rsMgr.deleteMenuItem((String) req.getData()), "메뉴삭제", null);
                            break;
                        case "RS_ADD_REQUEST":
                            Map<String, Object> reqOrder = (Map<String, Object>) req.getData();
                            String reqId = rsMgr.addServiceRequest((String)reqOrder.get("room"), (String)reqOrder.get("items"), (Long)reqOrder.get("price"));
                            res = new NetworkMessage(reqId != null, "주문추가", reqId);
                            break;
                        case "RS_GET_ALL_REQUESTS":
                            res = new NetworkMessage(true, "요청목록", rsMgr.getAllRequests());
                            break;
                        case "RS_GET_REQ_BY_STATUS":
                            res = new NetworkMessage(true, "상태별요청", rsMgr.getRequestsByStatus((String) req.getData()));
                            break;
                        case "RS_UPDATE_REQ_STATUS":
                            String[] rsStat = ((String) req.getData()).split(",");
                            res = new NetworkMessage(rsMgr.updateRequestStatus(rsStat[0], rsStat[1]), "요청상태변경", null);
                            break;
                        case "RS_UPDATE_STATUS_BY_ROOM":
                            String[] rsRoomStat = ((String) req.getData()).split(","); // room, oldStat, newStat
                            res = new NetworkMessage(rsMgr.updateStatusByRoomAndStatus(rsRoomStat[0], rsRoomStat[1], rsRoomStat[2]), "일괄변경", null);
                            break;

                        default:
                            res = new NetworkMessage(false, "알수없는명령", null);
                    }
                    out.writeObject(res);
                    out.flush();
                } catch (EOFException e) { break; }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}