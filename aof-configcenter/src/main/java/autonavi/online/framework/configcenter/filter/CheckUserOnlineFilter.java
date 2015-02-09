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

import autonavi.online.framework.configcenter.util.AofCcProps;

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
		}else{
			if((Boolean)req.getSession().getAttribute(AofCcProps.SESSION_FLAG_RUN)){
				if(req.getSession().getAttribute(AofCcProps.SESSION_ZK)==null){
					logger.info("Session超时");
					req.getSession().invalidate();
					req.getRequestDispatcher("/session_timeout.jsp").forward(request, response);
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
