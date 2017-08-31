package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.paths.Paths;
import com.codepoetics.raffia.predicates.BasketPredicates;
import javafx.geometry.Pos;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.reflect.KFunction;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

public class PathAwareWriterTest {

  private static final Class<? extends PathBindingState> partial = PathBindingState.Partial.class;
  private static final Class<? extends PathBindingState> deviated = PathBindingState.Deviated.class;
  private static final Class<? extends PathBindingState> complete = PathBindingState.Complete.class;
  private static final Class<? extends PathBindingState> conditional = PathBindingState.Conditional.class;

  private static <T> Function1<T, T> id() {
    return new Function1<T, T>() {
      @Override
      public T invoke(T value) {
        return value;
      }
    };
  }

  private <T> Filter<T> getWriter(Function2<T, Token, T> stateMachine, T state) {
    Function1<T, T> id = id();
    return new InterpretingWriter<>(stateMachine, state, id);
  }

  @Test
  public void positionTracking() {
    Filter<Position> writer = getWriter(PathAwareWriterKt.getPositionStateMachine(), Position.getEmpty());

    assertPosition("", writer);
    assertPosition("[0]", writer.beginArray());
    assertPosition("[0].?", writer.beginObject());
    assertPosition("[0].foo", writer.key("foo"));
    assertPosition("[0].foo[0]", writer.beginArray());
    assertPosition("[0].foo[1]", writer.add("value"));
    assertPosition("[0].?", writer.end());
    assertPosition("[0].bar", writer.key("bar"));
    assertPosition("[0].?", writer.add("value"));
    assertPosition("[1]", writer.end());
    assertPosition("", writer.end());
  }

  private void assertPosition(String expected, Filter<Position> writer) {
    assertEquals(expected, writer.getResult().toString());
  }

  @Test
  public void emptyPathIsComplete() {
    Path emptyPath = lens("$").getPath();

    Filter<PositionTrackingState> writer = getWriter(
        PathAwareWriterKt.getPositionTrackingStateMachine(),
        PositionTrackingState.fromPath(emptyPath));

    assertState(PathBindingState.Complete.class, writer);
    assertState(PathBindingState.Complete.class, writer.beginArray());
    assertState(PathBindingState.Complete.class, writer.add("value"));
    assertState(PathBindingState.Complete.class, writer.end());
  }

  @Test
  public void conditionalPathIsConditional() {
    Path conditionalPath = lens("$[?]", BasketPredicates.isFalse()).getPath();

    Filter<PositionTrackingState> writer = getWriter(
        PathAwareWriterKt.getPositionTrackingStateMachine(),
        PositionTrackingState.fromPath(conditionalPath));

    assertState(partial, writer);
    assertState(conditional, writer.beginArray());
    assertState(conditional, writer.add("value"));
    assertState(conditional, writer.end());
  }

  @Test
  public void partialMatch() {
    Path path = lens("$[1].bar").getPath();

    Filter<PositionTrackingState> writer = getWriter(
        PathAwareWriterKt.getPositionTrackingStateMachine(),
        PositionTrackingState.fromPath(path));

    assertState(partial, writer);
    assertState(deviated, writer.beginArray());
    assertState(deviated, writer.beginObject());
    assertState(partial, writer.end());
    assertState(deviated, writer.beginObject());
    assertState(deviated, writer.key("foo"));
    assertState(deviated, writer.add("value"));
    assertState(complete, writer.key("bar"));
    assertState(complete, writer.beginArray());
    assertState(deviated, writer.end());
    assertState(deviated, writer.key("baz"));
    assertState(deviated, writer.add("value"));
    assertState(deviated, writer.end());
    assertState(deviated, writer.end());
  }

  @Test
  public void nestedConditional() {
    Path path = lens("$.foo[?]", BasketPredicates.isArray()).getPath();

    Filter<PositionTrackingState> writer = getWriter(
        PathAwareWriterKt.getPositionTrackingStateMachine(),
        PositionTrackingState.fromPath(path));

    assertState(partial, writer);
    assertState(deviated, writer.beginObject());
    assertState(partial, writer.key("foo"));
    assertState(conditional, writer.beginArray());
  }

  @Test
  public void multipleIndices() {
    Path path = lens("$['foo', 'bar'][?]", BasketPredicates.isArray()).getPath();

    Filter<PositionTrackingState> writer = getWriter(
        PathAwareWriterKt.getPositionTrackingStateMachine(),
        PositionTrackingState.fromPath(path));

    assertState(partial, writer);
    assertState(deviated, writer.beginObject());
    assertState(partial, writer.key("foo"));
    assertState(conditional, writer.beginArray());
    assertState(conditional, writer.beginObject());
    assertState(conditional, writer.end());
    assertState(deviated, writer.end());
    assertState(deviated, writer.key("baz"));
    assertState(deviated, writer.add("value"));
    assertState(partial, writer.key("bar"));
    assertState(conditional, writer.beginObject());
  }

  private void assertState(Class<? extends PathBindingState> stateClass, Filter<PositionTrackingState> writer) {
    if (!stateClass.isInstance(writer.getResult().getPathBindingState())) {
      System.out.println(writer.getResult().getPosition());
    }
    assertEquals(stateClass.getSimpleName(), writer.getResult().getPathBindingState().getClass().getSimpleName());
  }

}
