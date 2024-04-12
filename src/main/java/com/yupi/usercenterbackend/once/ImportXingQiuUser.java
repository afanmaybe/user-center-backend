package com.yupi.usercenterbackend.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/5 22:05
 */
public class ImportXingQiuUser {

    public static void main(String[] args) {
        String fileName = "D:\\CodeSpace\\MyProject\\yupi-projects\\user-center-backend\\src\\main\\resources\\testExcel.xlsx";
        List<XingQiuTableUserInfo> userInfoList = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        System.out.println("总数是："+userInfoList.size());
        Map<String, List<XingQiuTableUserInfo>> listMap = userInfoList.stream()
                .filter(userinfo -> StringUtils.isNotBlank(userinfo.getUsername()))
                .collect(Collectors.groupingBy(XingQiuTableUserInfo::getUsername));
        for (Map.Entry<String, List<XingQiuTableUserInfo>> stringListEntry : listMap.entrySet()) {
            if(stringListEntry.getValue().size()>1){
                System.out.println(stringListEntry.getKey());
            }
        }
        System.out.println("不重复数是："+listMap.keySet().size());
    }
}
