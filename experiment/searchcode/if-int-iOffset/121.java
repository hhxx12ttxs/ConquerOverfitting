/**
 * This class has been hightly inspired by the work
 * of Juanco Anez (juanco@suigeneris.org) who delivered
 * his own work under the Apache Licence:
 *
 *
 *
 *
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package com.qarks.util.files.diff.core;

import java.util.*;
import com.qarks.util.files.diff.*;
import com.qarks.util.*;

public class MyersDiff {

  public static FileDiffResult diff(ParsedFile orig, ParsedFile rev, Cancellable cancellable, boolean ignoreLeadingSpaces) {
    return diff(orig.getLines(), rev.getLines(), cancellable, ignoreLeadingSpaces);
  }

  public static FileDiffResult diff(ParsedFile orig, ParsedFile rev, boolean ignoreLeadingSpaces) {
    return diff(orig.getLines(), rev.getLines(), new CancellableImpl(), ignoreLeadingSpaces);
  }

  public static FileDiffResult diff(FileLine[] orig, FileLine[] rev, Cancellable cancellable, boolean ignoreLeadingSpaces) {
    FileDiffResult result = null;
    PathNode path = buildPath(orig, rev, cancellable, ignoreLeadingSpaces);
    if (path != null) {
      result = buildRevision(path, orig, rev);
    }
    return result;
  }

  public static PathNode buildPath(FileLine[] orig, FileLine[] rev,
                                   Cancellable cancellable, boolean ignoreLeadingSpaces) {
    if (orig == null){
      throw new IllegalArgumentException("original sequence is null");
    }
    if (rev == null){
      throw new IllegalArgumentException("revised sequence is null");
    }

    final int N = orig.length;
    final int M = rev.length;

    final int MAX = N + M + 1;
    final int size = 1 + 2 * MAX;
    final int middle = (size + 1) / 2;
    final PathNode diagonal[] = new PathNode[size];

    diagonal[middle + 1] = new Snake(0, -1, null);
    for (int d = 0; d < MAX; d++) {
      if (cancellable.isCancelled()) {
        return null;
      }

      for (int k = -d; k <= d; k += 2) {

        if (cancellable.isCancelled()) {
          return null;
        }

        final int kmiddle = middle + k;
        final int kplus = kmiddle + 1;
        final int kminus = kmiddle - 1;
        PathNode prev = null;

        int i;
        if ( (k == -d)
            || (k != d && diagonal[kminus].i < diagonal[kplus].i)) {
          i = diagonal[kplus].i;
          prev = diagonal[kplus];
        }
        else {
          i = diagonal[kminus].i + 1;
          prev = diagonal[kminus];
        }

        diagonal[kminus] = null;

        int j = i - k;

        PathNode node = new DiffNode(i, j, prev);

        while (i < N && j < M && orig[i].matches(rev[j], ignoreLeadingSpaces)) {
          i++;
          j++;
        }
        if (i > node.i)
          node = new Snake(i, j, node);

        diagonal[kmiddle] = node;

        if (i >= N && j >= M) {
          return diagonal[kmiddle];
        }
      }
      diagonal[middle + d - 1] = null;
    }
    return null;
  }

  public static FileDiffResult buildRevision(PathNode path, FileLine[] orig, FileLine[] rev) {
    ArrayList<Delta> deltas = new ArrayList<Delta> ();

    if (path == null){
      throw new IllegalArgumentException("path is null");
    }
    if (orig == null){
      throw new IllegalArgumentException("original sequence is null");
    }
    if (rev == null){
      throw new IllegalArgumentException("revised sequence is null");
    }

    if (path.isSnake()){
      path = path.prev;
    }

    while (path != null && path.prev != null && path.prev.j >= 0) {
      if (path.isSnake()){
        throw new IllegalStateException("bad diffpath: found snake when looking for diff");
      }
      int i = path.i;
      int j = path.j;

      path = path.prev;
      int ianchor = path.i;
      int janchor = path.j;

      Delta delta = new Delta(new Chunk(ianchor, i - ianchor),
                              new Chunk(janchor, j - janchor));
      deltas.add(0, delta);

      if (path.isSnake()){
        path = path.prev;
      }
    }

    int ioffset = 0;
    int joffset = 0;
    for (Delta delta : deltas) {
      Chunk left = delta.getOriginal();
      Chunk right = delta.getRevised();

      int status = (left.count == 0 || right.count == 0) ? FileLine.NO_MATCH : FileLine.MODIFIED;
      for (int i = ioffset; i < left.first(); i++) {
        if (i < orig.length) {
          FileLine line = orig[i];
          line.setStatus(FileLine.UNCHANGED);
        }
      }
      ioffset = left.first();
      for (int i = ioffset; i < (ioffset + left.count); i++) {
        if (i < orig.length) {
          FileLine line = orig[i];
          line.setStatus(status);
        }
      }
      ioffset += left.count;

      for (int i = joffset; i < right.first(); i++) {
        if (i < rev.length) {
          FileLine line = rev[i];
          line.setStatus(FileLine.UNCHANGED);
        }
      }
      joffset = right.first();
      for (int i = joffset; i < (joffset + right.count); i++) {
        if (i < rev.length) {
          FileLine line = rev[i];
          line.setStatus(status);
        }
      }
      joffset += right.count;
    }

    for (int i = ioffset; i < orig.length; i++) {
      orig[i].setStatus(FileLine.UNCHANGED);
    }
    for (int i = joffset; i < rev.length; i++) {
      rev[i].setStatus(FileLine.UNCHANGED);
    }

    FileDiffResult diffResult = new FileDiffResult(new ParsedFile(orig),new ParsedFile(rev));

    return diffResult;
  }

}

