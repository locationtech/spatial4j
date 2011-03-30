package org.apache.lucene.spatial.base.io.sample;

import java.util.Comparator;


public class SampleData {
  public String id;
  public String name;
  public String shape;

  public SampleData(String line) {
    String[] vals = line.split("\t");
    id = vals[0];
    name = vals[1];
    shape = vals[2];
  }

  public static Comparator<SampleData> NAME_ORDER = new Comparator<SampleData>() {
    @Override
    public int compare(SampleData o1, SampleData o2) {
      return o1.name.compareTo( o2.name );
    }
  };
}
