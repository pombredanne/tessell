package org.tessell.tests.model.properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.tessell.model.properties.NewProperty.stringProperty;

import org.junit.Test;
import org.tessell.model.properties.PropertyGroup;
import org.tessell.model.properties.StringProperty;
import org.tessell.model.validation.Valid;
import org.tessell.tests.model.validation.rules.AbstractRuleTest;

public class PropertyGroupTest extends AbstractRuleTest {

  @Test
  public void touchesAllChildren() {
    PropertyGroup all = new PropertyGroup("all", "some message");
    StringProperty p1 = stringProperty("p1").in(all);
    StringProperty p2 = stringProperty("p1").in(all);
    assertThat(p1.isTouched(), is(false));
    assertThat(p2.isTouched(), is(false));

    all.touch();
    assertThat(p1.isTouched(), is(true));
    assertThat(p2.isTouched(), is(true));
  }

  @Test
  public void isInvalidIfChildIsInvalid() {
    PropertyGroup all = new PropertyGroup("all", "some message");
    StringProperty p1 = stringProperty("p1").in(all).max(5);

    p1.set("123456");
    assertThat(all.wasValid(), is(Valid.NO));
  }

  @Test
  public void becomesValidIfChildIsValid() {
    PropertyGroup all = new PropertyGroup("all", "some message");
    StringProperty p1 = stringProperty("p1").in(all).max(5);

    p1.set("123456");
    assertThat(all.wasValid(), is(Valid.NO));

    p1.set("1234");
    assertThat(all.wasValid(), is(Valid.YES));
  }

  @Test
  public void becomesValidOnRemovalOfInvalidChild() {
    PropertyGroup all = new PropertyGroup("all", "some message");
    StringProperty p1 = stringProperty("p1").in(all).max(5);

    p1.set("123456");
    assertThat(all.wasValid(), is(Valid.NO));

    all.remove(p1);
    assertThat(all.wasValid(), is(Valid.YES));

    // flipping p1 valid/invalid doesn't change all
    p1.set("1234");
    p1.set("123456");
    assertThat(all.wasValid(), is(Valid.YES));
  }

  @Test
  public void changesItsValueBasedOnBeingValid() {
    PropertyGroup all = new PropertyGroup("all", "some message");
    assertThat(all.get(), is(true));

    StringProperty p1 = stringProperty("p1").in(all).max(5);
    p1.set("123456");
    assertThat(all.get(), is(false));

    all.remove(p1);
    assertThat(all.get(), is(true));
  }

  @Test
  public void addingAlreadyInvalidPropertyMakesGroupInvalid() {
    PropertyGroup all = new PropertyGroup("all", "some message");

    StringProperty p1 = stringProperty("p1").max(5);
    p1.set("123456");

    all.add(p1);
    assertThat(all.wasValid(), is(Valid.NO));

    p1.set("1234");
    assertThat(all.wasValid(), is(Valid.YES));
  }

}
