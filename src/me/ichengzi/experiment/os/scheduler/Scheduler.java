package me.ichengzi.experiment.os.scheduler;

import me.ichengzi.experiment.os.ReturnCode;
import me.ichengzi.experiment.os.ReturnUtil;
import me.ichengzi.experiment.os.process.Process;
import me.ichengzi.experiment.os.process.ProcessImpl;
import me.ichengzi.experiment.os.process.ProcessManager;
import me.ichengzi.experiment.os.process.ReadyQueue;

import javax.print.attribute.standard.RequestingUserName;

/**
 * Coding is pretty charming when you love it!
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 9:19
 */
public class Scheduler {

    private static final ReadyQueue readyQueue = ReadyQueue.getReadyQueue() ;
    private static final ProcessManager manager = ProcessManager.getManager();


    /**
     * 要看规则2，scheduler的主要任务是更新currentProcess和readyList。
     * @return
     */
    public static ReturnUtil scheduler() {

        Process currentProcess = manager.getCurrentProcess();
        Process readyProcess = readyQueue.getProcess();

        /*
            首先如果是杀init进程，有专门处理函数destroyInit，就不会调用这里的代码，也就是说此刻init还活着。
            那么如果就绪队列都空了，那么init进程状态只能是block或者running，又根据规则1,init进程不阻塞，
            所以此时CPU上运行的必定是init进程。
            关于init进程不阻塞在Resource的request方法里面已经做过处理，所以这里可能是timeout导致,
            或者是杀进程导致的调度
         */
        if (readyProcess==null){
            manager.getCurrentProcess().setState(ProcessImpl.State.RUNNING);
            return ReturnUtil.success();
        }
        /*
            这种情况就是当刚刚创建init进程的那次调度准备，从此之后，这块代码就不可能用到了(规则1)，除非reset系统
         */
        else if(currentProcess==null){
            readyQueue.removeProcess(readyProcess);
            manager.setCurrentProcess(readyProcess);
            readyProcess.setState(ProcessImpl.State.RUNNING);
            return ReturnUtil.success();
        }
        /*
            下面主要依据的规则2
         */
        else if(currentProcess.getState() == ProcessImpl.State.TERMINATED
                || currentProcess.getState()==ProcessImpl.State.BLOCKED){//进程被杀或者阻塞
            readyQueue.removeProcess(readyProcess);
            manager.setCurrentProcess(readyProcess);
            readyProcess.setState(ProcessImpl.State.RUNNING);
        }else if(currentProcess.getState()==ProcessImpl.State.RUNNING){//新创建了进程，或者阻塞队列中进程转移到readyList
            if (currentProcess.getPriority()<readyProcess.getPriority()){
                preempt(readyProcess,currentProcess);
            }else{
                //do nothing...
            }
        }else if(currentProcess.getState()== ProcessImpl.State.READY){//timeout的情况。
            if (currentProcess.getPriority()<=readyProcess.getPriority()){//注意这里是小于等于
                preempt(readyProcess,currentProcess);
            }else{
                currentProcess.setState(ProcessImpl.State.RUNNING);
            }
        }
        return ReturnUtil.success();
    }

    /**
     * 模拟RR调度算法的时间片耗尽
     * @return
     */
    public static ReturnUtil timeout() {
        manager.getCurrentProcess().setState(ProcessImpl.State.READY);
        return scheduler();
    }

    /**
     * 进程切换，当前进程和readyList中优先级最高的进程交换CPU使用权。
     * @param readyProcess
     * @param currentProcess
     * @return
     */
    public static ReturnUtil preempt(Process readyProcess,Process currentProcess) {
        readyQueue.addProcess(currentProcess);
        currentProcess.setState(ProcessImpl.State.READY);

        readyQueue.removeProcess(readyProcess);
        manager.setCurrentProcess(readyProcess);
        readyProcess.setState(ProcessImpl.State.RUNNING);
        return ReturnUtil.success();
    }
}
