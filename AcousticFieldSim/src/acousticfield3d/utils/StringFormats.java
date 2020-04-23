package acousticfield3d.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 *
 * @author am14010
 */
public class StringFormats {
    
    public static StringFormats get(){
        return instance;
    }
    
    private final static StringFormats instance = new StringFormats();
    
    private StringFormats(){
        formatWith4Decs = new DecimalFormat("0.0000");
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        formatWith4Decs.setDecimalFormatSymbols(otherSymbols);
        formatWith2Decs = new DecimalFormat("0.00");
        formatWith2Decs.setDecimalFormatSymbols(otherSymbols);
    }
    
    private final DecimalFormat formatWith4Decs;
    private final DecimalFormat formatWith2Decs;
    
    
    public String dc4( double f ){
        return formatWith4Decs.format( f );
    }
    
    public String dc2( double f ){
        return formatWith2Decs.format( f );
    }
    
}
