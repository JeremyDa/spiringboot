package com.jeremy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Jeremy on 2017/3/2.
 */
@RestController
public class UploadCtrl {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${upload.url}")
    private String uploadUrl;

    @Value("${upload.absolute}")
    private String uploadAbsolute;

    @Value("${upload.relative}")
    private String uploadRelative;

    @RequestMapping(value="/upload", method= RequestMethod.GET)
    public @ResponseBody
    String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

    @RequestMapping(value="/upload", method= RequestMethod.POST)
    public Map handleFileUpload(@RequestParam(value="filename") MultipartFile file,HttpServletRequest request){
        logger.debug(request.getParameter("test"));
        logger.debug((String) request.getAttribute("test"));
        Map retMap = new HashMap();
        String name = file.getOriginalFilename();
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                logger.debug(file.getContentType());
                logger.debug(file.getName());
                logger.debug(file.getOriginalFilename());

                new File(uploadAbsolute+uploadRelative+"tmp/").mkdirs();

                String flag = "/";
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + flag + "MM" + flag + "dd" + flag);
                String today = sdf.format(date);
                String uuid = UUID.randomUUID().toString().toUpperCase();
                String fileName = file.getOriginalFilename();
                String ext = fileName.substring(fileName.indexOf("."));

                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(uploadAbsolute+uploadRelative+"tmp/" + uuid + ext)));
                stream.write(bytes);
                stream.close();

                retMap.put("rspCode","00");
                retMap.put("rspMsg","上传成功");
                retMap.put("fileName",uuid+ext);
            } catch (Exception e) {
                retMap.put("rspCode","99");
                retMap.put("rspMsg","上传失败");
            }
        } else {
            retMap.put("rspCode","90");
            retMap.put("rspMsg","文件为空");
        }
        return  retMap;
    }

}
