package org.mouter.admin.handler.dataHandler.config;

import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import org.mintflow.annotation.MintFlowHandler;
import org.mintflow.async.result.AsyncResult;
import org.mintflow.handler.async.AsyncSampleFnHandler;
import org.mintflow.param.ParamWrapper;
import org.mintflow.scheduler.async.AsyncScheduler;
import org.mouter.admin.code.ErrorCode;
import org.mouter.admin.data.answer.Answer;
import org.mouter.admin.data.ConfigInformationData;
import org.mouter.admin.data.answer.ErrorAnser;
import org.mouter.admin.dataBase.MysqlPool;
import org.mouter.admin.util.ObjectUtils;

import java.util.List;

@MintFlowHandler
public class UpdateConfigInformationAsyncHandler extends AsyncSampleFnHandler {



    public static String SQL_DATA_KEY = "data.update.config";
    public static String SQL_DATA_RESULT_KEY = "data.update.application.config";


    public UpdateConfigInformationAsyncHandler(String name) {
        super(name);
    }
        @Override
        public void asyncHandle(ParamWrapper paramWrapper, AsyncResult asyncResult, AsyncScheduler asyncScheduler) {
            MysqlPool.mysql.getConnection((res)->{
                if(res.succeeded()){
                    ConfigInformationData appData = paramWrapper.getContextParam(SQL_DATA_KEY);
                    //如果 appId或者 group id 没有填写直接返回报错
                    if(ObjectUtils.isNullOrEmpty(appData.getAppId(),appData.getGroupId(),appData.getConfigId())){
                        paramWrapper.setParam(Answer.createAnswer(200,"success",new ErrorAnser(ErrorCode.PARAMS_ERROR,"新建应用请求参数异常，需要appId和 groupId")));
                        asyncResult.doResult(paramWrapper);
                        return;
                    }
                    SqlConnection connection = res.result();
                    Transaction tx = connection.begin();
                    connection.preparedQuery("select * from config_information where group_id=? and app_id=? and config_id = ?")
                            .execute(Tuple.of(appData.getGroupId(),appData.getAppId(),appData.getConfigId()),(result)->{
                                if(result.succeeded()){
                                    List<ConfigInformationData> configInformationData = null;
                                    configInformationData = ObjectUtils.getDataFrom(result.result(),ConfigInformationData.class);
                                    ConfigInformationData appDataUpdate = configInformationData.get(0);
                                    ObjectUtils.mergeObject(appDataUpdate, appData);
                                    connection.preparedQuery("update config_information set config_name = ?,config_data=?,update_time=?,update_user=?  where group_id=? and app_id=? and config_id = ?")
                                            .execute(Tuple.of(appDataUpdate.getConfigName(),appDataUpdate.getConfigData(),System.currentTimeMillis(),appDataUpdate.getUpdateUser(),appDataUpdate.getGroupId(),appDataUpdate.getAppId(),appDataUpdate.getConfigId()),r->{
                                                if(r.succeeded()){
                                                    tx.commit((vo)->{
                                                        if(vo.succeeded()) {
                                                            paramWrapper.setContextParam(SQL_DATA_RESULT_KEY, Boolean.TRUE);
                                                            paramWrapper.setParam(Answer.createAnswer(200, "success", null));
                                                            asyncScheduler.next(paramWrapper, asyncResult);
                                                        }else{
                                                            vo.cause().printStackTrace();
                                                            paramWrapper.setParam(Answer.createAnswer(200,"success",new ErrorAnser(ErrorCode.PARAMS_ERROR,"更新应用信息，数据合并失败")));
                                                            asyncResult.doResult(paramWrapper);
                                                            return;
                                                        }
                                                        connection.close();
                                                    });
                                                }else{
                                                    r.cause().printStackTrace();
                                                }
                                            });
                                }
                            });
                }
            });
    }
}
