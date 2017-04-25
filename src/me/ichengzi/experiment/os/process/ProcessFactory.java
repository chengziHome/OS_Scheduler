package me.ichengzi.experiment.os.process;

import me.ichengzi.experiment.os.scheduler.Scheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Coding is pretty charming when you love it!
 *
 *
 * 创建进程的工厂。虽然这个只是进程调度的一个实验Demo。但一开始构思的时候
 * 仍然习惯性的考虑了程序的扩展性和维护性。显然，工厂类的存在让程序更加符合OCP原则。
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 10:05
 */
public class ProcessFactory {

    public static Process createProcess(String name,int priority){

        Process currentProcess = ProcessManager.getManager().getCurrentProcess();
        Process process =  new ProcessImpl(ProcessManager.getManager().generatorId()
                            ,name
                            ,priority
                            ,ProcessImpl.State.NEW
                            ,new HashMap<>()
                            ,currentProcess
                            ,new LinkedList<>());

        if (currentProcess != null){//排除第一个进程的情况
            currentProcess.getChildren().add(process);
            process.setParent(currentProcess);
        }

        ProcessManager.getManager().addExistList(process);
        ReadyQueue.getReadyQueue().addProcess(process);
        process.setState(ProcessImpl.State.READY);

        Scheduler.scheduler();
        return process;
    }

}
