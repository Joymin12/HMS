package hms.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDataManager (파일 관리인)
 * (★수정: 6개 항목(role 포함)을 읽고 쓰도록 수정됨)
 */
public class UserDataManager {

    private final String filePath = "data/userinfo.txt";

    // ... (deleteUserById 메서드는 변경 없음) ...
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


    // ... (checkIfUserExists 메서드는 변경 없음) ...
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

    /**
     * (★수정★) (로그인용) ID로 사용자 찾기
     * 6개 항목을 읽어서 User 객체를 생성합니다.
     */
    public User findUserById(String id) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                // (★수정) data.length == 5 -> 6
                // data[5] = role
                if (data.length == 6 && data[0].equals(id)) {
                    String password = data[1];
                    String name = data[2];
                    String number = data[3];
                    int age = Integer.parseInt(data[4]);
                    String role = data[5]; // (★ '역할' 읽기)

                    // (★수정) 생성자에 6개 항목 전달
                    return new User(id, password, name, number, age, role);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // 사용자를 못 찾으면 null 반환
    }

    /**
     * (★수정★) (회원가입용) 파일에 사용자 추가
     * 6개 항목(role 포함)을 파일에 씁니다.
     */
    public boolean addUser(User user) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {

            // (★수정) "id,password,name,number,age,role" 형식의 CSV 문자열 생성
            String csvLine = String.join(",",
                    user.getId(),
                    user.getPassword(),
                    user.getName(),
                    user.getPhoneNumber(),
                    String.valueOf(user.getAge()),
                    user.getRole() // (★ '역할' 추가)
            );

            out.println(csvLine); // 파일에 한 줄 쓰기
            return true; // 성공

        } catch (IOException e) {
            e.printStackTrace();
            return false; // 실패
        }
    }
}