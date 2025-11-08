package hms.model;

/**
 * 회원 '바구니' (Data Transfer Object)
 * (★수정: 'role' 필드 추가됨)
 */
public class User {
    private String id;
    private String password;
    private String name;
    private String phoneNumber;
    private int age;
    private String role; // (★ 1/3) '역할' 필드 추가

    /**
     * (★ 2/3) 생성자 수정 (6개 항목을 받도록 변경)
     */
    public User(String id, String password, String name, String phoneNumber, int age, String role) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.role = role; // (★ '역할' 저장)
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

    /**
     * (★ 3/3) '역할'을 반환하는 getter 추가
     */
    public String getRole() {
        return role;
    }
}