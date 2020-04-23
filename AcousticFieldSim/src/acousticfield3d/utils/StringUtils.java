package acousticfield3d.utils;

/**
 *
 * @author Asier
 */
public class StringUtils {
    public static String getBetween(String sourceString, String a, String b){
        int lA = a.length();
        int indexStart = sourceString.indexOf(a);
        int indexEnd;
        if (b != null){
            indexEnd = sourceString.indexOf(b, indexStart + lA);
        }else{
            indexEnd = sourceString.length();
        }
        if(indexStart != -1 && indexEnd != -1){
            return sourceString.substring(indexStart + lA, indexEnd);
        }
        return null;
    }
    
    public static String getBetween(String sourceString, String a, String b, int startAt){
        int lA = a.length();
        int indexStart = sourceString.indexOf(a, startAt);
        int indexEnd;
        if (b != null){
            indexEnd = sourceString.indexOf(b, indexStart + lA);
        }else{
            indexEnd = sourceString.length();
        }
        if(indexStart != -1 && indexEnd != -1){
            return sourceString.substring(indexStart + lA, indexEnd);
        }
        return null;
    }
}
