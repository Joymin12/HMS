package hms;

import hms.view.LoginFrame; // <-- LoginFrame을 import
import javax.swing.SwingUtilities;

/**
 * 프로그램의 유일한 시작점
 * LoginFrame을 실행합니다.
 */
public class App {

    static void main(String[] args) {

        // 실행할 클래스를 'LoginFrame::new'로 변경합니다.
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}