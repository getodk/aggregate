package org.opendatakit.aggregate.client.widgets;

public class EnumListBox<T extends Enum<T>> extends AbstractListBox {

  private Class<T> classType;

  public EnumListBox(String tooltipText, T[] values) {
    super(tooltipText, false);

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

  public T getSelectedValue() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex > -1) {
      return (T) Enum.valueOf(classType, getValue(selectedIndex));
    } else {
      return null;
    }
  }
}
