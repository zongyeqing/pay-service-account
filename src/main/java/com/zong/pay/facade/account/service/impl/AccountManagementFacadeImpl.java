package com.zong.pay.facade.account.service.impl;

import com.zong.pay.core.account.biz.AccountManagementBiz;
import com.zong.pay.facade.account.enums.AccountInitiatorEnum;
import com.zong.pay.facade.account.enums.AccountOperationTypeEnum;
import com.zong.pay.facade.account.enums.AccountTypeEnum;
import com.zong.pay.facade.account.exception.AccountBizException;
import com.zong.pay.facade.account.service.AccountManagementFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 账号管理
 * @author 宗叶青 on 2017/8/26/14:51
 */
@Component("accountManagementFacade")
public class AccountManagementFacadeImpl implements AccountManagementFacade {

    @Autowired
    private AccountManagementBiz accountManagementBiz;

    /**
     * 重新绑定商户编号
     */
    @Override
    public long reBindUserNo(String accountNo, String userNo) throws AccountBizException {
        return accountManagementBiz.reBindUserNo(accountNo, userNo);
    }

    /**
     * 生成账户编号
     *
     * @entity accountType 账户类型.
     * @return accountNo 账户编号.
     * @throws AccountBizException
     */
    @Override
    public String buildAccountNo(AccountTypeEnum accountType) throws AccountBizException {
        return accountManagementBiz.buildAccountNo(accountType);
    }

    /**
     * 创建账户
     *
     * @param userNo
     * @param accountType
     * @return
     * @throws AccountBizException
     */
    @Override
    public long createAccount(String userNo, String accountNo, int accountType) throws AccountBizException {
        return accountManagementBiz.createAccount(userNo, accountNo, accountType);
    }

    @Override
    public long createPrivateAccount(String userNo, String accountNo, Double balance, Double securityMoney) throws AccountBizException {
        return accountManagementBiz.createPrivateAccount(userNo, accountNo, balance, securityMoney);
    }

    /**
     * 账户状态变更操作.
     *
     * @param userNo
     *            用户编号.
     * @param operationType
     *            账户操作类型.
     * @param initiator
     *            账户操作,发起方.
     * @param desc
     *            变更操作说明.
     */
    @Override
    public void changeAccountStatus(String userNo, AccountOperationTypeEnum operationType, AccountInitiatorEnum initiator, String desc) throws AccountBizException {
        accountManagementBiz.changeAccountStatus(userNo, operationType, initiator, desc);
    }
}
