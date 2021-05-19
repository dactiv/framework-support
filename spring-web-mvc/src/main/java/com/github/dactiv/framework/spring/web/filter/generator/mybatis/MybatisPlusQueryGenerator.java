package com.github.dactiv.framework.spring.web.filter.generator.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.filter.QueryGenerator;
import com.github.dactiv.framework.spring.web.filter.condition.Condition;
import com.github.dactiv.framework.spring.web.filter.condition.ConditionParser;
import com.github.dactiv.framework.spring.web.filter.condition.ConditionType;
import com.github.dactiv.framework.spring.web.filter.condition.support.SimpleConditionParser;
import com.github.dactiv.framework.spring.web.filter.generator.WildcardParser;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.wildcard.*;
import org.springframework.data.domain.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mybatis-Plus 查询生成器实现
 *
 * @author maurice.chen
 */
public class MybatisPlusQueryGenerator<T> implements QueryGenerator<QueryWrapper<T>> {

    /**
     * 通配符解析器集合
     */
    private final List<WildcardParser> wildcardParsers;

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
    public MybatisPlusQueryGenerator(List<WildcardParser> wildcardParsers, List<ConditionParser> conditionParsers) {
        this.wildcardParsers = wildcardParsers;
        this.conditionParsers = conditionParsers;
    }

    @Override
    public QueryWrapper<T> generate(List<Condition> conditions) {

        QueryWrapper<T> queryWrapper = Wrappers.query();

        for (Condition c : conditions) {

            QueryWrapper<T> temp = queryWrapper;

            if (ConditionType.Or.equals(c.getType())) {
                temp = queryWrapper.or();
            }

            List<WildcardParser> result = getWildcardParserList()
                    .stream()
                    .filter(w -> w.isSupport(c.getName()))
                    .collect(Collectors.toList());

            for (WildcardParser wildcardParser : result) {
                wildcardParser.structure(c.getProperty(), temp);
            }

        }

        return queryWrapper;
    }

    @Override
    public List<WildcardParser> getWildcardParserList() {
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
    public List<WildcardParser> getDefaultWildcardParserList() {
        return Arrays.asList(
                new EqWildcardParser(),
                new NeWildcardParser(),
                new LikeWildcardParser(),
                new GeWildcardParser(),
                new GtWildcardParser(),
                new LeWildcardParser(),
                new LtWildcardParser(),
                new InWildcardParser(),
                new NotInWildcardParser()
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

        if (page.orders().isEmpty()) {
            return new PageImpl<>(
                    page.getRecords(),
                    PageRequest.of((int) page.getCurrent(), (int) page.getSize()),
                    page.getTotal()
            );
        } else {

            List<Sort.Order> orders = new LinkedList<>();

            page.orders().forEach(o -> {
                if (o.isAsc()) {
                    orders.add(new Sort.Order(Sort.Direction.ASC, o.getColumn()));
                } else {
                    orders.add(new Sort.Order(Sort.Direction.DESC, o.getColumn()));
                }
            });

            return new PageImpl<>(
                    page.getRecords(),
                    PageRequest.of((int) page.getCurrent(), (int) page.getSize(), Sort.by(orders)),
                    page.getTotal()
            );
        }
    }

    /**
     * 创建查询分页
     *
     * @param pageable spring data 分页请求对象
     * @param <S>      分页范型类型
     *
     * @return Mybatis 分页查询对象
     */
    public static <S> com.baomidou.mybatisplus.extension.plugins.pagination.Page<S> createQueryPage(Pageable pageable) {

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<S> page;

        page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                false
        );

        pageable.getSort().stream().forEach(o -> {

            if (o.isAscending()) {
                page.getOrders().add(OrderItem.asc(o.getProperty()));
            } else {
                page.getOrders().add(OrderItem.desc(o.getProperty()));
            }

        });

        return page;
    }
}
