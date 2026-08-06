/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package poly.bachhoa.app;
import javax.swing.JFrame;
import poly.bachhoa.ui.component.WelcomeJDialog;

/**
 *
 * @author vuong
 */
public class CuaHangBachHoa {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
          java.awt.EventQueue.invokeLater(() -> {
              WelcomeJDialog welcome = new WelcomeJDialog(new JFrame(), true);
            welcome.setVisible(true); // mở cửa sổ chào
        });
    }
    
}
