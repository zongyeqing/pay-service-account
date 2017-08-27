package com.zong.pay.core.account.biz;

import com.zong.pay.core.account.dao.AccountDao;
import com.zong.pay.core.account.dao.AccountFrozenRecordDao;
import com.zong.pay.facade.account.entity.Account;
import com.zong.pay.facade.account.entity.AccountFrozenRecord;
import com.zong.pay.facade.account.enums.AccountInitiatorEnum;
import com.zong.pay.facade.account.enums.AccountOperationTypeEnum;
import com.zong.pay.facade.account.enums.AccountStatusEnum;
import com.zong.pay.facade.account.enums.AccountTypeEnum;
import com.zong.pay.facade.account.exception.AccountBizException;
import com.zong.paycommon.utils.string.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author 宗叶青 on 2017/8/23/0:06
 */
@Component("accountManagementBiz")
@Transactional(rollbackFor = Exception.class)
public class AccountManagementBiz {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private AccountFrozenRecordDao accountFrozenRecordDao;

    private static final Logger LOGGER = Logger.getLogger(AccountSettBiz.class);

    /**
     * 重新绑定商户编号
     *
     * @param accountNo
     * @param userNo
     * @return
     */
    public long reBindUserNo(String accountNo, String userNo){
        Account account = accountDao.getByAccountNo(accountNo);
        if(account == null)
            throw AccountBizException.ACCOUNT_NOT_EXIT.print();

        account.setUserNo(userNo);
        account.setLastTime(new Date());

        long v = accountDao.update(account);

        LOGGER.info("==> changeUserNo, accountNo : " + accountNo + ", userNo :" + userNo);

        return v;
    }

    /**
     * 生成账户编号为20位
     * @param accountType 用户类型
     * @return
     */
    public String buildAccountNo(AccountTypeEnum accountType){
        String accountNo = accountDao.buildAccountNo(StringUtil.leftPadWithBytes(String.valueOf(accountType.getValue()), 3,'0', "UTF-8"));

        LOGGER.info("==>buildAccountNo:" + accountNo);

        return accountNo;
    }

    /**
     * 创建账户
     * @param userNo
     * @param accountNo
     * @param accountType
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public long createAccount(String userNo, String accountNo, int accountType){
        LOGGER.info("==>createAccount");

        //隶属叶子科目编号
        String titleNo = "";
        if(accountType == AccountTypeEnum.CUSTOMER.getValue()){
            titleNo = "2001";
        }else if(accountType == AccountTypeEnum.MERCHANT.getValue()
                || accountType == AccountTypeEnum.AGENT.getValue()
                || accountType == AccountTypeEnum.POS.getValue()
                || accountType == AccountTypeEnum.POSAGENT.getValue()
                || accountType == AccountTypeEnum.POS_OUT_SETT.getValue()){
            titleNo = "2002";
        }else if(accountType == AccountTypeEnum.PRIVATE.getValue()){
            titleNo = "1002";
        }

        Account account = new Account();
        account.setUserNo(userNo);
        account.setAccountType(accountType);
        account.setAccountNo(accountNo);
        account.setStatus(AccountStatusEnum.ACTIVE.getValue());
        account.setAccountTitleNo(titleNo);

        return accountDao.insert(account);
    }

    /**
     * 创建内部银行虚拟账户
     * @param userNo
     * @param accountNo
     * @param balance
     * @param securityMoney
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public long createPrivateAccount(String userNo, String accountNo, Double balance, Double securityMoney){
        LOGGER.info("==> createPrivateAccount");

        //隶属叶子科目编号
        String titleNo = "1002";

        Account account = new Account();
        account.setUserNo(userNo);
        account.setAccountNo(accountNo);
        account.setAccountType(AccountTypeEnum.PRIVATE.getValue());
        account.setStatus(AccountStatusEnum.ACTIVE.getValue());
        account.setAccountTitleNo(titleNo);
        account.setBalance(balance);
        account.setSecurityMoney(securityMoney);

        return accountDao.insert(account);
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
    public void changeAccountStatus(String userNo, AccountOperationTypeEnum operationType, AccountInitiatorEnum initiator, String desc){
        LOGGER.info("==> changeAccountStatus");
        LOGGER.info(String.format("==>userNo:%s, operationType:%s, desc:%s",userNo, operationType.name(), initiator.name(),desc));

        Account account = accountDao.getByUserNo_IsPessimist(userNo, false);
        if(account == null){
            throw AccountBizException.ACCOUNT_NOT_EXIT.print();
        }

        if (operationType.equals(AccountOperationTypeEnum.FREEZE_DEBIT)) {
            account.setStatus(AccountStatusEnum.INACTIVE_FREEZE_DEBIT.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.UNFREEZE_DEBIT)) {
            account.setStatus(AccountStatusEnum.ACTIVE.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.FREEZE_CREDIT)) {
            account.setStatus(AccountStatusEnum.INACTIVE_FREEZE_CREDIT.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.UNFREEZE_CREDIT)) {
            account.setStatus(AccountStatusEnum.ACTIVE.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.FREEZE_ACCOUNT)) {
            account.setStatus(AccountStatusEnum.INACTIVE.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.UNFREEZE_ACCOUNT)) {
            account.setStatus(AccountStatusEnum.ACTIVE.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.CREATE_ACCOUNT)) {
            account.setStatus(AccountStatusEnum.ACTIVE.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.FREEZE_FUND)) {
            account.setStatus(AccountStatusEnum.INACTIVE.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.UNFREEZE_FUND)) {
            account.setStatus(AccountStatusEnum.ACTIVE.getValue());
        } else if (operationType.equals(AccountOperationTypeEnum.CANCEL_ACCOUNT)) {
            account.setStatus(AccountStatusEnum.CANCELLED.getValue());
        }

        account.setLastTime(new Date());
        accountDao.update(account);

        AccountFrozenRecord accountFrozenRecord = new AccountFrozenRecord();
        accountFrozenRecord.setLastTime(new Date());
        accountFrozenRecord.setAccountNo(account.getAccountNo());
        accountFrozenRecord.setRemark(desc);
        accountFrozenRecord.setInitiator(initiator.getValue());
        accountFrozenRecord.setOperationType(operationType.getValue());

        accountFrozenRecordDao.insert(accountFrozenRecord);
    }
}
