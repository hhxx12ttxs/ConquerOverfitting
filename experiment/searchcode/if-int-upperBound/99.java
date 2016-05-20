// Copyright 2012 Cloudera Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cloudera.impala.planner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.impala.analysis.Analyzer;
import com.cloudera.impala.analysis.BinaryPredicate;
import com.cloudera.impala.analysis.Expr;
import com.cloudera.impala.analysis.SlotDescriptor;
import com.cloudera.impala.analysis.StringLiteral;
import com.cloudera.impala.analysis.TupleDescriptor;
import com.cloudera.impala.catalog.HBaseColumn;
import com.cloudera.impala.catalog.HBaseTable;
import com.cloudera.impala.catalog.PrimitiveType;
import com.cloudera.impala.common.InternalException;
import com.cloudera.impala.common.Pair;
import com.cloudera.impala.thrift.TExplainLevel;
import com.cloudera.impala.thrift.THBaseFilter;
import com.cloudera.impala.thrift.THBaseKeyRange;
import com.cloudera.impala.thrift.THBaseScanNode;
import com.cloudera.impala.thrift.TPlanNode;
import com.cloudera.impala.thrift.TPlanNodeType;
import com.cloudera.impala.thrift.TScanRange;
import com.cloudera.impala.thrift.TScanRangeLocation;
import com.cloudera.impala.thrift.TScanRangeLocations;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;



/**
 * Full scan of an HBase table.
 * Only families/qualifiers specified in TupleDescriptor will be retrieved in the backend.
 */
public class HBaseScanNode extends ScanNode {
  private final static Logger LOG = LoggerFactory.getLogger(HBaseScanNode.class);
  private final TupleDescriptor desc;

  // One range per clustering column. The range bounds are expected to be constants.
  // A null entry means there's no range restriction for that particular key.
  // If keyRanges is non-null it always contains as many entries as there are clustering
  // cols.
  private List<ValueRange> keyRanges;

  // derived from keyRanges; empty means unbounded;
  // initialize start/stopKey to be unbounded.
  private byte[] startKey = HConstants.EMPTY_START_ROW;
  private byte[] stopKey = HConstants.EMPTY_END_ROW;

  // List of HBase Filters for generating thrift message. Filled in finalize().
  private final List<THBaseFilter> filters = new ArrayList<THBaseFilter>();

  // The suggested value for "hbase.client.scan.setCaching", which batches maxCaching
  // rows per fetch request to the HBase region server. If the value is too high,
  // then the hbase region server will have a hard time (GC pressure and long response
  // times). If the value is too small, then there will be extra trips to the hbase
  // region server.
  // Default to 1024 and update it based on row size estimate such that each batch size
  // won't exceed 500MB.
  private final static int MAX_HBASE_FETCH_BATCH_SIZE = 500 * 1024 * 1024;
  private final static int DEFAULT_SUGGESTED_CACHING = 1024;
  private int suggestedCaching = DEFAULT_SUGGESTED_CACHING;

  // HBase config; Common across all object instance.
  private static Configuration hbaseConf = HBaseConfiguration.create();

  public HBaseScanNode(PlanNodeId id, TupleDescriptor desc) {
    super(id, desc, "SCAN HBASE");
    this.desc = desc;
  }

  public void setKeyRanges(List<ValueRange> keyRanges) {
    Preconditions.checkNotNull(keyRanges);
    this.keyRanges = keyRanges;
  }

  /**
   * This finalize() implementation also includes the computeStats() logic
   * (and there is no computeStats()), because it's easier to do that during
   * ValueRange construction.
   */
  @Override
  public void finalize(Analyzer analyzer) throws InternalException {
    Preconditions.checkNotNull(keyRanges);
    Preconditions.checkState(keyRanges.size() == 1);
    super.finalize(analyzer);
    HBaseTable tbl = (HBaseTable) desc.getTable();

    // If ValueRange is not null, transform it into start/stopKey by printing the values.
    // At present, we only do that for string-mapped keys because the hbase
    // data is stored as text.
    // ValueRange is null if there is no qualification on the row-key.
    ValueRange rowRange = keyRanges.get(0);
    if (rowRange != null) {
      if (rowRange.lowerBound != null) {
        Preconditions.checkState(rowRange.lowerBound.isConstant());
        Preconditions.checkState(rowRange.lowerBound instanceof StringLiteral);
        startKey = convertToBytes(((StringLiteral) rowRange.lowerBound).getValue(),
                                  !rowRange.lowerBoundInclusive);
      }
      if (rowRange.upperBound != null) {
        Preconditions.checkState(rowRange.upperBound.isConstant());
        Preconditions.checkState(rowRange.upperBound instanceof StringLiteral);
        stopKey = convertToBytes(((StringLiteral) rowRange.upperBound).getValue(),
                                  rowRange.upperBoundInclusive);
      }
    }

    if (rowRange != null && rowRange.isEqRange()) {
      cardinality = 1;
    } else {
     // Set maxCaching so that each fetch from hbase won't return a batch of more than
     // MAX_HBASE_FETCH_BATCH_SIZE bytes.
      Pair<Long, Long> estimate = tbl.getEstimatedRowStats(startKey, stopKey);
      cardinality = estimate.first.longValue();
      if (estimate.second.longValue() > 0) {
        suggestedCaching = (int)
            Math.max(MAX_HBASE_FETCH_BATCH_SIZE / estimate.second.longValue(), 1);
      }
    }

    cardinality *= computeSelectivity();
    cardinality = Math.max(0, cardinality);
    LOG.info("finalize HbaseScan: cardinality=" + Long.toString(cardinality));

    // Convert predicates to HBase filters.
    createHBaseFilters(analyzer);

    // TODO: take actual regions into account
    numNodes = desc.getTable().getNumNodes();
    LOG.info("finalize HbaseScan: #nodes=" + Integer.toString(numNodes));
  }

  @Override
  protected String debugString() {
    HBaseTable tbl = (HBaseTable) desc.getTable();
    return Objects.toStringHelper(this)
        .add("tid", desc.getId().asInt())
        .add("hiveTblName", tbl.getFullName())
        .add("hbaseTblName", tbl.getHBaseTableName())
        .add("startKey", ByteBuffer.wrap(startKey).toString())
        .add("stopKey", ByteBuffer.wrap(stopKey).toString())
        .addValue(super.debugString())
        .toString();
  }

  // We convert predicates of the form <slotref> op <constant> where slotref is of
  // type string to HBase filters. We remove the corresponding predicate from the
  // conjuncts.
  // TODO: expand this to generate nested filter lists for arbitrary conjunctions
  // and disjunctions.
  private void createHBaseFilters(Analyzer analyzer) {
    for (SlotDescriptor slot: desc.getSlots()) {
      // TODO: Currently we can only push down predicates on string columns.
      if (slot.getType() != PrimitiveType.STRING) {
        continue;
      }
      // List of predicates that cannot be pushed down as an HBase Filter.
      List<Expr> remainingPreds = new ArrayList<Expr>();
      for (Expr e: conjuncts) {
        if (!(e instanceof BinaryPredicate)) {
          remainingPreds.add(e);
          continue;
        }
        BinaryPredicate bp = (BinaryPredicate) e;
        Expr bindingExpr = bp.getSlotBinding(slot.getId());
        if (bindingExpr == null || !(bindingExpr instanceof StringLiteral)) {
          remainingPreds.add(e);
          continue;
        }
        CompareFilter.CompareOp hbaseOp = impalaOpToHBaseOp(bp.getOp());
        // Currently unsupported op, leave it as a predicate.
        if (hbaseOp == null) {
          remainingPreds.add(e);
          continue;
        }
        StringLiteral literal = (StringLiteral) bindingExpr;
        HBaseColumn col = (HBaseColumn) slot.getColumn();
        filters.add(new THBaseFilter(col.getColumnFamily(), col.getColumnQualifier(),
              (byte) hbaseOp.ordinal(), literal.getValue()));
      }
      conjuncts = remainingPreds;
    }
  }

  @Override
  protected void toThrift(TPlanNode msg) {
    msg.node_type = TPlanNodeType.HBASE_SCAN_NODE;
    HBaseTable tbl = (HBaseTable) desc.getTable();
    msg.hbase_scan_node =
      new THBaseScanNode(desc.getId().asInt(), tbl.getHBaseTableName());
    if (!filters.isEmpty()) {
      msg.hbase_scan_node.setFilters(filters);
    }
    msg.hbase_scan_node.setSuggested_max_caching(suggestedCaching);
  }

  /**
   * We create a TScanRange for each region server that contains at least one
   * relevant region, and the created TScanRange will contain all the relevant regions
   * of that region server.
   */
  @Override
  public List<TScanRangeLocations> getScanRangeLocations(long maxScanRangeLength) {
    // Retrieve relevant HBase regions and their region servers
    HBaseTable tbl = (HBaseTable) desc.getTable();
    HTable hbaseTbl = null;
    List<HRegionLocation> regionsLoc;
    try {
      hbaseTbl   = new HTable(hbaseConf, tbl.getHBaseTableName());
      regionsLoc = HBaseTable.getRegionsInRange(hbaseTbl, startKey, stopKey);
    } catch (IOException e) {
      throw new RuntimeException(
          "couldn't retrieve HBase table (" + tbl.getHBaseTableName() + ") info:\n"
          + e.getMessage());
    }

    // Convert list of HRegionLocation to Map<hostport, List<HRegionLocation>>.
    // The List<HRegionLocations>'s end up being sorted by start key/end key, because
    // regionsLoc is sorted that way.
    Map<String, List<HRegionLocation>> locationMap = Maps.newHashMap();
    for (HRegionLocation regionLoc: regionsLoc) {
      String locHostPort = regionLoc.getHostnamePort();
      if (locationMap.containsKey(locHostPort)) {
        locationMap.get(locHostPort).add(regionLoc);
      } else {
        locationMap.put(locHostPort, Lists.newArrayList(regionLoc));
      }
    }

    List<TScanRangeLocations> result = Lists.newArrayList();
    for (Map.Entry<String, List<HRegionLocation>> locEntry: locationMap.entrySet()) {
      // HBaseTableScanner(backend) initializes a result scanner for each key range.
      // To minimize # of result scanner re-init, create only a single HBaseKeyRange
      // for all adjacent regions on this server.
      THBaseKeyRange keyRange = null;
      byte[] prevEndKey = null;
      for (HRegionLocation regionLoc: locEntry.getValue()) {
        byte[] curRegStartKey = regionLoc.getRegionInfo().getStartKey();
        byte[] curRegEndKey   = regionLoc.getRegionInfo().getEndKey();
        if (prevEndKey != null &&
            Bytes.compareTo(prevEndKey, curRegStartKey) == 0) {
          // the current region starts where the previous one left off;
          // extend the key range
          setKeyRangeEnd(keyRange, curRegEndKey);
        } else {
          // create a new HBaseKeyRange (and TScanRange2/TScanRangeLocations to go
          // with it).
          keyRange = new THBaseKeyRange();
          setKeyRangeStart(keyRange, curRegStartKey);
          setKeyRangeEnd(keyRange, curRegEndKey);

          TScanRangeLocations scanRangeLocation = new TScanRangeLocations();
          scanRangeLocation.addToLocations(
              new TScanRangeLocation(addressToTNetworkAddress(locEntry.getKey())));
          result.add(scanRangeLocation);

          TScanRange scanRange = new TScanRange();
          scanRange.setHbase_key_range(keyRange);
          scanRangeLocation.setScan_range(scanRange);
        }
        prevEndKey = curRegEndKey;
      }
    }
    return result;
  }

  /**
   * Set the start key of keyRange using the provided key, bounded by startKey
   * @param keyRange the keyRange to be updated
   * @param rangeStartKey the start key value to be set to
   */
  private void setKeyRangeStart(THBaseKeyRange keyRange, byte[] rangeStartKey) {
    keyRange.unsetStartKey();
    // use the max(startKey, rangeStartKey) for scan start
    if (!Bytes.equals(rangeStartKey, HConstants.EMPTY_START_ROW) ||
        !Bytes.equals(startKey, HConstants.EMPTY_START_ROW)) {
      byte[] partStart = (Bytes.compareTo(rangeStartKey, startKey) < 0) ?
          startKey : rangeStartKey;
      keyRange.setStartKey(Bytes.toString(partStart));
    }
  }

  /**
   * Set the end key of keyRange using the provided key, bounded by stopKey
   * @param keyRange the keyRange to be updated
   * @param rangeEndKey the end key value to be set to
   */
  private void setKeyRangeEnd(THBaseKeyRange keyRange, byte[] rangeEndKey) {
    keyRange.unsetStopKey();
    // use the min(stopkey, regionStopKey) for scan stop
    if (!Bytes.equals(rangeEndKey, HConstants.EMPTY_END_ROW) ||
        !Bytes.equals(stopKey, HConstants.EMPTY_END_ROW)) {
      if (Bytes.equals(stopKey, HConstants.EMPTY_END_ROW)) {
        keyRange.setStopKey(Bytes.toString(rangeEndKey));
      } else if (Bytes.equals(rangeEndKey, HConstants.EMPTY_END_ROW)) {
        keyRange.setStopKey(Bytes.toString(stopKey));
      } else {
        byte[] partEnd = (Bytes.compareTo(rangeEndKey, stopKey) < 0) ?
            rangeEndKey : stopKey;
        keyRange.setStopKey(Bytes.toString(partEnd));
      }
    }
  }

  @Override
  protected String getNodeExplainString(String prefix,
      TExplainLevel detailLevel) {
    HBaseTable tbl = (HBaseTable) desc.getTable();
    StringBuilder output = new StringBuilder()
        .append(prefix + "table:" + tbl.getName() + "\n");
    if (!Bytes.equals(startKey, HConstants.EMPTY_START_ROW)) {
      output.append(prefix + "start key: " + printKey(startKey) + "\n");
    }
    if (!Bytes.equals(stopKey, HConstants.EMPTY_END_ROW)) {
      output.append(prefix + "stop key: " + printKey(stopKey) + "\n");
    }
    if (!filters.isEmpty()) {
      output.append(prefix + "hbase filters: ");
      if (filters.size() == 1) {
        THBaseFilter filter = filters.get(0);
        output.append(filter.family + ":" + filter.qualifier + " " +
            CompareFilter.CompareOp.values()[filter.op_ordinal].toString() + " " +
            "'" + filter.filter_constant + "'");
      } else {
        for (int i = 0; i < filters.size(); ++i) {
          THBaseFilter filter = filters.get(i);
          output.append("\n  " + filter.family + ":" + filter.qualifier + " " +
              CompareFilter.CompareOp.values()[filter.op_ordinal].toString() + " " +
              "'" + filter.filter_constant + "'");
        }
      }
      output.append('\n');
    }
    if (!conjuncts.isEmpty()) {
      output.append(prefix + "predicates: " + getExplainString(conjuncts) + "\n");
    }
    return output.toString();
  }

  /**
   * Convert key into byte array and append a '\0' if 'nextKey' is true.
   */
  private byte[] convertToBytes(String rowKey, boolean nextKey) {
    byte[] keyBytes = Bytes.toBytes(rowKey);
    if (!nextKey) {
      return keyBytes;
    } else {
      // append \0
      return Arrays.copyOf(keyBytes, keyBytes.length + 1);
    }
  }

  /**
   * Prints non-printable characters in escaped octal, otherwise outputs
   * the characters.
   */
  public static String printKey(byte[] key) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < key.length; ++i) {
      if (!Character.isISOControl(key[i])) {
        result.append((char) key[i]);
      } else {
        result.append("\\");
        result.append(Integer.toOctalString(key[i]));
      }
    }
    return result.toString();
  }

  private static CompareFilter.CompareOp impalaOpToHBaseOp(
      BinaryPredicate.Operator impalaOp) {
    switch(impalaOp) {
      case EQ: return CompareFilter.CompareOp.EQUAL;
      case NE: return CompareFilter.CompareOp.NOT_EQUAL;
      case GT: return CompareFilter.CompareOp.GREATER;
      case GE: return CompareFilter.CompareOp.GREATER_OR_EQUAL;
      case LT: return CompareFilter.CompareOp.LESS;
      case LE: return CompareFilter.CompareOp.LESS_OR_EQUAL;
      // TODO: Add support for pushing LIKE/REGEX down to HBase with a different Filter.
      default: throw new IllegalArgumentException(
          "HBase: Unsupported Impala compare operator: " + impalaOp);
    }
  }
}

