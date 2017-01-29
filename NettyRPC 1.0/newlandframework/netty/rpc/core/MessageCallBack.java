package newlandframework.netty.rpc.core;

import newlandframework.netty.rpc.model.MessageRequest;
import newlandframework.netty.rpc.model.MessageResponse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 客户端调用: Rpc消息回调
 */
public class MessageCallBack {

    private MessageRequest request;
    private MessageResponse response;
    private Lock lock = new ReentrantLock();//显式锁:可重入锁
    private Condition finish = lock.newCondition();//signal

    /**
     * 构造器
     * @param request
     */
    public MessageCallBack(MessageRequest request) {
        this.request = request;
    }

    /**
     * 开始处理request
     * @return
     * @throws InterruptedException
     */
    public Object start() throws InterruptedException {
        try {
            lock.lock();//加锁
            finish.await(10*1000, TimeUnit.MILLISECONDS);//阻塞,释放锁,等待信号

            //被唤醒之后
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * over
     * @param reponse
     */
    public void over(MessageResponse reponse) {
        try {
            lock.lock();
            finish.signal();//唤醒
            this.response = reponse;
        } finally {
            lock.unlock();
        }
    }
}
