package acousticfield3d.utils;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author Asier
 */
public class DialogUtils {

    public static String getStringDialog(Component father,String title, String defaultMsg){
        return (String) JOptionPane.showInputDialog(father,title, defaultMsg);
    }

    public static void showError(Component father, String title, String content){
        JOptionPane.showMessageDialog(father, content, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static int getBooleanDialog(Component father, String title){
        int option = JOptionPane.showConfirmDialog(father,title);
        if ( option == JOptionPane.YES_OPTION) { return 1;}
        else if ( option == JOptionPane.NO_OPTION) { return 0;}
        else{ return -1;}
    }

    public static Object getObjectListDialog(Component father,String title, String content, Object[] list){
       return JOptionPane.showInputDialog(father, title, content, JOptionPane.INFORMATION_MESSAGE, null,list , null);
    }


}
