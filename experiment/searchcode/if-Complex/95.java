            CHK_NL(parser.cursor);
            if(complex) {
                FORCE_NEXT_TOKEN( DefaultYAMLParser.YAML_IOPEN );
   private void CHK_NL(int ptr) {
       if(parser.buffer.buffer[ptr - 1] == '\\n' && ptr > parser.linectptr) {
           parser.lineptr = ptr;
                ++parser.cursor;
                if ((parser.limit - parser.cursor) < 2) parser.read();
                yych = parser.buffer.buffer[parser.cursor];
       public void cat(byte l) {
           if(idx + 1 >= capa) {
               capa += QUOTELEN;
            if(complex) {
                FORCE_NEXT_TOKEN( DefaultYAMLParser.YAML_IOPEN );
            CHK_NL(parser.cursor);

