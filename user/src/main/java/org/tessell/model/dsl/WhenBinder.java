package org.tessell.model.dsl;

import static org.tessell.util.ObjectUtils.eq;

import org.tessell.model.properties.Property;

/** Does various things as the boolean property changes from true/false. */
public class WhenBinder<P> {

  private final Binder b;
  private final Property<P> property;

  public WhenBinder(final Binder b, final Property<P> property) {
    this.b = b;
    this.property = property;
  }

  public WhenIsBinder<P> is(final P value) {
    return new WhenIsBinder<P>(b, property, new WhenCondition<P>() {
      public boolean evaluate(Property<P> property) {
        return eq(property.get(), value);
      }

      public void setInitialValue(Property<P> property) {
        property.setInitialValue(value);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public WhenIsBinder<P> is(WhenCondition<? super P> condition) {
    // any condition written for a superclass of P is fine
    return new WhenIsBinder<P>(b, property, (WhenCondition<P>) condition);
  }

}
