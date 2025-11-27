package hms.controller;

import hms.network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationController {

    // 상수 (View에서 쓰임)
    public static final int RES_IDX_ID = 0;
    public static final int RES_IDX_ROOM_NUM = 9;
    public static final int RES_IDX_TOTAL_PRICE = 10;
    public static final int RES_IDX_STATUS = 12;
    public static final int RES_IDX_CHECKOUT_TIME = 13;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT";

    private String serverIp = "110.46.46.92";
    private int serverPort = 5000;

    private NetworkMessage sendRequest(String command, Object data) {
        try (Socket socket = new Socket(serverIp, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new NetworkMessage(command, data));
            out.flush();
            return (NetworkMessage) in.readObject();

        } catch (Exception e) {
            System.err.println("네트워크 오류: " + e.getMessage());
            return new NetworkMessage(false, "통신 오류", null);
        }
    }

    public boolean saveReservationToFile(Map<String, Object> data) {
        return sendRequest("RES_SAVE", data).isSuccess();
    }

    public String[] searchReservation(String name, String phoneNumber) {
        NetworkMessage res = sendRequest("RES_SEARCH", name + "," + phoneNumber);
        if (res.isSuccess() && res.getData() instanceof String[]) {
            return (String[]) res.getData();
        }
        return null;
    }

    public String[] getReservationDetailsById(String reservationId) {
        NetworkMessage res = sendRequest("RES_GET_BY_ID", reservationId);
        if (res.isSuccess()) return (String[]) res.getData();
        return null;
    }

    public boolean updateReservationStatus(String reservationId, String newStatus) {
        return sendRequest("RES_UPDATE_STATUS", reservationId + "," + newStatus).isSuccess();
    }

    public List<String> getBookedRooms(String checkInStr, String checkOutStr) {
        NetworkMessage res = sendRequest("RES_GET_BOOKED", checkInStr + "," + checkOutStr);
        if (res.isSuccess()) return (List<String>) res.getData();
        return new ArrayList<>();
    }

    public long getRoomCharge(String[] reservationData) {
        if (reservationData.length > RES_IDX_TOTAL_PRICE) {
            try {
                return Long.parseLong(reservationData[RES_IDX_TOTAL_PRICE].replaceAll("[^0-9]", ""));
            } catch (Exception e) { return 0; }
        }
        return 0;
    }

    public boolean validateReservationAndCheckIn(String lastSixDigits, String inputRoomNumber) {
        // (임시) 서버 검증 로직을 호출하거나, 여기서 목록을 받아와서 검증
        // 편의상 AUTH_ROOM_SERVICE와 비슷하게 처리
        return sendRequest("RES_VALIDATE_CHECKIN", lastSixDigits + "," + inputRoomNumber).isSuccess();
    }

    public boolean processCheckout(String roomNumber) {
        return sendRequest("RES_CHECKOUT", roomNumber).isSuccess();
    }
}