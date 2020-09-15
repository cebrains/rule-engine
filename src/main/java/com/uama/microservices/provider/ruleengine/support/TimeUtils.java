package com.uama.microservices.provider.ruleengine.support;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-24 17:37
 **/
public class TimeUtils {
    private TimeUtils() {}
    
    public static String secondToRunningTime(long second) {
        String minStr = " min ";
        long days = second / 86400;
        second = second % 86400;
        long hours = second / 3600;
        second = second % 3600;
        long minutes = second / 60;
        second = second % 60;
        if (days > 0) {
            return days + " day " + hours + " hour " + minutes + minStr + second + " sec";
        } else if (hours > 0) {
            return hours + " hour " + minutes + minStr + second + " sec";
        } else if (minutes > 0) {
            return minutes + minStr + second + " sec";
        } else {
            return second + " sec";
        }
    }
}
