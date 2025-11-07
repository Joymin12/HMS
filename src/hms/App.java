package hms; // 'hms' 패키지에 속합니다.

import hms.view.MainFrame; // hms.view 안의 MainFrame을 사용합니다.
import javax.swing.SwingUtilities;

public class App {

    static void main(String[] args) {

        // SwingUtilities.invokeLater(() -> new MainFrame()); 과 동일한 의미입니다.
        // "Swing 스레드에서 MainFrame의 생성자(new)를 실행해줘"
        SwingUtilities.invokeLater(MainFrame::new);
    }
}