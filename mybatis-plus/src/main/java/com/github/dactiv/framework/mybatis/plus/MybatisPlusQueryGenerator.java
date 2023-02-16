package com.github.dactiv.framework.mybatis.plus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.wildcard.*;
import com.github.dactiv.framework.spring.web.query.QueryGenerator;
import com.github.dactiv.framework.spring.web.query.condition.Condition;
import com.github.dactiv.framework.spring.web.query.condition.ConditionParser;
import com.github.dactiv.framework.spring.web.query.condition.ConditionType;
import com.github.dactiv.framework.spring.web.query.condition.support.SimpleConditionParser;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mybatis-Plus 查询生成器实现
 *
 * @author maurice.chen
 */
public class MybatisPlusQueryGenerator<T> implements QueryGenerator<QueryWrapper<T>> {

    /**
     * 通配符解析器集合
     */
    private final List<WildcardParser<QueryWrapper<T>>> wildcardParsers;

    /**
     * 条件解析器集合
     */
    private final List<ConditionParser> conditionParsers;

    /**
     * 创建一个 Mybatis-Plus 查询生成器实现
     */
    public MybatisPlusQueryGenerator() {
        this.wildcardParsers = getDefaultWildcardParserList();
        this.conditionParsers = getDefaultConditionParserList();
    }

    /**
     * 创建一个 Mybatis-Plus 查询生成器实现
     *
     * @param wildcardParsers  通配符解析器集合
     * @param conditionParsers 条件解析器集合
     */
    public MybatisPlusQueryGenerator(List<WildcardParser<QueryWrapper<T>>> wildcardParsers, List<ConditionParser> conditionParsers) {
        this.wildcardParsers = wildcardParsers;
        this.conditionParsers = conditionParsers;
    }

    @Override
    public QueryWrapper<T> generate(Map<String, List<Condition>> filterConditionMap) {

        QueryWrapper<T> queryWrapper = Wrappers.query();

        for (Map.Entry<String, List<Condition>> entry: filterConditionMap.entrySet()) {
            queryWrapper.and(subWrapper -> {
                for (Condition c : entry.getValue()) {

                    List<WildcardParser<QueryWrapper<T>>> result = getWildcardParserList()
                            .stream()
                            .filter(w -> w.isSupport(c.name()))
                            .toList();

                    for (WildcardParser<QueryWrapper<T>> wildcardParser : result) {
                        wildcardParser.structure(c.property(), subWrapper);
                    }

                    if (ConditionType.Or.equals(c.type())) {
                        subWrapper = subWrapper.or();
                    }

                }
            });
        }

        return queryWrapper;
    }

    @Override
    public List<WildcardParser<QueryWrapper<T>>> getWildcardParserList() {
        return wildcardParsers;
    }

    @Override
    public List<ConditionParser> getConditionParserList() {
        return conditionParsers;
    }

    /**
     * 获取默认通配符解析器集合
     *
     * @return 通配符解析器集合
     */
    public List<WildcardParser<QueryWrapper<T>>> getDefaultWildcardParserList() {
        return Arrays.asList(
                new BetweenWildcardParser<>(),
                new EqWildcardParser<>(),
                new NeWildcardParser<>(),
                new LikeWildcardParser<>(),
                new LikeRightWildcardParser<>(),
                new LikeLeftWildcardParser<>(),
                new GteWildcardParser<>(),
                new GtWildcardParser<>(),
                new LteWildcardParser<>(),
                new LtWildcardParser<>(),
                new InWildcardParser<>(),
                new NotInWildcardParser<>(),
                new EqnWildcardParser<>(),
                new NenWildcardParser<>(),
                new JsonContainsWildcardParser<>(),
                new JsonEqWildcardParser<>()
        );
    }

    /**
     * 获取条件解析器集合
     *
     * @return 条件解析器集合
     */
    public List<ConditionParser> getDefaultConditionParserList() {
        return Collections.singletonList(new SimpleConditionParser());
    }

    /**
     * 通过 request 获取指定的查询对象
     *
     * @param request http servelt request
     * @param <S>     查询条件包装器范型类型
     *
     * @return 包装器
     */
    public <S> QueryWrapper<S> getQueryWrapperByHttpRequest(HttpServletRequest request) {
        return Casts.cast(createQueryWrapperFromHttpRequest(request));
    }

    /**
     * 转换分页为 spring 分页
     *
     * @param page 分页结果对象
     * @param <S>  分页范型类型
     *
     * @return spring data 分页
     */
    public static <S> Page<S> convertResultPage(IPage<S> page) {

        return new Page<>(
                new PageRequest((int) page.getCurrent(), (int) page.getSize()),
                page.getRecords()
        );
    }

    /**
     * 创建查询分页
     *
     * @param pageRequest 分页请求
     * @param <S>         分页范型类型
     *
     * @return Mybatis 分页查询对象
     */
    public static <S> PageDTO<S> createQueryPage(PageRequest pageRequest) {

        return new PageDTO<>(
                pageRequest.getNumber(),
                pageRequest.getSize(),
                false
        );
    }
}
