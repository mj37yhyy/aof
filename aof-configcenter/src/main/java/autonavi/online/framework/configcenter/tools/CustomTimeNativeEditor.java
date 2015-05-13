package autonavi.online.framework.configcenter.tools;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.util.StringUtils;

public class CustomTimeNativeEditor extends CustomDateEditor {
	private static final String DATETIME_MILLIS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSS";
	
	private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	private static final String DATETIME_SECOND_PATTERN = "yyyy-MM-dd HH:mm";

	private static final String DATE_PATTERN = "yyyy-MM-dd";

	private static final String MONTH_PATTERN = "yyyy-MM";

	public CustomTimeNativeEditor(DateFormat dateFormat, boolean allowEmpty) {
		super(dateFormat, allowEmpty);
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		try {
			if (!StringUtils.hasText(text))
				return;
			Date rs = DateUtils.parseDate(text, new String[] { DATETIME_MILLIS_PATTERN, DATETIME_PATTERN, DATETIME_SECOND_PATTERN, DATE_PATTERN, MONTH_PATTERN });
			super.setValue(new Timestamp(rs.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
