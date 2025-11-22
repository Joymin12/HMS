package hms.model;

import java.io.Serializable; // ⭐ 이 줄이 꼭 필요합니다!

/**
 * 회원 '바구니' (Data Transfer Object)
 * (★수정: Serializable 구현 추가)
 */
public class User implements Serializable { // ⭐ implements Serializable 추가

    private static final long serialVersionUID = 1L; // 버전 관리용 ID (선택 권장)

    private String id;
    private String password;
    private String name;
    private String phoneNumber;
    private int age;
    private String role;

    public User(String id, String password, String name, String phoneNumber, int age, String role) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.role = role;
    }

    // --- Getter 메서드들 ---

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getAge() {
        return age;
    }

    public String getRole() {
        return role;
    }
}