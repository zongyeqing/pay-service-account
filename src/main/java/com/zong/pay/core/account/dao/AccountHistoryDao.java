package com.zong.pay.core.account.dao;

import com.zong.pay.common.core.dao.BaseDao;
import com.zong.pay.facade.account.entity.AccountHistory;
import com.zong.pay.facade.account.vo.DailyCollectAccountHistoryVo;

import java.util.List;

/**
 * @author 宗叶青 on 2017/8/23/0:14
 */
public interface AccountHistoryDao extends BaseDao<AccountHistory> {

    /**
     * 获取账户历史
     *
     * @param accountNo
     * @param requestNo
     * @param trxType
     * @return
     */
    AccountHistory getByAccountNo_requestNo_trxType(String accountNo, String requestNo, Integer trxType);

    /**
     * 日汇总账户待结算金额
     *
     * @param accountNo
     * @param statDate
     * @param riskDay
     * @param fundDirection
     * @return
     */
    List<DailyCollectAccountHistoryVo> listDailyCollectAccountHistoryVo(String accountNo, String statDate, Integer riskDay,
                                                                        Integer fundDirection);

    /**
     * 日汇总账户待结算金额_针对单笔t+0结算
     *
     * @param accountNo
     * @param requestNo
     */
    List<DailyCollectAccountHistoryVo> listDailyCollectAccountHistoryVo_t0(String accountNo, String requestNo);

    /**
     * 更新账户风险预存期外的账户历史记录记为结算完成
     *
     * @param accountNo
     * @param statDate
     * @param riskDay
     */
    void updateCompleteSettTo100(String accountNo, String statDate, Integer riskDay);

    /**
     * 更新账户历史记录记为结算完成_针对单笔t+0结算
     *
     * @param accountNo
     * @param requestNo
     */
    void updateCompleteSettTo100_t0(String accountNo, String requestNo);

    /**
     * 更新账户历史记录记为结算完成
     *
     * @param accountNo
     * @param lastId
     */
    void updateCompleteSettTo100LastId(String accountNo, Long lastId);
}
