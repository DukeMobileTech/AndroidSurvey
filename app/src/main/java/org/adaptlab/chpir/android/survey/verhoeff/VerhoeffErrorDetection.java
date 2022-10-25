package org.adaptlab.chpir.android.survey.verhoeff;

public class VerhoeffErrorDetection {
    private static final int[][] op = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
            {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
            {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
            {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
            {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
            {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
            {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
            {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
            {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}};

    private static final int[][] F = new int[8][];
    private static final int[] F0 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] F1 = {1, 5, 7, 6, 2, 8, 3, 0, 9, 4};

    public VerhoeffErrorDetection() {
        F[0] = F0;
        F[1] = F1;
        for (int i = 2; i < 8; i++) {
            F[i] = new int[10];
            for (int j = 0; j < 10; j++)
                F[i][j] = F[i - 1][F[1][j]];
        }
    }

    private static boolean doCheck(int[] a) {
        int check = 0;
        for (int i = 0; i < a.length; i++)
            check = op[check][F[i % 8][a[i]]];
        return check == 0;
    }

    /*
     * Format: $ - ### - %% - @
     *
     * $ = One letter to indicate participant type
     * # = Three-digit facility numeric ID
     * % = Two-digit participant numeric ID
     * @ = One check-digit letter
     */
    public boolean performCheck(String checkString) {
        if (!checkString.matches("^[A-Z]\\-\\d{3}\\-\\d{2}\\-[A-J]$")) {
            return false;
        }

        return doCheck(generateCheckArray(checkString));
    }

    private int[] generateCheckArray(String checkString) {
        String[] splitString = checkString.split("-");

        int[] checkArray = new int[8];
        int charToAscii = splitString[0].charAt(0);
        checkArray[7] = charToAscii / 10;
        checkArray[6] = charToAscii % 10;

        String[] facilityId = splitString[1].split("");
        checkArray[5] = Integer.parseInt(facilityId[1]);
        checkArray[4] = Integer.parseInt(facilityId[2]);
        checkArray[3] = Integer.parseInt(facilityId[3]);

        String[] participantId = splitString[2].split("");
        checkArray[2] = Integer.parseInt(participantId[1]);
        checkArray[1] = Integer.parseInt(participantId[2]);

        checkArray[0] = (int) splitString[3].charAt(0) - 65;

        return checkArray;
    }
}
