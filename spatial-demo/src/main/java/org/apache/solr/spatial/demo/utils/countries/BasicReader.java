package org.apache.solr.spatial.demo.utils.countries;

import java.io.File;

import org.apache.solr.client.solrj.SolrServer;
import org.opengis.feature.simple.SimpleFeature;

public abstract class BasicReader<T extends BasicInfo>
{
  public abstract T read( SimpleFeature f );
  public abstract void index( SolrServer solr, File shp ) throws Exception;
}
