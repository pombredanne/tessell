package org.tessell.model.properties;

import static org.tessell.model.properties.NewProperty.booleanProperty;
import static org.tessell.model.properties.NewProperty.integerProperty;
import static org.tessell.model.properties.NewProperty.listProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.tessell.model.events.*;
import org.tessell.model.values.DerivedValue;
import org.tessell.model.values.Value;
import org.tessell.util.ListDiff;
import org.tessell.util.MapToList;

import com.google.gwt.event.shared.HandlerRegistration;

public class ListProperty<E> extends AbstractProperty<List<E>, ListProperty<E>> implements HasMemberChangedHandlers {

  private IntegerProperty size;
  private List<E> readOnly;
  private List<E> readOnlySource;

  /** Used to convert a list from one type of element to another. */
  public interface ElementConverter<E, F> {
    F to(E element);

    E from(F element);
  }

  /** Used to filter a list to a matching condition. */
  public interface ElementFilter<E> {
    boolean matches(E element);
  }

  @SuppressWarnings("unchecked")
  public ListProperty(final Value<? extends List<E>> value) {
    // the "? extends List<E>" is so we can be called with Value<ArrayList<E>>
    // types, which dtonator currently generates in the value inner classes
    super((Value<List<E>>) value);
  }

  @Override
  public List<E> get() {
    List<E> current = super.get();
    // change the wrapped list only when the source identity changes
    if (readOnly == null || current != readOnlySource) {
      readOnly = current == null ? null : Collections.unmodifiableList(current);
      readOnlySource = current;
    }
    return readOnly;
  }

  @Override
  public Property<Boolean> is(final List<E> value) {
    return is(value, new ArrayList<E>());
  }

  @Override
  public Property<Boolean> is(final Property<List<E>> other) {
    return is(other, new ArrayList<E>());
  }

  /** @return a copy of our list as an {@link ArrayList}, e.g. for GWT-RPC calls. */
  public ArrayList<E> toArrayList() {
    return new ArrayList<E>(getDirect());
  }

  /** Adds {@code item}, firing a {@link ValueAddedEvent}. */
  public void add(final E item) {
    getDirect().add(item);
    setTouched(true);
    listenForMemberChanged(item);
    // will fire add+change if needed
    reassess();
  }

  /** Adds each item in {@code items}, firing a {@link ValueAddedEvent} for each. */
  public void addAll(Collection<? extends E> items) {
    if (items.size() == 0) {
      return;
    }
    getDirect().addAll(items);
    setTouched(true);
    for (E item : items) {
      listenForMemberChanged(item);
    }
    // will fire adds+change if needed
    reassess();
  }

  /** Removes {@code item}, firing a {@link ValueRemovedEvent}. */
  public void remove(final E item) {
    getDirect().remove(item);
    setTouched(true);
    // will fire remove+change if needed
    reassess();
  }

  /** Removes each item in {@code items}, firing a {@link ValueRemovedEvent} for each. */
  public void removeAll(Collection<? extends E> items) {
    if (items.size() == 0) {
      return;
    }
    getDirect().removeAll(items);
    setTouched(true);
    // will fire adds+change if needed
    reassess();
  }

  /** Removes all entries, firing a {@link ValueRemovedEvent} for each. */
  public void clear() {
    getDirect().clear();
    // will fire removes+change if needed
    reassess();
  }

  /** @return a derived property of whether {@code item} is in this list. */
  public BooleanProperty contains(final E item) {
    return addDerived(booleanProperty(new Value<Boolean>() {
      @Override
      public Boolean get() {
        final List<E> current = ListProperty.this.get();
        return (current == null) ? false : current.contains(item);
      }

      @Override
      public void set(Boolean value) {
        final List<E> current = ListProperty.this.get();
        if (current != null) {
          if (Boolean.TRUE.equals(value) && !current.contains(item)) {
            add(item);
          } else if (!Boolean.TRUE.equals(value) && current.contains(item)) {
            remove(item);
          }
        }
      }

      @Override
      public String getName() {
        return "contains " + item;
      }

      @Override
      public boolean isReadOnly() {
        return false;
      }
    }));
  }

  /** @return a derived property that reflects this list's size. */
  public IntegerProperty size() {
    if (size == null) {
      size = addDerived(integerProperty(new DerivedValue<Integer>(getValueObject().getName() + "Size") {
        public Integer get() {
          final List<E> current = ListProperty.this.get();
          return (current == null) ? null : current.size();
        }
      }));
    }
    return size;
  }

  /** Registers {@code handler} to be called when new values are added. */
  public HandlerRegistration addValueAddedHandler(final ValueAddedHandler<E> handler) {
    return addHandler(ValueAddedEvent.getType(), handler);
  }

  /** Registers {@code handler} to be called when values are removed. */
  public HandlerRegistration addValueRemovedHandler(final ValueRemovedHandler<E> handler) {
    return addHandler(ValueRemovedEvent.getType(), handler);
  }

  /** Registers {@code handler} to be called when values changed. */
  public HandlerRegistration addMemberChangedHandler(final MemberChangedHandler handler) {
    return addHandler(MemberChangedEvent.getType(), handler);
  }

  /**
   * Creates a new {@link ListProperty>} of type {@code F}.
   *
   * Any changes made to either list will be reflected in the other,
   * using {@code converter} to go between {@code E} and {@code F}.
   */
  public <F> ListProperty<F> as(final ElementConverter<E, F> converter) {
    final MapToList<E, F> eToF = new MapToList<E, F>();
    final MapToList<F, E> fToE = new MapToList<F, E>();
    // make an intial copy of all the elements currently in our list
    List<F> initial = new ArrayList<F>();
    if (get() != null) {
      for (E e : get()) {
        F f = converter.to(e);
        eToF.add(e, f);
        fToE.add(f, e);
        initial.add(f);
      }
    }
    final ListProperty<F> as = listProperty(getName(), initial);
    final boolean[] active = { false };
    // keep converting E -> F into as
    addValueAddedHandler(new ValueAddedHandler<E>() {
      public void onValueAdded(ValueAddedEvent<E> event) {
        if (!active[0]) {
          active[0] = true;
          E e = event.getValue();
          F f = converter.to(e);
          eToF.add(e, f);
          fToE.add(f, e);
          as.add(f);
          active[0] = false;
        }
      }
    });
    // remove Fs as Es are removed
    addValueRemovedHandler(new ValueRemovedHandler<E>() {
      public void onValueRemoved(ValueRemovedEvent<E> event) {
        if (!active[0]) {
          active[0] = true;
          E e = event.getValue();
          F f = eToF.removeOne(e);
          fToE.removeOne(f);
          as.remove(f);
          active[0] = false;
        }
      }
    });
    // also convert new Fs back into Es
    as.addValueAddedHandler(new ValueAddedHandler<F>() {
      public void onValueAdded(ValueAddedEvent<F> event) {
        if (!active[0]) {
          active[0] = true;
          F f = event.getValue();
          E e = converter.from(f);
          fToE.add(f, e);
          eToF.add(e, f);
          add(e);
          active[0] = false;
        }
      }
    });
    // and remove Es as Fs are removed
    as.addValueRemovedHandler(new ValueRemovedHandler<F>() {
      public void onValueRemoved(ValueRemovedEvent<F> event) {
        if (!active[0]) {
          active[0] = true;
          F f = event.getValue();
          E e = fToE.removeOne(f);
          eToF.removeOne(e);
          remove(e);
          active[0] = false;
        }
      }
    });
    return as;
  }

  public ListProperty<E> filter(final ElementFilter<E> filter) {
    return listProperty(new DerivedValue<List<E>>(getValueObject().getName() + "Filtered") {
      public List<E> get() {
        List<E> filtered = new ArrayList<E>();
        if (ListProperty.this.get() != null) {
          for (E item : ListProperty.this.get()) {
            if (filter.matches(item)) {
              filtered.add(item);
            }
          }
        }
        return Collections.unmodifiableList(filtered);
      }
    });
  }

  @Override
  protected ListProperty<E> getThis() {
    return this;
  }

  @Override
  protected List<E> copyLastValue(List<E> newValue) {
    if (newValue == null) {
      return null;
    }
    return new ArrayList<E>(newValue);
  }

  @Override
  protected void fireChanged(List<E> oldValue, List<E> newValue) {
    ListDiff<E> diff = ListDiff.of(oldValue, newValue);
    for (E added : diff.added) {
      fireEvent(new ValueAddedEvent<E>(this, added));
    }
    for (E removed : diff.removed) {
      fireEvent(new ValueRemovedEvent<E>(this, removed));
    }
    super.fireChanged(oldValue, newValue);
  }

  private List<E> getDirect() {
    return super.get();
  }

  // Forwards member changed events on our models to our own model
  private void listenForMemberChanged(final E item) {
    if (item instanceof HasMemberChangedHandlers) {
      ((HasMemberChangedHandlers) item).addMemberChangedHandler(new MemberChangedHandler() {
        public void onMemberChanged(MemberChangedEvent event) {
          // in case the item was removed, we don't currently unsubscribe
          if (getDirect().contains(item)) {
            fireEvent(event);
          }
        }
      });
    }
  }

}
