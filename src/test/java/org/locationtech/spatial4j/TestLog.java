/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j;

import com.carrotsearch.randomizedtesting.rules.TestRuleAdapter;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility logger for tests in which log statements are logged following
 * test failure only.  Add this to a JUnit based test class with a {@link org.junit.Rule}
 * annotation.
 */
public class TestLog extends TestRuleAdapter {

  //TODO does this need to be threadsafe (such as via thread-local state)?
  private static ArrayList<LogEntry> logStack = new ArrayList<LogEntry>();
  private static final int MAX_LOGS = 1000;

  public static final TestLog instance = new TestLog();

  private TestLog() {}

  @Override
  protected void before() throws Throwable {
    logStack.clear();
  }

  @Override
  protected void afterAlways(List<Throwable> errors) throws Throwable {
    if (!errors.isEmpty())
      logThenClear();
  }

  private void logThenClear() {
    for (LogEntry entry : logStack) {
      System.out.println(MessageFormatter.arrayFormat(entry.msg, entry.args).getMessage());
    }
    logStack.clear();
  }

  public static void clear() {
    logStack.clear();
  }

  /**
   * Enqueues a log message with substitution arguments ala SLF4J (i.e. {} syntax).
   * If the test fails then it'll be logged then, otherwise it'll be forgotten.
   */
  public static void log(String msg, Object... args) {
    if (logStack.size() > MAX_LOGS) {
      throw new RuntimeException("Too many log statements: "+logStack.size() + " > "+MAX_LOGS);
    }
    LogEntry entry = new LogEntry();
    entry.msg = msg;
    entry.args = args;
    logStack.add(entry);
  }

  private static class LogEntry { String msg; Object[] args; }
}
