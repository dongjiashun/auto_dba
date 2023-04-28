package com.autodb.ops.dms.web.velocity;

import com.autodb.ops.dms.common.AppContext;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.service.sys.MenuService;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.velocity.tools.config.DefaultKey;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Static toolbox
 * @author dongjs
 * @since 2015/11/9
 */
@DefaultKey("static")
public class StaticTool {
    private String staticPath;
    private String staticVersion;

    public StaticTool() {
        super();
        Environment env = AppContext.getApplicationContext().getEnvironment();
        Objects.requireNonNull(env);
        staticPath = env.getProperty("server.static-path", "/static/");
        staticVersion = FastDateFormat.getInstance("yyyyMMdd").format(new Date());
    }

    /** append static path **/
    public String path(String path) {
        return staticPath + path;
    }

    /**
     * 生成JavaScript标签，可以同时生成多个
     * @param jsList 一个和多个js地址，相对于static路径
     */
    public String javascript(String... jsList) {
        StringBuilder sb = new StringBuilder();
        for (String js : jsList) {
            sb.append("<script type=\"text/javascript\" src=\"").append(staticPath).append(js);
            if (js.indexOf('?') < 0) {
                sb.append("?v=").append(staticVersion);
            } else {
                sb.append("&v=").append(staticVersion);
            }
            sb.append("\"></script>\n");
        }
        return sb.toString();
    }


    /**
     * 生成CSS标签，可以同时生成多个
     * @param cssList 一个和多个css地址，相对于static路径
     */
    public String css(String... cssList) {
        StringBuilder sb = new StringBuilder();
        for (String css : cssList) {
            sb.append("<link rel=\"stylesheet\" href=\"").append(staticPath).append(css);
            if (css.indexOf('?') < 0) {
                sb.append("?v=").append(staticVersion);
            } else {
                sb.append("&v=").append(staticVersion);
            }
            sb.append("\" type=\"text/css\">\n");
        }
        return sb.toString();
    }

    /**
     * 生成javascript变量<br/>
     * name, value, name, value, ...
     */
    public String var(String... nameAndValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("<script type=\"text/javascript\">try{");
        for (int i = 0; i < nameAndValue.length; i++) {
            if (i % 2 == 0) {
                sb.append("var ").append(nameAndValue[i]);
            } else {
                if (null != nameAndValue[i]) {
                    sb.append('=').append('\"').append(nameAndValue[i]).append('\"').append(';');
                } else {
                    sb.append('=').append("null").append(';');
                }
            }
        }
        sb.append("}catch(e){}</script>");
        return sb.toString();
    }

    public String referMenu(String menu) {
        return var("_referMenu", menu);
    }

    /** active menus **/
    public List<?> menus() throws IOException {
        MenuService menuService = AppContext.getApplicationContext().getBean(MenuService.class);
        return menuService.menus();
    }

    public Map<String, ?> menu(String key) {
        MenuService menuService = AppContext.getApplicationContext().getBean(MenuService.class);
        return menuService.menu(key);
    }

    public String env(String env) {
        return DataSource.Env.getEnvName(env);
    }

    public String getPath() {
        return staticPath;
    }
}
