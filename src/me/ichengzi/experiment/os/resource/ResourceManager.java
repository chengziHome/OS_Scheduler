package me.ichengzi.experiment.os.resource;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coding is pretty charming when you love it!
 *
 * 资源的一个全局管理类，和进程管理器类似的。资源一旦创建也要在这里注册接受管理
 *
 * @author Chengzi Start
 * @date 2017/4/22
 * @time 10:20
 */
public class ResourceManager {

    private AtomicInteger idGenerator;
    private static final Deque<Resource> resourceDeque = new LinkedList<>();

    private static final ResourceManager manager = new ResourceManager();

    public static boolean addResource(Resource resource){
        resourceDeque.addLast(resource);
        return true;
    }

    private ResourceManager(){
        this.idGenerator = new AtomicInteger(1);//资源起始值为1，更加直观
    }

    public static ResourceManager getManager(){
        return manager;
    }

    public int generateRID(){
        return idGenerator.getAndIncrement();
    }

    /**
     * 打印所有资源的当前使用状况
     */
    public static void printCurrentStatus(){
        for (Resource resource:resourceDeque){
            resource.printCurrentStauts();
        }
    }

    /**
     * 初始化所有资源
     */
    public void clear(){
        for (Resource resource:resourceDeque){
            resource.clear();
        }
    }

}
