package com.mochaeng.theia_api.shared;

import io.vavr.control.Either;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Result<T, E> {

    private final Either<E, T> either;

    private Result(Either<E, T> either) {
        this.either = either;
    }

    public static <T, E> Result<T, E> ok(T value) {
        return new Result<>(Either.right(value));
    }

    public static <T, E> Result<T, E> error(E error) {
        return new Result<>(Either.left(error));
    }

    public boolean isOk() {
        return either.isRight();
    }

    public boolean isError() {
        return either.isLeft();
    }

    public T unwrap() {
        return either.get();
    }

    public T unwrapOr(T defaultValue) {
        return either.getOrElse(defaultValue);
    }

    public E unwrapErr() {
        return either.getLeft();
    }

    public <U> Result<U, E> map(Function<T, U> mapper) {
        return new Result<>(either.map(mapper));
    }

    public <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        if (either.isLeft()) {
            return new Result<>(Either.left(either.getLeft()));
        }
        return mapper.apply(either.get());
    }

    public <F> Result<T, F> mapErr(Function<E, F> mapper) {
        return new Result<>(either.mapLeft(mapper));
    }

    public <U> U match(Function<E, U> onError, Function<T, U> onSuccess) {
        return either.fold(onError, onSuccess);
    }

    public Result<T, E> inspect(Function<T, Void> inspector) {
        if (either.isRight()) {
            inspector.apply(either.get());
        }
        return this;
    }

    public Result<T, E> inspectErr(Function<E, Void> inspector) {
        if (either.isLeft()) {
            inspector.apply(either.getLeft());
        }
        return this;
    }
}
