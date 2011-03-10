package voyager.quads.utils.countries;

import java.util.Comparator;

import com.vividsolutions.jts.geom.Geometry;

public class CountryInfo
{
  public Geometry geometry;
  public String name;
  public String longName;
  public String fips;
  public String status;
  public Double sqKM;
  public Double sqMI;
  public Long population2005;

  public static Comparator<CountryInfo> NAME_ORDER = new Comparator<CountryInfo>() {
    @Override
    public int compare(CountryInfo o1, CountryInfo o2) {
      return o1.name.compareTo( o2.name );
    }
  };

  public static Comparator<CountryInfo> POPULATION_ORDER = new Comparator<CountryInfo>() {
    @Override
    public int compare(CountryInfo o1, CountryInfo o2) {
      Long pop1 = o1.population2005;
      Long pop2 = o2.population2005;

      if( pop1 == null ) pop1 = new Long(0);
      if( pop2 == null ) pop2 = new Long(0);

      return pop2.compareTo( pop1 );
    }
  };
}
