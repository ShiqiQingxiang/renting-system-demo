package com.rental.common.service;

import com.rental.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 12; // 96 bits for GCM
    private static final int TAG_LENGTH = 16; // 128 bits for GCM tag

    // 在生产环境中，这个密钥应该从配置文件或环境变量中获取
    // 这里为了演示目的使用硬编码，实际应用中请使用更安全的方式
    private static final String MASTER_KEY = "MySecretKey12345MySecretKey12345"; // 32 bytes

    private final SecretKey secretKey;

    public EncryptionService() {
        // 从主密钥创建 SecretKey
        byte[] keyBytes = MASTER_KEY.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Master key must be 32 bytes long");
        }
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 加密文本
     *
     * @param plainText 明文
     * @return 加密后的文本（Base64编码）
     */
    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            throw new IllegalArgumentException("待加密文本不能为空");
        }

        try {
            // 生成随机IV
            byte[] iv = generateIV();
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            // 执行加密
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // 组合 IV + 加密数据
            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);
            
            // Base64编码返回
            String result = Base64.getEncoder().encodeToString(encryptedWithIv);
            log.debug("文本加密成功，原长度：{}，加密后长度：{}", plainText.length(), result.length());
            
            return result;
            
        } catch (Exception e) {
            log.error("加密失败", e);
            throw new BusinessException("数据加密失败：" + e.getMessage());
        }
    }

    /**
     * 解密文本
     *
     * @param encryptedText 加密文本（Base64编码）
     * @return 解密后的明文
     */
    public String decrypt(String encryptedText) {
        if (!StringUtils.hasText(encryptedText)) {
            throw new IllegalArgumentException("待解密文本不能为空");
        }

        try {
            // Base64解码
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
            
            if (encryptedWithIv.length < IV_LENGTH + TAG_LENGTH) {
                throw new IllegalArgumentException("加密数据格式错误");
            }
            
            // 分离IV和加密数据
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, IV_LENGTH);
            System.arraycopy(encryptedWithIv, IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            // 执行解密
            byte[] decryptedData = cipher.doFinal(encryptedData);
            String result = new String(decryptedData, StandardCharsets.UTF_8);
            
            log.debug("文本解密成功，解密后长度：{}", result.length());
            return result;
            
        } catch (Exception e) {
            log.error("解密失败", e);
            throw new BusinessException("数据解密失败：" + e.getMessage());
        }
    }

    /**
     * 验证加密/解密功能
     *
     * @param testText 测试文本
     * @return 是否验证成功
     */
    public boolean validateEncryption(String testText) {
        try {
            String encrypted = encrypt(testText);
            String decrypted = decrypt(encrypted);
            return testText.equals(decrypted);
        } catch (Exception e) {
            log.error("加密验证失败", e);
            return false;
        }
    }

    /**
     * 检查文本是否已加密
     *
     * @param text 待检查的文本
     * @return true表示已加密，false表示未加密
     */
    public boolean isEncrypted(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        
        try {
            // 尝试解码Base64，如果成功且长度合理，可能是加密的
            byte[] decoded = Base64.getDecoder().decode(text);
            return decoded.length > IV_LENGTH + TAG_LENGTH;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成随机IV
     */
    private byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * 生成新的密钥（用于密钥轮转等场景）
     */
    public String generateNewKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new BusinessException("生成密钥失败：" + e.getMessage());
        }
    }

    /**
     * 安全比较两个字符串（防止时序攻击）
     */
    public boolean secureEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
}