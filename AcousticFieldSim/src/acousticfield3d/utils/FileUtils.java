package acousticfield3d.utils;

import java.awt.Component;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Asier
 */
public class FileUtils {
  private static File lastChooserPath;
  private static File lastIndicatedPath;

    public static void setLastIndicatedPath(File lastIndicatedPath) {
        FileUtils.lastIndicatedPath = lastIndicatedPath;
    }

    public static void setLastChooserPath(File lastChooserPath) {
        FileUtils.lastChooserPath = lastChooserPath;
    }
    
    

    public static File getLastChooserPath() {
        return lastChooserPath;
    }

    public static File getLastIndicatedPath() {
        return lastIndicatedPath;
    }

  
    
  public static String getFileName(final String p){
      return getFileName(new File(p));
  }
  
  public static String getFileName(File p){
      String name = p.getName();
      int firstIndex = name.indexOf(".");
      if(firstIndex != -1){
          return name.substring(0,firstIndex);
      }else{
          return name;
      }
  }
  
  public static String selectFile(Component component, String name, String extension, File pathToUse){
        JFileChooser chooser = new JFileChooser();
        if(extension != null){        
            chooser.setFileFilter(new ExtensionFilter(extension));
        }
        
        boolean useGivenPath = pathToUse != null;
        if (useGivenPath && lastIndicatedPath == null){
            lastIndicatedPath = pathToUse;
        }
        chooser.setCurrentDirectory(useGivenPath?lastIndicatedPath:lastChooserPath);
	int result = chooser.showDialog(component, name);
        if ( result == JFileChooser.APPROVE_OPTION){
            try{
                if(useGivenPath){
                    lastIndicatedPath = chooser.getCurrentDirectory();
                }else{
                    lastChooserPath = chooser.getCurrentDirectory();
                }
                String p = chooser.getSelectedFile().getAbsolutePath();
                if (extension.startsWith(".")){
                    if (p.endsWith(extension)){
                        return p;
                    }else{
                        return p + extension;
                    }
                }else{
                    if (p.endsWith(extension)){
                        return p;
                    }else{
                        return p + "." + extension;
                    }
                }
               
            }catch(Exception e){e.printStackTrace();}
        }

        return null;
    }


  public static String[] selectFiles(Component component,String name,String extension,File pathToUse){
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if(extension != null){
            chooser.setFileFilter(new ExtensionFilter(extension));
        }
        boolean useIndicated = pathToUse != null;
        if (useIndicated && lastIndicatedPath == null){
            lastIndicatedPath = pathToUse;
        }

        chooser.setCurrentDirectory(useIndicated?lastIndicatedPath:lastChooserPath);
	int result = chooser.showDialog(component, name);
        if ( result == JFileChooser.APPROVE_OPTION){
            try{
                if(useIndicated){
                    lastIndicatedPath = chooser.getCurrentDirectory();
                }else{
                    lastChooserPath = chooser.getCurrentDirectory();
                }

                File[] files = chooser.getSelectedFiles();
                String[] forReturn = new String[files.length];
                for(int i = 0; i < files.length; i++){
                    forReturn[i] = files[i].getAbsolutePath();
                }
                return forReturn;
            }catch(Exception e){e.printStackTrace();}
        }

        return null;
    }

    public static String selectDirectory(Component component,String name,File pathToUse){

        JFileChooser chooser = new JFileChooser();
        boolean useIndicated = pathToUse != null;
        if (useIndicated && lastIndicatedPath == null){
            lastIndicatedPath = pathToUse;
        }

        chooser.setCurrentDirectory(useIndicated?lastIndicatedPath:lastChooserPath);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	int result = chooser.showDialog(component, name);
        if ( result == JFileChooser.APPROVE_OPTION){
            try{
                if(useIndicated){
                    lastIndicatedPath = chooser.getCurrentDirectory();
                }else{
                    lastChooserPath = chooser.getCurrentDirectory();
                }
                return chooser.getSelectedFile().getAbsolutePath();
            }catch(Exception e){e.printStackTrace();}
        }
        return null;
    }


     
      public static String selectNonExistingFile(Component parent,String extensionWanted){
        String forReturn = null;
        final String endWith = extensionWanted;

        JFileChooser chooser = new JFileChooser(lastChooserPath);
        chooser.setFileFilter(new FileFilter(){
            @Override
				public boolean accept(File file) {
					String filename = file.getName();
					return (filename.endsWith(endWith)||file.isDirectory());
				}
            @Override
				public String getDescription() {
					return endWith;
				}
			});
	int result = chooser.showSaveDialog(parent);
        if ( result == JFileChooser.APPROVE_OPTION){
             try{
                lastChooserPath = chooser.getCurrentDirectory();
                forReturn = chooser.getSelectedFile().getCanonicalPath();
            }catch(Exception e){e.printStackTrace();}
        }
        if(forReturn != null){
            if(!forReturn.endsWith(extensionWanted)){
                forReturn += extensionWanted;
            }
        }
        return forReturn;
    }

    public static String getAsRelative(File prefixPath, String path) throws IOException {
        if (prefixPath == null) {
            return path;
        }
        File filePath = new File(path);
        if(!filePath.isAbsolute()) { return path; }

        String a = prefixPath.getCanonicalFile().toURI().getPath();
        String b = filePath.getCanonicalFile().toURI().getPath();
        String[] basePaths = a.split("/");
        String[] otherPaths = b.split("/");

        int lastIndex = 0;
        for (int n = 0; n < basePaths.length && n < otherPaths.length; n++) {
            lastIndex = n;
            if (!basePaths[n].equals(otherPaths[n])) {
                break;
            }
        }
        if (lastIndex < basePaths.length-1){
            return path;
        }else{
            StringBuilder sb = new StringBuilder();
            for(int m = lastIndex+1; m < otherPaths.length-1; m++){
                sb.append(otherPaths[m]);
                sb.append("/");
            }
            sb.append(otherPaths[otherPaths.length-1]);
            return sb.toString();
        }
    }

    public static String calculatePath(File prefixPath, String path){
        File f = new File(path);
        if (f.isAbsolute()) { return path; }
        else {
            return new File(prefixPath, path).getAbsolutePath();
        }
    }

    public static byte[] getBytesFromInputStream(InputStream is) throws IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }
    
    public static String[] getLinesFromFile(File file) throws IOException {
        return getStringFromFile(file).split("\\n");
    }
    
    public static String getStringFromFile (File file) throws IOException {
        return new String(getBytesFromFile(file));
    }
    
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static void writeBytesInFile(String f, String data) throws FileNotFoundException, IOException{
        writeBytesInFile(new File(f), data.getBytes());
    }
    
    public static void writeBytesInFile(File f, String data) throws FileNotFoundException, IOException{
        writeBytesInFile(f, data.getBytes());
    } 
        
    public static void writeBytesInFile(File f, byte[] data) throws FileNotFoundException, IOException{
        FileOutputStream fos = new FileOutputStream(f, false);
        fos.write(data);
        fos.close();
    }

    public static File cloneFileInTemp(File f){
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try{
            byte[] buffer = new byte[1024];
            int len;
            File forReturn = File.createTempFile("KDD", ".jar");
            forReturn.deleteOnExit();
            fos = new FileOutputStream(forReturn, false);
            fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            return forReturn;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{ if(fos != null) {fos.close();}}catch(Exception e){}
            try{ if(fis != null) {fis.close();}}catch(Exception e){}
        }
        return null;
    }

    public static byte[] objectToXml(Object obj){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder xml = new XMLEncoder(bos);
        xml.writeObject(obj);
        xml.close();
        return bos.toByteArray();
    }

    public static byte[] objectToXml(Object obj,ClassLoader cl){
        if(cl == null){ return objectToXml(obj); }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder xml = new XMLEncoder(bos);
        //HACK
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        if (cl != oldLoader && cl != null){
            try{
                Thread.currentThread().setContextClassLoader(cl);
                xml.writeObject(obj);
            }catch(Exception e){e.printStackTrace();}
            finally {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }else{
            xml.writeObject(obj);
        }

        xml.close();
        return bos.toByteArray();
    }

    public static Object xmlToObject(byte[] data){
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        XMLDecoder xml = new XMLDecoder(bis);
        return xml.readObject();
    }

    public static Object xmlToObject(byte[] data,ClassLoader cl){
        if(cl == null){ return xmlToObject(data); }
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        XMLDecoder xml = new XMLDecoder(bis, null, null, cl);
        return xml.readObject();
    }

    public static String lastNameOfPath(String path){
        if(path == null) { return null; }
        int length = path.length();
        if(length == 0) { return path; }
        int index = Math.max(path.lastIndexOf("\\"), path.lastIndexOf("/"));
        if(index == length-1){
            path = path.substring(0, index);
            index = Math.max(path.lastIndexOf("\\"), path.lastIndexOf("/"));
        }

        if (index != -1){
            return path.substring(index+1);
        }else{
            return path;
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String MD5(byte[] data)
    throws NoSuchAlgorithmException, UnsupportedEncodingException  {
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] md5hash = new byte[32];
        md.update(data, 0, data.length);
        md5hash = md.digest();
        return convertToHex(md5hash);
    }

    public static byte[] inflate(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InflaterInputStream iis = new InflaterInputStream(bis, new Inflater(true));
        byte[] buffer = new byte[1024];
        int readed;
        while( (readed = iis.read(buffer)) > 0){
            bos.write(buffer, 0, readed);
        }
        return bos.toByteArray();
    }

    public static byte[] deflate(byte[] bytes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(bos, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        dos.write(bytes);
        dos.close();
        return bos.toByteArray();
    }
    
    public static byte[] ungzip(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream gis = new GZIPInputStream(bis);
        byte[] buffer = new byte[1024];
        int readed;
        while( (readed = gis.read(buffer)) > 0){
            bos.write(buffer, 0, readed);
        }
        return bos.toByteArray();
    }

    public static byte[] gzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        gos.write(bytes);
        gos.close();
        return bos.toByteArray();
    }

    public static void writeCompressedObject(File output, Object obj) throws FileNotFoundException, IOException{
        writeBytesInFile(output, gzip(objectToXml(obj)));
    }
    
    public static Object readCompressedObject(File input) throws IOException{
        return xmlToObject(ungzip(getBytesFromFile(input)));
    }
    
    public static void writeObject(File output, Object obj) throws FileNotFoundException, IOException{
        writeBytesInFile(output, objectToXml(obj));
    }
    
    public static Object readObject(File input) throws IOException{
        return xmlToObject(getBytesFromFile(input));
    }
}
