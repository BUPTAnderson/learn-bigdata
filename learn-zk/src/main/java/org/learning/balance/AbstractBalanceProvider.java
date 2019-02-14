package org.learning.balance;

import java.util.List;

/**
 * Created by Anderson on 2018/9/18
 */
public abstract class AbstractBalanceProvider<T> implements BalanceProvider<T> {
    protected abstract T balanceAlgorithm(List<T> items);

    protected abstract List<T> getBalanceItems();

    public T getBalanceItem() {
        return balanceAlgorithm(getBalanceItems());
    }
}
