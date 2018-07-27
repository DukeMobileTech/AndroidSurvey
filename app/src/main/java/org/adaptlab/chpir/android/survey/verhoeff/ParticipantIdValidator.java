package org.adaptlab.chpir.android.survey.verhoeff;

public class ParticipantIdValidator {

    public static boolean validate(String value) {
        VerhoeffErrorDetection verhoeff = new VerhoeffErrorDetection();
        return verhoeff.performCheck(value);
    }

    public static String formatText(String text) {
        text = text.toUpperCase();

        if (text.matches("[A-Z]") ||
                text.matches("[A-Z]\\-\\d{3}") ||
                text.matches("[A-Z]\\-\\d{3}\\-\\d{2}")) {
            text = text + "-";
        } else if (text.matches("[A-Z]\\d")) {
            char lastChar = text.charAt(text.length() - 1);
            text = text.substring(0, 1) + "-" + lastChar;
        } else if (text.matches("[A-Z]\\-\\d{4}")) {
            char lastChar = text.charAt(text.length() - 1);
            text = text.substring(0, 5) + "-" + lastChar;
        } else if (text.matches("[A-Z]\\-\\d{3}\\-\\d{2}[A-Z]")) {
            char lastChar = text.charAt(text.length() - 1);
            text = text.substring(0, 8) + "-" + lastChar;
        }

        return text;
    }
}
