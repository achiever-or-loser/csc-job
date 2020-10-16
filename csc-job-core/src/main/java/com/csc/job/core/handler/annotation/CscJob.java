package com.csc.job.core.handler.annotation;

import java.lang.annotation.*;

/**
 * @Description:
 * @PackageName: com.csc.job.core.handler.annotation
 * @Author: 陈世超
 * @Create: 2020-10-16 13:33
 * @Version: 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CscJob {
    String value();

    String init() default "";

    String destroy() default "";
}
