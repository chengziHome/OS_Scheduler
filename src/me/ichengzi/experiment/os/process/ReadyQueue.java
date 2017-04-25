package me.ichengzi.experiment.os.process;

import com.sun.deploy.security.DeployAuthenticator;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Coding is pretty charming when you love it!
 *
 * 就按照指导书上面的指导定义了一个就绪队列的数据结构。有一点不同的是:
 * 当前CPU执行进程是有ProcessManager中的currentProcess来维护的。这样逻辑更清晰一点。
 *
 * 后来想：可以吧ReadyQueue再嵌套到manager里面去，然后进程管理就更集中，简明了。
 * 算作是一开始设计上的一点疏忽。但由于涉及到改动比较大，算了，不想改了。
 *
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 9:16
 */
public class ReadyQueue {

    private Deque<Process>[] deques;//不同优先级就绪队列组成数组，简单起见，索引值级代表优先级
    private int levelCount;
    private static volatile ReadyQueue singleton;

    private ReadyQueue(){
        levelCount = 3;
        deques = new LinkedList[levelCount];
        for (int i = 0; i < levelCount; i++) {
            deques[i] = new LinkedList<>();
        }
    }


    /**
     * 虽然这个实验的Demo绝对是单线程的，但是我单例模式仍然还是
     * 按照线程安全的方法去实现
     * @return
     */
    public static ReadyQueue getReadyQueue(){
        if (singleton==null){
            synchronized (ReadyQueue.class){
                if(singleton==null){
                    singleton = new ReadyQueue();
                }
            }
        }
        return singleton;
    }

    /**
     * 获得就绪队列里面优先级最高的进程，
     * 队列为空，则返回null
     * @return
     */
    public Process getProcess(){
        for (int i = levelCount-1; i >=0; i--) {
            Deque<Process> deque = deques[i];
            if(!deque.isEmpty()){
                return deque.peekFirst();//返回队列首元素，暂时不删除
            }
        }
        return null;
    }


    /**
     * 删除就绪队列里面指定的进程。删除成功返回true，
     * 进程不存在就返回false。
     * @param process
     * @return
     */
    public boolean removeProcess(Process process){
        int priority = process.getPriority();
        Deque<Process> deque = deques[priority];
        return deque.remove(process);

    }

    /**
     * 将某个进程加入就绪队列
     * @param process
     * @return
     */
    public boolean addProcess(Process process){

        int priority = process.getPriority();
        Deque<Process> deque = deques[priority];
        try {
            deque.addLast(process);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 清空就绪队列
     */
    public void clear(){
        for (int i = 0; i < levelCount; i++) {
            Deque deque = deques[i];
            deque.clear();
        }
    }



    /**
     *
     * 测试使用
     * 打印出当前就绪队列的整体状态，
     */
    public void printStatus(){
        for (int i = 0; i < levelCount; i++) {
            Deque<Process> deque = deques[i];
            StringBuilder sb = new StringBuilder();
            sb.append("readyQueue"+i+"[");
            for (Process process:deque){
                sb.append(",").append(process.getName());
            }
            sb.append("]");
            String result = sb.toString();
            System.out.println(result.replaceFirst(",",""));
        }
    }


}
