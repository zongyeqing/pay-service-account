package com.zong.pay.facade.account.service.impl;

import com.zong.pay.core.account.biz.AccountTransactionBiz;
import com.zong.pay.facade.account.enums.AccountTradeTypeEnum;
import com.zong.pay.facade.account.vo.AccountTransactionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 账户交易
 * @author 宗叶青 on 2017/8/26/16:33
 */
@Component("accountTransactionFacade")
public class AccountTransactionFacadeImpl {

    @Autowired
    private AccountTransactionBiz accountTransactionBiz;

    /**
     * 账户收/付款(单笔).
     *
     * @param vo
     *            交易命令参数vo .
     */
    public void execute(AccountTransactionVo vo) {
        accountTransactionBiz.execute(vo);
    }

    /**
     * 账户收/付款(批量)
     *
     * @param voList
     *            交易命令参数vo集.
     */
    public void execute(List<AccountTransactionVo> voList) {
        accountTransactionBiz.execute(voList);
    }

    /**
     * 同一账户批量加款.
     *
     * @param voList
     *            交易命令参数vo集.
     */
    public void batchCreditForSameAccount(List<AccountTransactionVo> voList) {
        accountTransactionBiz.batchCreditForSameAccount(voList);
    }

    /**
     * 资金冻结.
     *
     * @param userNo
     *            用户编号.
     * @param frozenAmount
     *            冻结金额.
     * @param requestNo
     *            请求号.
     * @param tradeType
     *            账户交易类型.
     */
    public void frozen(String userNo, double frozenAmount, String requestNo, AccountTradeTypeEnum tradeType) {
        accountTransactionBiz.frozen(userNo, frozenAmount, requestNo, tradeType);
    }

    /**
     * 资金解冻.
     *
     * @param userNo
     *            用户编号.
     * @param unFrozenAmount
     *            解冻金额.
     * @param requestNo
     *            请求号.
     * @param tradeType
     *            账户交易类型.
     */
    public void unFrozen(String userNo, double unFrozenAmount, String requestNo, AccountTradeTypeEnum tradeType) {
        accountTransactionBiz.unFrozen(userNo, unFrozenAmount, requestNo, tradeType);
    }

    /**
     * 资金解冻并减款.
     *
     * @param userNo
     *            用户编号.
     * @param requestNo
     *            请求号.
     * @param tradeType
     *            账户交易类型.
     * @param fee
     *            手续费
     */
    public void unfrozen_debit(String userNo, double amount, String requestNo, AccountTradeTypeEnum tradeType, double fee) {
        accountTransactionBiz.unfrozen_debit(userNo, amount, requestNo, tradeType, fee);
    }
}
