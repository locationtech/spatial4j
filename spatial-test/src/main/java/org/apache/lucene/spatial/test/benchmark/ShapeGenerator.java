package org.apache.lucene.spatial.test.benchmark;

import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.spatial.base.shape.Shape;


public abstract class ShapeGenerator {

  private Config config;

  protected ShapeGenerator(Config config) {
    this.config = config;
  }

  public abstract Shape generate();

  protected Config getConfig() {
    return config;
  }
}
