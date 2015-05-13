package autonavi.online.framework.configcenter.tools;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

public class BindingInitializer implements WebBindingInitializer {
    /**
     * 此类用来绑定MVC入参拼接对象时
     * 如对象的某个属性是date 且格式是yyyy-mm-dd 在此处用来转换入参并拼接仅对象
     * 防止因为入参格式问题（如默认时间格式不是yyyy-mm-dd）造成对象无法拼接
     */
    public void initBinder(WebDataBinder binder, WebRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class,  new CustomDateEditor(dateFormat, true));
        binder.registerCustomEditor(Timestamp.class, new CustomTimeNativeEditor(null, true));
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(byte.class, null,new CustomNumNativeEditor(Integer.class, true));
        binder.registerCustomEditor(int.class, null,new CustomNumNativeEditor(Integer.class, true));
        binder.registerCustomEditor(long.class, null,new CustomNumNativeEditor(Long.class, true));
        binder.registerCustomEditor(float.class, null,new CustomNumNativeEditor(Long.class, true));
        binder.registerCustomEditor(double.class, null,new CustomNumNativeEditor(Long.class, true));
        binder.registerCustomEditor(boolean.class, null,new CustomNumNativeEditor(Long.class, true));
        
    }
}
