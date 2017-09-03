package com.jeremy.controller;

import com.jeremy.service.WebService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserCtrl {

    @Autowired
    private WebService webService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Object login(@RequestParam Map<String, Object> paramMap)
            throws Exception {
        paramMap.put("tradeCode","user.selectByPassword");
        String name = (String) paramMap.get("name");

        Map retMap = new HashMap();

        try {
            if (name == null) {
                throw new ServletException("Invalid login");
            }

            List<Map> userList = (List<Map>) webService.dbinvoke(paramMap);

            if (userList.size() == 0){
                throw new Exception("用户名或密码错误");
            }

            if (userList.size() > 1){
                throw new Exception("登录验证失败，请联系管理员");
            }
            Map user = userList.get(0);
            retMap.put("token",Jwts.builder().setSubject(name)
                    .claim("roles", user.get("name")).setIssuedAt(new Date())
                    .signWith(SignatureAlgorithm.HS256, "secretkey").compact());

            if(paramMap.get("fPushid") != null){
                HashMap map = new HashMap();
                map.put("ofPid",paramMap.get("name"));
                map.put("fPushid",paramMap.get("fPushid"));
                map.put("tradeCode","user.updateByPrimaryKeySelective");
                int ret = (int) webService.dbinvoke(map);
                logger.debug("login update pushid :" + ret);
            }

            retMap.put("user",user);
            retMap.put("rspCode", "00");
            retMap.put("rspMsg", "交易成功");
        }catch (Exception e){
            retMap.put("rspCode", "99");
            retMap.put("rspMsg", e.getMessage());
            e.printStackTrace();
            System.out.println(e.getCause());
        }finally {

            HashMap map =new HashMap();
            map.put("tradeCode","actionLog.insertSelective");
            map.put("fPid",paramMap.get("userid"));
            map.put("fAction",paramMap.get("tradeCode"));
            map.put("fRetcode",retMap.get("rspCode"));
            map.put("fRetmsg",retMap.get("rspMsg"));
//            webService.dbinvoke(map);

            return retMap;
        }
    }

    @RequestMapping(value = "updatePassword", method = RequestMethod.POST)
    public Object updatePassword(@RequestParam Map<String, Object> paramMap)
    {
        paramMap.put("tradeCode","user.selectByPrimaryKey");
        String fPid = (String) paramMap.get("fPid");
        String name = (String) paramMap.get("name");
        String password = (String) paramMap.get("password");
        logger.debug( fPid + ", " + name + ", " + password);
        String user_new_passwd = (String) paramMap.get("user_new_passwd");

        Map retMap = new HashMap();

        try {
            if (name == null) {
                throw new ServletException("Invalid username");
            }

            List<Map> userList = (List<Map>) webService.dbinvoke(paramMap);
            logger.debug(userList.toString());
            if (userList.size() == 0){
                throw new Exception("用户名或密码错误");
            }

            if (userList.size() > 1){
                throw new Exception("原始密码验证失败，请联系管理员");
            }
            if(user_new_passwd != null){
                HashMap map = new HashMap();
                map.put("ofPid",fPid);
                map.put("fPassword",user_new_passwd);
                map.put("tradeCode","user.updateByPrimaryKeySelective");
                int ret = (int) webService.dbinvoke(map);
                logger.debug("login update pushid :" + ret);
            }

            retMap.put("rspCode", "00");
            retMap.put("rspMsg", "交易成功");
        }catch (Exception e){
            retMap.put("rspCode", "99");
            retMap.put("rspMsg", e.getMessage());
            System.out.println(e.getCause());
        }finally {
            return retMap;
        }
    }

}
