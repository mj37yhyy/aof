package autonavi.online.framework.sharding.transaction.holder;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

public class TxManagerHolder {
	private PlatformTransactionManager tm;
	private TransactionStatus status;
	private Integer dataSourceKey;
	

	public TxManagerHolder(Integer dataSourceKey, PlatformTransactionManager tm,
			TransactionStatus status) {
		this.tm = tm;
		this.status = status;
		this.dataSourceKey=dataSourceKey;
	}
	
	
	





	public Integer getDataSourceKey() {
		return dataSourceKey;
	}



	public void setDataSourceKey(Integer dataSourceKey) {
		this.dataSourceKey = dataSourceKey;
	}



	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}


	public PlatformTransactionManager getTm() {
		return tm;
	}

	public void setTm(PlatformTransactionManager tm) {
		this.tm = tm;
	}

}
