package org.adaptlab.chpir.android.survey.verhoeff;

public class VerhoeffErrorDetection {
    private static final String TAG = "VerhoeffErrorDetection";
    // The multiplication table
    static int[][] d = new int[][]
            {
                    {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                    {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
                    {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
                    {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
                    {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
                    {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
                    {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
                    {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
                    {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
                    {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}
            };
    // The permutation table
    static int[][] p = new int[][]
            {
                    {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                    {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
                    {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
                    {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
                    {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
                    {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
                    {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
                    {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}
            };

    private static boolean doCheck(int[] a) {
        int c = 0;
        for (int i = 0; i < a.length; i++) {
            c = d[c][p[(i % 8)][a[i]]];
        }
        return (c == 0);
    }

    private static int[] generateCheckArray(String checkString) {
        String[] splitString = checkString.split("-");
        int[] checkArray = new int[6];
        String[] facilityId = splitString[0].split("");
        int start = facilityId.length == 2 ? 0 : 1;
        checkArray[5] = Integer.parseInt(facilityId[start]);
        checkArray[4] = Integer.parseInt(facilityId[start + 1]);

        String[] participantId = splitString[1].split("");
        start = participantId.length == 3 ? 0 : 1;
        checkArray[3] = Integer.parseInt(participantId[start]);
        checkArray[2] = Integer.parseInt(participantId[start + 1]);
        checkArray[1] = Integer.parseInt(participantId[start + 2]);

        checkArray[0] = ((int) splitString[2].charAt(0)) - 65;

        return checkArray;
    }

    /*
     * Format: ## - %%% - @
     *
     * # = Two-digit site numeric ID
     * % = Three-digit participant numeric ID
     * @ = One check-digit letter
     */
    public boolean performCheck(String checkString) {
        if (checkString.matches("\\d{2}\\-\\d{3}\\-[a-z]")) {
            char lastChar = checkString.charAt(checkString.length() - 1);
            checkString = checkString.substring(0, checkString.length() - 1) + Character.toUpperCase(lastChar);
        }
        if (!checkString.matches("\\d{2}\\-\\d{3}\\-[A-J]")) {
            return false;
        }
        return doCheck(generateCheckArray(checkString));
    }
}
