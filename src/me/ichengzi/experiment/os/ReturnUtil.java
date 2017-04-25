package me.ichengzi.experiment.os;

/**
 * Coding is pretty charming when you love it!
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 11:43
 */
public class ReturnUtil {

    private int ret_code;
    private String err_msg;

    private ReturnUtil(int ret_code,String err_msg){
        this.ret_code = ret_code;
        this.err_msg = err_msg;
    }

    public int getRet_code() {
        return ret_code;
    }

    public String getErr_msg() {
        return err_msg;
    }

    public static ReturnUtil getResult(int ret_code, String err_msg){
        return new ReturnUtil(ret_code,err_msg);
    }

    public static ReturnUtil success(){
        return new ReturnUtil(ReturnCode.SUCCESS,"success!");
    }

    public static ReturnUtil resourceFailure(){
        return new ReturnUtil(ReturnCode.RESOURCE_FAILURE,"资源申请失败，进程阻塞");
    }

    public static ReturnUtil resourceError(){
        return new ReturnUtil(ReturnCode.RESOURCE_ERROR,"资源总数不足,无法完成操作");
    }



}
