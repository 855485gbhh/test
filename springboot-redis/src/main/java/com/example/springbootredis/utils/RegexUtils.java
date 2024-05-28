package com.example.springbootredis.utils;

import org.apache.commons.lang3.StringUtils;

public class RegexUtils {

    /**
     * 手机号正则
     */
    public static final String PHONE_REGEX = "^1\\d{10}$";
    /**
     * 邮箱正则
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    /**
     * 密码正则。4~32位的字母、数字、下划线
     */
    public static final String PASSWORD_REGEX = "^\\w{4,32}$";
    /**
     * 验证码正则, 6位数字或字母
     */
    public static final String VERIFY_CODE_REGEX = "^[a-zA-Z\\d]{6}$";

    /**
     * 是否是无效手机格式
     *
     * @param phone 要校验的手机号
     * @return true:符合，false：不符合
     */
    public static boolean isPhoneInvalid(String phone) {
        return mismatch(phone, PHONE_REGEX);
    }

    /**
     * 是否是无效邮箱格式
     *
     * @param email 要校验的邮箱
     * @return true:符合，false：不符合
     */
    public static boolean isEmailInvalid(String email) {
        return mismatch(email, EMAIL_REGEX);
    }

    /**
     * 是否是无效验证码格式
     *
     * @param code 要校验的验证码
     * @return true:符合，false：不符合
     */
    public static boolean isCodeInvalid(String code) {
        return mismatch(code, VERIFY_CODE_REGEX);
    }

    // 校验是否不符合正则格式
    private static boolean mismatch(String str, String regex) {
        if (isNotBlank(str) == false) {
            return false;
        }
        return !str.matches(regex);
    }

    public static boolean isNotBlank(String str) {
        int length = str.length();
        if (length == 0) {
            return false;
        } else {
            for (int i = 0; i < length; i++) {
                if ((str.charAt(i) - 32) != 0) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
    }
}
