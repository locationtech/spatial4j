package org.apache.solr.spatial.demo.utils.countries;

import java.util.Comparator;


public class CountryInfo extends BasicInfo
{
  public String status;
  public Double sqKM;
  public Double sqMI;

  public Integer population2005;


  public static Comparator<CountryInfo> POPULATION_ORDER = new Comparator<CountryInfo>() {
    @Override
    public int compare(CountryInfo o1, CountryInfo o2) {
      Integer pop1 = o1.population2005;
      Integer pop2 = o2.population2005;

      if( pop1 == null ) pop1 = new Integer(0);
      if( pop2 == null ) pop2 = new Integer(0);

      return pop2.compareTo( pop1 );
    }
  };
}
