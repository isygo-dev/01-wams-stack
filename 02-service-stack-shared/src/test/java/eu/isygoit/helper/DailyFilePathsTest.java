package eu.isygoit.helper;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyFilePathsTest {
    @Test
    void generateDailyFilePaths_ShouldGenerateCorrectPaths() {
        // Setup
        String validPath = "valid";
        String invalidPath = "invalid";

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int yesterday = today - 1;

        String monthFormatted = new DecimalFormat("#00").format(month);
        String todayFormatted = new DecimalFormat("#00").format(today);
        String yesterdayFormatted = new DecimalFormat("#00").format(yesterday);

        // Test
        List<String> paths = FileHelper.generateDailyFilePaths(validPath, invalidPath);

        // Verify
        assertEquals(4, paths.size());
        assertTrue(paths.contains(invalidPath + File.separator + year + "_" + monthFormatted + File.separator + yesterdayFormatted));
        assertTrue(paths.contains(invalidPath + File.separator + year + "_" + monthFormatted + File.separator + todayFormatted));
        assertTrue(paths.contains(validPath + File.separator + year + "_" + monthFormatted + File.separator + yesterdayFormatted));
        assertTrue(paths.contains(validPath + File.separator + year + "_" + monthFormatted + File.separator + todayFormatted));
    }
}