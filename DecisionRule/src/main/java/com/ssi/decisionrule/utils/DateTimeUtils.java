package com.ssi.decisionrule.utils;

import org.apache.log4j.Logger;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateTimeUtils {
    private static final Logger logger = Logger.getLogger(DateTimeUtils.class);

    private static final SimpleDateFormat ddMMYYYFormat = new SimpleDateFormat("dd-MM-yyyy");

    public static Date convertToSQLDate(String ddmmYYYY) {
        if (ddmmYYYY == null || ddmmYYYY.trim().isEmpty()) {
            return null;
        }
        try {
            java.util.Date parsed = ddMMYYYFormat.parse(ddmmYYYY);
            Date date = new java.sql.Date(parsed.getTime());
            return date;
        } catch (ParseException e) {
            logger.error(e, e);
        }
        return null;
    }

    public static String convertSQLDate2String(Date date) {
        if (date != null) {
            return ddMMYYYFormat.format(date);
        }
        return "";
    }
}
