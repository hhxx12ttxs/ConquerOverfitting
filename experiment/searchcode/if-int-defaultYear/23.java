package com.trinea.sns.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.trinea.java.common.MapUtils;
import com.trinea.sns.util.QqTConstant;

/**
 * ??????????????
 * http://wiki.open.t.qq.com/index.php/%E5%B8%90%E6%88%B7%E7%9B%B8%E5%85%B3/%E6%9B%B4%E6%96%B0%E7%94%A8%E6%88%B7%E6%95%
 * 99%E8%82%B2%E4%BF%A1%E6%81%AF
 * 
 * @author Trinea 2011-10-29 ??06:51:15
 */
public class QqTUserEduPara implements Serializable {

    private static final long serialVersionUID    = 3298635316340763420L;

    /** ??????? **/
    private String            format;

    /**
     * ??????id
     * ??????feildId=1
     * ??????????feildId
     * ??????????????
     **/
    private long              feildId;

    /** ??? **/
    private String            year;

    /** ??id **/
    private long              schoolId;

    /** ??id **/
    private long              departmentId;

    /** ?????1?????2?????3?????4?????5?????6???? **/
    private int               level;

    /** ??? **/
    private static String     defaultFormat       = "";
    private static long       defaultFeildId      = -1;
    private static String     defaultYear         = "";
    private static long       defaultSchoolId     = -1;
    private static long       defaultDepartmentId = -1;
    private static int        defaultLevel        = -1;

    public QqTUserEduPara(){
        super();

        this.format = defaultFormat;
        this.feildId = defaultFeildId;
        this.year = defaultYear;
        this.schoolId = defaultSchoolId;
        this.departmentId = defaultDepartmentId;
        this.level = defaultLevel;
    }

    /**
     * ????????????api???map
     * 
     * @return
     */
    public Map<String, String> getParasMap() {
        Map<String, String> parasMap = new HashMap<String, String>();
        MapUtils.putMapNotEmptyValue(parasMap, QqTConstant.PARA_FORMAT, format);
        MapUtils.putMapNotEmptyValue(parasMap, QqTConstant.PARA_USER_EDU_YEAR, year);
        if (feildId >= 0) {
            parasMap.put(QqTConstant.PARA_USER_EDU_FEILD_ID, Long.toString(feildId));
        }
        if (schoolId >= 0) {
            parasMap.put(QqTConstant.PARA_USER_EDU_SCHOOL_ID, Long.toString(schoolId));
        }
        if (departmentId >= 0) {
            parasMap.put(QqTConstant.PARA_USER_EDU_DEPARTMENT_ID, Long.toString(departmentId));
        }
        if (level >= 0) {
            parasMap.put(QqTConstant.PARA_USER_EDU_LEVEL, Long.toString(level));
        }
        return parasMap;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public long getFeildId() {
        return feildId;
    }

    public void setFeildId(long feildId) {
        this.feildId = feildId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(long schoolId) {
        this.schoolId = schoolId;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}

