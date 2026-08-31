package com.score.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

/**
 * 微信服务
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class WxService {

    @Value("${wx.appid}")
    private String appId;

    @Value("${wx.appsecret}")
    private String appSecret;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取用户openid
     * 
     * @param code 微信登录凭证
     * @return openid
     */
    public String getOpenid(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId 
                   + "&secret=" + appSecret 
                   + "&js_code=" + code 
                   + "&grant_type=authorization_code";
        
        try {
            String result = restTemplate.getForObject(url, String.class);
            System.out.println("微信jscode2session返回: " + result);
            
            if (result == null || result.isEmpty()) {
                return null;
            }
            
            // 检查是否有错误
            if (result.contains("\"errcode\"")) {
                System.err.println("微信接口错误: " + result);
                return null;
            }
            
            // 简单提取openid（不依赖JSON库）
            int openidStart = result.indexOf("\"openid\":\"");
            if (openidStart >= 0) {
                openidStart += 11; // "\"openid\":\"".length()
                int openidEnd = result.indexOf("\"", openidStart);
                if (openidEnd > openidStart) {
                    return result.substring(openidStart, openidEnd);
                }
            }
        } catch (Exception e) {
            System.err.println("微信接口调用异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 获取用户信息（需要unionid授权）
     * 
     * @param openid 用户openid
     * @return 用户信息JSON
     */
    public String getUserInfo(String openid) {
        String accessToken = getAppAccessToken();
        if (accessToken == null) {
            return null;
        }
        
        String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + accessToken 
                   + "&openid=" + openid + "&lang=zh_CN";
        
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取微信全局access_token
     * 
     * @return access_token
     */
    private String getAppAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId 
                   + "&secret=" + appSecret;
        
        try {
            String result = restTemplate.getForObject(url, String.class);
            
            if (result != null && result.contains("\"access_token\"")) {
                int start = result.indexOf("\"access_token\":\"") + 17;
                int end = result.indexOf("\"", start);
                return result.substring(start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}