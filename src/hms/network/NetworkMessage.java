package hms.network;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String command; // "LOGIN", "SIGNUP" 등 명령
    private Object data;    // 보낼 데이터 (ID, PW 등)
    private boolean success; // 성공 여부
    private String message;  // 결과 메시지

    // 기본 생성자
    public NetworkMessage() {}

    // 클라이언트 -> 서버 (요청용)
    public NetworkMessage(String command, Object data) {
        this.command = command;
        this.data = data;
    }

    // 서버 -> 클라이언트 (응답용)
    public NetworkMessage(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getter & Setter
    public String getCommand() { return command; }
    public Object getData() { return data; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public void setCommand(String command) { this.command = command; }
    public void setData(Object data) { this.data = data; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
}