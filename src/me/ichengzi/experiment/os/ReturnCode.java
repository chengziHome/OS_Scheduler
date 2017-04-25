package me.ichengzi.experiment.os;

/**
 * Coding is pretty charming when you love it!
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 11:59
 */
public class ReturnCode {

    /*
    0代表成功，非0代表其他的各种错误
     */

    public static final int SUCCESS = 0;//成功
    public static final int RESOURCE_ERROR = -1;//不用于failure，这种情况是need的资源数目大于资源最大值max
    public static final int RESOURCE_FAILURE = -2;//资源获取失败
    public static final int PROCESS_DESTROY_FAILURE = -3;
    public static final int TIMEOUT_FAILURE = -4;


}
