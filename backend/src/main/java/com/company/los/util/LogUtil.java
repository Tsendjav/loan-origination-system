package com.company.los.util;

/**
 * Log Utility class for masking sensitive data in logs.
 * Логт нууц мэдээллийг масклах туслах класс.
 *
 * @author LOS Development Team
 * @version 1.0
 * @since 2025-08-04
 */
public class LogUtil {

    /**
     * Нууц мэдээллийг (жишээ нь, хэрэглэгчийн нэр) логт харуулахгүйгээр масклах функц.
     * Эхний 3 үсгийг хадгалж, үлдсэнийг нь "****" болгоно.
     *
     * @param data Масклах ёстой мэдээлэл (жишээ нь, хэрэглэгчийн нэр)
     * @return Масклагдсан мэдээлэл
     */
    public static String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        if (data.length() > 3) {
            return data.substring(0, 3) + "****";
        }
        return "****"; // 3-аас бага урттай бол бүгдийг нь масклана
    }
}