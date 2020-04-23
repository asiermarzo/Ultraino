package acousticfield3d.utils;

import java.util.ArrayList;

/**
 *
 * @author Asier
 */
public class Parse {
    
    
    public static float toFloat(String text) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    public static int toInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double toDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public static ArrayList<Float> parseFloats(String text){
        String[] ss = text.split("\\n");
        ArrayList<Float> v = new ArrayList<>();
        for(String s : ss){
            v.add(toFloat(s) );
        }
        return v;
    }
    
    public static String printFloats(ArrayList<Float> floats){
        StringBuilder sb = new StringBuilder();
        for(Float f : floats){
            sb.append(f.toString() + "\n");
        }
        return sb.toString();
    }
    
    public static String printIntArray(int[] array){
        StringBuilder sb = new StringBuilder();
        final int size = array.length;
        for(int i = 0; i < size; ++i){
            sb.append(array[i] + "\n");
        }
        return sb.toString();
    }
    
     public static int[] parseIntArray(final String text){
         return parseIntArray(text,  "\\n");
     }
     
    public static int[] parseIntArray(final String text,final String separator){
        String[] ss = text.split(separator);
        final int size = ss.length;
        int[] v = new int[size];
        for(int i = 0; i < size; ++i){
            v[i] = Parse.toInt(ss[i]);
        }
        return v;
    }
}
