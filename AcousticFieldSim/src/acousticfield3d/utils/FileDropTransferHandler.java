package acousticfield3d.utils;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.DropMode;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

/**
 *
 * @author Asier
 */
public class FileDropTransferHandler extends TransferHandler{

    public static void addDroper(JTextField field){
        field.setDragEnabled(true);
        field.setDropMode(DropMode.INSERT);
        field.setTransferHandler( new FileDropTransferHandler() );
    }
    
    public FileDropTransferHandler() {}

    
    @Override
    public boolean canImport(TransferSupport support) {
        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
            support.setDropAction(COPY);
            return true;
        }
        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {
       if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try{
                List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if(! files.isEmpty()){
                    if (support.getComponent() instanceof JTextField){
                        JTextField label = (JTextField)support.getComponent();
                        File f = files.get(0);
                        label.setText( f.getAbsolutePath() );
                    }
                }
                return true;
            }catch(UnsupportedFlavorException e ){ e.printStackTrace();
            }catch(IOException e) {e.printStackTrace();}
        }
        return false;
    }
}
