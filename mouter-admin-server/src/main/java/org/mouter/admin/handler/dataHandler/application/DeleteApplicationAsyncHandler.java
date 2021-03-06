package org.mouter.admin.handler.dataHandler.application;

import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.mintflow.annotation.MintFlowHandler;
import org.mintflow.async.result.AsyncResult;
import org.mintflow.handler.async.AsyncSampleFnHandler;
import org.mintflow.param.ParamWrapper;
import org.mintflow.scheduler.async.AsyncScheduler;
import org.mouter.admin.code.ErrorCode;
import org.mouter.admin.data.ApplicationInformationData;
import org.mouter.admin.data.answer.Answer;
import org.mouter.admin.data.answer.ErrorAnser;
import org.mouter.admin.dataBase.MysqlPool;
import org.mouter.admin.util.ObjectUtils;

@MintFlowHandler
public class DeleteApplicationAsyncHandler extends AsyncSampleFnHandler {

    public static String SQL_DATA_KEY = "data.delete.application";
    public static String SQL_DATA_RESULT_KEY = "data.delete.application.result";

    public DeleteApplicationAsyncHandler(String name) {
        super(name);
    }

    @Override
    public void asyncHandle(ParamWrapper paramWrapper, AsyncResult asyncResult, AsyncScheduler asyncScheduler) {
        MysqlPool.mysql.getConnection((res)->{
            if(res.succeeded()){
                ApplicationInformationData appdata = paramWrapper.getContextParam(SQL_DATA_KEY);
                //如果 appId或者 group id 没有填写直接返回报错
                if(ObjectUtils.isNullOrEmpty(appdata.getAppId(),appdata.getGroupId())){
                    paramWrapper.setParam(Answer.createAnswer(200,"success",new ErrorAnser(ErrorCode.PARAMS_ERROR,"删除应用请求参数异常，需要appId和 groupId")));
                    asyncResult.doResult(paramWrapper);
                    return;
                }
                SqlConnection connection = res.result();
                connection.preparedQuery("delete from application_information where group_id=? and app_id =?")
                        .execute(Tuple.of(appdata.getGroupId(),appdata.getAppId()),(result)->{
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
