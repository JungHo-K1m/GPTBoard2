package com.example.gptboard2;

public class HangulAutomata {

    private static final int[] CHOSEONGS = {
            0x1100, // ㄱ
            0x1101, // ㄲ
            0x1102, // ㄴ
            0x1103, // ㄷ
            0x1104, // ㄸ
            0x1105, // ㄹ
            0x1106, // ㅁ
            0x1107, // ㅂ
            0x1108, // ㅃ
            0x1109, // ㅅ
            0x110A, // ㅆ
            0x110B, // ㅇ
            0x110C, // ㅈ
            0x110D, // ㅉ
            0x110E, // ㅊ
            0x110F, // ㅋ
            0x1110, // ㅌ
            0x1111, // ㅍ
            0x1112  // ㅎ
    };

    private static final int[] JUNGSEONGS = {
            0x1161, // ㅏ
            0x1162, // ㅐ
            0x1163, // ㅑ
            0x1164, // ㅒ
            0x1165, // ㅓ
            0x1166, // ㅔ
            0x1167, // ㅕ
            0x1168, // ㅖ
            0x1169, // ㅗ
            0x116A, // ㅘ
            0x116B, // ㅙ
            0x116C, // ㅚ
            0x116D, // ㅛ
            0x116E, // ㅜ
            0x116F, // ㅝ
            0x1170, // ㅞ
            0x1171, // ㅟ
            0x1172, // ㅠ
            0x1173, // ㅡ
            0x1174, // ㅢ
            0x1175  // ㅣ
    };

    private static final int[] JONGSEONGS = {
            0x0000, // Empty (No final consonant)
            0x11A8, // ㄱ
            0x11A9, // ㄲ
            0x11AA, // ㄳ
            0x11AB, // ㄴ
            0x11AC, // ㄵ
            0x11AD, // ㄶ
            0x11AE, // ㄷ
            0x11AF, // ㄹ
            0x11B0, // ㄺ
            0x11B1, // ㄻ
            0x11B2, // ㄼ
            0x11B3, // ㄽ
            0x11B4, // ㄾ
            0x11B5, // ㄿ
            0x11B6, // ㅀ
            0x11B7, // ㅁ
            0x11B8, // ㅂ
            0x11B9, // ㅄ
            0x11BA, // ㅅ
            0x11BB, // ㅆ
            0x11BC, // ㅇ
            0x11BD, // ㅈ
            0x11BE, // ㅊ
            0x11BF, // ㅋ
            0x11C0, // ㅌ
            0x11C1, // ㅍ
            0x11C2  // ㅎ
    };

    private int choseongIdx = -1;
    private int jungseongIdx = -1;
    private int jongseongIdx = -1;


    public String composeHangul(int primaryCode) {
        String result = "";

        if (isChoseong(primaryCode)) {
            if (choseongIdx == -1) {
                choseongIdx = indexOf(CHOSEONGS, primaryCode);
            } else if (jungseongIdx != -1) {
                result = String.valueOf(composeSyllable(choseongIdx, jungseongIdx, jongseongIdx));
                reset();
                choseongIdx = indexOf(CHOSEONGS, primaryCode);
            }
        } else if (isJungseong(primaryCode)) {
            if (choseongIdx != -1 && jungseongIdx == -1) {
                jungseongIdx = indexOf(JUNGSEONGS, primaryCode);
            } else if (jungseongIdx != -1) {
                result = String.valueOf(composeSyllable(choseongIdx, jungseongIdx, jongseongIdx));
                reset();
                choseongIdx = indexOf(CHOSEONGS, primaryCode);
                jungseongIdx = indexOf(JUNGSEONGS, primaryCode);
            }
        } else if (isJongseong(primaryCode)) {
            if (choseongIdx != -1 && jungseongIdx != -1) {
                jongseongIdx = indexOf(JONGSEONGS, primaryCode);
                result = String.valueOf(composeSyllable(choseongIdx, jungseongIdx, jongseongIdx));
            }
        } else {
            if (choseongIdx != -1 && jungseongIdx != -1) {
                result = String.valueOf(composeSyllable(choseongIdx, jungseongIdx, jongseongIdx));
            } else if (choseongIdx != -1) {
                result = String.valueOf((char) CHOSEONGS[choseongIdx]);
            }
            reset();
        }

        return result;
    }


    private int indexOf(int[] array, int primaryCode) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == primaryCode) {
                return i;
            }
        }
        return -1;
    }


    private char composeSyllable(int choseongIdx, int jungseongIdx, int jongseongIdx) {
        int syllableCode = 0xAC00 + (choseongIdx * 21 * 28) + (jungseongIdx * 28) + jongseongIdx;
        return (char) syllableCode;
    }



    private boolean isChoseong(int primaryCode) {
        for (int choseong : CHOSEONGS) {
            if (choseong == primaryCode) {
                return true;
            }
        }
        return false;
    }


    private boolean isJungseong(int primaryCode) {
        for (int jungseong : JUNGSEONGS) {
            if (jungseong == primaryCode) {
                return true;
            }
        }
        return false;
    }


    private boolean isJongseong(int primaryCode) {
        for (int jongseong : JONGSEONGS) {
            if (jongseong == primaryCode) {
                return true;
            }
        }
        return false;
    }


    private void reset() {
        choseongIdx = -1;
        jungseongIdx = -1;
        jongseongIdx = -1;
    }

    private int getChoseongIndex(int primaryCode) {
        for (int i = 0; i < CHOSEONGS.length; i++) {
            if (CHOSEONGS[i] == primaryCode) {
                return i;
            }
        }
        return -1;
    }

    private int getJungseongIndex(int primaryCode) {
        for (int i = 0; i < JUNGSEONGS.length; i++) {
            if (JUNGSEONGS[i] == primaryCode) {
                return i;
            }
        }
        return -1;
    }

    private int getJongseongIndex(int primaryCode) {
        for (int i = 0; i < JONGSEONGS.length; i++) {
            if (JONGSEONGS[i] == primaryCode) {
                return i;
            }
        }
        return -1;
    }


}
