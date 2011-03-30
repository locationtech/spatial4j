package org.apache.solr.spatial.demo.utils.countries;

import java.util.Comparator;

import com.vividsolutions.jts.geom.Geometry;

public class BasicInfo
{
  public Geometry geometry;
  public String id;
  public String name;

  public static Comparator<BasicInfo> NAME_ORDER = new Comparator<BasicInfo>() {
    @Override
    public int compare(BasicInfo o1, BasicInfo o2) {
      return o1.name.compareTo( o2.name );
    }
  };
}
