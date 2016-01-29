package org.locationtech.spatial4j.io.benchmark;

import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.io.LegacyShapeWriter;
import org.locationtech.spatial4j.io.ShapeIO;
import org.locationtech.spatial4j.io.ShapeWriter;
import org.locationtech.spatial4j.shape.Shape;

import java.io.*;
import java.text.NumberFormat;

public class ShapeBenchmarks {
  
  public static void main(String[] args) throws Exception {
    

    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.geo = true;
    factory.normWrapLongitude = true;
    JtsSpatialContext ctx = new JtsSpatialContext(factory);
  
    PrintStream out = System.out;

    NumberFormat nf = NumberFormat.getPercentInstance();
    InputStreamReader in = new InputStreamReader(ShapeBenchmarks.class.getResourceAsStream("/samples.txt"));
    try (BufferedReader br = new BufferedReader(in)) {
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if(line.startsWith("#") || line.length()==0) {
          continue;
        }

        Shape shape = ctx.getFormats().getWktReader().read(line);
        
        double poly = ctx.getFormats().getWriter(ShapeIO.POLY).toString(shape).getBytes().length;

        out.println("Format | bytes | %poly | encoded");
        out.println("------ | ----- | ----- | -------");
        

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ctx.getBinaryCodec().writeShape(new DataOutputStream(baos), shape);
        
        out.print(" binary | ");
        out.print(baos.size());
        out.print(" | ");
        out.print(nf.format(poly/baos.size()));
        out.print(" | ");
        out.print("...");
        out.println();
        
        for(ShapeWriter writer : ctx.getFormats().getWriters()) {
          if(writer instanceof LegacyShapeWriter) {
            continue;
          }
          
          String str = writer.toString(shape);
          out.print(writer.getFormatName());
          out.print(" | ");
          out.print(str.length());
          out.print(" | ");
          out.print(nf.format(poly/str.getBytes().length));
          out.print(" | ");
          out.print(str);
          out.println();
        }
        out.println();
        out.println();
      }
    }
    
  }
}
