package efi.unleashed.util;

import efi.unleashed.domain.DateRange;

import org.joda.time.LocalDate;
import org.joda.time.DateTimeConstants;

/**
 * This class is used to create instances of DateRange for specific date range types.
 */
public class DateRangeUtils {

    private DateRangeUtils() {
    }

    /**
     * Answer a date range for given DateRangeIndicator relative to "today".
     *
     * @param indicator a DateRangeIndicator
     * @return a DateRange
     */
    public static DateRange dateRangeForIndicator( DateRangeIndicator indicator ) {
        LocalDate today = new LocalDate();
        return dateRangeForIndicator( indicator, today );
    }

    /**
     * Answer a date range for given DateRangeIndicator and the given LocalDate.
     *
     * @param indicator a DateRangeIndicator
     * @param aDate     a LocalDate, the relative date for calculating the date range
     * @return a DateRange
     */
    public static DateRange dateRangeForIndicator( DateRangeIndicator indicator, LocalDate aDate ) {
        DateRange dateRange;
        switch ( indicator ) {
        case CURRENT_WEEK:
            dateRange = currentWeekForDate( aDate );
            break;
        case CURRENT_MONTH:
            dateRange = currentMonthForDate( aDate );
            break;
        case CURRENT_QUARTER:
            dateRange = currentQuarterForDate( aDate );
            break;
        case CURRENT_YEAR:
            dateRange = currentYearForDate( aDate );
            break;
        case LAST_WEEK:
            dateRange = lastWeekForDate( aDate );
            break;
        case LAST_MONTH:
            dateRange = lastMonthForDate( aDate );
            break;
        case LAST_QUARTER:
            dateRange = lastQuarterForDate( aDate );
            break;
        case LAST_YEAR:
            dateRange = lastYearForDate( aDate );
            break;
        case NEXT_WEEK:
            dateRange = nextWeekForDate( aDate );
            break;
        case NEXT_MONTH:
            dateRange = nextMonthForDate( aDate );
            break;
        case NEXT_QUARTER:
            dateRange = nextQuarterForDate( aDate );
            break;
        case NEXT_YEAR:
            dateRange = nextYearForDate( aDate );
            break;
        case MONTH_TO_DATE:
            dateRange = monthToDateForDate( aDate );
            break;
        case QUARTER_TO_DATE:
            dateRange = quarterToDateForDate( aDate );
            break;
        case YEAR_TO_DATE:
            dateRange = yearToDateForDate( aDate );
            break;
        case TODAY:
            dateRange = todayForDate( aDate );
            break;
        case YESTERDAY:
            dateRange = yesterdayForDate( aDate );
            break;
        case TOMORROW:
            dateRange = tomorrowForDate( aDate );
            break;
        default:
            dateRange = todayForDate( aDate );
            break;
        }
        return dateRange;
    }

    /**
     * Answer a date range for the week (Sunday - Saturday) containing the given date.
     *
     * @param aDate the date in the desired week.
     * @return a DateRange for the week of Sunday - Saturday that contains aDate.
     */
    public static DateRange currentWeekForDate( LocalDate aDate ) {
        /**
         * Set the index of the first day of the week. We use Sunday by default.
         * In the future when we have a property profile, let the user choose this value.
         */
        int weekStartIndex = DateTimeConstants.SUNDAY;
        int dayIndex = aDate.getDayOfWeek();
        int dayOffset = dayIndex - weekStartIndex;

        if ( dayOffset < 0 ) {
            dayOffset = 7 + dayOffset;
        }

        LocalDate startDate = aDate.minusDays( dayOffset );
        LocalDate endDate = startDate.plusDays( 6 );

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the month containing the given date.
     *
     * @param aDate the date in the desired month.
     * @return a DateRange for the month that contains aDate.
     */
    public static DateRange currentMonthForDate( LocalDate aDate ) {
        LocalDate startDate = aDate.dayOfMonth().withMinimumValue();
        LocalDate endDate = aDate.dayOfMonth().withMaximumValue();

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the quarter containing the given date.
     *
     * @param aDate the date in the desired quarter.
     * @return a DateRange for the quarter that contains aDate.
     */
    public static DateRange currentQuarterForDate( LocalDate aDate ) {
        // find which quarter aDate is in
        int quarterIndex = (int) Math.ceil( aDate.getMonthOfYear() / 3.0 );

        LocalDate startDate = new LocalDate( aDate.getYear(), ( quarterIndex * 3 ) - 2, 1 );
        LocalDate endDate = new LocalDate( aDate.getYear(), ( quarterIndex * 3 ), 1 );
        endDate = endDate.dayOfMonth().withMaximumValue();

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the year containing the given date.
     *
     * @param aDate the date in the desired year.
     * @return a DateRange for the year that contains aDate.
     */
    public static DateRange currentYearForDate( LocalDate aDate ) {

        LocalDate startDate = new LocalDate( aDate.getYear(), 1, 1 );
        LocalDate endDate = new LocalDate( aDate.getYear(), 12, 31 );

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the week (Sunday - Saturday) prior to the one containing the given date.
     *
     * @param aDate the date in the week after the desired week.
     * @return a DateRange for the week of Sunday - Saturday prior to the one that contains aDate.
     */
    public static DateRange lastWeekForDate( LocalDate aDate ) {

        DateRange currentWeek = currentWeekForDate( aDate );
        LocalDate startDate = currentWeek.getStartDate().minusDays( 7 );
        LocalDate endDate = currentWeek.getEndDate().minusDays( 7 );

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the month prior to the one containing the given date.
     *
     * @param aDate the date in the month after the desired month.
     * @return a DateRange for the month prior to the one that contains aDate.
     */
    public static DateRange lastMonthForDate( LocalDate aDate ) {

        DateRange currentMonth = currentMonthForDate( aDate );
        LocalDate startDate = currentMonth.getStartDate().minusMonths( 1 );
        LocalDate endDate = startDate.dayOfMonth().withMaximumValue();

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the quarter prior to the one containing the given date.
     *
     * @param aDate the date in the quarter after the desired quarter.
     * @return a DateRange for the quarter prior to the one that contains aDate.
     */
    public static DateRange lastQuarterForDate( LocalDate aDate ) {

        DateRange currentQuarter = currentQuarterForDate( aDate );

        LocalDate startDate = currentQuarter.getStartDate().minusMonths( 3 );
        LocalDate endDate = startDate.plusMonths( 2 );
        endDate = endDate.dayOfMonth().withMaximumValue();

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the year prior to the one containing the given date.
     *
     * @param aDate the date in the year after the desired year.
     * @return a DateRange for the year prior to the one that contains aDate.
     */
    public static DateRange lastYearForDate( LocalDate aDate ) {

        int year = aDate.getYear() - 1;
        LocalDate startDate = new LocalDate( year, 1, 1 );
        LocalDate endDate = new LocalDate( year, 12, 31 );

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the week (Sunday - Saturday) after the one containing the given date.
     *
     * @param aDate the date in the week prior to the desired week.
     * @return a DateRange for the week of Sunday - Saturday after the one that contains aDate.
     */
    public static DateRange nextWeekForDate( LocalDate aDate ) {

        DateRange currentWeek = currentWeekForDate( aDate );
        LocalDate startDate = currentWeek.getStartDate().plusDays( 7 );
        LocalDate endDate = currentWeek.getEndDate().plusDays( 7 );

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the month after the one containing the given date.
     *
     * @param aDate the date in the month prior to the desired month.
     * @return a DateRange for the month after the one that contains aDate.
     */
    public static DateRange nextMonthForDate( LocalDate aDate ) {

        DateRange currentMonth = currentMonthForDate( aDate );
        LocalDate startDate = currentMonth.getStartDate().plusMonths( 1 );
        LocalDate endDate = startDate.dayOfMonth().withMaximumValue();

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the quarter after the one containing the given date.
     *
     * @param aDate the date in the quarter prior to the desired quarter.
     * @return a DateRange for the quarter after the one that contains aDate.
     */
    public static DateRange nextQuarterForDate( LocalDate aDate ) {

        DateRange currentQuarter = currentQuarterForDate( aDate );

        LocalDate startDate = currentQuarter.getStartDate().plusMonths( 3 );
        LocalDate endDate = startDate.plusMonths( 2 );
        endDate = endDate.dayOfMonth().withMaximumValue();

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the year after the one containing the given date.
     *
     * @param aDate the date in the year prior to the desired year.
     * @return a DateRange for the year after the one that contains aDate.
     */
    public static DateRange nextYearForDate( LocalDate aDate ) {

        int year = aDate.getYear() + 1;
        LocalDate startDate = new LocalDate( year, 1, 1 );
        LocalDate endDate = new LocalDate( year, 12, 31 );

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the month to date relative to the given date.
     *
     * @param aDate the date in the desired month.
     * @return a DateRange for the month to date ending on aDate.
     */
    public static DateRange monthToDateForDate( LocalDate aDate ) {

        LocalDate startDate = aDate.dayOfMonth().withMinimumValue();
        LocalDate endDate = aDate;

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the quarter to date relative to the given date.
     *
     * @param aDate the date in the desired quarter.
     * @return a DateRange for the quarter to date ending on aDate.
     */
    public static DateRange quarterToDateForDate( LocalDate aDate ) {

        DateRange currentQuarter = currentQuarterForDate( aDate );

        LocalDate startDate = currentQuarter.getStartDate();
        LocalDate endDate = aDate;

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range for the year to date relative to the given date.
     *
     * @param aDate the date in the desired year.
     * @return a DateRange for the year to date ending on aDate.
     */
    public static DateRange yearToDateForDate( LocalDate aDate ) {

        DateRange currentYear = currentYearForDate( aDate );

        LocalDate startDate = currentYear.getStartDate();
        LocalDate endDate = aDate;

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( startDate );
        dateRange.setEndDate( endDate );
        return dateRange;
    }

    /**
     * Answer a date range that starts and ends the given date.
     *
     * @param aDate the start and end date of the desired date range.
     * @return a DateRange.
     */
    public static DateRange todayForDate( LocalDate aDate ) {

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( aDate );
        dateRange.setEndDate( aDate );
        return dateRange;
    }

    /**
     * Answer a date range that starts and ends the day before the given date.
     *
     * @param aDate the day after the start and end date of the desired date range.
     * @return a DateRange.
     */
    public static DateRange yesterdayForDate( LocalDate aDate ) {

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( aDate.minusDays( 1 ) );
        dateRange.setEndDate( aDate.minusDays( 1 ) );
        return dateRange;
    }

    /**
     * Answer a date range that starts and ends the day after the given date.
     *
     * @param aDate the day before the start and end date of the desired date range.
     * @return a DateRange.
     */
    public static DateRange tomorrowForDate( LocalDate aDate ) {

        DateRange dateRange = new DateRange();
        dateRange.setStartDate( aDate.plusDays( 1 ) );
        dateRange.setEndDate( aDate.plusDays( 1 ) );
        return dateRange;
    }

}

