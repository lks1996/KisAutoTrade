package com.example.boot.KisAutoTrade.Common;

public class Parser {

    /**
     * 구글 시트 데이터 값 검증.
     * @param value
     * @return
     */
    public static Double safeParseDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        try {
            String str = value.toString().trim();
            if (str.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0; // 혹은 null 반환도 가능
        }
    }
}
