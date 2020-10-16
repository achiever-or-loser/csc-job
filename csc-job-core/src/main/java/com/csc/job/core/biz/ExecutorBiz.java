package com.csc.job.core.biz;

import com.csc.job.core.biz.model.*;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz
 * @Author: 陈世超
 * @Create: 2020-10-14 15:44
 * @Version: 1.0
 */
public interface ExecutorBiz {
    public ReturnT<String> beat();

    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam);

    ReturnT<String> run(TriggerParam triggerParam);

    ReturnT<String> kill(KillParam killParam);

    ReturnT<LogResult> log(LogParam logParam);
}
