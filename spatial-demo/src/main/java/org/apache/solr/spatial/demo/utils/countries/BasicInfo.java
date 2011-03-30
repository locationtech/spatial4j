package org.apache.solr.spatial.demo.utils.countries;

import java.util.Comparator;

import com.vividsolutions.jts.geom.Geometry;

public class BasicInfo
{
  public Geometry geometry;
  public String name;
  public String fips;
  public Integer population2005;

  public static Comparator<BasicInfo> NAME_ORDER = new Comparator<BasicInfo>() {
    @Override
    public int compare(BasicInfo o1, BasicInfo o2) {
      return o1.name.compareTo( o2.name );
    }
  };

  public static Comparator<BasicInfo> POPULATION_ORDER = new Comparator<BasicInfo>() {
    @Override
    public int compare(BasicInfo o1, BasicInfo o2) {
      Integer pop1 = o1.population2005;
      Integer pop2 = o2.population2005;

      if( pop1 == null ) pop1 = new Integer(0);
      if( pop2 == null ) pop2 = new Integer(0);

      return pop2.compareTo( pop1 );
    }
  };
}
