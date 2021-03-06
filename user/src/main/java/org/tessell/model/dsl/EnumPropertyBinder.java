package org.tessell.model.dsl;

import java.util.Arrays;

import org.tessell.gwt.user.client.ui.IsListBox;
import org.tessell.model.events.PropertyChangedEvent;
import org.tessell.model.events.PropertyChangedHandler;
import org.tessell.model.properties.EnumProperty;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

/** Binds {@link EnumProperty}s to widgets. */
public class EnumPropertyBinder<E extends Enum<E>> extends PropertyBinder<E> {

  private final EnumProperty<E> ep;

  EnumPropertyBinder(final Binder b, final EnumProperty<E> ep) {
    super(b, ep);
    this.ep = ep;
  }

  public void to(final IsListBox source, final E[] values) {
    int i = 0;
    for (E value : values) {
      source.addItem(value.toString(), Integer.toString(i++));
    }
    if (ep.get() == null) {
      // TODO don't currently support an empty option
      ep.set(values[0]);
    }
    source.setSelectedIndex(Arrays.asList(values).indexOf(ep.get()));
    b.add(source.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        int i = source.getSelectedIndex();
        if (i == -1) {
          ep.set(null);
        } else {
          ep.set(values[i]);
        }
      }
    }));
    ep.addPropertyChangedHandler(new PropertyChangedHandler<E>() {
      public void onPropertyChanged(PropertyChangedEvent<E> event) {
        source.setSelectedIndex(Arrays.asList(values).indexOf(ep.get()));
      }
    });
  }

}
