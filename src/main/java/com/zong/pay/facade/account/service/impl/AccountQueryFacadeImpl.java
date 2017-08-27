package com.zong.pay.facade.account.service.impl;

import com.zong.pay.core.account.dao.AccountDao;
import com.zong.pay.core.account.dao.AccountHistoryDao;
import com.zong.pay.facade.account.entity.Account;
import com.zong.pay.facade.account.entity.AccountHistory;
import com.zong.pay.facade.account.exception.AccountBizException;
import com.zong.pay.facade.account.service.AccountQueryFacade;
import com.zong.pay.facade.account.vo.DailyCollectAccountHistoryVo;
import com.zong.paycommon.page.PageBean;
import com.zong.paycommon.page.PageParam;
import com.zong.paycommon.utils.string.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author 宗叶青 on 2017/8/26/14:59
 */
@Component("accountQueryFacade")
public class AccountQueryFacadeImpl implements AccountQueryFacade {

    @Autowired
    private AccountDao accountDao;
    @Autowired
    private AccountHistoryDao accountHistoryDao;
    /**
     * 账户历史查询.
     *
     * @param pageParam
     *            分页参数.
     * @param paramMap
     *            查询参数.
     * @return AccountHistoryList.
     */
    @Override
    public PageBean queryAccountHistoryListPage(PageParam pageParam, Map<String, Object> paramMap) throws AccountBizException {
        return accountHistoryDao.listPage(pageParam, paramMap);
    }

    /**
     * 获取账户历史
     *
     * @param accountNo
     * @param requestNo
     * @param trxType
     * @return
     */
    @Override
    public AccountHistory getAccountHistoryByAccountNo_requestNo_trxType(String accountNo, String requestNo, Integer trxType) {
        return accountHistoryDao.getByAccountNo_requestNo_trxType(accountNo, requestNo, trxType);
    }

    /**
     * 根据用户编号获取账户信息 .
     *
     * @param userNo
     *            用户编号.
     * @return account 查询到的账户信息.
     */
    @Override
    public Account getAccountByUserNo(String userNo) throws AccountBizException {
        if (StringUtil.isBlank(userNo)) {
            return null;
        }
        return accountDao.getByUserNo_IsPessimist(userNo, false);
    }

    /**
     * 根据账户编号查询账户信息.
     *
     * @param accountNo
     *            账户编号.
     * @return account 查询到的账户信息.
     */
    @Override
    public Account getAccountByAccountNo(String accountNo) throws AccountBizException {
        return accountDao.getByAccountNo(accountNo);
    }

    @Override
    public List<DailyCollectAccountHistoryVo> listDailyCollectAccountHistoryVo(String accountNo, String statDate, Integer riskDay, Integer fundDirection) throws AccountBizException {
        return accountHistoryDao.listDailyCollectAccountHistoryVo(accountNo, statDate, riskDay, fundDirection);
    }

    /**
     * 日汇总账户待结算金额_针对单笔t+0结算
     *
     * @param accountNo
     * @param requestNo
     */
    @Override
    public List<DailyCollectAccountHistoryVo> listDailyCollectAccountHistoryVo_t0(String accountNo, String requestNo) throws AccountBizException {
        return accountHistoryDao.listDailyCollectAccountHistoryVo_t0(accountNo, requestNo);
    }
}
