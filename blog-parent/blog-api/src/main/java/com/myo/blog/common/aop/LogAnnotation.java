package com.myo.blog.common.aop;

import java.lang.annotation.*;
//Type 代表可以放在类上面 Method 代表可以放在方法上
@Target({ElementType.METHOD})//描述的注解可以用在什么地方( METHOD用于描述方法)
@Retention(RetentionPolicy.RUNTIME)//@Retention(RetentionPolicy.RUNTIME)注解表明 Test_Retention注解将会由虚拟机保留,以便它可以在运行时通过反射读取.
@Documented//Documented注解只是用来做标识，没什么实际作用
public @interface LogAnnotation {
    //日志记录，记录操作
    String module() default "";//模块

    String operator() default "";//操作
}
