package org.gwtmpv.widgets.form.lines;

import static org.gwtmpv.widgets.form.lines.NewFormLine.newListBoxFormLine;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;

import org.gwtmpv.model.dsl.ListBoxAdaptor;
import org.gwtmpv.widgets.StubListBox;
import org.gwtmpv.widgets.form.AbstractFormPresenterTest;
import org.gwtmpv.widgets.form.EmployeeModel;
import org.gwtmpv.widgets.form.EmployerDto;
import org.junit.Test;

public class ListBoxFormLineTest extends AbstractFormPresenterTest {

  final EmployeeModel employee = new EmployeeModel();
  final ArrayList<EmployerDto> employers = new ArrayList<EmployerDto>();
  {
    employers.add(new EmployerDto(1, "Employer One"));
    employers.add(new EmployerDto(2, "Employer Two"));
  }

  @Test
  public void htmlOfOneListBox() {
    p.add(newListBoxFormLine(employee.employerId, employers, new EmployerDtoAdaptor()));
    assertHtml("<div class=\"form\">",//
      "<div class=\"lines\"><ol>",
      "<li>",
      "<div class=\"label\"><label for=\"p-employerId\">Employer Id</label></div>",
      "<div class=\"value\"><div id=\"mpv-hb-1\"></div><div class=\"errors\"><div id=\"mpv-hb-2\"></div></div></div>",
      "</li>",
      "</ol></div>",
      "</div>");
  }

  @Test
  public void listBoxIsBound() {
    p.add(newListBoxFormLine(employee.employerId, employers, new EmployerDtoAdaptor()));
    assertThat(listBox("p-employerId").getItemCount(), is(2));
    assertThat(listBox("p-employerId").getItems(), contains("Employer One", "Employer Two"));
  }

  @Test
  public void listBoxIsBoundCurrentlyAssignsFirstvalue() {
    assertThat(employee.employerId.get(), is(nullValue()));
    p.add(newListBoxFormLine(employee.employerId, employers, new EmployerDtoAdaptor()));
    assertThat(employee.employerId.get(), is(1));
  }

  private StubListBox listBox(String id) {
    return (StubListBox) html().getElementById(id);
  }

  private static final class EmployerDtoAdaptor implements ListBoxAdaptor<Integer, EmployerDto> {
    public String toDisplay(EmployerDto option) {
      return option.name;
    }

    public Integer toValue(EmployerDto option) {
      return option.id;
    }
  }

}
