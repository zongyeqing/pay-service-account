package com.zong.pay.facade.account.service.impl;

import com.zong.pay.core.account.biz.AccountSettBiz;
import com.zong.pay.facade.account.exception.AccountBizException;
import com.zong.pay.facade.account.service.AccountSettFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 宗叶青 on 2017/8/26/16:21
 */

@Component("accountSettFacade")
public class AccountSettFacadeImpl implements AccountSettFacade {

    @Autowired
    private AccountSettBiz accountSettBiz;

    /**
     * 结算创建_T+0
     *
     * @param userNo
     * @param settAmount
     * @param requestNo 结算请求
     * @param trxNo 账户历史交易请求
     */
    @Override
    public void settCreateTo(String userNo, Double settAmount, String requestNo, String trxNo) throws AccountBizException {
        accountSettBiz.settCreateT0(userNo, settAmount, requestNo,trxNo);

    }

    /**
     * 结算成功
     *
     * @param userNo
     * @param settAmount
     * @param requestNo
     */
    @Override
    public void settSuccess(String userNo, Double settAmount, String requestNo) throws AccountBizException {
        accountSettBiz.settSuccess(userNo, settAmount, requestNo);
    }

    /**
     * 结算汇总成功
     *
     * @param userNo
     * @param statDate
     * @param riskDay
     */
    @Override
    public void settCollectSucess(String userNo, String statDate, Integer riskDay) throws AccountBizException {
        accountSettBiz.settCollectSuccess(userNo, statDate, riskDay);
    }

    /**
     * 结算创建
     *
     * @param userNo
     * @param settAmount
     * @param requestNo
     * @param lastId
     */
    @Override
    public void settCreate(String userNo, Double settAmount, String requestNo, Long lastId) throws AccountBizException {
        accountSettBiz.settCreate(userNo, settAmount, requestNo, lastId);
    }

    /**
     * 结算失败
     *
     * @param userNo
     * @param settAmount
     * @param requestNo
     */
    @Override
    public void settFail(String userNo, Double settAmount, String requestNo) throws AccountBizException {
        accountSettBiz.settFail(userNo, settAmount, requestNo);
    }
}
