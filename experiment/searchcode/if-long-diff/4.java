public class WarrantyDataCompareTime implements WarrantyDataCountedI {

private long diffTime = 0;


public boolean isCounted (WarrantyAuditCompareI a, WarrantyAuditCompareI b) {
boolean counted = false;
//long diff = (long) ( (double) ((a.getTime()-b.getTime()) / (double) 1000 ) ) ;

