package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Option;

import java.util.GregorianCalendar;
import java.util.List;

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
    
    public static GregorianCalendar unformatDate(String date) {
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
    public static String formatMinute(int minute) {
        if (minute < 10) {
            return "0" + minute;
        } else {
            return String.valueOf(minute);
        }
    }
    
    public static String unformatMultipleResponses(List<Option> options, String responseText, Context context, Instrument instrument) {
        String[] responses = responseText.split(",");
        String multipleText = "";
        for (int i = 0; i < responses.length; i++) {
            multipleText += options.get(Integer.parseInt(responses[i])).getText(instrument);
            if (i < responses.length - 2) multipleText += context.getString(R.string.comma) + context.getString(R.string.space);
            else if (i == responses.length - 2) multipleText += context.getString(R.string.space)
                    + context.getString(R.string.and) + context.getString(R.string.space);
        }
        return multipleText;
    }

    public static String stripHtml(String withHtml) {
        return Html.fromHtml(withHtml).toString().trim();
    }

    public static Spanned styleTextWithHtml(String text) {
        return Html.fromHtml(text);
    }

    public static String removeNonNumericCharacters(String string) {
        return string.replaceAll("[^\\d.]", "");
    }
}