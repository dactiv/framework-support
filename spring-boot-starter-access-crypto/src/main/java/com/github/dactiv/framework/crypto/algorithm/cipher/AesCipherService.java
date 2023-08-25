

package com.github.dactiv.framework.crypto.algorithm.cipher;

import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.exception.CryptoException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * AES 对称加密实现，特点：密钥建立时间短、灵敏性好、内存需求低、安全性高
 *
 * @author maurice
 */
public class AesCipherService extends SymmetricCipherService {

    private static final int DEFAULT_KEY_SIZE = 128;

    /**
     * AES 对称加密实现
     */
    public AesCipherService() {
        super("AES");

        setKeySize(DEFAULT_KEY_SIZE);
        setInitializationVectorSize(DEFAULT_KEY_SIZE);

        setMode(OperationMode.CBC);
        setPaddingScheme(PaddingScheme.PKCS5);

        setStreamingMode(OperationMode.CBC);
        setStreamingPaddingScheme(PaddingScheme.PKCS5);
    }

    public ByteSource encrypt(byte[] plaintext, byte[] key, byte[] iv) {
        return super.encrypt(plaintext, key, iv);
    }

    public void encrypt(InputStream in, OutputStream out, byte[] key, byte[] iv) throws CryptoException {
        super.encrypt(in, out, key, iv);
    }

    public ByteSource decrypt(byte[] cipherText, byte[] key, byte[] iv) {
        return super.decrypt(cipherText, key, iv);
    }

    public void decrypt(InputStream in, OutputStream out, byte[] key, byte[] iv) throws CryptoException {
        super.decrypt(in, out, key, iv);
    }

}
