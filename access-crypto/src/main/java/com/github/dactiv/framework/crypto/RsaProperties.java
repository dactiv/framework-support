package com.github.dactiv.framework.crypto;

import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.SimpleByteSource;

/**
 * rsa 配置信息
 *
 * @author maurice.chen
 */
public class RsaProperties {

    /**
     * 公共密钥
     */
    private String publicKey;

    /**
     * 私有密钥
     */
    private String privateKey;

    /**
     * 创建一个 rsa 配置信息
     */
    public RsaProperties() {
    }

    /**
     * 创建一个 rsa 配置信息
     *
     * @param publicKey 公共密钥
     * @param privateKey 私有密钥
     */
    public RsaProperties(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * 获取公共密钥
     *
     * @return 公共密钥
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * 设置公共密钥
     *
     * @param publicKey 公共密钥
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * 获取私有密钥
     *
     * @return 私有密钥
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * 设置私有密钥
     *
     * @param privateKey 私有密钥
     */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * 创建一个 rsa 配置信息
     *
     * @param publicKey 公共密钥
     * @param privateKey 私有密钥
     */
    public static RsaProperties of(String publicKey, String privateKey) {
        return new RsaProperties(publicKey, privateKey);
    }

    /**
     * 获取公共密钥字节原
     *
     * @return 公共密钥字节原
     */
    public ByteSource getPublicKeyByteSource() {
        return new SimpleByteSource(publicKey);
    }

    /**
     * 获取私有密钥字节原
     *
     * @return 私有密钥字节原
     */
    public ByteSource getPrivateKeyByteSource() {
        return new SimpleByteSource(privateKey);
    }

}
