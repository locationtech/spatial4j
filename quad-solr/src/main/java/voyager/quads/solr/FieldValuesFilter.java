package voyager.quads.solr;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.OpenBitSet;


/**
 * Find everything
 */
class FieldValuesFilter extends Filter
{
  String fieldName;
  List<String> vals;

  public FieldValuesFilter(String fname, Collection<String> vals) {
    fieldName = fname;
    if( vals instanceof List ) {
      this.vals = (List)vals;
    }
    else {
      this.vals = new ArrayList<String>( vals );
    }
    // Need to sort since the Terms are in order
    Collections.sort( this.vals );
  }

  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext context) throws IOException {
    IndexReader reader = context.reader;
    OpenBitSet result=new OpenBitSet(reader.maxDoc());
    Terms terms = MultiFields.getTerms(reader, fieldName);
    if( terms != null ) {
      DocsEnum docs = null;
      Bits delDocs = reader.getDeletedDocs();
      TermsEnum te = terms.iterator();
      for( String v : vals ) {
        BytesRef ref = new BytesRef(v);
        if (te.seek(ref) == TermsEnum.SeekStatus.FOUND) {
          docs = te.docs(delDocs, docs);
          while(docs.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
            result.set(docs.docID());
          }
        }
      }
    }
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) return true;
    if((obj == null) || (obj.getClass() != this.getClass())) return false;

    FieldValuesFilter test = (FieldValuesFilter)obj;
    return fieldName.equals( test.fieldName ) && vals.equals(test.vals);
  }

  @Override
  public int hashCode()
  {
    int hash=9 + fieldName.hashCode();
    for (Iterator<String> iter = vals.iterator(); iter.hasNext();)
    {
      String term = iter.next();
      hash = 31 * hash + term.hashCode();
    }
    return hash;
  }

  @Override
  public String toString()
  {
    return "FieldValues("+fieldName+">"+vals.size()+")";
  }
}