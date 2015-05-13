package autonavi.online.framework.cc.config;

import java.util.ArrayList;
import java.util.List;

import autonavi.online.framework.util.ScanAllClassHandle;

public class ShardPipelineHolder {
	public static final List<ScanAllClassHandle> scanPipeline = new ArrayList<ScanAllClassHandle>();
	public static final List<ScanPipelineAfter> scanPipelineAfters = new ArrayList<ScanPipelineAfter>();

}
