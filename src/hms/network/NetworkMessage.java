package hms.network;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String command;  // 명령어 (예: LOGIN, RS_GET_MENU)
    private Object data;     // 데이터 (Map, String, User 객체 등)
    private boolean success; // 성공 여부
    private String message;  // 응답 메시지

    public NetworkMessage() {}

    // 요청용
    public NetworkMessage(String command, Object data) {
        this.command = command;
        this.data = data;
    }

    // 응답용
    public NetworkMessage(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public String getCommand() { return command; }
    public Object getData() { return data; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}