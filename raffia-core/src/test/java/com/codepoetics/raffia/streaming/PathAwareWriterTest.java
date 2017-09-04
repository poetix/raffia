package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.predicates.BasketPredicates;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import org.junit.Test;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.junit.Assert.assertEquals;

public class PathAwareWriterTest {

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

  private static final Function2<Position, Token, Position> positionStateMachine = new Function2<Position, Token, Position>() {
    @Override
    public Position invoke(Position position, Token token) {
      return position.receive(token);
    }
  };

  private static final Function2<PositionTrackingState, Token, PositionTrackingState> positionTrackingStateMachine = new Function2<PositionTrackingState, Token, PositionTrackingState>() {
    @Override
    public PositionTrackingState invoke(PositionTrackingState positionTrackingState, Token token) {
      positionTrackingState.receive(token);
      return positionTrackingState;
    }
  };

  @Test
  public void positionTracking() {
    Filter<Position> writer = getWriter(positionStateMachine, new Position());

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
        positionTrackingStateMachine,
        PositionTrackingState.fromPath(emptyPath));

    assertState(PathBindingType.COMPLETE, writer);
    assertState(PathBindingType.COMPLETE, writer.beginArray());
    assertState(PathBindingType.COMPLETE, writer.add("value"));
    assertState(PathBindingType.COMPLETE, writer.end());
  }

  @Test
  public void conditionalPathIsConditional() {
    Path conditionalPath = lens("$[?]", BasketPredicates.isFalse()).getPath();

    Filter<PositionTrackingState> writer = getWriter(
        positionTrackingStateMachine,
        PositionTrackingState.fromPath(conditionalPath));

    assertState(PathBindingType.PARTIAL, writer);
    assertState(PathBindingType.CONDITIONAL, writer.beginArray());
    assertState(PathBindingType.CONDITIONAL, writer.add("value"));
    assertState(PathBindingType.CONDITIONAL, writer.end());
  }

  @Test
  public void partialMatch() {
    Path path = lens("$[1].bar").getPath();

    Filter<PositionTrackingState> writer = getWriter(
        positionTrackingStateMachine,
        PositionTrackingState.fromPath(path));

    assertState(PathBindingType.PARTIAL, writer);
    assertState(PathBindingType.DEVIATED, writer.beginArray());
    assertState(PathBindingType.DEVIATED, writer.beginObject());
    assertState(PathBindingType.PARTIAL, writer.end());
    assertState(PathBindingType.DEVIATED, writer.beginObject());
    assertState(PathBindingType.DEVIATED, writer.key("foo"));
    assertState(PathBindingType.DEVIATED, writer.add("value"));
    assertState(PathBindingType.COMPLETE, writer.key("bar"));
    assertState(PathBindingType.COMPLETE, writer.beginArray());
    assertState(PathBindingType.DEVIATED, writer.end());
    assertState(PathBindingType.DEVIATED, writer.key("baz"));
    assertState(PathBindingType.DEVIATED, writer.add("value"));
    assertState(PathBindingType.DEVIATED, writer.end());
    assertState(PathBindingType.DEVIATED, writer.end());
  }

  @Test
  public void nestedConditional() {
    Path path = lens("$.foo[?]", BasketPredicates.isArray()).getPath();

    Filter<PositionTrackingState> writer = getWriter(
        positionTrackingStateMachine,
        PositionTrackingState.fromPath(path));

    assertState(PathBindingType.PARTIAL, writer);
    assertState(PathBindingType.DEVIATED, writer.beginObject());
    assertState(PathBindingType.PARTIAL, writer.key("foo"));
    assertState(PathBindingType.CONDITIONAL, writer.beginArray());
  }

  @Test
  public void multipleIndices() {
    Path path = lens("$['foo', 'bar'][?]", BasketPredicates.isArray()).getPath();

    Filter<PositionTrackingState> writer = getWriter(
        positionTrackingStateMachine,
        PositionTrackingState.fromPath(path));

    assertState(PathBindingType.PARTIAL, writer);
    assertState(PathBindingType.DEVIATED, writer.beginObject());
    assertState(PathBindingType.PARTIAL, writer.key("foo"));
    assertState(PathBindingType.CONDITIONAL, writer.beginArray());
    assertState(PathBindingType.CONDITIONAL, writer.beginObject());
    assertState(PathBindingType.CONDITIONAL, writer.end());
    assertState(PathBindingType.DEVIATED, writer.end());
    assertState(PathBindingType.DEVIATED, writer.key("baz"));
    assertState(PathBindingType.DEVIATED, writer.add("value"));
    assertState(PathBindingType.PARTIAL, writer.key("bar"));
    assertState(PathBindingType.CONDITIONAL, writer.beginObject());
  }

  private void assertState(PathBindingType expectedType, Filter<PositionTrackingState> writer) {
    if (writer.getResult().getCurrent().getType() != expectedType) {
      System.out.println(writer.getResult().getPosition());
    }
    assertEquals(expectedType, writer.getResult().getCurrent().getType());
  }

}
