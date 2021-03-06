package org.tessell.model;

import static org.tessell.model.properties.NewProperty.booleanProperty;

import org.tessell.model.events.*;
import org.tessell.model.properties.BooleanProperty;
import org.tessell.model.properties.Property;
import org.tessell.model.properties.PropertyGroup;
import org.tessell.model.values.DerivedValue;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimplerEventBus;

/** A base class for models. Provides a {@link PropertyGroup} for all of the properties. */
public abstract class AbstractModel implements Model {

  protected final PropertyGroup all = new PropertyGroup("all", "model invalid");
  private BooleanProperty isTouched;
  private final EventBus handlers = new SimplerEventBus();

  @Override
  public Property<Boolean> allValid() {
    return all;
  }

  @Override
  public Property<Boolean> isTouched() {
    if (isTouched == null) {
      isTouched = booleanProperty(new DerivedValue<Boolean>("isTouched") {
        public Boolean get() {
          for (Property<?> property : all.getProperties()) {
            if (property.isTouched()) {
              return true;
            }
          }
          return false;
        }
      });
    }
    return isTouched;
  }

  @Override
  public HandlerRegistration addMemberChangedHandler(MemberChangedHandler handler) {
    return handlers.addHandler(MemberChangedEvent.getType(), handler);
  }

  /** Adds {@code p} to the property group. */
  protected <P extends Property<U>, U> P add(P p) {
    p.addPropertyChangedHandler(new PropertyChangedHandler<U>() {
      public void onPropertyChanged(PropertyChangedEvent<U> event) {
        fireEvent(new MemberChangedEvent());
      }
    });
    if (p instanceof HasMemberChangedHandlers) {
      // forward on member changes up the tree, e.g. from ListProperties
      ((HasMemberChangedHandlers) p).addMemberChangedHandler(new MemberChangedHandler() {
        public void onMemberChanged(MemberChangedEvent event) {
          fireEvent(event);
        }
      });
    }
    all.add(p);
    return p;
  }

  protected void fireEvent(GwtEvent<?> event) {
    handlers.fireEvent(event);
  }
}
