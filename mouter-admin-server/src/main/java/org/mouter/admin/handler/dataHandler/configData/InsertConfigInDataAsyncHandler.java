package org.mouter.admin.handler.dataHandler.configData;

import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.mintflow.annotation.MintFlowHandler;
import org.mintflow.async.result.AsyncResult;
import org.mintflow.handler.async.AsyncSampleFnHandler;
import org.mintflow.param.ParamWrapper;
import org.mintflow.scheduler.async.AsyncScheduler;
import org.mouter.admin.code.ErrorCode;
import org.mouter.admin.data.ConfigAppData;
import org.mouter.admin.data.ConfigInformationData;
import org.mouter.admin.data.answer.Answer;
import org.mouter.admin.data.answer.ErrorAnser;
import org.mouter.admin.dataBase.MysqlPool;
import org.mouter.admin.util.ObjectUtils;

@MintFlowHandler
public class InsertConfigInDataAsyncHandler extends AsyncSampleFnHandler {


    public static String SQL_DATA_KEY = "data.insert.config";
    public static String SQL_DATA_RESULT_KEY = "data.insert.config.result";


    public InsertConfigInDataAsyncHandler(String name) {
        super(name);
    }

    @Override
    public void asyncHandle(ParamWrapper paramWrapper, AsyncResult asyncResult, AsyncScheduler asyncScheduler) {
        MysqlPool.mysql.getConnection((res)->{
            if(res.succeeded()){
                ConfigAppData appData = paramWrapper.getContextParam(SQL_DATA_KEY);
                //如果 appId或者 group id 没有填写直接返回报错
                if(ObjectUtils.isNullOrEmpty(appData.getAppId(),appData.getAppDataId(),appData.getGroupId())){
                    paramWrapper.setParam(Answer.createAnswer(200,"success",new ErrorAnser(ErrorCode.PARAMS_ERROR,"新建配置请求参数异常，需要appId和 groupId和configId")));
                    asyncResult.doResult(paramWrapper);
                    return;
                }
                SqlConnection connection = res.result();
                connection.preparedQuery("insert into config_app_data (group_id, app_id, app_data_id, level_key, value, value_type, create_user, update_user, create_time, update_time) value (?,?,?,?,?,?,?,?,?,?)")
                        .execute(Tuple.of(appData.getGroupId(),appData.getAppId(),appData.getAppDataId(),appData.getLevelKey(),appData.getValue(),appData.getValueType(),System.currentTimeMillis(),System.currentTimeMillis(),appData.getCreateUser(),appData.getUpdateUser()),(result)->{
                            if(result.succeeded()){
                                paramWrapper.setContextParam(SQL_DATA_RESULT_KEY, Boolean.TRUE);
                                paramWrapper.setParam(Answer.createAnswer(200,"success",null));
                                asyncScheduler.next(paramWrapper,asyncResult);
                            }else{
                                result.cause().printStackTrace();
                            }
                            connection.close();
                        });
            }
        });
    }
}
