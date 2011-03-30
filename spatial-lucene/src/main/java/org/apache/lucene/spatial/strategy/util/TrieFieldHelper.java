package org.apache.lucene.spatial.strategy.util;

import java.util.Date;

import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

/**
 *  see: LUCENE-3001
 *
 *  should not be here...
 */
public class TrieFieldHelper {

  private TrieFieldHelper() {}

  public static class FieldInfo {
    public int precisionStep = 8; // same as solr default
    public boolean store = true;
    public boolean index = true;
    public boolean omitNorms = true;
    public boolean omitTF = true;

    public void setPrecisionStep( int p ) {
      precisionStep = p;
      if (precisionStep<=0 || precisionStep>=64)
        precisionStep=Integer.MAX_VALUE;
    }
  }

  //----------------------------------------------
  // Create Field
  //----------------------------------------------

  private static Fieldable createField(String name, byte[] arr, TokenStream ts, FieldInfo info, float boost) {

    Field f;
    if (info.store) {
      f = new Field(name, arr);
      if (info.index) f.setTokenStream(ts);
    } else {
      f = new Field(name, ts);
    }

    // term vectors aren't supported
    f.setOmitNorms(info.omitNorms);
    f.setOmitTermFreqAndPositions(info.omitTF);
    f.setBoost(boost);
    return f;
  }

  public static Fieldable createIntField(String name, int value, FieldInfo info, float boost) {

    byte[] arr=null;
    TokenStream ts=null;

    if (info.store) arr = TrieFieldHelper.toArr(value);
    if (info.index) ts = new NumericTokenStream(info.precisionStep).setIntValue(value);

    return createField(name, arr, ts, info, boost);
  }

  public static Fieldable createFloatField(String name, float value, FieldInfo info, float boost) {

    byte[] arr=null;
    TokenStream ts=null;

    if (info.store) arr = TrieFieldHelper.toArr(value);
    if (info.index) ts = new NumericTokenStream(info.precisionStep).setFloatValue(value);

    return createField(name, arr, ts, info, boost);
  }

  public static Fieldable createLongField(String name, long value, FieldInfo info, float boost) {

    byte[] arr=null;
    TokenStream ts=null;

    if (info.store) arr = TrieFieldHelper.toArr(value);
    if (info.index) ts = new NumericTokenStream(info.precisionStep).setLongValue(value);

    return createField(name, arr, ts, info, boost);
  }

  public static Fieldable createDoubleField(String name, double value, FieldInfo info, float boost) {

    byte[] arr=null;
    TokenStream ts=null;

    if (info.store) arr = TrieFieldHelper.toArr(value);
    if (info.index) ts = new NumericTokenStream(info.precisionStep).setDoubleValue(value);

    return createField(name, arr, ts, info, boost);
  }

  public static Fieldable createDateField(String name, Date value, FieldInfo info, float boost) {
    // TODO, make sure the date is within long range!
    return createLongField(name, value.getTime(), info, boost);
  }


  //----------------------------------------------
  // number <=> byte[]
  //----------------------------------------------

  public static int toInt(byte[] arr) {
    return (arr[0]<<24) | ((arr[1]&0xff)<<16) | ((arr[2]&0xff)<<8) | (arr[3]&0xff);
  }

  public static long toLong(byte[] arr) {
    int high = (arr[0]<<24) | ((arr[1]&0xff)<<16) | ((arr[2]&0xff)<<8) | (arr[3]&0xff);
    int low = (arr[4]<<24) | ((arr[5]&0xff)<<16) | ((arr[6]&0xff)<<8) | (arr[7]&0xff);
    return (((long)high)<<32) | (low&0x0ffffffffL);
  }

  public static float toFloat(byte[] arr) {
    return Float.intBitsToFloat(toInt(arr));
  }

  public static double toDouble(byte[] arr) {
    return Double.longBitsToDouble(toLong(arr));
  }

  public static byte[] toArr(int val) {
    byte[] arr = new byte[4];
    arr[0] = (byte)(val>>>24);
    arr[1] = (byte)(val>>>16);
    arr[2] = (byte)(val>>>8);
    arr[3] = (byte)(val);
    return arr;
  }

  public static byte[] toArr(long val) {
    byte[] arr = new byte[8];
    arr[0] = (byte)(val>>>56);
    arr[1] = (byte)(val>>>48);
    arr[2] = (byte)(val>>>40);
    arr[3] = (byte)(val>>>32);
    arr[4] = (byte)(val>>>24);
    arr[5] = (byte)(val>>>16);
    arr[6] = (byte)(val>>>8);
    arr[7] = (byte)(val);
    return arr;
  }

  public static byte[] toArr(float val) {
    return toArr(Float.floatToRawIntBits(val));
  }

  public static byte[] toArr(double val) {
    return toArr(Double.doubleToRawLongBits(val));
  }
}
