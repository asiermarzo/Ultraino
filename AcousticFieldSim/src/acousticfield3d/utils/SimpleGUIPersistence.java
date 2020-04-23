package acousticfield3d.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 *
 * @author am14010
 */
public class SimpleGUIPersistence {
    
    //JTextField -> isEditable, text
        //JCheckBox -> isSelected
        //JRadioButton -> isSelected
        //JSpinner -> ToDo
        //JComboBox -> index
        //JPanel -> iterate
        //JFrame -> iterate
    
    
    public static void extractValuesFrom(final HashMap<String,String> keys, final String name, final Object panel){
        final HashMap<Object,Object> exploredObjs = new HashMap<>();
        try {
            exploredObjs.put(panel, panel);
            recExtractValuesFrom(exploredObjs,keys,name,panel);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(SimpleGUIPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private static int getObjType(final Field field){
        final Class cls = field.getType();
        int objType;
        if (JTextField.class.isAssignableFrom(cls)) {
            objType = 1;
        } else if (JCheckBox.class.isAssignableFrom(cls)) {
            objType = 2;
        } else if (JRadioButton.class.isAssignableFrom(cls)) {
            objType = 3;
        } else if (JComboBox.class.isAssignableFrom(cls)) {
            objType = 4;
        } else if (JPanel.class.isAssignableFrom(cls)) {
            objType = 5;
        } else if (JFrame.class.isAssignableFrom(cls)) {
            objType = 5;
        } else {
            objType = -1;
        }

        return objType;
    }
    
    private static void recExtractValuesFrom(final HashMap<Object,Object> exploredObjs,final HashMap<String,String> keys, final String name, final Object panel) throws IllegalArgumentException, IllegalAccessException{  
        final Field[] fieldlist = panel.getClass().getDeclaredFields();
        
        for(Field f : fieldlist){
            
            final int objType = getObjType(f);
            
            if (objType == -1){ continue; } //we cannot process this type of Class
            
            f.setAccessible(true);
            final Object obj = f.get(panel);
            if (exploredObjs.containsKey(obj)){ //avoid cyclic references, I do not know if there will be any.
                continue;
            }
            exploredObjs.put(obj, obj);
            final String objFullName = name + "." + f.getName();
            
            if (objType == 1){
                final JTextField co = (JTextField)obj;
                if(co.isEditable()){
                    keys.put(objFullName, co.getText());
                }
            }else if ( objType == 2 ){
                final JCheckBox co = (JCheckBox)obj;
                keys.put(objFullName, co.isSelected() ? "1" : "0");
            }else if ( objType == 3 ){
                final JRadioButton co = (JRadioButton)obj;
                keys.put(objFullName, co.isSelected() ? "1" : "0");
            }else if ( objType == 4 ){
                final JComboBox co = (JComboBox)obj;
                keys.put(objFullName, co.getSelectedIndex() + "");
            
            }else if ( objType == 5){
                recExtractValuesFrom(exploredObjs, keys, objFullName, obj);
            }
        }
    }
    
    public static void applyValuesTo(final HashMap<String,String> keys, final String name, final Object panel){
        if (keys == null || keys.isEmpty()){ return; }
        
        final HashMap<Object,Object> exploredObjs = new HashMap<>();
        try {
            exploredObjs.put(panel, panel);
            recApplyValuesTo(exploredObjs,keys,name,panel);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(SimpleGUIPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void recApplyValuesTo(final HashMap<Object,Object> exploredObjs,final HashMap<String,String> keys, final String name, final Object panel) throws IllegalArgumentException, IllegalAccessException{      
        final Field[] fieldlist = panel.getClass().getDeclaredFields();
        for(Field f : fieldlist){
            final int objType = getObjType(f);
            if (objType == -1){ continue; } //we cannot process this type of Class
            
            
            final String objFullName = name + "." + f.getName();
            if (objType >= 1 && objType <= 4 && !keys.containsKey(objFullName)){ 
                //the keys do not contain a value for this component
                continue;
            }
            
            
            f.setAccessible(true);
            final Object obj = f.get(panel);
            //we have already assigned this element
            if (exploredObjs.containsKey(obj)){
                continue;
            }
            exploredObjs.put(obj, obj);
            final String value = keys.get(objFullName);
            
            if ( objType == 1 ){
                final JTextField co = (JTextField)obj;
                co.setText(value);
            }else if ( objType == 2 ){
                final JCheckBox co = (JCheckBox)obj;
                co.setSelected( ! value.equals("0") );
            }else if ( objType == 3 ){
                final JRadioButton co = (JRadioButton)obj;
                co.setSelected( ! value.equals("0") );
            }else if ( objType == 4){
                final JComboBox co = (JComboBox)obj;
                co.setSelectedIndex( Parse.toInt(value) );
            }else if ( objType == 5){
                recApplyValuesTo(exploredObjs, keys, objFullName, obj);
            }
        }
    }
    
    
}
