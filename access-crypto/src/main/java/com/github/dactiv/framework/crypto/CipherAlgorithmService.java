package com.github.dactiv.framework.crypto;

import com.github.dactiv.framework.crypto.algorithm.cipher.*;
import com.github.dactiv.framework.crypto.algorithm.exception.CryptoException;
import com.github.dactiv.framework.crypto.algorithm.exception.UnknownAlgorithmException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 加密算法服务
 *
 * @author maurice
 */
public class CipherAlgorithmService {

    /**
     * AES 算法名称
     */
    public static final String AES_ALGORITHM = "AES";

    /**
     * DES 算法名称
     */
    public static final String DES_ALGORITHM = "DES";

    /**
     * RSA 算法名称
     */
    public static final String RSA_ALGORITHM = "RSA";

    /**
     * 默认密文名称
     */
    public static String DEFAULT_CIPHER_TEXT_NAME = "cipherText";

    /**
     * 默认访问 token 的请求头名称
     */
    public static String DEFAULT_REQUEST_ACCESS_TOKEN_KEY_HEADER_NAME = "X-ACCESS-TOKEN";

    /**
     * 默认访问 token 的请求头名称
     */
    public static String DEFAULT_REQUEST_ACCESS_TOKEN_KEY_PARAM_NAME = "accessToken";

    /**
     * 加密服务的算法 map
     */
    private final Map<String, Class<? extends CipherService>> algorithmServiceMap = new LinkedHashMap<>();

    /**
     * 加密算法服务
     */
    public CipherAlgorithmService() {
        // 添加 AES 算法服务
        algorithmServiceMap.put(AES_ALGORITHM, AesCipherService.class);
        // 添加 DES 算法服务
        algorithmServiceMap.put(DES_ALGORITHM, DesCipherService.class);
        // 添加 RSA 算法服务
        algorithmServiceMap.put(RSA_ALGORITHM, RsaCipherService.class);
    }

    /**
     * 加密算法服务
     *
     * @param algorithmServiceMap 算法服务的 map 集合
     */
    public CipherAlgorithmService(Map<String, Class<? extends CipherService>> algorithmServiceMap) {
        this.algorithmServiceMap.putAll(algorithmServiceMap);
    }

    /**
     * 通过算法名称和算法类型获取密码服务
     *
     * @param algorithmName 算法
     * @param <T>           实现 CipherService 的子类
     *
     * @return 密码服务
     */
    @SuppressWarnings("unchecked")
    public <T extends CipherService> T getCipherService(String algorithmName) {
        if (!algorithmServiceMap.containsKey(algorithmName)) {
            String msg = "算法服务找不到 " + algorithmName + " 的算法实现";
            throw new UnknownAlgorithmException(msg);
        }
        try {
            return (T) algorithmServiceMap.get(algorithmName).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 获取秘密法服务
     *
     * @param ca 加解密算法模型配置
     * @param <T> 加解密服务实现类型
     *
     * @return 加解密服务
     */
    @SuppressWarnings("unchecked")
    public <T extends CipherService> T getCipherService(AlgorithmProperties ca) {

        AbstractBlockCipherService cipherService = getCipherService(ca.getName());

        cipherService.setMode(OperationMode.valueOf(ca.getMode()));
        cipherService.setPaddingScheme(PaddingScheme.getPaddingScheme(ca.getPaddingScheme()));
        cipherService.setBlockSize(ca.getBlockSize());
        cipherService.setStreamingMode(OperationMode.valueOf(ca.getStreamingMode()));
        cipherService.setStreamingPaddingScheme(PaddingScheme.getPaddingScheme(ca.getStreamingPaddingScheme()));
        cipherService.setStreamingBlockSize(ca.getStreamingBlockSize());
        cipherService.setKeySize(ca.getKeySize());
        cipherService.setInitializationVectorSize(ca.getInitializationVectorSize());

        return (T) cipherService;

    }

    /**
     * 获取算法服务 map
     *
     * @return 算法服务 map
     */
    public Map<String, Class<? extends CipherService>> getAlgorithmServiceMap() {
        return algorithmServiceMap;
    }

    /**
     * 设置算法服务 map
     *
     * @param algorithmServiceMap 算法服务 map
     */
    public void setAlgorithmServiceMap(Map<String, Class<? extends CipherService>> algorithmServiceMap) {
        this.algorithmServiceMap.putAll(algorithmServiceMap);
    }
}
