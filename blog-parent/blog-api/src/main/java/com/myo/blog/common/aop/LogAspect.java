package com.myo.blog.common.aop;

import com.alibaba.fastjson.JSON;
import com.myo.blog.utils.HttpContextUtils;
import com.myo.blog.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Component
@Aspect //切面 定义了通知和切点的关系
@Slf4j//log日志的功能
public class LogAspect {

    @Pointcut("@annotation(com.myo.blog.common.aop.LogAnnotation)")
    public void pt(){}

    //环绕增强
    @Around("pt()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println(joinPoint);
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = joinPoint.proceed();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志
        recordLog(joinPoint, time);

        return result;
    }
    //日志
    private void recordLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAnnotation logAnnotation = method.getAnnotation(LogAnnotation.class);
        log.info("=====================日志开始log start================================");
        log.info("模块(module):{}",logAnnotation.module());
        log.info("操作(operation):{}",logAnnotation.operator());

        //请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        log.info("请求的方法名(request method):{}",className + "." + methodName + "()");

//        //请求的参数
        Object[] args = joinPoint.getArgs();
        String params = JSON.toJSONString(args[0]);
        log.info("请求的参数(params):{}",params);

        //获取request 设置IP地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        if(IpUtils.getIpAddr(request).equals("14.150.222.68")||IpUtils.getIpAddr(request).equals("127.0.0.1")){
            log.info("本人:{}", IpUtils.getIpAddr(request));
        }else{
            log.info("访问者ip:{}", IpUtils.getIpAddr(request));
        }



        log.info("执行时间(excute time) : {} ms",time);
        log.info("=====================日志结束log end================================");
    }
}
