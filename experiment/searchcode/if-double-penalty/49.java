double userBalance = (double) request.getSession().getAttribute(USER_BALANCE);
double penaltySum = penalty.getSum();
if (penaltySum > userBalance) {
isPayed = penaltyService.changePenaltyStatus(penalty, true);
if (isPayed) {
request.getSession().removeAttribute(PENALTY);

