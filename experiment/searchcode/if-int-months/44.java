 * @param date
 *            The Date to modify
 * @param months
 */
public static Date addMonths(Date date, int months) {
return new Date(date.getYear(), date.getMonth() + months, date
 * @param date
 *            The Date to modify
 * @param days
 *            Number of day to add
 * @return The modified Date object
 */
public static Date addDays(Date date, int days) {
return new Date(date.getYear(), date.getMonth(), date.getDate() + days);
/**
 * Add months to the Date object.
 * 

