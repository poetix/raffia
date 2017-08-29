package com.codepoetics.raffia.operations

import com.codepoetics.raffia.functions.ValuePredicate
import com.codepoetics.raffia.functions.Mapper
import org.pcollections.PVector
import org.pcollections.TreePVector

import java.util.*

sealed class ProjectionResult<T> : Iterable<T> {

    abstract fun add(other: ProjectionResult<T>): ProjectionResult<T>
    abstract fun <O> map(mapper: Mapper<T, O>): ProjectionResult<O>
    abstract val single: T

    val isEmpty: Boolean
        get() = this is EmptyProjectionResult

    abstract val size: Int

    fun allMatch(matcher: ValuePredicate<T>): Boolean {
        for (item in this) {
            if (!matcher.test(item)) {
                return false
            }
        }
        return true
    }

    fun asList(): List<T> = toList()

    override fun equals(other: Any?): Boolean {
        return this === other || other is ProjectionResult<*> && equalsResult(other)
    }

    private fun equalsResult(other: ProjectionResult<*>): Boolean {
        val left = iterator()
        val right = other.iterator()

        while (left.hasNext()) {
            if (!right.hasNext()) {
                return false
            }
            if (right.next() != left.next()) {
                return false
            }
        }

        if (right.hasNext()) {
            return false
        }

        return true
    }

    override fun hashCode(): Int = fold(0) { hash, item -> Objects.hash(hash, item) }

    override fun toString(): String {
        return toList().toString()
    }

    object EmptyProjectionResult : ProjectionResult<Any>() {
        override val size: Int get() = 0

        override fun iterator(): Iterator<Any> = Collections.emptyListIterator<Any>()

        override fun add(other: ProjectionResult<Any>): ProjectionResult<Any> = other

        override fun <O> map(mapper: Mapper<Any, O>): ProjectionResult<O> = empty()

        override val single: Any
            get() = throw NoSuchElementException("Empty projection result contains no values")
    }

    private class SingletonProjectionResult<T>(override val single: T) : ProjectionResult<T>() {
        override val size: Int get() = 1

        override fun iterator(): Iterator<T> = object : Iterator<T> {
            private var hasReturnedValue = false
            override fun hasNext(): Boolean {
                return !hasReturnedValue
            }

            override fun next(): T {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                hasReturnedValue = true
                return single
            }
        }

        override fun add(other: ProjectionResult<T>): ProjectionResult<T> =
            when(other) {
                is EmptyProjectionResult -> this
                is SingletonProjectionResult -> MultipleProjectionResult(TreePVector.singleton(single).plus(other.single))
                is MultipleProjectionResult -> other.prepend(single)
                is NestedProjectionResult -> other.prepend(this)
            }

        override fun <O> map(mapper: Mapper<T, O>): ProjectionResult<O> = SingletonProjectionResult(mapper.map(single))
    }

    private class MultipleProjectionResult<T> internal constructor(private val values: PVector<T>) : ProjectionResult<T>() {

        internal fun prepend(value: T): MultipleProjectionResult<T> = MultipleProjectionResult(values.plus(0, value))

        override fun add(other: ProjectionResult<T>): ProjectionResult<T> =
            when(other) {
                is EmptyProjectionResult -> this
                is SingletonProjectionResult -> MultipleProjectionResult(values.plus(other.single))
                is MultipleProjectionResult -> NestedProjectionResult(
                        TreePVector.singleton<ProjectionResult<T>>(this).plus(other),
                        this.size + other.size)
                is NestedProjectionResult -> other.prepend(this)
            }

        override fun <O> map(mapper: Mapper<T, O>): ProjectionResult<O> =
                MultipleProjectionResult(TreePVector.from(map(mapper::map)))

        override val single: T
            get() = throw IllegalStateException("getSingle() called, but multiple values available: " + values)

        override val size: Int get() = values.size

        override fun iterator(): Iterator<T> = values.iterator()
    }

    private class NestedProjectionResult<T> internal constructor(private val results: PVector<ProjectionResult<T>>, override val size: Int) : ProjectionResult<T>() {

        internal fun prepend(other: ProjectionResult<T>): NestedProjectionResult<T> {
            return NestedProjectionResult(results.plus(0, other), size + other.size)
        }

        private fun PVector<ProjectionResult<T>>.appendToLast(other: ProjectionResult<T>): PVector<ProjectionResult<T>> =
            with(size - 1, this[size - 1].add(other))

        override fun add(other: ProjectionResult<T>): ProjectionResult<T> =
            when(other) {
                is EmptyProjectionResult -> this
                is SingletonProjectionResult -> NestedProjectionResult(results.appendToLast(other), size + 1)
                is MultipleProjectionResult -> NestedProjectionResult(results.plus(other), size + other.size)
                is NestedProjectionResult -> NestedProjectionResult(results.plus(other), size + other.size)
            }

        override fun <O> map(mapper: Mapper<T, O>): ProjectionResult<O> {
            val mapped = ArrayList<O>()
            for (value in this) {
                mapped.add(mapper.map(value))
            }
            return MultipleProjectionResult(TreePVector.from(mapped))
        }

        override val single: T
            get() = throw IllegalStateException("getSingle() called, but multiple values available: ")

        override fun iterator(): Iterator<T> {
            val iterator = results.iterator()

            return object : Iterator<T> {
                private var current = iterator.next().iterator()

                override fun hasNext(): Boolean {
                    if (current.hasNext()) {
                        return true
                    }
                    if (!iterator.hasNext()) {
                        return false
                    }
                    current = iterator.next().iterator()
                    return current.hasNext()
                }

                override fun next(): T {
                    if (!hasNext()) {
                        throw NoSuchElementException()
                    }
                    return current.next()
                }
            }
        }
    }

    companion object {

        @JvmStatic
        fun <T> empty(): ProjectionResult<T> {
            return EmptyProjectionResult as ProjectionResult<T>
        }

        @JvmStatic
        fun <T> ofSingle(value: T): ProjectionResult<T> {
            return SingletonProjectionResult(value)
        }
    }
}
