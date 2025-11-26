// UserDataManager.java (최종 완성본)
package hms.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDataManager (파일 관리인)
 * - data/userinfo.txt 파일을 직접 읽고 쓰는 로직을 담당합니다.
 */
public class UserDataManager {

    private final String filePath = "data/userinfo.txt";
    private final String delimiter = ",";

    // 1. 특정 ID의 사용자를 파일에서 삭제합니다. (deleteUser)
    public boolean deleteUser(String userIdToDelete) {
        List<User> userList = readAllUsers();
        boolean removed = userList.removeIf(user -> user.getId().equals(userIdToDelete));

        if (removed) {
            // ⭐ [수정] 삭제 후 정리된 리스트를 파일에 다시 저장합니다.
            return saveUsersToFile(userList);
        }
        return false;
    }

    // 2. 특정 ID가 이미 존재하는지 확인합니다. (isUserIdExists)
    public boolean isUserIdExists(String id) {
        return readAllUsers().stream().anyMatch(user -> user.getId().equals(id));
    }

    // 3. ID로 특정 사용자 찾기 (findUserById)
    public User findUserById(String id) {
        return readAllUsers().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // 4. 새 사용자 정보를 파일에 추가합니다. (addUser)
    public boolean addUser(User user) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            String csvLine = String.join(delimiter,
                    user.getId(),
                    user.getPassword(), // User.getPassword() 호출
                    user.getName(),
                    user.getPhoneNumber(),
                    String.valueOf(user.getAge()),
                    user.getRole()
            );
            out.println(csvLine);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. userinfo.txt 파일의 모든 사용자를 List<User>로 반환합니다. (readAllUsers)
    public List<User> readAllUsers() {
        List<User> userList = new ArrayList<>();
        // 파일이 존재하지 않는 경우 대비
        File file = new File(filePath);
        if (!file.exists()) return userList;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(delimiter);
                if (data.length == 6) {
                    try {
                        String id = data[0];
                        String password = data[1];
                        String name = data[2];
                        String number = data[3];
                        int age = Integer.parseInt(data[4]);
                        String role = data[5];

                        userList.add(new User(id, password, name, number, age, role));
                    } catch (NumberFormatException ignored) {
                        // 데이터 줄에 오류가 있는 경우 건너뜁니다.
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("사용자 파일 읽기 오류: " + e.getMessage());
        }
        return userList;
    }

    // 6. 사용자 정보 수정 (updateUser)
    public boolean updateUser(User updatedUser) {
        List<User> userList = readAllUsers();
        boolean found = false;

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(updatedUser.getId())) {
                userList.set(i, updatedUser);
                found = true;
                break;
            }
        }

        if (found) {
            return saveUsersToFile(userList);
        }
        return false;
    }

    // 7. 현재 리스트를 파일에 덮어쓰기 합니다. (saveUsersToFile)
    private boolean saveUsersToFile(List<User> userList) {
        // FileWriter의 두 번째 인자를 false로 하여 파일 내용을 덮어씁니다.
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)))) {
            for (User user : userList) {
                // ⭐ [수정] user.getPassword() 사용 (getPw 오류 해결)
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
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}