package com.myo.blog.utils;

import com.myo.blog.entity.params.QRcodeParar;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.iherus.codegen.Codectx;
import org.iherus.codegen.qrcode.QrcodeConfig;
import org.iherus.codegen.qrcode.QreyesFormat;
import org.iherus.codegen.qrcode.SimpleQrcodeGenerator;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Base64;


/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/11 22:07
 */
public class QRcodeUtils {




    /**
     * 自定义二维码配置
     * @throws IOException
     */

    public static void testCustomConfig(HttpServletResponse response,QRcodeParar qr) throws IOException {
/*
        QrcodeConfig config = new QrcodeConfig()
                .setBorderSize(2)
                .setPadding(10)
                .setMasterColor("#00BFFF")
                .setLogoBorderColor("#B0C4DE");
*/
        response.setContentType("image/png");//自定义在前端展示的格式
        OutputStream out = response.getOutputStream();//创建一个相响应输出流
        if(qr.getLogo()==""||qr.getLogo().equals("")||qr.getLogo()==null){
            new SimpleQrcodeGenerator().generate(qr.getContent()).toStream(out);
        }else {
            new SimpleQrcodeGenerator().setRemoteLogo(qr.getLogo()).generate(qr.getContent()).toStream(out);
        }
    }

    /**
     * 自定义二维码码眼颜色
     * @throws IOException
     */

    public static void testCustomCodeEyes(HttpServletResponse response, QRcodeParar qr) throws IOException {
        QrcodeConfig config = new QrcodeConfig()
                .setBorderSize(2)
                .setPadding(10)
                /*.setMasterColor("#778899")
                .setLogoBorderColor("#B0C4DE")
                .setCodeEyesPointColor("#BC8F8F")*/
                .setMasterColor(qr.getColor1())
                .setLogoBorderColor(qr.getColor2())
                .setCodeEyesPointColor(qr.getColor3())
                .setCodeEyesFormat(QreyesFormat.DR2_BORDER_C_POINT);

        response.setContentType("image/png");//自定义在前端展示的格式
        OutputStream out = response.getOutputStream();//创建一个相响应输出流
        if(qr.getLogo()==""||qr.getLogo().equals("")||qr.getLogo()==null){
            new SimpleQrcodeGenerator(config).generate(qr.getContent()).toStream(out);

        }else {
            new SimpleQrcodeGenerator(config).setRemoteLogo(qr.getLogo()).generate(qr.getContent()).toStream(out);
        }


        //关闭响应输出流
        out.close();
    }

    /**
     * 自定义圆形logo
     * @throws IOException
     */
    public static void testYuanXing(HttpServletResponse response, QRcodeParar qr) throws IOException {
        QrcodeConfig config = new QrcodeConfig()
                .setMasterColor(qr.getColor1())
                .setLogoBorderColor(qr.getColor2())
                .setCodeEyesPointColor(qr.getColor3())
                .setLogoShape(Codectx.LogoShape.CIRCLE);

        response.setContentType("image/png");//自定义在前端展示的格式
        OutputStream out = response.getOutputStream();//创建一个相响应输出流
        if(qr.getLogo()==""||qr.getLogo().equals("")||qr.getLogo()==null){
            new SimpleQrcodeGenerator(config).generate(qr.getContent()).toStream(out);
        }else {
            new SimpleQrcodeGenerator(config).setRemoteLogo(qr.getLogo()).generate(qr.getContent()).toStream(out);
        }

    }
}
