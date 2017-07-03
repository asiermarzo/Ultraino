/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.utils;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author am14010
 */
public class SimpleInput extends JFrame{
    public interface SimpleInputEventListener{
        public static int EVENT_OK = 1;
        public static int EVENT_CANCEL = 2;
        public static int EVENT_FIELDPRESSED = 3;
        
        void simpleInputEvent(int event, String component);
    }
    
    final boolean shouldClose;
    final int columns;
    final boolean okEvent;
    final boolean cancelEvent;
    final boolean enterEvent;
    final SimpleInputEventListener listener;
    
    final private ArrayList<InputVar> vars = new ArrayList<>();
    final private JPanel panel;
    final private GridLayout gridLayout;
    final private JButton okButton = new JButton("OK");
    final private JButton cancelButton = new JButton("Close");
   
    final private HashMap<String, JTextField> stringComponents = new HashMap<>();
    final private HashMap<String, JTextField> intComponents = new HashMap<>();
    final private HashMap<String, JTextField> floatComponents = new HashMap<>();
    final private HashMap<String, JCheckBox> booleanComponents = new HashMap<>();
    final private HashMap<String, EnumComponents> enumComponents = new HashMap<>();
    
    class EnumComponents{
        public ButtonGroup group = new ButtonGroup();
        public ArrayList<String> names = new ArrayList<>();
        public ArrayList<JRadioButton> radios = new ArrayList<>();
    }
        
    public static SimpleInput createSimpleInput(
            final String[] names, 
            final Class[] classes, 
            final String[] defValues){
        assert( names.length == classes.length && classes.length == defValues.length);
        
        SimpleInput frame = new SimpleInput(true, 4, true, false, false, null);
        
        final int n = names.length;
        for(int i = 0; i < n; ++i){
            frame.putVar(names[i], classes[i], defValues[i]);
        }
        
        frame.update();
        return frame;
    }
    
    public void debugGenCode(){
        final StringBuilder sb = new StringBuilder();
        
        for(InputVar var : vars){
            //final float pixelsPermm = si.getFloat(\"Pixels/mm\");
            final String fullName = var.name;
            final String varName = fullName.replaceAll(" ", "").replaceAll("-", "").replaceAll("/", "");
            String typeStr = "";
            String getCommand = "";
            
            if(var.cClass == String.class){
                typeStr = "String";
                getCommand = "getString";
            }else if(var.cClass == Integer.class){
                typeStr = "int";
                getCommand = "getInt";
            }else if(var.cClass == Float.class){
                typeStr = "float";
                getCommand = "getFloat";
            }else if(var.cClass == Boolean.class){
                typeStr = "boolean";
                getCommand = "getBool";
            }else if(var.cClass == Enum.class){
                typeStr = "int";
                getCommand = "getEnum";
            }
            
            
            sb.append("final "+ typeStr +" "+ varName +" = si."+ getCommand +"(\""+ fullName +"\");\n");
        }
        
        System.out.println( sb.toString() );
    }

    public SimpleInput(boolean shouldClose, int columns, boolean okEvent, boolean cancelEvent, boolean enterEvent, SimpleInputEventListener listener) {
        this.shouldClose = shouldClose;
        this.columns = columns;
        this.okEvent = okEvent;
        this.cancelEvent = cancelEvent;
        this.enterEvent = enterEvent;
        this.listener = listener;
        
        gridLayout = new GridLayout(0, columns, 1, 1);
        panel = new JPanel();
        panel.setLayout(gridLayout);
        this.getContentPane().add(panel);
        
        okButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });
        cancelButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });
        
        if(shouldClose){
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }else{
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }
    
    
    
    public void putVar(String name, Class cClass, String defValue){
        InputVar var = new InputVar();
            var.name = name;
            var.cClass = cClass;
            var.defValue = defValue;
            getVars().add(var);
    }

    public ArrayList<InputVar> getVars() {
        return vars;
    }
     
    
    private void okPressed(){
        if(okEvent && listener != null){
            listener.simpleInputEvent( SimpleInputEventListener.EVENT_OK, null);
        }
    }
    private void cancelPressed(){
       if(cancelEvent && listener != null){
            listener.simpleInputEvent( SimpleInputEventListener.EVENT_CANCEL, null);
        } 
    }
    
    
    public void update(){
        //empty the contents
        panel.removeAll();
        stringComponents.clear();
        intComponents.clear();
        floatComponents.clear();
        booleanComponents.clear();
        enumComponents.clear();
        
        int nElements = 0;
        
        //iterate the list
        for(InputVar var : vars){  
            ++nElements;
            
            final Class t = var.cClass;
            if (t == String.class){
                panel.add( new JLabel(var.name) );
                JTextField input = new JTextField( var.defValue );
                stringComponents.put(var.name, input);
                panel.add(input);
            }else if(t == Integer.class){
                panel.add( new JLabel(var.name) );
                JTextField input = new JTextField( var.defValue );
                intComponents.put(var.name, input);
                panel.add(input);
            }else if(t == Float.class){
                panel.add( new JLabel(var.name) );
                JTextField input = new JTextField( var.defValue );
                floatComponents.put(var.name, input);
                panel.add(input);
            }else if(t == Boolean.class){
                JCheckBox input = new JCheckBox( var.name );
                booleanComponents.put( var.name, input );
                panel.add(input);
            }else if(t == Enum.class){
                EnumComponents comps = new EnumComponents();
                comps.group = new ButtonGroup();
                panel.add( new JLabel(var.name) );
                final String[] values = var.defValue.split(";");
                final int n = values.length;
                for(String s : values){
                    JRadioButton rad = new JRadioButton(s, false);
                    comps.group.add(rad);
                    comps.radios.add(rad);
                    comps.names.add(s);
                    panel.add( rad );
                }
                if (! comps.radios.isEmpty()) {
                    comps.radios.get(0).setSelected( true );
                }
                enumComponents.put( var.name, comps);
            }
        }
        for( int elementsToFinishRow = columns - (nElements % columns+1); 
                elementsToFinishRow > 0; --elementsToFinishRow){
            panel.add(new JLabel());
        }
        
        //add basic buttons
        if(okEvent){
            panel.add(okButton);
        }
        if(cancelEvent){
            panel.add(cancelButton);
        }
        
        pack();
    }
    
    
    public String getString(String name){
        final JTextField t = stringComponents.get(name);
        return t.getText();
    }
    public int getInt(String name){
        final JTextField t = intComponents.get(name);
        return Integer.parseInt(t.getText() );
    }
    public float getFloat(String name){
        final JTextField t = floatComponents.get(name);
        return Float.parseFloat(t.getText() );
    }
    public boolean getBoolean(String name){
        final JCheckBox t = booleanComponents.get(name);
        return t.isSelected();
    }
    public int getEnum(String name){
        final EnumComponents ec = enumComponents.get(name);
        for(int i = ec.radios.size() - 1; i >= 0; --i){
            if(ec.radios.get(i).isSelected()){
                return i;
            }
        }
        return -1;
    }

    
    public void setString(String name, String value){
        final JTextField t = stringComponents.get(name);
        t.setText(value);
    }
    public void setInt(String name, int value){
        final JTextField t = intComponents.get(name);
        t.setText(value + "");
    }
    public void setFloat(String name, float value){
        final JTextField t = floatComponents.get(name);
        t.setText(value + "");
    }
    public void setBoolean(String name, boolean value){
        final JCheckBox t = booleanComponents.get(name);
        t.setSelected( value );
    }
    public void setEnum(String name, int value){
        final EnumComponents ec = enumComponents.get(name);
        ec.radios.get(value).setSelected( true );
    }
  
}

class InputVar {

    public String name;
    public Class cClass;
    public String defValue;
    public float min, max;
    public boolean mandatory;
}
