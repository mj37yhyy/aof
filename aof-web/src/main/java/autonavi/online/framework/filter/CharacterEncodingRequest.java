package autonavi.online.framework.filter;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class CharacterEncodingRequest extends HttpServletRequestWrapper {
	private String encoding="utf-8";
	public CharacterEncodingRequest(HttpServletRequest request,String encoding) {
		super(request);
		this.encoding=encoding;
	}

	private String changeEncoding(String input, String srcEncoding,
			String targetEncoding) {
		try {
			// 1. 获取源编码的bytes[]
			byte[] data = input.getBytes(srcEncoding);
			// 2. 将bytes[]按照制定编码转换为String
			return new String(data, targetEncoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return input;
	}

	@Override
	public String getParameter(String name) {
		String value = super.getParameter(name);

		if (value != null) {
			value = changeEncoding(value, "ISO8859-1", this.encoding);
		}
		return value;
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = super.getParameterValues(name);
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				String value = values[i];

				values[i] = changeEncoding(value, "ISO8859-1",this.encoding);
			}
		}
		return values;
	}
}
