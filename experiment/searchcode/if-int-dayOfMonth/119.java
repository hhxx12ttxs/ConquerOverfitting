package com.iguan.model.util.redis;

import com.iguan.model.dto.DeltaTimeDto;

import java.io.IOException;
import java.util.*;

/**
 * Created by Iguan on 10/10/2014.
 */
public class RedisManager extends Redis {


    private static String key ;

    public RedisManager(String dbName) {
        super("localhost");
        this.key = dbName;
    }

    public RedisManager(String host, Integer port,String dbName) {
        super(host, port);
        this.key = dbName;
    }

    public void setDeltaTimeDto(DeltaTimeDto dto){

        super.set(key,getDate(dto.getDay()),dto);
    }

    public List<DeltaTimeDto> getTodayDeltaTimeDto(){
       Map<String,List<DeltaTimeDto>> map = super.get(key);
        String day = getDate(new Date());
        if (map != null) {
            if (map.containsKey(day)) {
                return map.get(day);
            }
        }
        return null;
    }

    public List<DeltaTimeDto> getThisWeekDeltaTimeDto(){
        List<DeltaTimeDto> list = new ArrayList<>();
        List<DeltaTimeDto> li ;
        Map<String,List<DeltaTimeDto>> map = super.get(key);
        if (map != null) {
             String [] days = getThisWeekDays();
            for (int i = 0; i< days.length; i++){
                if (map.containsKey(days[i])) {
                    li = map.get(days[i]);
                    for (int j = 0; j < li.size(); j++) {
                        list.add(li.get(j));
                    }
                }
            }
           /* Calendar c = Calendar.getInstance();
            int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            int lastDayOfWeekInMonth = getLastDayOfWeekInMonth();
            int minus = dayOfMonth - lastDayOfWeekInMonth;
            if (minus < 0) {
                int count = 0;
                while (dayOfMonth > 0) {
                    if (map.containsKey(String.valueOf(dayOfMonth))) {
                        li = map.get(String.valueOf(dayOfMonth));
                        for (int i = 0; i < li.size(); i++) {
                            list.add(li.get(i));
                        }
                    }
                    dayOfMonth--;
                    count++;
                }
                int m = 7 - count;
                while (m >= 0) {
                    if (map.containsKey(String.valueOf(lastDayOfWeekInMonth))) {
                        li = map.get(String.valueOf(lastDayOfWeekInMonth));
                        lastDayOfWeekInMonth++;
                        for (int i = 0; i < li.size(); i++) {
                            list.add(li.get(i));
                        }
                    }
                    m--;
                }
            } else {
                while (minus >= 0) {
                    if (map.containsKey(String.valueOf(lastDayOfWeekInMonth))) {
                        li = map.get(String.valueOf(lastDayOfWeekInMonth));

                        for (int i = 0; i < li.size(); i++) {
                            list.add(li.get(i));
                        }
                    }
                    lastDayOfWeekInMonth++;
                    minus--;
                }
            }*/
        }else {
            return null;
        }
        return list;
    }

    public void updateDeltaMemo(String memo,String day,String deltaTimeNumber){
        Map<String,List<DeltaTimeDto>> map = super.get(key);
        List<DeltaTimeDto> list;
        if (map != null){
           if (map.containsKey(day)){
               list = map.get(day);
               for (int i = 0; i <list.size() ; i++) {
                   if (deltaTimeNumber.equals(list.get(i).getDeltaTimeNumber())){
                       list.get(i).setMemo(memo);
                   }
               }
           }
        }

    }

    public void deleteDeltaTime(String day,String deltaTimeNumber){
        Map<String,List<DeltaTimeDto>> map = super.get(key);
        List<DeltaTimeDto> list;
        if (map != null){
            if (map.containsKey(day)){
                list = map.get(day);
                for (int i = 0; i <list.size() ; i++) {
                    if (deltaTimeNumber.equals(list.get(i).getDeltaTimeNumber())){
                        list.remove(i);
                    }
                }
            }
        }
    }
    public void deleteDeltaTimeDtoInDay(String day){
        Map<String,List<DeltaTimeDto>> map = super.get(key);
        if (map != null){
            if (map.containsKey(day)) {
                  map.remove(day);
            }
        }
    }




    public List<DeltaTimeDto> getDeltaTimeDto(String day,String deltaTimeNumber){
        Map<String,List<DeltaTimeDto>> map = super.get(key);
        List<DeltaTimeDto> resultList = new ArrayList<>();
        List<DeltaTimeDto> list;
        if (map != null){
            if (map.containsKey(day)){
                list = map.get(day);
                for (int i = 0; i <list.size() ; i++) {
                    if (deltaTimeNumber.equals(list.get(i).getDeltaTimeNumber())){
                        resultList.add(list.get(i));
                    }
                }
            }
        }
        return resultList;
    }

    public List<DeltaTimeDto> getAllDeltaTimeDto(){
        Map<String,List<DeltaTimeDto>> map = super.get(key);
        List<DeltaTimeDto> resultList = new ArrayList<>();
        for (Map.Entry<String,List<DeltaTimeDto>> entry : map.entrySet()){
            resultList.addAll(entry.getValue());
        }
        return resultList;
    }

    public void delete(){
        try {
            super.delete(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





/*
    private int getLastDayOfWeekInMonth(){
        Calendar c = Calendar.getInstance();
        int lastDayOfWeek ;

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1){
            dayOfWeek = 7;
        }else {
            dayOfWeek -= 1;
        }
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int dayOfWeekInMonth = c.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        lastDayOfWeek = dayOfMonth - dayOfWeek + 1;

        switch (dayOfWeekInMonth){
            case 1:
                if (lastDayOfWeek<=0){
                    lastDayOfWeek =  lastDayOfWeek + c.getActualMaximum(Calendar.MONTH - 1);
                }
                break;
            case 4:
                if (lastDayOfWeek<=0){
                    lastDayOfWeek =  lastDayOfWeek + c.getActualMaximum(Calendar.MONTH - 1);
                }
                break;
            case 5:
                if (lastDayOfWeek<=0){
                    lastDayOfWeek =  lastDayOfWeek + c.getActualMaximum(Calendar.MONTH - 1);
                }
                break;
        }
        return lastDayOfWeek;
    }*/

    private static String [] getThisWeekDays(){
        String [] result = null;
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        boolean isYear = false;

        int year = cal.get(Calendar.YEAR);
        int month  = cal.get(Calendar.MONTH)+1;

        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int lastDayOfWeek ;

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1){
            dayOfWeek = 7;
        }else {
            dayOfWeek -= 1;
        }

        int dayOfWeekInMonth = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        lastDayOfWeek = dayOfMonth - dayOfWeek + 1;

        switch (dayOfWeekInMonth){
            case 1:
                if (lastDayOfWeek<=0){
                    if (month == 1){
                        month = 12;
                        year -= 1;
                        isYear = true;
                        cal.set(year, month, 0);
                        lastDayOfWeek = lastDayOfWeek + cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }else {
                        month -= 1;
                        cal.set(year, month, 0);
                        lastDayOfWeek = lastDayOfWeek + cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }
                }
                break;

        }

        if (lastDayOfWeek < dayOfMonth){
            result = new String[dayOfWeek];
            for (int i = 0; i< result.length; i++){
                result[i] = year+"-"+month +"-" +lastDayOfWeek++;
            }
        }else {
            if (month != 12) {
                result = new String[dayOfWeek];
                for (int i = 0; i < result.length; i++) {
                    if (lastDayOfWeek <= cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {

                        result[i] = year + "-" + cal.get(Calendar.MONTH) + "-" + lastDayOfWeek++;


                    } else {
                        lastDayOfWeek = 1;
                        if (isYear)
                            if (lastDayOfWeek <= dayOfMonth) {
                                if (isYear){
                                    isYear = false;
                                    year +=1;
                                }
                                cal.set(year,month+1,dayOfMonth);
                                result[i] = year + "-" + cal.get(Calendar.MONTH) + "-" + lastDayOfWeek++;
                            }
                    }


                }
            }
        }




        return result;
    }

    private String getDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return year+"-"+month +"-" +day;
    }
}

