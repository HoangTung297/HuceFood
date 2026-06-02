package com.example.foodorder.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * Chuyển chuỗi có dấu thành không dấu
     * @param s Chuỗi đầu vào
     * @return Chuỗi không dấu, viết thường
     */
    public static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(temp).replaceAll("");
        return result.toLowerCase().replaceAll("đ", "d");
    }

    /**
     * Kiểm tra source có chứa keyword (không phân biệt hoa thường, không dấu)
     * @param source Chuỗi nguồn (có thể có dấu)
     * @param keyword Từ khóa cần tìm (có thể không dấu)
     * @return true nếu chứa, false nếu không
     */
    public static boolean containsIgnoreCaseAndAccent(String source, String keyword) {
        if (source == null || keyword == null) return false;
        String sourceNoAccent = removeAccent(source);
        String keywordNoAccent = removeAccent(keyword);
        return sourceNoAccent.contains(keywordNoAccent);
    }
}