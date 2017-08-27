package com.zong.pay.core.account.dao.impl;

import com.zong.pay.common.core.dao.BaseDaoImpl;
import com.zong.pay.core.account.dao.AccountDao;
import com.zong.pay.facade.account.entity.Account;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 宗叶青 on 2017/8/23/0:15
 */
public class AccountDaoImpl extends BaseDaoImpl<Account> implements AccountDao {
    /**
     * 生成账户编号20位
     */
    @Override
    public String buildAccountNo(String accountType) {
        String accountNoSeq = super.getSeqNextValue("ACCOUNT_NO_SEQ");
        String accountNo = "8008" + accountType + accountNoSeq + "0101";
        return accountNo;
    }

    /**
     * 根据账户编号获取账户信息
     * @param accountNo 账户编号
     * @return
     */
    @Override
    public Account getByAccountNo(String accountNo) {
        Map<String, Object> params= new HashMap<>();
        params.put("accountNo", accountNo);
        return super.getBy(params);
    }

    /**
     * 获取账户实体
     * @param userNo
     * @param isPessimist
     *            是否乐观锁
     * @return
     */
    @Override
    public Account getByUserNo_IsPessimist(String userNo, boolean isPessimist) {
        Map<String, Object> params = new HashMap<>();
        params.put("userNo", userNo);
        params.put("isPessimist", isPessimist);
        return super.getBy(params);
    }
}
