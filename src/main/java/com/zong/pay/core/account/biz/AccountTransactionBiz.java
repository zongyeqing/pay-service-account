package com.zong.pay.core.account.biz;

import com.zong.pay.core.account.dao.AccountDao;
import com.zong.pay.core.account.dao.AccountFrozenHistoryDao;
import com.zong.pay.core.account.dao.AccountHistoryDao;
import com.zong.pay.facade.account.entity.Account;
import com.zong.pay.facade.account.entity.AccountFrozenHistory;
import com.zong.pay.facade.account.entity.AccountHistory;
import com.zong.pay.facade.account.enums.*;
import com.zong.pay.facade.account.exception.AccountBizException;
import com.zong.pay.facade.account.vo.AccountTransactionVo;
import com.zong.paycommon.constant.PublicStatus;
import com.zong.paycommon.utils.string.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 账户交易
 * @author 宗叶青 on 2017/8/25/3:28
 */
@Component("accountTransactionBiz")
@Transactional(rollbackFor =  Exception.class)
public class AccountTransactionBiz {

    @Autowired
    private AccountDao accountDao;
    @Autowired
    private AccountHistoryDao accountHistoryDao;
    @Autowired
    private AccountFrozenHistoryDao accountFrozenHistoryDao;

    private static final Logger LOGGER = Logger.getLogger(AccountTransactionBiz.class);

    public void execute(AccountTransactionVo vo){
        if(StringUtil.isBlank(vo.getUserNo()))
            return;

        LOGGER.info("==> execute");

        if(vo.getAccountFundDirection().equals(AccountFundDirectionEnum.ADD)){
            this.credit(vo.getUserNo(), vo.getAmount(), vo.getRequestNo(), vo.getTradeType(), vo.getDesc(), vo.getRiskDay(), vo.getFee());
        } else if (vo.getAccountFundDirection().equals(AccountFundDirectionEnum.SUB)) {
            this.debit(vo.getUserNo(), vo.getAmount(), vo.getRequestNo(), vo.getTradeType(), vo.getDesc(), vo.getFee());
        } else if (vo.getAccountFundDirection().equals(AccountFundDirectionEnum.FROZEN)) {
            this.frozen(vo.getUserNo(), vo.getFrezonAmount(), vo.getRequestNo(), vo.getTradeType());
        } else if (vo.getAccountFundDirection().equals(AccountFundDirectionEnum.UNFROZEN)) {
            this.unFrozen(vo.getUserNo(), vo.getUnFrezonAmount(), vo.getRequestNo(), vo.getTradeType());
        }
    }

    /**
     * 同一账户批量加款
     *
     * @param voList
     */
    @Transactional(rollbackFor = Exception.class)
    public void execute(List<AccountTransactionVo> voList){

        LOGGER.info("==> executeList");
        for(AccountTransactionVo vo : voList){
            this.execute(vo);
        }

        LOGGER.info("==> executeList <==");
    }

    public void batchCreditForSameAccount(List<AccountTransactionVo> list){
        if(list == null || list.isEmpty())
            return;
        LOGGER.info("==> batchCreditForSameAccount");

        Account account = accountDao.getByUserNo_IsPessimist(list.get(0).getUserNo(),true);
        if(account == null)
            throw AccountBizException.ACCOUNT_NOT_EXIT.newInstance("账户不存在，用户编号{%s}", list.get(0).getUserNo()).print();

        if(account.getStatus() == AccountStatusEnum.INACTIVE.getValue() || account.getStatus() == AccountStatusEnum.CANCELLED.getValue()
                || account.getStatus() == AccountStatusEnum.INACTIVE_FREEZE_CREDIT.getValue()){
            throw AccountBizException.ACCOUNT_STATUS_IS_INACTIVE.newInstance("账户状态异常，用户编号{%s},账户状态{%s}", list.get(0).getUserNo(), account.getStatus()).print();
        }

        int isAllowSett = PublicStatus.ACTIVE;

        //如果accountType是会员， isAllowSett置false
        if(account.getAccountType() == AccountTypeEnum.CUSTOMER.getValue())
            isAllowSett = PublicStatus.INACTIVE;

        List<AccountHistory> listHistory = new ArrayList<>();
        for(AccountTransactionVo vo : list){
            if(!vo.getUserNo().equals(account.getUserNo()))
                throw AccountBizException.BATCH_CREDIT_FOR_SAME_ACCOUNT_ERROR.print();

            //加款
            account.credit(vo.getAmount());

            AccountHistory accountHistory = new AccountHistory();
            accountHistory.setAllowSett(isAllowSett);
            accountHistory.setAmount(vo.getAmount());
            accountHistory.setFee(vo.getFee());
            accountHistory.setBalance(account.getAvailableBalance());
            accountHistory.setRequestNo(vo.getRequestNo());
            accountHistory.setCompleteSett(PublicStatus.INACTIVE);
            accountHistory.setRemark(vo.getTradeType().getDesc());
            accountHistory.setFundDirection(AccountFundDirectionEnum.ADD.getValue());
            accountHistory.setAccountNo(account.getAccountNo());
            accountHistory.setTrxType(vo.getTradeType().getValue());
            accountHistory.setRiskDay(vo.getRiskDay());
            listHistory.add(accountHistory);
        }

        accountDao.update(account);
        accountHistoryDao.insert(listHistory);
    }

    /**
     * 加款
     *
     * @param userNo
     * @param amount
     * @param requestNo
     * @param tradeType
     * @param remark
     * @param riskDay
     * @param fee
     * @return
     */
    public String credit(String userNo, double amount, String requestNo, AccountTradeTypeEnum tradeType, String remark, Integer riskDay, double fee){
        LOGGER.info("==> credit");
        LOGGER.info(String.format("==>userNo:%s, amount:%s, requestNo:%s, tradeType:%s, remark:%s",userNo, amount, requestNo, tradeType.name(), remark));

        Account account = accountDao.getByUserNo_IsPessimist(userNo, true);
        if(account == null)
            throw AccountBizException.ACCOUNT_NOT_EXIT.newInstance("账户不存在，用户编号{%s}", userNo).print();

        account.credit(amount);

        int isAllowSett = PublicStatus.ACTIVE;

        //如果accountType是会员,isAllowSett置false
        if(account.getAccountType() == AccountTypeEnum.CUSTOMER.getValue()){
            isAllowSett = PublicStatus.INACTIVE;
        }

        AccountHistory accountHistoryEntity = new AccountHistory();
        accountHistoryEntity.setAllowSett(isAllowSett);
        accountHistoryEntity.setAmount(amount);
        accountHistoryEntity.setBalance(account.getAvailableBalance());
        accountHistoryEntity.setRequestNo(requestNo);
        accountHistoryEntity.setCompleteSett(PublicStatus.INACTIVE);
        accountHistoryEntity.setRemark(remark);
        accountHistoryEntity.setFee(fee);
        accountHistoryEntity.setFundDirection(AccountFundDirectionEnum.ADD.getValue());
        accountHistoryEntity.setAccountNo(account.getAccountNo());
        accountHistoryEntity.setTrxType(tradeType.getValue());
        accountHistoryEntity.setRiskDay(riskDay);

        accountHistoryDao.insert(accountHistoryEntity);
        accountDao.update(account);

        LOGGER.info("==> credit <==");

        return account.getAccountTitleNo();
    }

    private String debit(String userNo, double amount, String requestNo, AccountTradeTypeEnum tradeType, String remark, double fee){
        LOGGER.info("==> debit <==");
        LOGGER.info(String.format("==>userNo:%s, amount:%s,requestNo:%s, tradeType:%s, remark:%s", userNo, amount, requestNo, tradeType.name(), remark));

        Account account = accountDao.getByUserNo_IsPessimist(userNo, true);
        if(account == null)
            throw AccountBizException.ACCOUNT_NOT_EXIT.newInstance("账户不存在，用户编号{%s}", userNo).print();

        account.debit(amount);

        int isAllowSett = PublicStatus.ACTIVE;
        if(account.getAccountType() == AccountTypeEnum.CUSTOMER.getValue())
            isAllowSett = PublicStatus.INACTIVE;

        if(tradeType.equals(AccountTradeTypeEnum.SETTLEMENT) || tradeType.equals(AccountTradeTypeEnum.ATM))
            isAllowSett = PublicStatus.INACTIVE;

        AccountHistory accountHistoryEntity = new AccountHistory();
        accountHistoryEntity.setAllowSett(isAllowSett);
        accountHistoryEntity.setAmount(amount);
        accountHistoryEntity.setFee(fee);
        accountHistoryEntity.setBalance(account.getAvailableBalance());
        accountHistoryEntity.setRequestNo(requestNo);
        accountHistoryEntity.setCompleteSett(PublicStatus.INACTIVE);
        accountHistoryEntity.setRemark(remark);
        accountHistoryEntity.setFundDirection(AccountFundDirectionEnum.SUB.getValue());
        accountHistoryEntity.setAccountNo(account.getAccountNo());
        accountHistoryEntity.setTrxType(tradeType.getValue());

        accountHistoryDao.insert(accountHistoryEntity);
        accountDao.update(account);

        LOGGER.info("==>debit<==");

        return account.getAccountTitleNo();
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
    @Transactional(rollbackFor = Exception.class)
    public void frozen(String userNo, double frozenAmount, String requestNo, AccountTradeTypeEnum tradeType){
        LOGGER.info("==> frozen");
        LOGGER.info(String.format("==>userNo:%s, frozenAmount:%s, requestNo:%s, tradeType:%s", userNo, frozenAmount, requestNo, tradeType.name()));

        Account account = accountDao.getByUserNo_IsPessimist(userNo, true);
        if(account == null)
            throw AccountBizException.ACCOUNT_NOT_EXIT.newInstance("账户不存在,用户编号{%s}", userNo).print();
        account.frozen(frozenAmount);

        //如果accountType是会员，isAllowSett置false
        int isAllowSett = PublicStatus.ACTIVE;
        if(account.getAccountType() == AccountTypeEnum.CUSTOMER.getValue())
            isAllowSett = PublicStatus.INACTIVE;

        //结算，提现交易类型不允许结算
        if(tradeType.equals(AccountTradeTypeEnum.SETTLEMENT) || tradeType.equals(AccountTradeTypeEnum.ATM))
            isAllowSett = PublicStatus.INACTIVE;

        // 结算，提现交易类型不允许结算 by chenjianhua
        if (tradeType.equals(AccountTradeTypeEnum.SETTLEMENT) || tradeType.equals(AccountTradeTypeEnum.ATM)) {
            isAllowSett = PublicStatus.INACTIVE;
        }

        AccountFrozenHistory accountFrozenHistory = new AccountFrozenHistory();
        accountFrozenHistory.setAmount(frozenAmount);
        accountFrozenHistory.setCurrentAmount(account.getUnBalance());
        accountFrozenHistory.setRequestNo(requestNo);
        accountFrozenHistory.setRemark(tradeType.getDesc()+"资金冻结");
        accountFrozenHistory.setAccountFrozenHistoryType(AccountFrozenHistoryTypeEnum.FROZEN.getValue());
        accountFrozenHistory.setAccountNo(account.getAccountNo());
        accountFrozenHistory.setTrxType(tradeType.getValue());

        AccountHistory accountHistoryEntity = new AccountHistory();
        accountHistoryEntity.setAllowSett(isAllowSett);
        accountHistoryEntity.setAmount(frozenAmount);
        accountHistoryEntity.setBalance(account.getAvailableBalance());
        accountHistoryEntity.setRequestNo(requestNo);
        accountHistoryEntity.setCompleteSett(PublicStatus.INACTIVE);
        accountHistoryEntity.setRemark(tradeType.getDesc()+"资金冻结");
        accountHistoryEntity.setFundDirection(AccountFundDirectionEnum.SUB.getValue());
        accountHistoryEntity.setAccountNo(account.getAccountNo());
        accountHistoryEntity.setTrxType(tradeType.getValue());

        accountDao.update(account);
        accountHistoryDao.insert(accountHistoryEntity);
        accountFrozenHistoryDao.insert(accountFrozenHistory);

        LOGGER.info("==>frozen<==");
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
    @Transactional(rollbackFor = Exception.class)
    public void unFrozen(String userNo, double unFrozenAmount, String requestNo, AccountTradeTypeEnum tradeType){
        LOGGER.info("==>unFrozen");
        LOGGER.info(String.format("==>userNo:%s, unFrozenAmount:%s, requestNo:%s, tradeType:%s", userNo, unFrozenAmount, requestNo, tradeType.name()));

        // if (AmountUtil.greaterThanOrEqualTo(0, unFrozenAmount)) {
        // throw AccountBizException.ACCOUNT_AMOUNT_ERROR.print();
        // }

        Account account = accountDao.getByUserNo_IsPessimist(userNo, true);
        if (account == null) {
            throw AccountBizException.ACCOUNT_NOT_EXIT.newInstance("账户不存在,用户编号{%s}", userNo).print();
        }

        account.unFrozen(unFrozenAmount); // 资金解冻

        // 如果accountType是会员，isAllowSett置false
        int isAllowSett = PublicStatus.ACTIVE;
        if (account.getAccountType() == AccountTypeEnum.CUSTOMER.getValue()) {
            isAllowSett = PublicStatus.INACTIVE;
        }

        // 结算，提现交易类型不允许结算 by chenjianhua
        if (tradeType.equals(AccountTradeTypeEnum.SETTLEMENT) || tradeType.equals(AccountTradeTypeEnum.ATM)) {
            isAllowSett = PublicStatus.INACTIVE;
        }

        AccountFrozenHistory accountFrozenHistory = new AccountFrozenHistory();
        accountFrozenHistory.setAmount(unFrozenAmount);
        accountFrozenHistory.setCurrentAmount(account.getUnBalance());
        accountFrozenHistory.setRequestNo(requestNo);
        accountFrozenHistory.setRemark(tradeType.getDesc()+"资金解冻");
        accountFrozenHistory.setAccountFrozenHistoryType(AccountFrozenHistoryTypeEnum.UNFROZEN.getValue());
        accountFrozenHistory.setAccountNo(account.getAccountNo());
        accountFrozenHistory.setTrxType(tradeType.getValue());

        AccountHistory accountHistoryEntity = new AccountHistory();
        accountHistoryEntity.setAllowSett(isAllowSett);
        accountHistoryEntity.setAmount(unFrozenAmount);
        accountHistoryEntity.setBalance(account.getAvailableBalance());
        accountHistoryEntity.setRequestNo(requestNo);
        accountHistoryEntity.setCompleteSett(PublicStatus.INACTIVE);
        accountHistoryEntity.setRemark(tradeType.getDesc()+"资金解冻");
        accountHistoryEntity.setFee(0D);
        accountHistoryEntity.setFundDirection(AccountFundDirectionEnum.ADD.getValue());
        accountHistoryEntity.setAccountNo(account.getAccountNo());
        accountHistoryEntity.setTrxType(tradeType.getValue());

        accountDao.update(account);
        accountFrozenHistoryDao.insert(accountFrozenHistory);
        accountHistoryDao.insert(accountHistoryEntity);

        LOGGER.info("==>unFrozen<==");
    }

    /**
     * 资金解冻并减款.
     *
     * @param userNo
     *            用户编号.
     *            冻结金额.
     * @param requestNo
     *            请求号.
     * @param tradeType
     *            账户交易类型.
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfrozen_debit(String userNo, double amount, String requestNo, AccountTradeTypeEnum tradeType, double fee){
        this.unFrozen(userNo, amount, requestNo, tradeType);
        this.debit(userNo, amount, requestNo, tradeType, tradeType.getDesc(), fee);
    }
}
