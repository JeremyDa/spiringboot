package com.jeremy.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeremy.exception.DBException;
import com.jeremy.util.StringUtil;
import commonj.sdo.DataObject;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 18/05/2017.
 */
@Service
@Transactional
public class WebService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${upload.absolute}")
    private String uploadAbsolute;

    @Autowired
    private SqlSession sqlSession;

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object service(Map paramMap) throws Exception {
        String tradeCodeList = (String) paramMap.get("tradeCodeList");
        String[] tradeCodeArray = tradeCodeList.split(",");
        String rollbackList = (String) paramMap.get("rollback");
        String[] rollbackArray = new String[0];
        if(rollbackList != null){
            rollbackArray = rollbackList.split(",");
        }

        int i = 0;
        for (String tradeCode : tradeCodeArray) {

            paramMap.put("tradeCode",tradeCode);
            int retCode = (int) dbinvoke(paramMap);
            if(rollbackArray.length > 0 && "1".equals(rollbackArray[i++])){

                if(SqlCommandType.INSERT == getSqlCommandType(tradeCode) ||
                        SqlCommandType.UPDATE == getSqlCommandType(tradeCode) ||
                        SqlCommandType.DELETE == getSqlCommandType(tradeCode)){
                    if(retCode <= 0){
                        throw new Exception("操作失败,数据已回滚");
                    }
                }
            }
        }
        return 1;
    }

    public Object dbinvoke(Map paramMap){

        String stmtName = (String) paramMap.get("tradeCode");
        logger.debug("stmtName:" + stmtName);

        MappedStatement ms = sqlSession.getConfiguration() .getMappedStatement(stmtName);
        SqlCommandType sqlct = ms.getSqlCommandType();
        String sql = ms.getBoundSql(paramMap).getSql();

        if (SqlCommandType.INSERT == sqlct) {
            return Integer.valueOf(sqlSession.insert(stmtName, paramMap));
        }
        if (SqlCommandType.UPDATE == sqlct) {
            return Integer.valueOf(sqlSession.update(stmtName, paramMap));
        }
        if (SqlCommandType.DELETE == sqlct) {
            return Integer.valueOf(sqlSession.delete(stmtName, paramMap));
        }
        if (SqlCommandType.SELECT == sqlct) {
            String offset = (String) paramMap.get("start");
            String limit = (String) paramMap.get("length");

            if(StringUtil.isEmpty(offset)|| StringUtil.isEmpty(limit)||Integer.parseInt(limit)<0){
                return sqlSession.selectList(stmtName, paramMap);
            }else{
                return sqlSession.selectList(stmtName, paramMap,new RowBounds(Integer.parseInt(offset),Integer.parseInt(limit)));
            }

        }

        throw new DBException(
                "exception.lg.runtime.db.unsupportedMappedStmt",
                new Object[] { sqlct.toString(), stmtName });
    }


    protected String getValue(Object container, String key) {
        if ((container instanceof Map)) {
            return (String) ((Map) container).get(key);
        }
        if ((container instanceof DataObject)) {
            return ((DataObject) container).getString(key);
        }
        throw new DBException(
                "exception.lg.runtime.db.unsupportedParameterType",
                new Object[] { container.getClass().getName(),container.toString()}
        );
    }

    protected boolean containsKey(Object container, String key) {
        if ((container instanceof Map)) {
            return ((Map) container).containsKey(key);
        }
        if ((container instanceof DataObject)) {
            return ((DataObject) container).isSet(key);
        }
        throw new DBException(
                "exception.lg.runtime.db.unsupportedParameterType",
                new Object[] { container.getClass().getName(),container.toString()}
        );
    }

    public SqlCommandType getSqlCommandType(String tradeCode){
        MappedStatement ms = sqlSession.getConfiguration() .getMappedStatement(tradeCode);
        SqlCommandType sqlct = ms.getSqlCommandType();

        return sqlct;
    }


    public int generateJsonFile(Map<String, Object> paramMap) {
        int ret = 1;
        try{

            String tradeCode = (String) paramMap.get("tradeCode");
            String domain = tradeCode.substring(0,tradeCode.indexOf("."));

            List list = sqlSession.selectList(domain + ".selectByPrimaryKey");
            logger.debug(list.toString());

            HashMap retMap = new HashMap();
            retMap.put("returnCode","0000");
            retMap.put("returnInfo","正常");
            retMap.put("data",list);

            Gson gson=new GsonBuilder().serializeNulls().create();
            String jsonString=gson.toJson(retMap);
            File file = new File(uploadAbsolute+"json/");
            if(!file.exists()){
                file.mkdirs();
            }
            Files.write(Paths.get(uploadAbsolute+"json/"+tradeCode.substring(0,tradeCode.indexOf("."))+".json"), jsonString.getBytes());

        }catch (Exception e){
            ret = 0;
            e.printStackTrace();
        }finally {
            return ret;
        }
    }
}
