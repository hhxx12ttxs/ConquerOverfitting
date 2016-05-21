                state.setMonth(tmpMon);
            } else if (!state.isYearSet()) {
            String lstr = str.toLowerCase();
            for (int i = 0; i < MONTHS.length; i++) {
                if (lstr.startsWith(MONTHS[i][0])
                        || MONTHS[i][1].toLowerCase().startsWith(lstr)) {
        // if either year or month is unset...
        if (!state.isYearSet() || !state.isMonthSet()) {
            // if day can't be a month, shift it into year
            if (state.getDate() > 12) {
                if (!state.isMonthSet() && !state.isYearBeforeMonth()) {
                    state.setMonth(state.getDate());
            // if month number is unset, set it and move on
            if (!state.isMonthSet()) {

