package com.github.dactiv.framework.spring.web.jackson;

import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 多个时间格式化的实现
 *
 * @author maurice.chen
 */
public class MultipleDateFormat extends DateFormat {

    private final List<SimpleDateFormat> dateFormats = new LinkedList<>();

    public MultipleDateFormat(String pattern) {

        this(
                StringUtils.splitByWholeSeparator(
                        pattern,
                        SpringMvcUtils.COMMA_STRING
                )
        );
    }

    public MultipleDateFormat(String[] patterns) {
        this(Arrays.stream(patterns).collect(Collectors.toList()));
    }

    public MultipleDateFormat(Collection<String> patterns) {
        for (String pattern : patterns) {
            dateFormats.add(new SimpleDateFormat(StringUtils.trim(pattern)));
        }

        Locale locale = Locale.getDefault(Locale.Category.FORMAT);

        calendar = Calendar.getInstance(locale);

        numberFormat = NumberFormat.getIntegerInstance(locale);
        numberFormat.setGroupingUsed(false);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        for (SimpleDateFormat simpleDateFormat : dateFormats) {
            StringBuffer value = simpleDateFormat.format(date, toAppendTo, fieldPosition);

            if (Objects.nonNull(value)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {

        for (SimpleDateFormat simpleDateFormat : dateFormats) {
            Date value = simpleDateFormat.parse(source, pos);

            if (Objects.nonNull(value)) {
                return value;
            }
        }

        return null;
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        dateFormats.forEach(f -> f.setTimeZone(zone));
    }

    @Override
    public void setCalendar(Calendar newCalendar) {
        dateFormats.forEach(f -> f.setCalendar(newCalendar));
    }

    @Override
    public void setNumberFormat(NumberFormat newNumberFormat) {
        dateFormats.forEach(f -> f.setNumberFormat(newNumberFormat));
    }

    @Override
    public void setLenient(boolean lenient) {
        dateFormats.forEach(f -> f.setLenient(lenient));
    }

    @Override
    public Object clone() {
        return super.clone();
    }
}
