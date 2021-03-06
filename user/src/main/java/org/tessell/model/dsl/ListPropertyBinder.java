package org.tessell.model.dsl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tessell.gwt.user.client.ui.IsPanel;
import org.tessell.gwt.user.client.ui.IsWidget;
import org.tessell.model.events.ValueAddedEvent;
import org.tessell.model.events.ValueAddedHandler;
import org.tessell.model.events.ValueRemovedEvent;
import org.tessell.model.events.ValueRemovedHandler;
import org.tessell.model.properties.ListProperty;
import org.tessell.presenter.BasicPresenter;
import org.tessell.presenter.Presenter;

/** Fluent binding methods for {@link ListProperty}s. */
public class ListPropertyBinder<P> extends PropertyBinder<List<P>> {

  /** Factory for creating {@link IsWidget}s for each item in a list. */
  public interface ListViewFactory<P> {
    IsWidget create(P value);
  }

  /** Factory for creating {@link Presenter}s for each item in a list. */
  public interface ListPresenterFactory<P> {
    Presenter create(P value);
  }

  private final ListProperty<P> p;

  public ListPropertyBinder(final Binder b, final ListProperty<P> p) {
    super(b, p);
    this.p = p;
  }

  /** Binds each value in {@code p} to a view created by {@code factory}. */
  public void to(final IsPanel panel, final ListViewFactory<P> factory) {
    // map to remember the model->view mapping so we know which view to remove later
    final Map<P, IsWidget> views = new HashMap<P, IsWidget>();
    for (P value : p.get()) {
      IsWidget view = factory.create(value);
      views.put(value, view);
      panel.add(view);
    }
    b.add(p.addValueAddedHandler(new ValueAddedHandler<P>() {
      public void onValueAdded(ValueAddedEvent<P> event) {
        IsWidget view = factory.create(event.getValue());
        views.put(event.getValue(), view);
        panel.add(view);
      }
    }));
    b.add(p.addValueRemovedHandler(new ValueRemovedHandler<P>() {
      public void onValueRemoved(ValueRemovedEvent<P> event) {
        IsWidget view = views.remove(event.getValue());
        if (view != null) {
          panel.remove(view);
        }
      }
    }));
  }

  /** Binds each value in {@code p} to a presenter created by {@code factory}.
   *
   * Also adds/removes the child presenters to the {@code parent} presenter for proper binding/unbinding.
   */
  public void to(final BasicPresenter<?> parent, final IsPanel panel, final ListPresenterFactory<P> factory) {
    // map to remember the model->presenter mapping so we know which view to remove later
    final Map<P, Presenter> views = new HashMap<P, Presenter>();
    for (P value : p.get()) {
      Presenter child = factory.create(value);
      parent.addPresenter(child);
      views.put(value, child);
      panel.add(child.getView());
    }
    b.add(p.addValueAddedHandler(new ValueAddedHandler<P>() {
      public void onValueAdded(ValueAddedEvent<P> event) {
        Presenter child = factory.create(event.getValue());
        parent.addPresenter(child);
        views.put(event.getValue(), child);
        panel.add(child.getView());
      }
    }));
    b.add(p.addValueRemovedHandler(new ValueRemovedHandler<P>() {
      public void onValueRemoved(ValueRemovedEvent<P> event) {
        Presenter child = views.remove(event.getValue());
        if (child != null) {
          panel.remove(child.getView());
          parent.removePresenter(child);
        }
      }
    }));
  }

}
