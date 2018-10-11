package com.baizhi.cmfz.aspect;

import com.baizhi.cmfz.entity.Admin;
import com.baizhi.cmfz.entity.Log;
import com.baizhi.cmfz.service.LogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * aspectJ 风格的aop
 */
@Component
@Aspect
public class LogAdvice {
    @Autowired
    private LogService logService;
    /**
     * 配置切入点
     */
    @Pointcut("execution(* com.baizhi.cmfz.serviceimpl.*.add*(..))||execution(* com.baizhi.cmfz.serviceimpl.*.modify*(..))||execution(* com.baizhi.cmfz.serviceimpl.*.remove*(..))")
    public void pc(){

    }

    /**
     * 配置环绕通知
     */
    @Around("pc()")
    public Object around(ProceedingJoinPoint pjp){
        //创建日志对象
        Log log = new Log();
        //设定时间
        log.setTime(new Date());
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        //获得方法实参的类名
        String calzzName = signature.getMethod().getParameterTypes()[0].getName();
        log.setResource(calzzName.substring(calzzName.lastIndexOf(".")+1));
        //根据方法名判断是什么操作
        String methodName = signature.getMethod().getName();
        if(methodName.contains("add")){
            log.setAction("新增");
        }else if(methodName.contains("modify")){
            log.setAction("修改");
        }else if(methodName.contains("remove")){
            log.setAction("删除");
        }
        //记录是哪个用户操作
        ServletRequestAttributes servletRequestAttributes=(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        HttpSession session = servletRequestAttributes.getRequest().getSession();
        String adminName = (String)session.getAttribute("adminName");
        log.setUser(adminName);
        //记录实参的实际参数
        Object proceed = null;
        try {
            proceed = pjp.proceed();
            log.setMessage(pjp.getArgs()[0].toString());
            log.setResult("success");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            log.setResult("false");
        }
        logService.insertLog(log);
        return proceed;
    }
}
