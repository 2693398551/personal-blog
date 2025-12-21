package com.myo.blog.controller;

import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.MessageParam;
import com.myo.blog.entity.params.QRcodeParar;
import com.myo.blog.service.MessageService;
import com.myo.blog.utils.IpUtils;
import com.myo.blog.utils.QRcodeUtils;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.ibatis.type.ShortTypeHandler;
import org.iherus.codegen.qrcode.QrcodeConfig;
import org.iherus.codegen.qrcode.QreyesFormat;
import org.iherus.codegen.qrcode.SimpleQrcodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/7 18:01
 */
@RestController
@RequestMapping("utils")
public class UtilController {
    @Autowired
    private MessageService messageService;

    //获取访问设备
    @GetMapping("getUserAgent")
    public Result getUserAgent(HttpServletRequest req){

        UserAgent useragent=IpUtils.getUserAgent(req);

        return Result.success(useragent);
    }
    //城市信息
    @GetMapping("getCityInfo")
    public Result getCityInfo(HttpServletRequest req){
        String ip=IpUtils.getIpAddr(req);
        String hostName=IpUtils.getCityInfo(ip);
        return Result.success(hostName);
    }
    //获取主机名
    @GetMapping("getHostName")
    public Result currentUser(HttpServletRequest req){
      //获取客户端主机名
        String hostName = req.getRemoteHost();
        //String hostName=IpUtils.getHostName();
        if(hostName.equals("hecs-79668")){
            hostName=IpUtils.getIpAddr(req);
        }

        return Result.success(hostName);
    }

    //当前ip
    @GetMapping("getIpAddress")
    public Result getIpAddress(HttpServletRequest req){

        String ip=IpUtils.getIpAddr(req);

        return Result.success(ip);
    }
    //二维码
    @PostMapping("/create")
    public void contextLoads(HttpServletResponse response,@RequestBody QRcodeParar qr) throws IOException {
;

        int type=qr.getType();
        if(type==1){
            QRcodeUtils.testCustomConfig(response,qr);
        }

        if(type==2){

            QRcodeUtils.testCustomCodeEyes(response,qr);
        }

        if(type==3){
            QRcodeUtils.testYuanXing(response,qr);
        }


    }
    //站点信息
    @GetMapping("getWebinfo")
    public Result getWebinfo(){

        return Result.success("");
    }
}
