package hms.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;                // 요청 종류 (LOGIN, SIGNUP, SAVE_RESERVATION 등)
    private Map<String, Object> data;   // 실제 데이터 (ID, 비번, 예약정보 등)
    private boolean isSuccess;          // 성공 여부
    private String responseMessage;     // 응답 메시지 (예: "로그인 성공")

    public Message(String type) {
        this.type = type;
        this.data = new HashMap<>();
    }

    // 데이터 넣기 편하게 만든 메서드
    public void putData(String key, Object value) {
        this.data.put(key, value);
    }

    // 데이터 꺼내기
    public Object getData(String key) {
        return this.data.get(key);
    }

    // Getter & Setter
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean success) { isSuccess = success; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String msg) { this.responseMessage = msg; }
}