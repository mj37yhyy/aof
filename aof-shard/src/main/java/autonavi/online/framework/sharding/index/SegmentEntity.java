package autonavi.online.framework.sharding.index;

public class SegmentEntity {
	private boolean segement = false;
	private String segmentTable = null;
	private int segemntCount = 0;

	public boolean isSegement() {
		return segement;
	}

	public void setSegement(boolean segement) {
		this.segement = segement;
	}

	public String getSegmentTable() {
		return segmentTable;
	}

	public void setSegmentTable(String segmentTable) {
		this.segmentTable = segmentTable;
	}

	public int getSegemntCount() {
		return segemntCount;
	}

	public void setSegemntCount(int segemntCount) {
		this.segemntCount = segemntCount;
	}

}
