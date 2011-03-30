package org.apache.lucene.spatial.base.io.sample;

import java.sql.Date;

import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;

public class SampleData {

  public String shape;
  public String name;
  public String fips;
  public Integer population2005;
  
  public SampleData(String line) {
    String[] vals = line.split("\t");
    name = vals[0];
    fips = vals[1];
    if (vals[2].length() > 0) {
      population2005 = Integer.valueOf( vals[2] );
    }
    shape = vals[3];
  }
}
