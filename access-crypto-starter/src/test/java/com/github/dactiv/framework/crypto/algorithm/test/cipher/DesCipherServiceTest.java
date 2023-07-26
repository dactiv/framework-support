

package com.github.dactiv.framework.crypto.algorithm.test.cipher;

import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.CodecUtils;
import com.github.dactiv.framework.crypto.algorithm.cipher.DesCipherService;
import com.github.dactiv.framework.crypto.algorithm.test.TestData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * des 加解密单元测试
 *
 * @author maurice
 */
public class DesCipherServiceTest {

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    public void test() throws IOException {

        DesCipherService cipherService = new DesCipherService();

        byte[] key = cipherService.generateKey().getEncoded();

        ByteSource source = cipherService.encrypt(TestData.TEXT.getBytes(), key);
        ByteSource target = cipherService.decrypt(source.obtainBytes(), key);

        Assert.assertArrayEquals(target.obtainBytes(), TestData.TEXT.getBytes());

        ByteArrayOutputStream encryptOut = new ByteArrayOutputStream();
        InputStream encryptIn = resourceLoader.getResource("classpath:/data.test").getInputStream();

        cipherService.encrypt(encryptIn, encryptOut, key);

        InputStream decryptIn = new ByteArrayInputStream(encryptOut.toByteArray());
        ByteArrayOutputStream decryptOut = new ByteArrayOutputStream();
        cipherService.decrypt(decryptIn, decryptOut, key);

        byte[] text = CodecUtils.toBytes(resourceLoader.getResource("classpath:/data.test").getInputStream());
        Assert.assertArrayEquals(decryptOut.toByteArray(), text);
    }
}
