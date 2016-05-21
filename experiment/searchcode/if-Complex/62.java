            _out.append(\"\\\\[\\\p{\");
        _out.append(texify(tfs.typeName()));
        if (complex)
            _out.append(\"}\");
        if (!tree.isLeaf())
            _out.append(\"\\\\end{bundle}\\n\");
 *
 *  Gralej is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
        }
        if (complex)
            _out.append(\"\\\\]\\n\");
        boolean complex = tfs.featureValuePairs().iterator().hasNext();
        if (complex)

