package org.learning.balance;

/**
 * Created by Anderson on 2018/9/18
 */
public interface RegistProvider {
    public void regist(Object context) throws Exception;

    public void unRegist(Object context) throws Exception;
}
