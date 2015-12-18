package org.opendatakit.aggregate.client.widgets;

public final class EnumListBox<T extends Enum<T>> extends AggregateListBox {

  private Class<T> classType;
  
  public EnumListBox(T[] values, String tooltipText) {
    this(values, tooltipText, null);
  }
  
  public EnumListBox(T[] values, String tooltipText, String helpBalloonTxt) {
    super(tooltipText, false, helpBalloonTxt);

    if (values == null) {
      return;
    }
    
    // get class type
    if (values.length > 0 && values[0] != null) {
      classType = values[0].getDeclaringClass();
    } 

    // populate values
    for (T val : values) {
      addItem(val.toString(), val.name());
    }
  }

  public String getSelectedValue() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex > -1) {
      String value = getValue(selectedIndex);
      return value;
    } else {
      return null;
    }
  }
}
