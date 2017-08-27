package com.zong.pay.core.account.dao;

import com.zong.pay.common.core.dao.BaseDao;
import com.zong.pay.facade.account.entity.Account;

/**
 * @author 宗叶青 on 2017/8/23/0:09
 */
public interface AccountDao extends BaseDao<Account> {
    /**
     * 生成账户编号20位
     *
     * @param accountType
     * @return
     */
    String buildAccountNo(String accountType);
    /**
     * 根據帳戶編號獲取帳戶信息
     *
     * @param accountNo
     * @return
     */
    Account getByAccountNo(String accountNo);
    /**
     * 获取账户实体
     *
     * @param userNo
     * @param isPessimist
     *            是否乐观锁
     * @return
     */
    Account getByUserNo_IsPessimist(String userNo, boolean isPessimist);
}
