package com.autodb.ops.dms;

import com.autodb.ops.dms.common.AppContext;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;

/**
 * The Bootstrap class.
 *
 * @author dongjs
 * @since 2015/10/23
 */
@SpringBootApplication
@ServletComponentScan
public class Main {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        ApplicationContext applicationContext = app.run(args);
        AppContext.setApplicationContext(applicationContext);
    }
}
