        int days = (int)BitsLong.unpack(v, DAY, DAY+DAY_LEN) ;
        sb.append('-') ;
        NumberUtils.formatInt(sb, months, 2) ;
        sb.append('-') ;
    // Const-ize
    static final int DATE_LEN = 22 ;    // 13 bits year, 4 bits month, 5 bits day => 22 bits
    static final int TIME_LEN = 27 ;    // 5 bits hour + 6 bits minute + 16 bits seconds (to millisecond)
import org.openjena.atlas.lib.BitsInt ;
import org.openjena.atlas.lib.BitsLong ;
        int years = (int)BitsLong.unpack(v, YEAR, YEAR+YEAR_LEN) ;
        int months = (int)BitsLong.unpack(v, MONTH, MONTH+MONTH_LEN) ;
        int days = (int)BitsLong.unpack(v, DAY, DAY+DAY_LEN) ;
        int years = (int)BitsLong.unpack(v, YEAR, YEAR+YEAR_LEN) ;
        int months = (int)BitsLong.unpack(v, MONTH, MONTH+MONTH_LEN) ;

