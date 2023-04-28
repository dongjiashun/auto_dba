package com.autodb.ops.dms.common.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Csv Utils
 *
 * @author dongjs
 * @since 16/1/24
 */
public final class CsvUtils {
    private static CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator('\n');

    public static void write2File(String filename, List<String> header, List<Map<String, Object>> data, String comment)
            throws IOException {
        CSVPrinter csvPrinter = null;
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            File file = new File(filename);
            fileOutputStream = new FileOutputStream(file, true);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
            csvPrinter = new CSVPrinter(outputStreamWriter, csvFormat);

            // comment
            if (comment != null && comment.length() > 0) {
                csvPrinter.printRecord(Collections.singletonList(comment));
            }
            // header
            csvPrinter.printRecord(header);
            // content
            for (Map<String, Object> record : data) {
                csvPrinter.printRecord(record.values());
            }
        } finally {
            try {
                if (csvPrinter != null) {
                    csvPrinter.flush();
                    csvPrinter.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private CsvUtils() {
    }
}
