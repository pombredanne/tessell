package org.tessell.widgets;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.tessell.gwt.dom.client.StubClickEvent;
import org.tessell.gwt.user.client.ui.IsWidget;
import org.tessell.gwt.user.client.ui.StubFlowPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class StubWidgetTest {

  @Test
  public void testFindByIdWithChildren() {
    StubFlowPanel parent = new StubFlowPanel();
    StubFlowPanel child = new StubFlowPanel();
    child.ensureDebugId("child");
    parent.add(child);
    assertThat(parent.findById("child"), is(sameInstance((IsWidget) child)));
  }

  @Test
  public void testFindByIdWithGrandchildren() {
    StubFlowPanel parent = new StubFlowPanel();
    StubFlowPanel child = new StubFlowPanel();
    StubFlowPanel grandchild = new StubFlowPanel();
    grandchild.ensureDebugId("grandchild");
    parent.add(child);
    child.add(grandchild);
    assertThat(parent.findById("grandchild"), is(sameInstance((IsWidget) grandchild)));
  }

  @Test
  public void testFindByIdWithCompositeIsWidget() {
    StubFlowPanel parent = new StubFlowPanel();

    StubFlowPanel uiXmlContent = new StubFlowPanel();
    uiXmlContent.ensureDebugId("uiXmlContent");

    CompositeIsWidget c = new CompositeIsWidget();
    c.setWidget(uiXmlContent);

    parent.add(c);
    assertThat(parent.findById("uiXmlContent"), is(sameInstance((IsWidget) uiXmlContent)));
  }

  @Test
  public void testDomHandler() {
    StubWidget w = new StubWidget();

    final boolean[] clicked = { false };
    w.addDomHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        clicked[0] = true;
      }
    }, ClickEvent.getType());

    w.fireEvent(new StubClickEvent());
    assertThat(clicked[0], is(true));
  }
}
