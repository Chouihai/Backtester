package Backtester.caches;

import Backtester.objects.Bar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BarCacheTest {

    private BarCache barCache;
    private List<Bar> testBars;

    @BeforeEach
    void setUp() {
        barCache = new BarCache();
        
        // Create test bars with dates from 2023-01-01 to 2023-01-10
        testBars = Arrays.asList(
            new Bar(0, LocalDate.of(2023, 1, 1), 100.0, 105.0, 98.0, 102.0, 1000),
            new Bar(1, LocalDate.of(2023, 1, 2), 102.0, 108.0, 101.0, 106.0, 1100),
            new Bar(2, LocalDate.of(2023, 1, 3), 106.0, 110.0, 104.0, 109.0, 1200),
            new Bar(3, LocalDate.of(2023, 1, 4), 109.0, 112.0, 107.0, 111.0, 1300),
            new Bar(4, LocalDate.of(2023, 1, 5), 111.0, 115.0, 110.0, 114.0, 1400),
            new Bar(5, LocalDate.of(2023, 1, 6), 114.0, 118.0, 113.0, 117.0, 1500),
            new Bar(6, LocalDate.of(2023, 1, 7), 117.0, 120.0, 116.0, 119.0, 1600),
            new Bar(7, LocalDate.of(2023, 1, 8), 119.0, 122.0, 118.0, 121.0, 1700),
            new Bar(8, LocalDate.of(2023, 1, 9), 121.0, 125.0, 120.0, 124.0, 1800),
            new Bar(9, LocalDate.of(2023, 1, 10), 124.0, 128.0, 123.0, 127.0, 1900)
        );
    }

    @Test
    void testFindIndexByDate_ExactMatches() {
        barCache.loadCache(testBars);
        
        // Test exact matches for each date
        assertEquals(0, barCache.findIndexByDate(LocalDate.of(2023, 1, 1)));
        assertEquals(1, barCache.findIndexByDate(LocalDate.of(2023, 1, 2)));
        assertEquals(2, barCache.findIndexByDate(LocalDate.of(2023, 1, 3)));
        assertEquals(3, barCache.findIndexByDate(LocalDate.of(2023, 1, 4)));
        assertEquals(4, barCache.findIndexByDate(LocalDate.of(2023, 1, 5)));
        assertEquals(5, barCache.findIndexByDate(LocalDate.of(2023, 1, 6)));
        assertEquals(6, barCache.findIndexByDate(LocalDate.of(2023, 1, 7)));
        assertEquals(7, barCache.findIndexByDate(LocalDate.of(2023, 1, 8)));
        assertEquals(8, barCache.findIndexByDate(LocalDate.of(2023, 1, 9)));
        assertEquals(9, barCache.findIndexByDate(LocalDate.of(2023, 1, 10)));
    }

    @Test
    void testFindIndexByDate_DateBeforeFirstBar() {
        barCache.loadCache(testBars);
        
        // Date before the first bar should return -1
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2022, 12, 31)));
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2022, 1, 1)));
    }

    @Test
    void testFindIndexByDate_DateAfterLastBar() {
        barCache.loadCache(testBars);
        
        // Date after the last bar should return -1
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2023, 1, 11)));
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2023, 2, 1)));
    }

    @Test
    void testFindIndexByDate_WeekendDates() {
        barCache.loadCache(testBars);
        
        // Test weekend dates (2023-01-07 and 2023-01-08 are Saturday and Sunday)
        // These should return -1 if not present
        assertEquals(6, barCache.findIndexByDate(LocalDate.of(2023, 1, 7))); // Saturday (present)
        assertEquals(7, barCache.findIndexByDate(LocalDate.of(2023, 1, 8))); // Sunday (present)
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2023, 1, 15))); // Not present
    }

    @Test
    void testFindIndexByDate_EmptyCache() {
        // Test with empty cache
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2023, 1, 1)));
    }

    @Test
    void testFindIndexByDate_SingleBar() {
        List<Bar> singleBar = Arrays.asList(
            new Bar(0, LocalDate.of(2023, 1, 1), 100.0, 105.0, 98.0, 102.0, 1000)
        );
        barCache.loadCache(singleBar);
        
        assertEquals(0, barCache.findIndexByDate(LocalDate.of(2023, 1, 1)));
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2022, 12, 31)));
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2023, 1, 2)));
    }

    @Test
    void testFindIndexByDate_TwoBars() {
        List<Bar> twoBars = Arrays.asList(
            new Bar(0, LocalDate.of(2023, 1, 1), 100.0, 105.0, 98.0, 102.0, 1000),
            new Bar(1, LocalDate.of(2023, 1, 2), 102.0, 108.0, 101.0, 106.0, 1100)
        );
        barCache.loadCache(twoBars);
        
        assertEquals(0, barCache.findIndexByDate(LocalDate.of(2023, 1, 1)));
        assertEquals(1, barCache.findIndexByDate(LocalDate.of(2023, 1, 2)));
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2022, 12, 31)));
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2023, 1, 3)));
    }

    @Test
    void testFindIndexByDate_LargeDataset() {
        // Create a larger dataset to test binary search efficiency
        List<Bar> largeBars = new java.util.ArrayList<>();
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        
        for (int i = 0; i < 1000; i++) {
            LocalDate date = startDate.plusDays(i);
            largeBars.add(new Bar(i, date, 100.0 + i, 105.0 + i, 98.0 + i, 102.0 + i, 1000 + i));
        }
        
        barCache.loadCache(largeBars);
        
        // Test various dates in the large dataset
        assertEquals(0, barCache.findIndexByDate(LocalDate.of(2020, 1, 1)));
        assertEquals(366, barCache.findIndexByDate(LocalDate.of(2021, 1, 1))); // 2020 was a leap year
        assertEquals(731, barCache.findIndexByDate(LocalDate.of(2022, 1, 1)));
        assertEquals(999, barCache.findIndexByDate(startDate.plusDays(999))); // Last date
        
        // Test dates not in the dataset
        assertEquals(-1, barCache.findIndexByDate(LocalDate.of(2019, 12, 31)));
        assertEquals(-1, barCache.findIndexByDate(startDate.plusDays(1000)));
    }

    @Test
    void testGetBarByDate() {
        barCache.loadCache(testBars);
        
        // Test getting bars by date
        Bar bar1 = barCache.getBarByDate(LocalDate.of(2023, 1, 1));
        assertNotNull(bar1);
        assertEquals(0, bar1.index);
        assertEquals(LocalDate.of(2023, 1, 1), bar1.date);
        
        Bar bar5 = barCache.getBarByDate(LocalDate.of(2023, 1, 5));
        assertNotNull(bar5);
        assertEquals(4, bar5.index);
        assertEquals(LocalDate.of(2023, 1, 5), bar5.date);
        
        // Test getting non-existent dates
        assertNull(barCache.getBarByDate(LocalDate.of(2022, 12, 31))); // Before range
        assertNull(barCache.getBarByDate(LocalDate.of(2023, 1, 11))); // After range
    }

    @Test
    void testGetStartDateAndEndDate() {
        barCache.loadCache(testBars);
        
        assertEquals(LocalDate.of(2023, 1, 1), barCache.getStartDate());
        assertEquals(LocalDate.of(2023, 1, 10), barCache.getEndDate());
    }

    @Test
    void testGetLastNDays() {
        barCache.loadCache(testBars);
        
        // Test getting last 3 days from index 4 (2023-01-05)
        List<Bar> last3Days = barCache.getLastNDays(3, 4);
        assertEquals(3, last3Days.size());
        assertEquals(LocalDate.of(2023, 1, 3), last3Days.get(0).date);
        assertEquals(LocalDate.of(2023, 1, 4), last3Days.get(1).date);
        assertEquals(LocalDate.of(2023, 1, 5), last3Days.get(2).date);
        
        // Test getting last 1 day
        List<Bar> last1Day = barCache.getLastNDays(1, 4);
        assertEquals(1, last1Day.size());
        assertEquals(LocalDate.of(2023, 1, 5), last1Day.get(0).date);
    }

    @Test
    void testGetLastNDays_ThrowsException() {
        barCache.loadCache(testBars);
        
        // Should throw exception when trying to look back more days than available
        assertThrows(RuntimeException.class, () -> {
            barCache.getLastNDays(6, 4); // Only 5 days available at index 4
        });
        
        assertThrows(RuntimeException.class, () -> {
            barCache.getLastNDays(10, 0); // Only 1 day available at index 0
        });
    }



    // ========== Tests for findIndexAfterDate ==========

    @Test
    void testFindIndexAfterDate_ExactMatches() {
        barCache.loadCache(testBars);

        // Test exact matches - should return the same index
        assertEquals(0, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 1)));
        assertEquals(1, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 2)));
        assertEquals(2, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 3)));
        assertEquals(3, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 4)));
        assertEquals(4, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 5)));
        assertEquals(5, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 6)));
        assertEquals(6, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 7)));
        assertEquals(7, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 8)));
        assertEquals(8, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 9)));
        assertEquals(9, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 10)));
    }

    @Test
    void testFindIndexAfterDate_DateBeforeFirstBar() {
        barCache.loadCache(testBars);

        // Date before the first bar should return 0 (start from the beginning)
        assertEquals(0, barCache.findIndexAfterDate(LocalDate.of(2022, 12, 31)));
        assertEquals(0, barCache.findIndexAfterDate(LocalDate.of(2022, 1, 1)));
    }

    @Test
    void testFindIndexAfterDate_DateAfterLastBar() {
        barCache.loadCache(testBars);

        // Date after the last bar should return -1 (no valid start point)
        assertEquals(-1, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 11)));
        assertEquals(-1, barCache.findIndexAfterDate(LocalDate.of(2023, 2, 1)));
    }

    @Test
    void testFindIndexAfterDate_WeekendDates() {
        barCache.loadCache(testBars);

        // Test weekend dates that don't exist in the data
        // 2023-01-01 is Sunday, 2023-01-02 is Monday
        // If we ask for 2023-01-01 (Sunday), it should return index 0 (the first available date)
        assertEquals(0, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 1)));

        // If we ask for a date between existing dates, it should return the next available date
        assertEquals(1, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 1).plusDays(1))); // 2023-01-02
        assertEquals(2, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 2).plusDays(1))); // 2023-01-03
    }

    @Test
    void testFindIndexAfterDate_EmptyCache() {
        // Test with empty cache
        assertEquals(-1, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 1)));
    }

    @Test
    void testFindIndexAfterDate_SingleBar() {
        List<Bar> singleBar = Arrays.asList(
                new Bar(0, LocalDate.of(2023, 1, 1), 100.0, 105.0, 98.0, 102.0, 1000)
        );
        barCache.loadCache(singleBar);

        assertEquals(0, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 1)));
        assertEquals(0, barCache.findIndexAfterDate(LocalDate.of(2022, 12, 31))); // Before, should return 0
        assertEquals(-1, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 2))); // After, should return -1
    }

    // ========== Tests for findIndexBeforeDate ==========

    @Test
    void testFindIndexBeforeDate_ExactMatches() {
        barCache.loadCache(testBars);

        // Test exact matches - should return the same index
        assertEquals(0, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 1)));
        assertEquals(1, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 2)));
        assertEquals(2, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 3)));
        assertEquals(3, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 4)));
        assertEquals(4, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 5)));
        assertEquals(5, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 6)));
        assertEquals(6, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 7)));
        assertEquals(7, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 8)));
        assertEquals(8, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 9)));
        assertEquals(9, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 10)));
    }

    @Test
    void testFindIndexBeforeDate_DateBeforeFirstBar() {
        barCache.loadCache(testBars);

        // Date before the first bar should return -1 (no valid end point)
        assertEquals(-1, barCache.findIndexBeforeDate(LocalDate.of(2022, 12, 31)));
        assertEquals(-1, barCache.findIndexBeforeDate(LocalDate.of(2022, 1, 1)));
    }

    @Test
    void testFindIndexBeforeDate_DateAfterLastBar() {
        barCache.loadCache(testBars);

        // Date after the last bar should return the last index
        assertEquals(9, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 11)));
        assertEquals(9, barCache.findIndexBeforeDate(LocalDate.of(2023, 2, 1)));
    }

    @Test
    void testFindIndexBeforeDate_WeekendDates() {
        barCache.loadCache(testBars);

        // Test weekend dates that don't exist in the data
        // If we ask for a date between existing dates, it should return the previous available date
        assertEquals(1, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 1).plusDays(1))); // 2023-01-02 -> should return index 1 (exact match)
        assertEquals(2, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 2).plusDays(1))); // 2023-01-03 -> should return index 2 (exact match)
        assertEquals(9, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 10).plusDays(1))); // 2023-01-11 -> should return index 9 (last available)
    }

    @Test
    void testFindIndexBeforeDate_EmptyCache() {
        // Test with empty cache
        assertEquals(-1, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 1)));
    }

    @Test
    void testFindIndexBeforeDate_SingleBar() {
        List<Bar> singleBar = Arrays.asList(
                new Bar(0, LocalDate.of(2023, 1, 1), 100.0, 105.0, 98.0, 102.0, 1000)
        );
        barCache.loadCache(singleBar);

        assertEquals(0, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 1)));
        assertEquals(-1, barCache.findIndexBeforeDate(LocalDate.of(2022, 12, 31))); // Before, should return -1
        assertEquals(0, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 2))); // After, should return 0
    }

    @Test
    void testFindIndexBeforeDate_LargeDataset() {
        // Create a larger dataset to test binary search efficiency
        List<Bar> largeBars = new java.util.ArrayList<>();
        LocalDate startDate = LocalDate.of(2020, 1, 1);

        for (int i = 0; i < 1000; i++) {
            LocalDate date = startDate.plusDays(i);
            largeBars.add(new Bar(i, date, 100.0 + i, 105.0 + i, 98.0 + i, 102.0 + i, 1000 + i));
        }

        barCache.loadCache(largeBars);

        // Test various dates in the large dataset
        assertEquals(0, barCache.findIndexBeforeDate(LocalDate.of(2020, 1, 1)));
        assertEquals(366, barCache.findIndexBeforeDate(LocalDate.of(2021, 1, 1))); // 2020 was a leap year
        assertEquals(731, barCache.findIndexBeforeDate(LocalDate.of(2022, 1, 1)));
        assertEquals(999, barCache.findIndexBeforeDate(startDate.plusDays(999))); // Last date

        // Test dates not in the dataset
        assertEquals(-1, barCache.findIndexBeforeDate(LocalDate.of(2019, 12, 31)));
        assertEquals(999, barCache.findIndexBeforeDate(startDate.plusDays(1000)));
    }

    @Test
    void testFindIndexAfterAndBefore_WeekendScenario() {
        // Create bars with gaps (weekends/holidays)
        List<Bar> barsWithGaps = Arrays.asList(
                new Bar(0, LocalDate.of(2023, 1, 2), 100.0, 105.0, 98.0, 102.0, 1000), // Monday
                new Bar(1, LocalDate.of(2023, 1, 3), 102.0, 108.0, 101.0, 106.0, 1100), // Tuesday
                new Bar(2, LocalDate.of(2023, 1, 4), 106.0, 110.0, 104.0, 109.0, 1200), // Wednesday
                new Bar(3, LocalDate.of(2023, 1, 5), 109.0, 112.0, 107.0, 111.0, 1300), // Thursday
                new Bar(4, LocalDate.of(2023, 1, 6), 111.0, 115.0, 110.0, 114.0, 1400), // Friday
                new Bar(5, LocalDate.of(2023, 1, 9), 114.0, 118.0, 113.0, 117.0, 1500), // Monday (next week)
                new Bar(6, LocalDate.of(2023, 1, 10), 117.0, 120.0, 116.0, 119.0, 1600)  // Tuesday
        );
        barCache.loadCache(barsWithGaps);

        // Test start date on weekend (should find next available date)
        assertEquals(0, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 1))); // Sunday -> Monday
        assertEquals(5, barCache.findIndexAfterDate(LocalDate.of(2023, 1, 7))); // Saturday -> Monday

        // Test end date on weekend (should find previous available date)
        assertEquals(4, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 7))); // Saturday -> Friday
        assertEquals(4, barCache.findIndexBeforeDate(LocalDate.of(2023, 1, 8))); // Sunday -> Friday
    }

} 