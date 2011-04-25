package org.apache.lucene.spatial.data.benchmark;

import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.tasks.PerfTask;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;

import java.util.UUID;

/**
 * @author Chris Male
 */
public abstract class IndexShapeTask<T extends SpatialFieldInfo> extends PerfTask implements StrategyAware<T> {

    private ShapeGenerator shapeGenerator;
    private int numShapes;

    public IndexShapeTask(PerfRunData runData) {
        super(runData);
    }

    @Override
    public void setup() throws Exception {
        Config config = getRunData().getConfig();
        String shapeGeneratorName = config.get("index.shapegenerator", ""); // TODO (cmale) - Setup default shape generator
        shapeGenerator = (ShapeGenerator) Class.forName(shapeGeneratorName)
                .getConstructor(Config.class)
                .newInstance(config);
        numShapes = config.get("index.numshapes", 1);
    }

    public int doLogic() throws Exception {
        SpatialStrategy<T> spatialStrategy = createSpatialStrategy();
        T fieldInfo = createFieldInfo();
        for (int i = 0; i < numShapes; i++) {
            Shape shape = shapeGenerator.generate();
            Fieldable[] fields = spatialStrategy.createFields(fieldInfo, shape, true, true);
            if (fields == null) {
                continue;
            }
            Document document = new Document();
            document.add(new Field("id", UUID.randomUUID().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
            for (Fieldable field : fields) {
                document.add(field);
            }
            getRunData().getIndexWriter().addDocument(document);
        }
        return 1;
    }
}
