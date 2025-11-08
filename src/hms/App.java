package hms;

import hms.view.LoginFrame;
import javax.swing.SwingUtilities;

/**
 * 프로그램의 유일한 시작점
 * LoginFrame을 실행합니다.
 */
public class App {

    static void main(String[] args) {

        // 프로그램 시작 시 LoginFrame을 띄웁니다.
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}