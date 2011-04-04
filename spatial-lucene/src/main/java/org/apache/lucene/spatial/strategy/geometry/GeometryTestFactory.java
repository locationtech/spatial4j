/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.spatial.strategy.geometry;

import org.apache.lucene.spatial.base.query.SpatialOperation;

import com.vividsolutions.jts.geom.Geometry;


public class GeometryTestFactory {

  public static GeometryTest get(SpatialOperation op, Geometry geo) {
    switch (op) {
      case BBoxIntersects:
        return new BBoxIntersectsTester(geo);
      case BBoxWithin:
        return new BBoxWithinTester(geo);
      case Contains:
        return new ContainsTester(geo);
      case Intersects:
        return new IntersectsTester(geo);
      case IsEqualTo:
        return new IsEqualToTester(geo);
      case IsDisjointTo:
        return new IsDisjointToTester(geo);
      case IsWithin:
        return new IsWithinTester(geo);
      case Overlaps:
        return new OverlapsTester(geo);
    }
    throw new UnsupportedOperationException(op.name());
  }

  private abstract static class BaseTester implements GeometryTest {
    protected Geometry queryGeo;

    public BaseTester(Geometry geo) {
      this.queryGeo = geo;
    }
  }

  public static class BBoxIntersectsTester extends BaseTester {
    public BBoxIntersectsTester(Geometry geo) {
      super(geo);
    }

    @Override
    public boolean matches(Geometry geo) {
      return geo.getEnvelope().intersects(queryGeo);
    }
  }

  public static class BBoxWithinTester extends BaseTester {
    public BBoxWithinTester(Geometry geo) {
      super(geo);
    }

    @Override
    public boolean matches(Geometry geo) {
      return geo.getEnvelope().within(queryGeo);
    }
  }

  public static class ContainsTester extends BaseTester {
    public ContainsTester(Geometry geo) {
      super(geo);
    }

    @Override
    public boolean matches(Geometry geo) {
      return geo.contains(queryGeo);
    }
  }

  public static class IntersectsTester extends BaseTester {
    public IntersectsTester(Geometry geo) {
      super(geo);
    }

    @Override
    public boolean matches(Geometry geo) {
      return geo.intersects(queryGeo);
    }
  }

  public static class IsEqualToTester extends BaseTester {
    public IsEqualToTester(Geometry geo) {
      super(geo);
    }

    @Override
    public boolean matches(Geometry geo) {
      return geo.equals(queryGeo);
    }
  }

  public static class IsDisjointToTester extends BaseTester {
    public IsDisjointToTester(Geometry geo) {
      super(geo);
    }

    @Override
    public boolean matches(Geometry geo) {
      return geo.disjoint(queryGeo);
    }
  }

  public static class IsWithinTester extends BaseTester {
    public IsWithinTester(Geometry geo) {
      super(geo);
    }

    @Override
    public boolean matches(Geometry geo) {
      return geo.within(queryGeo);
    }
  }

  public static class OverlapsTester extends BaseTester {
    public OverlapsTester(Geometry geo) {
      super(geo);
    }

    @Override
    public boolean matches(Geometry geo) {
      return geo.overlaps(queryGeo);
    }
  }
}
