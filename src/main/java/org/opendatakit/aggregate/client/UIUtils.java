package org.opendatakit.aggregate.client;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.gwt.user.client.Window;

public class UIUtils {

  public static String promptForFilterName( ArrayList<FilterGroup> currentFilters) throws Exception{
    boolean match = false;
    String newFilterName = Window.prompt(UIConsts.PROMPT_FOR_NAME_TXT, BasicConsts.EMPTY_STRING);
  
    while (true) {
      if (newFilterName != null) {
        for (FilterGroup filter : currentFilters) {
          if (filter.getName().equals(newFilterName)) {
            match = true;
          }
        }
      }
      if (newFilterName == null) { // cancel was pressed
        throw new Exception("User Cancelled"); // exit
      } else if (match) {
        match = false;
        newFilterName = Window.prompt(UIConsts.REPROMPT_FOR_NAME_TXT, BasicConsts.EMPTY_STRING);
      } else if (newFilterName.equals(BasicConsts.EMPTY_STRING)) {
        newFilterName = Window.prompt(UIConsts.ERROR_NO_NAME, BasicConsts.EMPTY_STRING);
      } else {
        break;
      }
    }
    return newFilterName;
  }

}
