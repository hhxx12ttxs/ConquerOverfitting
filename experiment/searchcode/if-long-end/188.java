package dovetaildb.dbservice;

import java.util.Collection;
import java.util.Map;


import dovetaildb.api.ApiBuffer;
import dovetaildb.api.ApiException;
import dovetaildb.iter.Iter;

public class TxnSpacedDbService extends WrappingDbService {
	/**
	 * Confines the transaction space of the underlying DbService
	 * Though the underlying service will start at 0, this service
	 * will appear to start at <spaceStart> and will not allow
	 * commits after <spaceEnd> 
	 */

	private static final long serialVersionUID = 33879804237499L;
	
	protected long spaceStart, spaceEnd;
	
	public String toString() {
		return "TxnSpacedDbService(start="+spaceStart+", end="+spaceEnd+", "+subService+")";
	}

	@Override
	public synchronized long getHighestCompletedTxnId() {
		return subService.getHighestCompletedTxnId() + spaceStart;
	}
	
	public TxnSpacedDbService(DbService subService, long start, long end) {
		super(subService);
		spaceStart = start;
		spaceEnd = end;
	}
	
	@Override
	public Iter query(String bag, long txnId, Object query, Map<String, Object> options) {
		return subService.query(bag, txnId-spaceStart, query, options);
	}

	@Override
	public Collection<String> getBags(long txnId) {
		return subService.getBags(txnId-spaceStart);
	}

	@Override
	public synchronized long commit(long fromTxnId, Map<String, ApiBuffer> batch) {
		if (subService.getHighestCompletedTxnId()+spaceStart+1 >= spaceEnd) {
			throw new ApiException("TxnSpaceFull", "No more transactions are allowed in the current space");
		}
		return subService.commit(fromTxnId-spaceStart, batch) + spaceStart;
	}

	@Override
	public synchronized void rollback(long txnId) throws TxnNotFoundException {
		subService.rollback(txnId-spaceStart);
	}

}

