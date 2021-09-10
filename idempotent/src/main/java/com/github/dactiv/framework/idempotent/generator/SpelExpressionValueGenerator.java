package com.github.dactiv.framework.idempotent.generator;

import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 简单的 key 生成实现
 * <p>
 * 假如 {@link Concurrent}("abs[#i]-[#j]")
 * 注解在方法 method(int i,in j) 上,而 i = 1，j = 2 时，生成 key 为: #{@link SpelExpressionValueGenerator#getPrefix()} + abs1-2
 * </p>
 *
 * @author maurice.chen
 */
public class SpelExpressionValueGenerator implements ValueGenerator {

    /**
     * 默认的并发 key 前缀
     */
    private static final String DEFAULT_PREFIX = "spring:el:value:generator:";

    /**
     * 并发 key 前缀
     */
    private String prefix = DEFAULT_PREFIX;

    /**
     * 变量截取的开始字符
     */
    private String openCharacter = "[";

    /**
     * 变量截取的结束字符
     */
    private String closeCharacter = "]";

    /**
     * spring el 表达式解析器
     */
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数名称发现者，用于获取 Concurrent 注解下的方法参数细信息
     */
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Override
    public Object generate(String expression, Method method, Object... args) {

        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        Map<String, Object> variables = new LinkedHashMap<>();

        if (ArrayUtils.isNotEmpty(args)) {
            for (int i = 0; i < (parameterNames != null ? parameterNames.length : 0); i++) {
                variables.put(parameterNames[i], args[i]);
            }
        }

        List<String> tokens = new LinkedList<>();

        String[] array = StringUtils.substringsBetween(expression, openCharacter, closeCharacter);

        if (ArrayUtils.isNotEmpty(array)) {
            tokens = Arrays.asList(array);
        }

        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setVariables(variables);

        String result = expression;

        List<String> replaceToken = new LinkedList<>();

        for (String t : tokens) {

            if (replaceToken.contains(t)) {
                continue;
            }

            Object value = parser.parseExpression(t).getValue(evaluationContext, String.class);

            if (Objects.nonNull(value)) {
                result = StringUtils.replace(result, getTokenValue(t), value.toString());
            }

            replaceToken.add(t);
        }

        return StringUtils.defaultString(getPrefix(), StringUtils.EMPTY) + result;
    }

    /**
     * 获取 token 值
     *
     * @param token token 值
     *
     * @return 添加开始和结束字符的 token 内容,如
     */
    private String getTokenValue(String token) {
        return openCharacter + token + closeCharacter;
    }

    /**
     * 获取默认的 key 前缀
     *
     * @return 默认的 key 前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 设置默认的 key 前缀
     *
     * @param prefix 默认的 key 前缀
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 获取变量截取的开始字符
     *
     * @return 变量截取的开始字符
     */
    public String getOpenCharacter() {
        return openCharacter;
    }

    /**
     * 设置变量截取的开始字符
     *
     * @param openCharacter 变量截取的开始字符
     */
    public void setOpenCharacter(String openCharacter) {
        this.openCharacter = openCharacter;
    }

    /**
     * 获取变量截取的结束字符
     *
     * @return 变量截取的结束字符
     */
    public String getCloseCharacter() {
        return closeCharacter;
    }

    /**
     * 设置变量截取的结束字符
     *
     * @param closeCharacter 变量截取的结束字符
     */
    public void setCloseCharacter(String closeCharacter) {
        this.closeCharacter = closeCharacter;
    }
}
