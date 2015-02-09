package autonavi.online.framework.configcenter.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;

import autonavi.online.framework.configcenter.util.AofCcProps;
import autonavi.online.framework.configcenter.util.ZooKeeperClientHolder;
import autonavi.online.framework.zookeeper.SysProps;
import autonavi.online.framework.zookeeper.ZooKeeperUtils;

public class CheckUserOnlineFilter implements Filter {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req=(HttpServletRequest)request;
		//HttpServletResponse repo=(HttpServletResponse)response;
//		logger.info("检测用户session "+req.getSession().getAttribute(AofCcProps.SESSION_APP));
		if(req.getSession().getAttribute(AofCcProps.SESSION_APP)==null
				||req.getSession().getAttribute(AofCcProps.SESSION_FLAG_RUN)==null){
			logger.info("Session超时");
			req.getSession().invalidate();
			req.getRequestDispatcher("/session_timeout.jsp").forward(request, response);
			return;
		}else {
			String appName=(String)req.getSession().getAttribute(AofCcProps.SESSION_APP);
			boolean isRunMode=(Boolean)req.getSession().getAttribute(AofCcProps.SESSION_FLAG_RUN);
			if(isRunMode){
				//运行模式下 单点登录和ZK连接状态校验
				ZooKeeper zk=ZooKeeperClientHolder.getZooKeeper(appName);
				if(zk==null){
					//ZK未获取到
					logger.info("未获取到ZooKeeper连接 应用名称["+appName+"]");
					req.getSession().setAttribute(AofCcProps.SESSION_APP, null);
					req.getSession().invalidate();
					req.getRequestDispatcher("/session_timeout.jsp").forward(request, response);
					return;
				}
				String sessionId="";
				try {
					sessionId=ZooKeeperUtils.getZkNode(zk, SysProps.AOF_ROOT + SysProps.AOF_PASS + "/" + appName+SysProps.LOGIN_FLAG);
				} catch (Exception e) {
					logger.info("ZooKeeper连接存在问题 应用名称["+appName+"]");
					//直接释放连接
					ZooKeeperClientHolder.cleanZooKeeperClinet(appName);
					req.getSession().setAttribute(AofCcProps.SESSION_APP, null);
					req.getSession().invalidate();
					req.getRequestDispatcher("/session_timeout.jsp").forward(request, response);
					return;
				}
				if(!req.getSession().getId().equals(sessionId)){
					logger.info("本次登录已经被踢");
					//设置为空防止清理掉已经被更新的ZK连接
					req.getSession().setAttribute(AofCcProps.SESSION_APP, null);
					req.getSession().invalidate();
					req.getRequestDispatcher("/session_timeout.jsp").forward(request, response);
					return;
				}
			}
			
			chain.doFilter(request, response);
		}

	}
	

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
