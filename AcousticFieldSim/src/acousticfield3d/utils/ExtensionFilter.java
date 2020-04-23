package acousticfield3d.utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Asier
 */
class ExtensionFilter extends FileFilter{
      private String extension;
      
      public ExtensionFilter(String extension){
          this.extension = extension;
      }
      
      @Override
      public boolean accept(File file) {
          String filename = file.getName().toLowerCase();
          return (filename.endsWith(extension)
                  || file.isDirectory());
      }
      
      @Override
      public String getDescription() {
          return extension;
      }     
  
}
