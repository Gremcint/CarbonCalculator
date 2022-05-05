/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Prototype.StaticClasses;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author Gregory
 */
public class DateTextField extends JFormattedTextField {
   Format format = new SimpleDateFormat("yyyy/MM/dd");
  
   public DateTextField() {
      super();
      MaskFormatter maskFormatter = null;
      try {
         maskFormatter = new MaskFormatter("####-##-##");
      } catch (ParseException e) {
      }
  
      maskFormatter.setPlaceholderCharacter('_');
      setFormatterFactory(new DefaultFormatterFactory(maskFormatter));
      
   }
  
   public void setValue(Date date) {
      super.setValue(toString(date));
   }
  
   
   private String toString(Date date) {
      try {
         return format.format(date);
      } catch (Exception e) {
         return "";
      }
   }
}

