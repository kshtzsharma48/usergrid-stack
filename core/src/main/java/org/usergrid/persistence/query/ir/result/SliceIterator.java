/*******************************************************************************
 * Copyright 2012 Apigee Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.usergrid.persistence.query.ir.result;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.cassandra.utils.ByteBufferUtil;
import org.usergrid.persistence.cassandra.CursorCache;
import org.usergrid.persistence.cassandra.index.IndexScanner;
import org.usergrid.persistence.query.ir.SliceNode;

/**
 * An iterator that will take all slices and order them correctly
 * @author tnine
 *
 */
public class SliceIterator<T> implements ResultIterator {

  private TreeSet<T> values;
  private Iterator<T> idIterator;
  private SliceNode slice;
  private SliceParser<T> parser;
  
  /**
   * 
   */
  public SliceIterator(IndexScanner scanResults, SliceNode slice, SliceParser<T> parser) {
    this.slice = slice;
    this.parser = parser;
    values = new TreeSet<T>(parser);
    
    while(scanResults.hasNext()){
      values.add(parser.parse(scanResults.next().getName()));
    }
    
    idIterator = values.iterator();
  
    
  }

  
  /* (non-Javadoc)
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<UUID> iterator() {
    return this;
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    return idIterator.hasNext();
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#next()
   */
  @Override
  public UUID next() {
    return parser.getUUID(idIterator.next());
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#remove()
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported");
  }

  /* (non-Javadoc)
   * @see org.usergrid.persistence.query.ir.result.ResultIterator#finalizeCursor()
   */
  @Override
  public void finalizeCursor(CursorCache cache) {
   
    ByteBuffer bytes = ByteBufferUtil.EMPTY_BYTE_BUFFER;
    
    if(hasNext()){
      bytes = parser.serialize(idIterator.next());
    }

    //otherwise it's an empty buffer
    cache.setNextCursor(slice.hashCode(), bytes);
  }
  
 
}