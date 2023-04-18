package com.github.dactiv.framework.spring.security.audit;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.framework.mybatis.plus.audit.MybatisPlusOperationDataTraceRepository;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.spring.security.authentication.token.SimpleAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.entity.UserDetailsOperationDataTraceRecord;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

/**
 * 用户明细操作数据留痕仓库实现
 *
 * @author maurice.chen
 */
public abstract class UserDetailsOperationDataTraceRepository extends MybatisPlusOperationDataTraceRepository implements ShardingOperationDataTraceRepository {

    public static final String OPERATION_DATA_TRACE_ID_ATTR_NAME = "operationDataTraceId";

    private final List<String> ignorePrincipals;

    public UserDetailsOperationDataTraceRepository(List<String> ignorePrincipals) {
        this.ignorePrincipals = ignorePrincipals;
    }

    @Override
    public List<OperationDataTraceRecord> createOperationDataTraceRecord(MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {

        Optional<HttpServletRequest> optional = SpringMvcUtils.getHttpServletRequest();

        if (optional.isEmpty()) {
            return null;
        }
        HttpServletRequest httpServletRequest = optional.get();
        Object trace = httpServletRequest.getAttribute(ControllerAuditHandlerInterceptor.OPERATION_DATA_TRACE_ATT_NAME);

        if (Objects.isNull(trace) || Boolean.FALSE.equals(trace)) {
            return null;
        }

        SecurityContext context = SecurityContextHolder.getContext();
        if (Objects.isNull(context.getAuthentication())) {
            return null;
        }

        Authentication authentication = context.getAuthentication();
        if (!authentication.isAuthenticated()) {
            return null;
        }

        if (context.getAuthentication() instanceof SimpleAuthenticationToken authenticationToken && authenticationToken.getDetails() instanceof SecurityUserDetails userDetails) {
            String username = userDetails.getUsername();
            if (ignorePrincipals.contains(username)) {
                return null;
            }

            Map<String, Object> meta = userDetails.getMeta();
            if (MapUtils.isEmpty(meta)) {
                meta = new LinkedHashMap<>();
            }

            meta.put(BasicUserDetails.USER_TYPE_FIELD_NAME, userDetails.getType());
            meta.put(BasicUserDetails.USER_ID_FIELD_NAME, userDetails.getId());

            List<OperationDataTraceRecord> records = super.createOperationDataTraceRecord(
                    mappedStatement,
                    statement,
                    parameter
            );

            Object traceId = httpServletRequest.getAttribute(OPERATION_DATA_TRACE_ID_ATTR_NAME);
            if (Objects.isNull(traceId)) {
                traceId = UUID.randomUUID().toString();
                httpServletRequest.setAttribute(OPERATION_DATA_TRACE_ID_ATTR_NAME, traceId);
            }

            List<OperationDataTraceRecord> result = new LinkedList<>();

            for (OperationDataTraceRecord record : records) {
                UserDetailsOperationDataTraceRecord userDetailsRecord = Casts.of(record, UserDetailsOperationDataTraceRecord.class);
                userDetailsRecord.setPrincipalMeta(meta);
                userDetailsRecord.setPrincipal(username);
                userDetailsRecord.setTraceId(traceId.toString());
                result.add(userDetailsRecord);
            }

            return result;

        }

        return null;
    }
}
