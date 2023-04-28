package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.domain.bi.EncryptionService;
import com.autodb.ops.dms.entity.sys.Broadcast;
import com.autodb.ops.dms.service.sys.BroadcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * root
 *
 * @author dongjs
 * @since 15/10/23
 */
@Controller
@Validated
public class RootController extends SuperController {
    @Value("${data.file.path}")
    protected String filePath;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private BroadcastService broadcastService;

    @RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/old_browser", method = RequestMethod.GET)
    public String oldBrowser() {
        return "old_browser";
    }

    @RequestMapping(value = "/downloads", method = RequestMethod.GET)
    public void downloads(@RequestParam("file") String filename, HttpServletResponse response) throws IOException {
        File file = new File(filePath + filename);
        if (file.exists()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                // MIME type of the file
                response.setContentType("application/octet-stream");
                // Response header
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

                ServletOutputStream os = response.getOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                response.flushBuffer();
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "file not find");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "file not find");
        }
    }

    @RequestMapping(value = "/decrypt", method = RequestMethod.GET)
    @ResponseBody
    public String decrypt(@RequestParam("sec") String sec) {
        return encryptionService.decrypt(sec, this.getUser().getUsername());
    }

    @RequestMapping(value = "/broadcasts", method = RequestMethod.GET)
    @ResponseBody
    public List<Broadcast> broadcasts() {
        return broadcastService.broadcasts();
    }
}
