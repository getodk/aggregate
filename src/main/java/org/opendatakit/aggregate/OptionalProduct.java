/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate;

import java.util.Optional;
import java.util.function.BiFunction;

public interface OptionalProduct {

  /**
   * Factory of {@link OptionalProduct} for an arity of 3
   */
  static <T, U, V> OptionalProduct3<T, U, V> all(Optional<T> t, Optional<U> u, Optional<V> v) {
    if (t.isPresent() && u.isPresent() && v.isPresent())
      return new OptionalProduct3.Some<>(t.get(), u.get(), v.get());
    return new OptionalProduct3.None<>();
  }

  /**
   * Factory of {@link OptionalProduct} for an arity of 2
   */
  static <T, U> OptionalProduct2<T, U> all(Optional<T> t, Optional<U> u) {
    if (t.isPresent() && u.isPresent())
      return new OptionalProduct2.Some<>(t.get(), u.get());
    return new OptionalProduct2.None<>();
  }

  interface OptionalProduct2<T, U> {
    <V> Optional<V> map(BiFunction<T, U, V> mapper);

    boolean isPresent();

    class Some<T, U> implements OptionalProduct2<T, U> {
      private final T t;
      private final U u;

      Some(T t, U u) {
        this.t = t;
        this.u = u;
      }

      @Override
      public <V> Optional<V> map(BiFunction<T, U, V> mapper) {
        return Optional.of(mapper.apply(t, u));
      }

      @Override
      public boolean isPresent() {
        return true;
      }
    }

    class None<T, U> implements OptionalProduct2<T, U> {

      @Override
      public <V> Optional<V> map(BiFunction<T, U, V> mapper) {
        return Optional.empty();
      }

      @Override
      public boolean isPresent() {
        return false;
      }
    }
  }

  interface OptionalProduct3<T, U, V> {
    <W> Optional<W> map(TriFunction<T, U, V, W> mapper);

    class Some<T, U, V> implements OptionalProduct3<T, U, V> {
      private final T t;
      private final U u;
      private final V v;

      Some(T t, U u, V v) {
        this.t = t;
        this.u = u;
        this.v = v;
      }

      @Override
      public <W> Optional<W> map(TriFunction<T, U, V, W> mapper) {
        return Optional.of(mapper.apply(t, u, v));
      }

    }

    class None<T, U, V> implements OptionalProduct3<T, U, V> {

      @Override
      public <W> Optional<W> map(TriFunction<T, U, V, W> mapper) {
        return Optional.empty();
      }

    }
  }

}
