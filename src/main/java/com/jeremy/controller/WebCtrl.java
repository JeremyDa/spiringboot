package com.jeremy.controller;

import com.jeremy.service.*;
import org.apache.ibatis.mapping.SqlCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 18/05/2017.
 */
@RestController
@RequestMapping(value = "/web")
public class WebCtrl {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebService service;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object controller(@RequestParam Map<String, Object> paramMap) throws Exception {
        String toList = (String) paramMap.get("toList");
        if (toList != null) {
            String[] array = toList.split(",");
            for (String key : array) {
                String value = (String) paramMap.get(key);
                if (value != null) {
                    paramMap.put(key + "List", Arrays.asList(value.split(",")));
                } else {
                    throw new Exception(key + "参数有误，请检查");
                }
            }
        }

        Map retMap = new HashMap();

        String tradeCode = getMapParamValue(paramMap,"tradeCode","");
        logger.info("tradeCode:" + tradeCode);

        String tradeCodeList = getMapParamValue(paramMap, "tradeCodeList", "");
        logger.info("tradeCodeList:" + tradeCodeList);

        if ("".equals(tradeCode) && "".equals(tradeCodeList)) {
            retMap.put("rspCode", "90");
            retMap.put("rspMsg", "Please Submit tradeCode!");
        }

        String listKey = getMapParamValue(paramMap,"listKey","listInfo");
        logger.debug("listKey:"+listKey);

//        System.out.println("tradeCode:" + tradeCode);
//        String[] arr = tradeCode.split("[.]");
//        String oper = arr[1];
//        String domain = arr[0];

        long startTime=System.currentTimeMillis();   //获取开始时间


        try {
            if (!"".equals(tradeCodeList)) {

                service.service(paramMap);

            } else if (SqlCommandType.SELECT == service.getSqlCommandType(tradeCode)) {
                List retList = (List) service.dbinvoke(paramMap);
                retMap.put(listKey, retList);
            }

            else {
                int ret = (int) service.dbinvoke(paramMap);
                System.out.println("ret:" + ret);
                if (0 >= ret) {
                    throw new Exception("操作失败");
                }
            }

            retMap.put("rspCode", "00");
            retMap.put("rspMsg", "交易成功");
        } catch (Exception e) {
            e.printStackTrace();
            retMap.put("rspCode", "99");
            if(e.getMessage().contains("Duplicate entry")){
                retMap.put("rspMsg", "主键重复");
            }else if(e.getMessage().contains("Data too long")){
                retMap.put("rspMsg", "参数长度过长");
            }else {
                retMap.put("rspMsg", "操作失败，请检查参数");
            }
        } finally {

            long endTime=System.currentTimeMillis(); //获取结束时间
            logger.debug("web dbruntime程序运行时间： "+(endTime-startTime)+" ms");

            HashMap map =new HashMap();
            map.put("tradeCode","actionLog.insertSelective");
            map.put("fPid",paramMap.get("userid"));
            logger.debug("paramMap.get(\"userid\"):"+paramMap.get("userid"));
            map.put("fAction",paramMap.get("tradeCode"));
            map.put("fRspcode",retMap.get("rspCode"));
            map.put("fRspmsg",retMap.get("rspMsg"));
            map.put("fCosttime", endTime - startTime);
//            service.dbinvoke(map);

            return retMap;
        }
    }

    private String getMapParamValue(Map paramMap, String listKey, String defVal){
        if(paramMap.get(listKey) != null){
            return (String) paramMap.get(listKey);
        }else{
            return defVal;
        }
    }

}