package me.ichengzi.experiment.os.process;

import com.sun.istack.internal.NotNull;
import me.ichengzi.experiment.os.resource.Resource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coding is pretty charming when you love it!
 *
 * 进程的一个管理器，系统中所有进程，从出生开始就要在这里注册，并且直至死亡仍由
 * 这个管理器管理。
 * 一方面，在程序中可以用来直接访问或者管理进程，另一方面，这个类提供了很多监视进程的工具。
 * 显然，这个类应该是全局唯一的，采用单例模式。
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 9:18
 */
public class ProcessManager {

    private AtomicInteger idGenerator;//id生成器，系统内部唯一标识

    private Map<String,Process> existProcesses;//所有存活的进程，包括Running,Blocked,Ready,new(单线程下不存在)
    private List<Process> deadList ;//已经被杀掉的进程，本来应该把这部分进程对象引用完全释放，这里维护这个死亡队列主要为了测试阶段监控方便。
    private Process currentProcess;//当前占用CPU的进程

    private static final ProcessManager manager = new ProcessManager();//私有构造器，实现饿汉类型的单例模式
    private static final ReadyQueue readyQueue = ReadyQueue.getReadyQueue();

    private ProcessManager(){
        existProcesses = new HashMap<>();
        deadList = new LinkedList<>();
        idGenerator = new AtomicInteger();
    }

    public static ProcessManager getManager(){
        return manager;
    }


    public  int generatorId(){
        return idGenerator.getAndIncrement();
    }

    public int maxPID(){
        return idGenerator.get();
    }

    /**
     * 主要用于判断用户输入的进程名称是否合法，因为name对用户来说是进程唯一标识
     * @param name
     * @return
     */
    public  boolean exsitName(String name){
        return existProcesses.containsKey(name);
    }

    /**
     * 进程一出生，就会调用这个放在注册在里面
     * @param process
     */
    public void addExistList(Process process){
        existProcesses.put(process.getName(),process);
    }

    /**
     * 通过进程名称在existList里面找到进程，返回引用
     * @param processName
     * @return
     */
    public Process findProcess(String processName){
        for (Map.Entry<String,Process> entry:existProcesses.entrySet()){
            String name = entry.getKey();
            if (processName.equals(name)){
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 获得当前占用CPU的进程
     * @return
     */
    public  Process getCurrentProcess() {
        return currentProcess;
    }

    /**
     * 更新当前CPU的执行进程
     * @param currentProcess
     */
    public void setCurrentProcess(Process currentProcess) {
        this.currentProcess = currentProcess;
    }

    /**
     * 当某个进程被杀掉，加入死亡队列
     * @param p
     */
    public void addDeadProcess(Process p){
        deadList.add(p);
        String name = p.getName();
        existProcesses.remove(name);
        if (deadList.size()==100)//默认死亡队列达到100时候清除整个队列
            clearDeadProcess();
    }


    /**
     * 强行切换到指定进程
     * @param process shell已做判断，必定是就绪进程
     */
    public void checkoutProcess(Process process){
        Process currentProcess = manager.getCurrentProcess();
        readyQueue.addProcess(currentProcess);
        currentProcess.setState(ProcessImpl.State.READY);

        readyQueue.removeProcess(process);
        setCurrentProcess(process);
        process.setState(ProcessImpl.State.RUNNING);

    }


    /**
     * 定期或者不定期的将死亡队列清楚
     */
    public void clearDeadProcess(){
        deadList.clear();
    }

    /**
     * 清空所有队列。唯独id生成器继续工作。
     */
    public void refresh(){
        existProcesses.clear();
        currentProcess = null;
        clearDeadProcess();
    }



    /**
     * 下面是打印一些监控信息
     */


    /**
     * 打印出所有存活进程的状态
     */
    public void printExistProcess(){
        StringBuilder sb = new StringBuilder();
        sb.append("existList:[");
        for (Map.Entry<String,Process> entry:existProcesses.entrySet()){
            String name = entry.getKey();
            String state = entry.getValue().getState().toString();
            sb.append(",").append(name)
                    .append("(").append(state).append(")");
        }
        sb.append("]");
        String result = sb.toString();
        System.out.println(result.replaceFirst(",",""));
    }


    /**
     * 打印死亡队列中所有进程
     */
    public void printDeadProcess(){
        StringBuilder sb = new StringBuilder();
        sb.append("deadList:[");
        for (Process process:deadList){
            sb.append(",").append(process.getName());
        }
        sb.append("]");
        String result = sb.toString();
        System.out.println(result.replaceFirst(",",""));
    }

    /**
     * 打印出以指定进程为根节点的整个进程树。
     * 格式还比较简陋，小型进程树基本可以一目了然了
     * @param root
     */
    public void printProcessTree(@NotNull Process root){
            printChildrenTree(root,0);
    }

    private void printChildrenTree(Process process,int retract){
        for (int i = 0; i < retract; i++) {
            System.out.print("  ");
        }
        System.out.println("|-"+process.getName()+"("+process.getState()+","+process.getPriority()+")");
        List<Process> children = process.getChildren();
        for (int i = 0; i <children.size(); i++) {
            Process child = children.get(i);
            printChildrenTree(child,retract+1);
        }

    }

    public void printProcessDetail(@NotNull Process process){
        System.out.println("********* Process Details **********");
        System.out.printf("%18s","PID:");
        System.out.println(process.getPID());
        System.out.printf("%18s","name:");
        System.out.println(process.getName());
        System.out.printf("%18s","priority:");
        System.out.println(process.getPriority());
        System.out.printf("%18s","state:");
        System.out.println(process.getState());

        System.out.printf("%18s","resource:");
        if (process.getResourceMap().isEmpty()){
            System.out.println("(no hold resource)");
        }else{
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (Map.Entry<Resource,Integer> entry:process.getResourceMap().entrySet()){
                Resource res = entry.getKey();
                int holdNum = entry.getValue();
                sb.append(",").append("R").append(res.getRID()).append(":").append(holdNum);
            }
            sb.append(")");
            String result = sb.toString();
            System.out.println(result.replaceFirst(",",""));
        }
        System.out.printf("%18s","parent:");
        if (process.getName().equals("init")){
            System.out.println("no");
        }else{
            System.out.println(process.getParent().getName());
        }
        System.out.printf("%18s","children:");
        if (process.getChildren().isEmpty()){
            System.out.println("(no children!)");
        }else{
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (Process child:process.getChildren()){
                sb.append(",").append(child.getName());
            }
            sb.append("]");
            String result = sb.toString();
            System.out.println(result.replaceFirst(",",""));
        }
    }




}
