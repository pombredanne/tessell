package org.tessell.model.values;

import org.bindgen.Bindable;
import org.bindgen.Binding;
import org.tessell.model.properties.Property;

/**
 * Wraps a {@link Binding} as a {@link Value}.
 * 
 * This is for making {@link Property}s around a DTO that is annotated with {@link Bindable}.
 */
public class BoundValue<P> implements Value<P> {

  private final Binding<P> binding;

  public BoundValue(final Binding<P> binding) {
    this.binding = binding;
  }

  @Override
  public P get() {
    return binding.getSafely();
  }

  @Override
  public void set(final P value) {
    if (binding.getIsSafe()) {
      binding.set(value);
    }
  }

  @Override
  public String getName() {
    return binding.getName();
  }

  @Override
  public String toString() {
    return getName() + " " + get();
  }

  @Override
  public boolean isReadOnly() {
    return binding.getBindingIsReadOnly();
  }

}
