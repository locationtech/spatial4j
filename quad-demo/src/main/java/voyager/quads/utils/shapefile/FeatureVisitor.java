package voyager.quads.utils.shapefile;

import org.opengis.feature.simple.SimpleFeature;

public interface FeatureVisitor
{
  public void visit( SimpleFeature f, int idx );
}
