package org.bitbucket.pusher.parser.units;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bitbucket.pusher.api.SubmitType;
import org.bitbucket.pusher.api.TypeFilePair;
import org.bitbucket.pusher.parser.ParsingContext;
import org.bitbucket.pusher.parser.ProcessingUnit;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class FileReferenceProcessingUnit implements ProcessingUnit {
    private Pattern fromPattern;
    private String toPattern;
    private SubmitType type;
    private boolean returnFile;

    public FileReferenceProcessingUnit(String fromPattern, String toPattern, SubmitType type, boolean returnFile) {
        this.fromPattern = Pattern.compile(fromPattern);
        this.toPattern = toPattern;
        this.type = type;
        this.returnFile = returnFile;
    }

    public boolean match(String text) {
        return fromPattern.matcher(text).find();
    }

    public Collection<File> process(String text, ParsingContext context) {
        Matcher m = fromPattern.matcher(text);
        String fileName = context.inlineVars(m.replaceAll(toPattern));

        File file = new File(context.getBaseDir(), fileName);

        context.getPairs().add(new TypeFilePair(type, file));

        if (returnFile) {
            return Arrays.asList(file);
        } else {
            return Collections.emptyList();
        }
    }
}

