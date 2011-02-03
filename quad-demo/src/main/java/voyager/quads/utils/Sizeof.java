package voyager.quads.utils;

import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

// FROM: http://www.javaworld.com/javaworld/javatips/jw-javatip130.html
public class Sizeof
{
  public static void main (String [] args) throws Exception
  {
    // Warm up all classes/methods we will use
    runGC ();
    usedMemory ();

    WKTReader reader = new WKTReader();
    WKBWriter writer = new WKBWriter();

    // Array to keep strong references to allocated objects
    final int count = 1000;
    Object [] objects = new Object [count];

    long heap1 = 0;
    // Allocate count+1 objects, discard the first one
    for (int i = -1; i < count; ++i)
    {
      Object object = null;

      // Instantiate your data here and assign it to object

//      object = new Object ();

//      Geometry geo = reader.read( "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))" );
//      ObjectOutputStream out = null;
//      try
//      {
//        ByteArrayOutputStream fos = new ByteArrayOutputStream();
//        out = new ObjectOutputStream(fos);
//        out.writeObject(geo);
//        out.close();
//
//        object = fos.toByteArray();
//      }
//      catch(IOException ex)
//      {
//        ex.printStackTrace();
//      }
//
//
//     // object = writer.write( geo );
//      geo = null;
      String s = new String( "-125.22656679154 49.089853763581, -123.99609804-123.99609804".toCharArray() );

      object = s;

  //    object = reader.read( "POINT(6 10)" );
   //   object = reader.read( "LINESTRING(3 4,5 7,8 2)" );
   //   object = reader.read( "POLYGON((-125.22656679154 49.089853763581, -123.99609804154 46.453135013581, -123.99609804154 44.343760013581, -124.34766054154 42.585947513581, -124.17187929154 40.828135013581, -123.29297304154 39.246103763581, -122.76562929154 36.960947513581, -121.18359804154 34.851572513581, -118.89844179154 34.324228763581, -117.66797304154 33.093760013581, -114.85547304154 33.093760013581, -111.51562929154 31.687510013581, -108.52734804154 31.863291263581, -105.71484804154 31.511728763581, -104.13281679154 29.578135013581, -101.14453554154 29.929697513581, -100.44141054154 27.644541263581, -97.277348041542 25.886728763581, -96.046879291542 28.171885013581, -93.585941791542 29.402353763581, -90.421879291542 29.402353763581, -88.488285541542 29.050791263581, -88.312504291542 30.281260013581, -83.390629291542 29.929697513581, -82.160160541542 26.765635013581, -80.226566791542 24.832041263581, -79.171879291542 27.292978763581, -80.929691791542 31.511728763581, -75.304691791542 35.554697513581, -75.304691791542 37.664072513581, -72.316410541542 41.179697513581, -69.328129291542 41.882822513581, -69.855473041542 44.167978763581, -65.988285541542 45.046885013581, -67.921879291542 48.562510013581, -70.382816791542 47.507822513581, -70.910160541542 45.925791263581, -76.710941791542 45.222666263581, -79.523441791542 44.519541263581, -82.687504291542 46.277353763581, -84.972660541542 46.804697513581, -89.894535541542 47.859385013581, -95.167973041542 49.089853763581, -125.22656679154 49.089853763581))");
   //   object = new Envelope( -1,-1,10,10 );

      if (i >= 0)
          objects [i] = object;
      else
      {
        object = null; // Discard the warm up object
        runGC ();
        heap1 = usedMemory (); // Take a before heap snapshot
      }
    }
    runGC ();
    long heap2 = usedMemory (); // Take an after heap snapshot:

    final int size = Math.round (((float)(heap2 - heap1))/count);
    System.out.println ("'before' heap: " + heap1 + ", 'after' heap: " + heap2);
    System.out.println ("heap delta: " + (heap2 - heap1) + ", {" + objects [0].getClass () + "} size = " + size + " bytes");

    for (int i = 0; i < count; ++ i)
      objects [i] = null;
    objects = null;
  }
  private static void runGC () throws Exception
  {
    // It helps to call Runtime.gc()
    // using several method calls:
    for (int r = 0; r < 4; ++ r) _runGC ();
  }
  private static void _runGC () throws Exception
  {
    long usedMem1 = usedMemory (), usedMem2 = Long.MAX_VALUE;
    for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++ i)
    {
      s_runtime.runFinalization ();
      s_runtime.gc ();
      Thread.currentThread ();
      Thread.yield ();

      usedMem2 = usedMem1;
      usedMem1 = usedMemory ();
    }
  }
  private static long usedMemory ()
  {
    return s_runtime.totalMemory () - s_runtime.freeMemory ();
  }

  private static final Runtime s_runtime = Runtime.getRuntime ();
} // End of class
