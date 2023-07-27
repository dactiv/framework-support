package com.github.dactiv.framework.spring.security.authentication.rememberme;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.util.DigestUtils;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 记住我 token
 *
 * @author maurice.chen
 */
public class RememberMeToken implements Serializable {

    
    private static final long serialVersionUID = 5489556035568760298L;

    private Object id;

    /**
     * 登陆账户
     */
    private String username;

    /**
     * token
     */
    private String token;

    /**
     * 类型, 用于区别每个 remember me token 的分类，该值取决于 {@link SecurityUserDetails#getType}
     *
     * @see SecurityUserDetails
     */
    private String type;

    public RememberMeToken(SecurityUserDetails details) {
        setId(details.getId());
        this.username = details.getUsername();
        this.type = details.getType();

        String value = this.username
                + CacheProperties.DEFAULT_SEPARATOR + this.type
                + CacheProperties.DEFAULT_SEPARATOR + System.currentTimeMillis();

        this.token = DigestUtils.md5DigestAsHex(value.getBytes(Charset.defaultCharset()));
    }

    public RememberMeToken(String username, String token, String type) {
        this.username = username;
        this.token = token;
        this.type = type;
    }

    public RememberMeToken() {
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RememberMeToken that = (RememberMeToken) o;
        return getId().equals(that.getId())
                && username.equals(that.username)
                && token.equals(that.token)
                && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), username, token, type);
    }

    /**
     * 获取登陆账户
     *
     * @return 登陆账户
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置登陆账户
     *
     * @param username 登陆账户
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取 token 值
     *
     * @return token 值
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置 token 值
     *
     * @param token token 值
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }
}
