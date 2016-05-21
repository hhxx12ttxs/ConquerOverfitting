            if (StreamReader.NON_PRINTABLE.matcher(value).find()) {
                tag = Tag.BINARY;
            String value;
            if (data instanceof Byte || data instanceof Short || data instanceof Integer
                    || data instanceof Long || data instanceof BigInteger) {
            buffer.append(\"-\");
                return representSequence(Tag.SEQ, asShortList(data), null);
            } else if (int.class == type) {
                return representSequence(Tag.SEQ, asIntList(data), null);
                buffer.append(\"0\");
            if (months < 10) {
            int years = calendar.get(Calendar.YEAR);
            int months = calendar.get(Calendar.MONTH) + 1; // 0..12
            int days = calendar.get(Calendar.DAY_OF_MONTH); // 1..31
            String value = data.toString();

