package com.rental.common.util;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 密码工具类
 */
public class PasswordUtil {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private static final SecureRandom RANDOM = new SecureRandom();

    // 密码强度正则表达式
    private static final Pattern WEAK_PASSWORD = Pattern.compile("^.{1,7}$");
    private static final Pattern MEDIUM_PASSWORD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    private static final Pattern STRONG_PASSWORD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{12,}$");

    /**
     * 生成随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("密码长度至少为4位");
        }

        StringBuilder password = new StringBuilder();
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;

        // 确保包含各种字符类型
        password.append(getRandomChar(UPPERCASE));
        password.append(getRandomChar(LOWERCASE));
        password.append(getRandomChar(DIGITS));
        password.append(getRandomChar(SPECIAL_CHARS));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(getRandomChar(allChars));
        }

        // 打乱顺序
        return shuffleString(password.toString());
    }

    /**
     * 检查密码强度
     */
    public static PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.INVALID;
        }

        if (WEAK_PASSWORD.matcher(password).matches()) {
            return PasswordStrength.WEAK;
        } else if (STRONG_PASSWORD.matcher(password).matches()) {
            return PasswordStrength.STRONG;
        } else if (MEDIUM_PASSWORD.matcher(password).matches()) {
            return PasswordStrength.MEDIUM;
        } else {
            return PasswordStrength.WEAK;
        }
    }

    /**
     * 验证密码是否符合要求
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 50) {
            return false;
        }

        // 至少包含一个字母和一个数字
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasLetter && hasDigit;
    }

    /**
     * 生成默认密码
     */
    public static String generateDefaultPassword() {
        return generateRandomPassword(8);
    }

    private static char getRandomChar(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }

    private static String shuffleString(String string) {
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int randomIndex = RANDOM.nextInt(chars.length);
            char temp = chars[i];
            chars[i] = chars[randomIndex];
            chars[randomIndex] = temp;
        }
        return new String(chars);
    }

    /**
     * 密码强度枚举
     */
    public enum PasswordStrength {
        INVALID("无效"),
        WEAK("弱"),
        MEDIUM("中等"),
        STRONG("强");

        private final String description;

        PasswordStrength(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
