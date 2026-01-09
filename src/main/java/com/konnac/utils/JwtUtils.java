package com.konnac.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtils {
    private static final String signKey = "u5k8jPq3RtXwL9zFbV2nM7cA1dG4hJ6yT0zQp9oL3iK=";
    private static final Long expire = 604800000L;

    /**
     * 生成JWT令牌
     * @param claims JWT第二部分负载 payload 中存储的内容
     * @return JWT令牌
     */
    public static String generateJwt(Map<String, Object> claims){
        String jwt = Jwts.builder()
                .addClaims(claims) // 添加自定义属性
                .signWith(SignatureAlgorithm.HS256, signKey) // 签名算法
                .setExpiration(new Date(System.currentTimeMillis() + expire)) // 设置令牌过期时间
                .compact(); // 创建令牌
        return jwt; // 返回令牌
    }

    /**
     * 解析JWT令牌
     * @param jwt JWT令牌
     * @return JWT第二部分负载 payload 中存储的内容
     */
    public static Claims parseJWT(String jwt){
        Claims claims = Jwts.parser()
                .setSigningKey(signKey) // 设置签名密钥
                .parseClaimsJws(jwt) // 解析令牌
                .getBody(); // 获取负载(有效信息):例如用户ID、用户名、角色等
        return claims;
    }

    /**
     * 从token中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Integer getUserIdFromToken(String token) {
        try {
            Claims claims = parseJWT(token); // 解析token中的内容
            Object userIdObj = claims.get("id"); // 获取用户ID的值
            if (userIdObj instanceof Integer) {
                return (Integer) userIdObj; // 如果用户ID是整数类型，则直接返回
            } else if (userIdObj instanceof Number) { // 如果是数字类型，将其转换为整数并返回
                return ((Number) userIdObj).intValue(); // 转换为整数并返回
            } else if (userIdObj instanceof String) { // 如果是字符串类型，则尝试将其转换为整数并返回
                return Integer.parseInt((String) userIdObj); // 尝试将字符串转换为整数并返回
            }
            return null; // 如果无法解析用户ID，则返回null
        } catch (Exception e) {
            return null; // 解析失败，返回null
        }
    }

    /**
     * 获取用户角色
     * @param token JWT令牌
     * @return 用户角色(String)
     */
    public static String getUserRoleFromToken(String token) {
        try {
            Claims claims = parseJWT(token); // 解析token中的内容
            return (String) claims.get("role"); // 获取用户角色
        } catch (Exception e) {
            return null; // 解析失败，返回null
        }
    }
}
