package com.github.dactiv.framework.minio.test;

import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;

/**
 * minio 终端测试
 *
 * @author maurice.chen
 */
@SpringBootTest
public class MinioEndpointTest {

    @Autowired
    private MinioTemplate minioTemplate;

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    public void uninstall() throws Exception {
        Bucket bucket = Bucket.of(MinioTemplateTest.DEFAULT_TEST_BUCKET);

        if (minioTemplate.isBucketExist(bucket)) {

            Iterable<Result<Item>> iterable = minioTemplate.getMinioClient().listObjects(ListObjectsArgs.builder().bucket(bucket.getBucketName()).build());

            for (Result<Item> r : iterable) {
                Item item = r.get();
                minioTemplate.deleteObject(FileObject.of(bucket, item.objectName()));
            }

            minioTemplate.deleteBucket(bucket);
        }
    }

    @Test
    public void testUploadEndpoint() throws Exception {
        InputStream is = resourceLoader.getResource(MinioTemplateTest.DEFAULT_TEST_FILE).getInputStream();

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/actuator/upload")
                .file("file", IOUtils.toByteArray(is))
                .param("bucketName",MinioTemplateTest.DEFAULT_TEST_BUCKET);

        MvcResult mvcResult = mockMvc
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        System.out.println(mvcResult);
    }
}
