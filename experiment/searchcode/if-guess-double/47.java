/*
 * MiTCR <http://milaboratory.com>
 *
 * Copyright (c) 2010-2013:
 *     Bolotin Dmitriy     <bolotin.dmitriy@gmail.com>
 *     Chudakov Dmitriy    <chudakovdm@mail.ru>
 *
 * MiTCR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.milaboratory.core.sequencing.io.fastq;

import cc.redberry.pipe.blocks.AbstractOutputPortUninterruptible;
import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequenceImpl;
import com.milaboratory.core.sequencing.io.SSequencingDataReader;
import com.milaboratory.core.sequencing.io.fastq.quality.QualityStringFormat;
import com.milaboratory.core.sequencing.io.fastq.quality.WrongQualityStringException;
import com.milaboratory.core.sequencing.read.SSequencingRead;
import com.milaboratory.core.sequencing.read.SSequencingReadImpl;
import com.milaboratory.util.CanReportProgress;
import com.milaboratory.util.CompressionType;
import com.milaboratory.util.CountingInputStream;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FASTQ files reader, using BufferedReader.readLine() method.
 *
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @author Shugay Mikhail (mikhail.shugay@gmail.com)
 */
// TODO: _close() and _take()
public class SFastqReader extends AbstractOutputPortUninterruptible<SSequencingRead> implements SSequencingDataReader, CanReportProgress {
    //Main Input
    private final BufferedReader reader;
    //To determine red percent
    //private final FileChannel channel;
    //private volatile int percentRed = 0;
    private final AtomicLong dotCorrections = new AtomicLong();
    private final long totalSize;
    private long counter = 0;
    //FASTQ format
    private final CountingInputStream countingStream;
    private final QualityStringFormat format;
    private final IlluminaInfoProvider infoProvider;
    //private final boolean notFilteredOnly;

    /**
     * Creates a {@link SSequencingRead} stream from a FASTQ files with single-end read data
     *
     * @param stream          stream with reads
     * @param format          read quality encoding format
     * @param ct              type of compression (NONE, GZIP, etc)
     * @param notFilteredOnly outputs only reads that are not marked by 'filtered' flag in their header
     * @throws IOException in case there is problem with reading from files
     */
    public SFastqReader(InputStream stream, QualityStringFormat format, CompressionType ct, boolean notFilteredOnly) throws IOException {
        this(stream, format, ct, notFilteredOnly, false);
    }

    /**
     * Creates a {@link SSequencingRead} stream from a FASTQ files with single-end read data
     *
     * @param stream             stream with reads
     * @param format             read quality encoding format, if {@code guessQualityFormat} is true this value is used
     *                           as default format
     * @param ct                 type of compression (NONE, GZIP, etc)
     * @param notFilteredOnly    outputs only reads that are not marked by 'filtered' flag in their header
     * @param guessQualityFormat if true Reader will try to guess quality string format, if guess fails {@code format}
     *                           will be used as quality string format
     * @throws IOException in case there is problem with reading from files
     */
    public SFastqReader(InputStream stream, QualityStringFormat format, CompressionType ct, boolean notFilteredOnly,
                        boolean guessQualityFormat) throws IOException {
        //Check for null
        if (stream == null)
            throw new NullPointerException();

        if (stream instanceof FileInputStream)
            totalSize = ((FileInputStream) stream).getChannel().size();
        else
            totalSize = 0L;

        if (notFilteredOnly)
            infoProvider = IlluminaInfoProviderFactory.createProvider(format);
        else
            infoProvider = null;

        //Initialization
        InputStream is = this.countingStream = new CountingInputStream(stream);

        //Wrapping stream if UnCompression needed
        is = ct.createInputStream(is);

        //Creating main reder
        this.reader = new BufferedReader(new InputStreamReader(is));

        if (guessQualityFormat) {
            reader.mark(8192);
            QualityStringFormat f = QualityFormatGuesser.guessFormat(reader, 5500);
            if (f != null)
                format = f;

            reader.reset();
        }

        if (format == null)
            if (guessQualityFormat)
                throw new RuntimeException("Format guess failed...");
            else
                throw new NullPointerException();

        this.format = format;
    }

    /**
     * Creates a {@link SSequencingRead} stream from a FASTQ files with single-end read data
     *
     * @param stream stream with reads
     * @param format read quality encoding format
     * @param ct     type of compression (NONE, GZIP, etc)
     * @throws IOException in case there is problem with reading from files
     */
    public SFastqReader(InputStream stream, QualityStringFormat format, CompressionType ct) throws IOException {
        this(stream, format, ct, false);
    }

    /**
     * Creates a {@link SSequencingRead} stream from a FASTQ files with single-end read data
     *
     * @param file            file with reads
     * @param format          read quality encoding format
     * @param ct              type of compression (NONE, GZIP, etc)
     * @param notFilteredOnly outputs only reads that are not marked by 'filtered' flag in their header
     * @throws IOException in case there is problem with reading from files
     */
    public SFastqReader(File file, QualityStringFormat format, CompressionType ct, boolean notFilteredOnly) throws IOException {
        this(new FileInputStream(file), format, ct, notFilteredOnly);
    }

    /**
     * Creates a {@link SSequencingRead} stream from a FASTQ files with single-end read data
     *
     * @param file   file with reads
     * @param format read quality encoding format
     * @param ct     type of compression (NONE, GZIP, etc)
     * @throws IOException in case there is problem with reading from files
     */
    public SFastqReader(File file, QualityStringFormat format, CompressionType ct) throws IOException {
        this(new FileInputStream(file), format, ct, false);
    }


    /*public SFastqReader(File file, QualityStringFormat format, CompressionType ct) throws FileNotFoundException {
        //Check for null
        if (file == null || format == null)
            throw new NullPointerException();

        //Initialization
        InputStream is = new FileInputStream(file);
        //this.channel = ((FileInputStream) is).getChannel();
        this.format = format;

        //Wrapping stream if UnCompression needed
        try {
            is = ct.createInputStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Creating main reder
        this.reader = new BufferedReader(new InputStreamReader(is));
    }*/

    //Additional constructors

    /**
     * Creates a {@link SSequencingRead} stream from a FASTQ files with single-end read data
     *
     * @param fileName name of file containing reads
     * @param format   read quality encoding format
     * @throws IOException in case there is problem with reading from files
     */
    public SFastqReader(String fileName, QualityStringFormat format) throws IOException {
        this(fileName, format, CompressionType.None);
    }

    /**
     * Creates a {@link SSequencingRead} stream from a FASTQ files with single-end read data
     *
     * @param fileName name of file containing reads
     * @param format   read quality encoding format
     * @param ct       type of compression (NONE, GZIP, etc)
     * @throws IOException in case there is problem with reading from files
     */
    public SFastqReader(String fileName, QualityStringFormat format, CompressionType ct) throws IOException {
        this(new File(fileName), format, ct);
    }

    /**
     * Creates a {@link SSequencingRead} stream from a FASTQ files with single-end read data
     *
     * @param file   file with reads
     * @param format read quality encoding format
     * @throws IOException in case there is problem with reading from files
     */
    public SFastqReader(File file, QualityStringFormat format) throws IOException {
        this(file, format, CompressionType.None);
    }

    @Override
    public SSequencingRead _take() {
        //Read serial id
        long id;
        SSequencingRead read;

        while (true) {
            //Read all 4 raw lines before parsing in synchronized block
            String[] lines = new String[4];
            synchronized (reader) {
                try {
                    for (int i = 0; i < 4; ++i)
                        if ((lines[i] = reader.readLine()) == null)
                            break;

                    //Close condition
                    if (lines[0] == null) {
                        reader.close();
                        return null;
                    }

                    //getting an id for future sequence
                    id = counter++;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            read = parse(format, lines, id);

            if (infoProvider != null) {
                IlluminaReadInfo info = infoProvider.getInfo(read.getDescription());

                if (info.isFiltered())
                    continue;
            }

            return read;
        }
    }

    public static SSequencingRead parse(QualityStringFormat format, String[] lines, long id) {
        String descriptionLine = lines[0];

        //Parsing:

        //Error check...
        if (descriptionLine.charAt(0) != '@')
            throw new RuntimeException("Wrong file format");

        //Remove '@' from description
        descriptionLine = descriptionLine.substring(1);

        //Reading sequence, plusline, quality
        String sequenceLine = lines[1];
        String plusLine = lines[2];
        String qualityString = lines[3];

        //First check
        if (sequenceLine == null || plusLine == null || qualityString == null
                || plusLine.charAt(0) != '+')
            throw new RuntimeException("Wrong file format");

        //Creating quality
        SequenceQuality quality;
        try {
            quality = format.getQualityFactory().create(qualityString.getBytes());
        } catch (WrongQualityStringException ex) {
            throw new RuntimeException("Error while parsing quality", ex);
        }

        //Dot correction
        //if (sequenceLine.contains(".")
        //        || sequenceLine.contains("n")
        //        || sequenceLine.contains("N")) {

        char[] seqChars = sequenceLine.toCharArray();
        for (int i = 0; i < seqChars.length; ++i)
            if (seqChars[i] == '.' || seqChars[i] == 'n' || seqChars[i] == 'N') {
                //Substituting '.'/'n'/'N' with A
                seqChars[i] = 'A';
                //and setting bad quality to this nucleotide
                quality.setRawQualityValue(i, format.getZeroQualityValue());
                //increment corresponding counter
                //dotCorrections.incrementAndGet();
            }

        //    //Creating new sequence line
        //    sequenceLine = new String(seqChars);
        //}

        //Parsing sequence
        NucleotideSequence sequence;
        try {
            sequence = NucleotideSequenceImpl.fromSequence(seqChars);
        } catch (RuntimeException re) {
            throw new RuntimeException("Error while parsing sequence.", re);
        }

        //Additional check
        if (sequence.size() != quality.size())
            throw new RuntimeException("Wrong file format. Different sequence and quality sizes.");

        //Refreshing redPrecent field for status update
        //refreshRedPercent();

        return new SSequencingReadImpl(descriptionLine, new NucleotideSQPair(sequence, quality), id);
    }

    public QualityStringFormat getQualityStringFormat() {
        return format;
    }

    /**
     * Closes the output port
     */
    @Override
    public void _close() {
        //is synchronized with itself and _next calls,
        //so no synchronization on innerReader is needed
        try {
            reader.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public double getProgress() {
        return ((double) countingStream.getBytesRead()) / totalSize;
    }

    @Override
    public boolean isFinished() {
        return closed;
    }
}

