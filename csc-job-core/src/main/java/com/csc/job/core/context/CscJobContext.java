package com.csc.job.core.context;

/**
 * @Description:
 * @PackageName: com.csc.job.core.context
 * @Author: 陈世超
 * @Create: 2020-10-14 9:50
 * @Version: 1.0
 */
public class CscJobContext {
    private final long jobId;
    private final String jobLogFileName;
    private final int shardIndex;
    private final int shardTotal;

    public CscJobContext(long jobId, String jobLogFileName, int shardIndex, int shardTotal) {
        this.jobId = jobId;
        this.jobLogFileName = jobLogFileName;
        this.shardIndex = shardIndex;
        this.shardTotal = shardTotal;
    }

    private static InheritableThreadLocal<CscJobContext> contextHolder = new InheritableThreadLocal<>();

    public static void setCscJobContext(CscJobContext cscJobContext) {
        contextHolder.set(cscJobContext);
    }
    public static CscJobContext getCscJobContext(){
        return contextHolder.get();
    }

    public long getJobId() {
        return jobId;
    }

    public String getJobLogFileName() {
        return jobLogFileName;
    }

    public int getShardIndex() {
        return shardIndex;
    }

    public int getShardTotal() {
        return shardTotal;
    }
}
