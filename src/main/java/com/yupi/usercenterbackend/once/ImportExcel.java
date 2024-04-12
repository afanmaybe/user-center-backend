package com.yupi.usercenterbackend.once;

import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/5 19:12
 */
public class ImportExcel {
    public static void main(String[] args) {
        String fileName = "D:\\CodeSpace\\MyProject\\yupi-projects\\user-center-backend\\src\\main\\resources\\testExcel.xlsx";
        // 写法1：监听器方式，一行一行读
        //readByListener(fileName);

        //写法2：同步读方式
        synchronousRead(fileName);
    }

    /**
     * 写法2：同步读方式
     * @param fileName
     */
    private static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuTableUserInfo> userInfoList = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo uerInfo : userInfoList) {
            System.out.println(uerInfo);
        }
    }

    /**
     * 写法1：监听器方式，一行一行读
     * @param fileName
     */
    private static void readByListener(String fileName) {
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();
    }
}
