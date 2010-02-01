package org.odk.aggregate.constants;

/**
 * This class is only a temporary placeholder until the code migrates to the new
 * release where the code will move to the new format to avoid the need for these
 * 
 * @author wbrunette@gmail.com
 *
 */

public class Compatibility {

    public static String removeDashes(String str) {
       if(str == null) {
          return str;
      }
       return str.replace(BasicConsts.DASH, BasicConsts.EMPTY_STRING);
    }
    
}
