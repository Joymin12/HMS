package hms.model;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UserDataManager (메모리 캐싱 적용 버전)
 * - 기존: 요청 때마다 파일 열기 -> 읽기 -> 닫기 (매우 느림)
 * - 변경: 서버 켤 때 1번만 로드 -> 이후엔 메모리(RAM)에서 0.001초만에 조회
 */
public class UserDataManager {

    private final String filePath = "data/userinfo.txt";
    private final String delimiter = ",";

    // ⭐ 핵심: 파일 내용을 담아둘 메모리 공간 (캐시)
    // 여러 사람이 동시에 접속해도 문제 없도록 동기화된 리스트 사용
    private final List<User> userCache = Collections.synchronizedList(new ArrayList<>());

    public UserDataManager() {
        // 1. 파일이 없으면 생성
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.getParentFile() != null) file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 2. 서버 시작 시, 파일 내용을 전부 읽어서 userCache(메모리)에 저장
        loadDataToMemory();
    }

    // --- [내부 메서드] 파일 -> 메모리 로드 ---
    private void loadDataToMemory() {
        userCache.clear(); // 기존 데이터 비우기
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(delimiter);
                if (data.length == 6) {
                    try {
                        String id = data[0];
                        String password = data[1]; // 암호화 없이 저장된 상태라면 그대로
                        String name = data[2];
                        String number = data[3];
                        int age = Integer.parseInt(data[4]);
                        String role = data[5];

                        // 메모리 리스트에 추가
                        userCache.add(new User(id, password, name, number, age, role));
                    } catch (NumberFormatException ignored) {}
                }
            }
            System.out.println(">>> [사용자] 데이터 로드 완료: " + userCache.size() + "명");
        } catch (IOException e) {
            System.err.println("사용자 파일 읽기 실패: " + e.getMessage());
        }
    }

    // --- [내부 메서드] 메모리 -> 파일 저장 (덮어쓰기) ---
    private boolean saveMemoryToFile() {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)))) {
            synchronized (userCache) { // 저장 중엔 다른 작업 대기
                for (User user : userCache) {
                    String csvLine = String.join(delimiter,
                            user.getId(),
                            user.getPassword(),
                            user.getName(),
                            user.getPhoneNumber(),
                            String.valueOf(user.getAge()),
                            user.getRole()
                    );
                    out.println(csvLine);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    //  아래 메서드들은 이제 파일을 읽지 않고 'userCache'만 봅니다.
    // =========================================================

    // 1. ID로 사용자 찾기 (로그인 시 사용 - 매우 빨라짐)
    public User findUserById(String id) {
        synchronized (userCache) {
            for (User user : userCache) {
                if (user.getId().equals(id)) {
                    return user;
                }
            }
        }
        return null;
    }

    // 2. ID 중복 확인
    public boolean isUserIdExists(String id) {
        return findUserById(id) != null;
    }

    // 3. 사용자 추가 (회원가입)
    public boolean addUser(User user) {
        // 메모리에 먼저 추가
        userCache.add(user);
        // 그 다음 파일에 저장
        return saveMemoryToFile();
    }

    // 4. 사용자 삭제
    public boolean deleteUser(String userIdToDelete) {
        synchronized (userCache) {
            boolean removed = userCache.removeIf(user -> user.getId().equals(userIdToDelete));
            if (removed) {
                return saveMemoryToFile();
            }
        }
        return false;
    }

    // 5. 사용자 정보 수정
    public boolean updateUser(User updatedUser) {
        synchronized (userCache) {
            boolean found = false;
            for (int i = 0; i < userCache.size(); i++) {
                if (userCache.get(i).getId().equals(updatedUser.getId())) {
                    userCache.set(i, updatedUser); // 메모리 수정
                    found = true;
                    break;
                }
            }
            if (found) {
                return saveMemoryToFile(); // 파일 저장
            }
        }
        return false;
    }

    // 6. 모든 사용자 조회 (관리자용)
    public List<User> readAllUsers() {
        // 캐시된 리스트의 복사본을 반환 (안전성 위해)
        synchronized (userCache) {
            return new ArrayList<>(userCache);
        }
    }
}