/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.exception;

/**
 * A shape was constructed but failed because, based on the given parts, it's invalid. For example
 * a rectangle's minimum Y was specified as greater than the maximum Y. This class is not used for
 * parsing exceptions; that's usually {@link java.text.ParseException}.
 */
public class InvalidShapeException extends RuntimeException {

  public InvalidShapeException(String reason, Throwable cause) {
    super(reason, cause);
  }

  public InvalidShapeException(String reason) {
    super(reason);
  }
}
