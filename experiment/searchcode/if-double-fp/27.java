private int fp_scheduler;//航班班期
private int fp_base_price;//航班计划基准票价
private double fp_season_discount;//经济舱季节折扣
FlightPlan other = (FlightPlan) obj;
if (fp_id != other.fp_id)
return false;
return true;
}
public int getFp_id() {

