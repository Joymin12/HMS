package hms.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDataManager (파일 관리인)
 * (★수정: getAllUsers() 메서드 추가됨)
 */
public class UserDataManager {

    private final String filePath = "data/userinfo.txt";

    public boolean deleteUserById(String userIdToDelete) {
        List<String> allLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        List<String> updatedLines = new ArrayList<>();
        for (String line : allLines) {
            String[] data = line.split(",");
            if (data.length > 0 && data[0].equals(userIdToDelete)) {
                continue;
            }
            updatedLines.add(line);
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)))) {
            for (String line : updatedLines) {
                out.println(line);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkIfUserExists(String id) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0 && data[0].equals(id)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User findUserById(String id) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6 && data[0].equals(id)) {
                    String password = data[1];
                    String name = data[2];
                    String number = data[3];
                    int age = Integer.parseInt(data[4]);
                    String role = data[5];

                    return new User(id, password, name, number, age, role);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addUser(User user) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            String csvLine = String.join(",",
                    user.getId(),
                    user.getPassword(),
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

    /**
     * (★★★ 이 메서드가 추가되어야 합니다 ★★★)
     * userinfo.txt 파일의 모든 사용자를 List<User>로 반환합니다.
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6) { // 6개 항목이 모두 있는 정상 데이터만
                    String id = data[0];
                    String password = data[1];
                    String name = data[2];
                    String number = data[3];
                    int age = Integer.parseInt(data[4]);
                    String role = data[5];

                    userList.add(new User(id, password, name, number, age, role));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userList; // (파일이 없거나 비어있으면 빈 리스트 반환)
    }
}