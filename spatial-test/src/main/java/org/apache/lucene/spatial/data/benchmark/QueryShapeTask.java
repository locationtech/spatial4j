package org.apache.lucene.spatial.data.benchmark;

import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.tasks.PerfTask;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.strategy.SpatialFieldInfo;

/**
 * @author Chris Male
 */
public abstract class QueryShapeTask<T extends SpatialFieldInfo> extends PerfTask implements StrategyAware<T> {

    private SpatialArgs spatialArgs;

    public QueryShapeTask(PerfRunData runData) {
        super(runData);
    }

    @Override
    public void setup() {
        Config config = getRunData().getConfig();
        String rawQuery = config.get("query.shapequery", ""); // TODO (cmale) - Come up with default query
        this.spatialArgs = new SpatialArgsParser().parse(rawQuery, getSpatialContext());
    }

    public int doLogic() throws Exception {
        Query query = createSpatialStrategy().makeQuery(spatialArgs, createFieldInfo());
        TopDocs topDocs = getRunData().getIndexSearcher().search(query, 10);
        System.out.println("Numfound: " + topDocs.totalHits);
        return 1;
    }
}
