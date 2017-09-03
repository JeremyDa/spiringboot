package com.jeremy.controller;

import com.jeremy.service.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by Jeremy on 2017/3/2.
 */
@RestController
public class TableCtrl {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebService service;

    @RequestMapping(value = "/table/{domain}/{sqlid}", method = RequestMethod.GET)
    public Object controller(@PathVariable String domain,@PathVariable String sqlid,@RequestParam Map<String, Object> paramMap) throws Exception {

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
                paramMap.remove(key);
            }
        }

        Map retMap = new HashMap();

        paramMap.put("tradeCode",domain+"."+sqlid);

        List<Map> retList = new ArrayList<Map>();
        long startTime=System.currentTimeMillis();   //获取开始时间
        retList = (List) service.dbinvoke(paramMap);
        long endTime=System.currentTimeMillis(); //获取结束时间
        logger.debug("table dbruntime程序运行时间： "+(endTime-startTime)+" ms");

        retMap.put("data",retList);
        logger.debug("retList:"+retList.size());
        return retMap;
    }
}
