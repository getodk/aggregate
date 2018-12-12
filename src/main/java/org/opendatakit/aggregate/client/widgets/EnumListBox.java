package org.opendatakit.aggregate.client.widgets;

public final class EnumListBox<T extends Enum<T>> extends AggregateListBox {

  public EnumListBox(T[] values, String tooltipText, String helpBalloonTxt) {
    super(tooltipText, false, helpBalloonTxt);

    if (values == null) {
      return;
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
