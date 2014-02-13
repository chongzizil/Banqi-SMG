package org.banqi.client;

import java.util.Objects;

/**
 * Copy from Cheat.
 */

/**
 * An instance of this class must have an immutable ID.
 * That ID is used to define equals and hashCode.
 *
 * Always override equals and hashCode together. Always!
 * Read why here: http://www.javamex.com/tutorials/collections/hash_code_equals.shtml
 * <br>
 * To help you override them correctly,
 * use {@link com.google.common.base.Objects#equal(Object, Object)} and
 * {@link com.google.common.base.Objects#hashCode(Object...)}.
 * <br>
 * See: http://code.google.com/p/guava-libraries/wiki/CommonObjectUtilitiesExplained
 * <br>
 * Overriding equality and hashCode is useful for testing.
 *
 * @author yzibin@google.com (Yoav Zibin)
 */

public abstract class Equality {

  public abstract Object getId();

  @Override
  public final boolean equals(Object other) {
    if (!(other instanceof Equality)) {
      return false;
    }
    return Objects.equals(getId(), ((Equality) other).getId());
  }

  @Override
  public final int hashCode() {
    return getId().hashCode();
  }
}