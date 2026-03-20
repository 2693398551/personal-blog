package com.myo.blog.utils;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.stereotype.Component;

/**
 * UserAgent 解析工具类
 * 基于 eu.bitwalker:UserAgentUtils
 *
 * pom.xml 依赖：
 * <dependency>
 *     <groupId>eu.bitwalker</groupId>
 *     <artifactId>UserAgentUtils</artifactId>
 *     <version>1.21</version>
 * </dependency>
 */
@Component
public class UserAgentUtils {

    /**
     * 获取操作系统名称
     * 示例：Windows 10 / iOS 18 / Android 14 / Mac OS X
     *
     * @param userAgentStr 请求头 User-Agent 原始字符串
     * @return OS 名称，解析失败返回 "Unknown"
     */
    public String getOs(String userAgentStr) {
        if (!hasText(userAgentStr)) return "Unknown";
        try {
            OperatingSystem os = UserAgent.parseUserAgentString(userAgentStr)
                    .getOperatingSystem();
            return os != null ? os.getName() : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * 获取浏览器名称（含主版本号）
     * 示例：Chrome 124 / Safari 17 / Firefox 125 / Edge 124
     *
     * @param userAgentStr 请求头 User-Agent 原始字符串
     * @return 浏览器名称，解析失败返回 "Unknown"
     */
    public String getBrowser(String userAgentStr) {
        if (!hasText(userAgentStr)) return "Unknown";
        try {
            UserAgent ua      = UserAgent.parseUserAgentString(userAgentStr);
            Browser   browser = ua.getBrowser();
            if (browser == null || browser == Browser.UNKNOWN) return "Unknown";

            // 拼接主版本号：Chrome 124
            String version = ua.getBrowserVersion() != null
                    ? ua.getBrowserVersion().getMajorVersion()
                    : null;

            return hasText(version)
                    ? browser.getName() + " " + version
                    : browser.getName();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}