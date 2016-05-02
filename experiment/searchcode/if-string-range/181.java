package com.github.adeshmukh.clisql.cli;

import static com.github.adeshmukh.clisql.cli.HeaderOption.LETTER;
import static com.github.adeshmukh.clisql.cli.Headers.letters;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.io.LineProcessor;

public class InputProcessor implements LineProcessor<Data> {

    private static final Logger LOG = LoggerFactory.getLogger(InputProcessor.class);

    private Range<Integer> range;
    private int lineNum, processedLineNum;
    private Data data = Data.EMPTY;
    private String delimiter;
    private HeaderOption headerOption;
    private List<String> headerNames = null;
    private List<List<Object>> records = new ArrayList<List<Object>>();
    private int rangeUpperEndpoint = Integer.MAX_VALUE;
    private int[] maxFieldLengths;

    public InputProcessor(Range<Integer> range, String delimiter, HeaderOption headerOption) {
        this.range = Objects.firstNonNull(range, Ranges.greaterThan(0));
        this.delimiter = Objects.firstNonNull(delimiter, "\\s+");
        this.headerOption = headerOption;
        if (this.range.hasUpperBound()) {
            this.rangeUpperEndpoint = this.range.upperEndpoint();
        }
    }

    @Override
    public boolean processLine(String line) throws IOException {
        lineNum++;
        if (range.contains(lineNum)) {
            String[] record = line.trim().split(delimiter);
            if (++processedLineNum == 1) {
                headerNames = LETTER == headerOption ? letters(record.length) : asList(record);
                maxFieldLengths = new int[record.length];
                Arrays.fill(maxFieldLengths, 1);
            } else {
                for (int i = 0, iSize = record.length; i < iSize; i++) {
                    maxFieldLengths[i] = Math.max(record[i].length(), maxFieldLengths[i]);
                    // TODO(adeshmukh) infer data type
                }
                records.add(asList((Object[]) record));
            }
        }
        return !(rangeUpperEndpoint < lineNum);
    }

    @Override
    public Data getResult() {
        if (data == Data.EMPTY && !headerNames.isEmpty()) {
            data = new Data(headers(headerNames), records);
        }
        return data;
    }

    public int getLastProcessedLineNum() {
        return lineNum;
    }

    public List<Header> headers(List<String> headerNames) {
        LOG.debug("headerNames.size(): {}, maxFieldLengths.length: {}", headerNames.size(), maxFieldLengths.length);
        List<Header> headers = Lists.newArrayListWithExpectedSize(headerNames.size());
        int i = -1;
        for (String headerName : headerNames) {
            headers.add(new Header(headerName, Types.VARCHAR, maxFieldLengths[++i]));
        }
        return headers;
    }
}

