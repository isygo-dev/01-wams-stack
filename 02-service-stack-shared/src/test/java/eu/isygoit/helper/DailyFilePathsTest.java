package eu.isygoit.helper;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The type Daily file paths test.
 */
class DailyFilePathsTest {
    /**
     * Generate daily file paths should generate correct paths.
     */
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
        assertTrue(paths.contains(Path.of(invalidPath).resolve(year + "_" + monthFormatted).resolve(yesterdayFormatted).toString()));
        assertTrue(paths.contains(Path.of(invalidPath).resolve(year + "_" + monthFormatted).resolve(todayFormatted).toString()));
        assertTrue(paths.contains(Path.of(validPath).resolve(year + "_" + monthFormatted).resolve(yesterdayFormatted).toString()));
        assertTrue(paths.contains(Path.of(validPath).resolve(year + "_" + monthFormatted).resolve(todayFormatted).toString()));
    }
}