package org.learning.balance;

public interface BalanceUpdateProvider {
    // 增加负载
    public boolean addBalance(Integer step);

    // 减少负载
    public boolean reduceBalance(Integer step);
}
