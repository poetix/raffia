package com.codepoetics.raffia.operations;

import com.codepoetics.raffia.mappers.Mapper;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.*;

public abstract class ProjectionResult<T> implements Iterable<T> {

  public static <T> ProjectionResult<T> empty() {
    return (ProjectionResult<T>) EmptyProjectionResult.EMPTY;
  }

  public static <T> ProjectionResult<T> ofSingle(T value) {
    return new SingletonProjectionResult<>(value);
  }

  public abstract ProjectionResult<T> add(ProjectionResult<T> result);
  public abstract List<T> toList();
  public abstract <O> ProjectionResult<O> map(Mapper<T, O> mapper);
  public abstract T getSingle();

  public boolean allMatch(ValuePredicate<T> matcher) {
    for (T item : this) {
      if (!matcher.test(item)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof ProjectionResult && equals(ProjectionResult.class.cast(other)));
  }

  private boolean equals(ProjectionResult<?> other) {
    Iterator<T> left = iterator();
    Iterator<?> right = other.iterator();

    while (left.hasNext()) {
      if (!right.hasNext()) {
        return false;
      }
      if (!right.next().equals(left.next())) {
        return false;
      }
    }

    if (right.hasNext()) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    for (T item : this) {
      hashCode = Objects.hash(hashCode, item.hashCode());
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return toList().toString();
  }

  private interface ResultVisitor<T> {
    ProjectionResult<T> visitEmpty();
    ProjectionResult<T> visitSingleton(T value);
    ProjectionResult<T> visitMultiple(PVector<T> values);
    ProjectionResult<T> visitNested(PVector<ProjectionResult<T>> results);
  }

  protected abstract ProjectionResult<T> visit(ResultVisitor<T> visitor);

  private static final class EmptyProjectionResult<T> extends ProjectionResult<T> {

    private static final EmptyProjectionResult<?> EMPTY = new EmptyProjectionResult<>();

    @Override
    public Iterator<T> iterator() {
      return Collections.emptyListIterator();
    }

    @Override
    public ProjectionResult<T> add(ProjectionResult<T> result) {
      return result;
    }

    @Override
    public List<T> toList() {
      return Collections.emptyList();
    }

    @Override
    public <O> ProjectionResult<O> map(Mapper<T, O> mapper) {
      return empty();
    }

    @Override
    public T getSingle() {
      throw new NoSuchElementException("Empty projection result contains no values");
    }

    @Override
    protected ProjectionResult<T> visit(ResultVisitor<T> visitor) {
      return visitor.visitEmpty();
    }
  }

  private static final class SingletonProjectionResult<T> extends ProjectionResult<T> {

    private final T value;

    private SingletonProjectionResult(T value) {
      this.value = value;
    }

    @Override
    public Iterator<T> iterator() {
      return new Iterator<T>() {
        private boolean hasReturnedValue = false;
        @Override
        public boolean hasNext() {
          return !hasReturnedValue;
        }

        @Override
        public T next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          hasReturnedValue = true;
          return value;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public ProjectionResult<T> add(ProjectionResult<T> result) {
      return result.visit(new ResultVisitor<T>() {
        @Override
        public ProjectionResult<T> visitEmpty() {
          return SingletonProjectionResult.this;
        }

        @Override
        public ProjectionResult<T> visitSingleton(T value) {
          return new MultipleProjectionResult<>(TreePVector.singleton(SingletonProjectionResult.this.value).plus(value));
        }

        @Override
        public ProjectionResult<T> visitMultiple(PVector<T> values) {
          return new MultipleProjectionResult<>(values.plus(0, SingletonProjectionResult.this.value));
        }

        @Override
        public ProjectionResult<T> visitNested(PVector<ProjectionResult<T>> projectionResults) {
          return new NestedProjectionResult<>(projectionResults.plus(0, SingletonProjectionResult.this));
        }
      });
    }

    @Override
    public List<T> toList() {
      return Collections.singletonList(value);
    }

    @Override
    public <O> ProjectionResult<O> map(Mapper<T, O> mapper) {
      return new SingletonProjectionResult<>(mapper.map(value));
    }

    @Override
    public T getSingle() {
      return value;
    }

    @Override
    protected ProjectionResult<T> visit(ResultVisitor visitor) {
      return visitor.visitSingleton(value);
    }
  }

  private static final class MultipleProjectionResult<T> extends ProjectionResult<T> {

    private final PVector<T> values;

    private MultipleProjectionResult(PVector<T> values) {
      this.values = values;
    }

    @Override
    public ProjectionResult<T> add(ProjectionResult<T> result) {
      return result.visit(new ResultVisitor<T>() {
        @Override
        public ProjectionResult<T> visitEmpty() {
          return MultipleProjectionResult.this;
        }

        @Override
        public ProjectionResult<T> visitSingleton(T value) {
          return new MultipleProjectionResult<>(values.plus(value));
        }

        @Override
        public ProjectionResult<T> visitMultiple(PVector<T> values) {
          return new NestedProjectionResult<>(TreePVector.<ProjectionResult<T>>singleton(MultipleProjectionResult.this)
              .plus(new MultipleProjectionResult<>(values)));
        }

        @Override
        public ProjectionResult<T> visitNested(PVector<ProjectionResult<T>> projectionResults) {
          return new NestedProjectionResult<>(projectionResults.plus(0, MultipleProjectionResult.this));
        }
      });
    }

    @Override
    public List<T> toList() {
      return values;
    }

    @Override
    public <O> ProjectionResult<O> map(Mapper<T, O> mapper) {
      List<O> mapped = new ArrayList<>(values.size());
      for (T value : values) {
        mapped.add(mapper.map(value));
      }
      return new MultipleProjectionResult<>(TreePVector.from(mapped));
    }

    @Override
    public T getSingle() {
      throw new IllegalStateException("getSingle() called, but multiple values available: " + values);
    }

    @Override
    protected ProjectionResult<T> visit(ResultVisitor<T> visitor) {
      return visitor.visitMultiple(values);
    }

    @Override
    public Iterator<T> iterator() {
      return values.iterator();
    }
  }

  private static final class NestedProjectionResult<T> extends ProjectionResult<T> {
    private final PVector<ProjectionResult<T>> results;

    private NestedProjectionResult(PVector<ProjectionResult<T>> results) {
      this.results = results;
    }

    public ProjectionResult<T> add(ProjectionResult<T> result) {
      return new NestedProjectionResult<>(results.plus(result));
    }

    @Override
    public List<T> toList() {
      List<T> result = new ArrayList<>();
      for (T value : this) {
        result.add(value);
      }
      return result;
    }

    @Override
    public <O> ProjectionResult<O> map(Mapper<T, O> mapper) {
      List<O> mapped = new ArrayList<>();
      for (T value : this) {
        mapped.add(mapper.map(value));
      }
      return new MultipleProjectionResult<>(TreePVector.from(mapped));
    }

    @Override
    public T getSingle() {
      throw new IllegalStateException("getSingle() called, but multiple values available: ");
    }

    @Override
    protected ProjectionResult<T> visit(ResultVisitor<T> visitor) {
      return visitor.visitNested(results);
    }

    @Override
    public Iterator<T> iterator() {
      final Iterator<ProjectionResult<T>> iterator = results.iterator();

      if (!iterator.hasNext()) {
        return Collections.emptyListIterator();
      }

      return new Iterator<T>() {

        private Iterator<T> current = iterator.next().iterator();

        @Override
        public boolean hasNext() {
          if (current.hasNext()) {
            return true;
          }
          if (!iterator.hasNext()) {
            return false;
          }
          current = iterator.next().iterator();
          return current.hasNext();
        }

        @Override
        public T next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          return current.next();
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }
}
