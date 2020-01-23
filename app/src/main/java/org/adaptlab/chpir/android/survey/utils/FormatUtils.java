package org.adaptlab.chpir.android.survey.utils;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class FormatUtils {
    public static String pluralize(int number, String singular, String plural) {
        if (number == 1) {
            return singular;
        } else {
            return plural;
        }
    }

    public static String formatDate(int month, int day, int year) {
        return ((month + 1) + "-" + day + "-" + year);
    }

    public static String formatDate(int month, int year) {
        return ((month + 1) + "-" + year);
    }

    public static GregorianCalendar unFormatDate(String date) {
        if (date.equals("")) return null;
        String[] dateComponents = date.split("-");
        int month, day, year;
        if (dateComponents.length == 3) {
            // Full date
            month = Integer.parseInt(dateComponents[0]) - 1;
            day = Integer.parseInt(dateComponents[1]);
            year = Integer.parseInt(dateComponents[2]);
        } else {
            // Just year and month
            month = Integer.parseInt(dateComponents[0]) - 1;
            year = Integer.parseInt(dateComponents[1]);
            day = 1; // not used           
        }
        return new GregorianCalendar(year, month, day);
    }

    // Format: HH:MM, 24 hour format
    public static String formatTime(int hour, int minute) {
        return hour + ":" + formatMinute(minute);
    }

    public static int[] unformatTime(String time) {
        if (time.equals("")) return null;
        String[] timeComponents = time.split(":");
        int hour = Integer.parseInt(timeComponents[0]);
        int minute = Integer.parseInt(timeComponents[1]);
        return new int[]{hour, minute};
    }

    // Add a 0 to minute values less than 10 to look more natural
    private static String formatMinute(int minute) {
        if (minute < 10) {
            return "0" + minute;
        } else {
            return String.valueOf(minute);
        }
    }

    public static Spanned styleTextWithHtml(String text) {
        text = text.replaceFirst("<p>", "<br>")
                .replace("<p>", "")
                .replace("</p>","<br><br>");
        int index = text.lastIndexOf("<br>");
        if (index > -1) text = text.substring(0, index);
        return Html.fromHtml(text);
    }

    public static Spanned styleTextWithHtmlWhitelist(String text) {
        text = text.replace("<p>", "").replace("</p>","");
        return Html.fromHtml(text);
    }

    public static String removeNonNumericCharacters(String string) {
        return string.replaceAll("[^\\d.]", "");
    }

    public static boolean isEmpty(String string) {
        return (TextUtils.isEmpty(string) || string.toLowerCase().equals("null"));
    }

    public static String arrayListToString(ArrayList list) {
        StringBuilder serialized = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            serialized.append(list.get(i));
            if (i < list.size() - 1) serialized.append(COMMA);
        }
        return serialized.toString();
    }
}