package com.emocional.diary.service;

import com.emocional.diary.dto.StressHistoryItem;
import com.emocional.diary.dto.WeeklyStatsResponse;
import com.emocional.diary.model.DiaryEntry;
import com.emocional.diary.repository.DiaryEntryRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final DiaryEntryRepository diaryEntryRepository;

    @Override
    public WeeklyStatsResponse getWeeklyStats(Long userId) {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Instant now = Instant.now();
        Instant fourteenDaysAgo = now.minus(14, ChronoUnit.DAYS);

        List<DiaryEntry> recentEntries = diaryEntryRepository.findByUserIdAndCreatedAtBetween(userId, fourteenDaysAgo, now);

        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);

        // Partition entries into current and previous week
        Map<Boolean, List<DiaryEntry>> partitionedEntries = recentEntries.stream()
                .collect(Collectors.partitioningBy(entry -> !entry.getCreatedAt().isBefore(sevenDaysAgo)));

        List<DiaryEntry> currentWeekEntries = partitionedEntries.get(true);
        List<DiaryEntry> previousWeekEntries = partitionedEntries.get(false);

        // --- Calculations for the current week ---
        double averageStress = currentWeekEntries.stream()
                .filter(entry -> entry.getUserStressLevel() != null)
                .mapToInt(DiaryEntry::getUserStressLevel)
                .average()
                .orElse(0.0);


// ... imports

// ... inside StatsServiceImpl

        double averageSleep = currentWeekEntries.stream()
                .filter(entry -> entry.getUserSleepHours() != null)
                .mapToInt(DiaryEntry::getUserSleepHours)
                .average()
                .orElse(0.0);

        // Find main worry from all-time entries
        List<String> frequentWorries = diaryEntryRepository.findMostFrequentMainWorry(userId, PageRequest.of(0, 1));
        String mainWorry = frequentWorries.isEmpty() ? "Ninguna preocupación dominante" : frequentWorries.get(0);

        // --- Calculation for the previous week ---
        double previousWeekStress = previousWeekEntries.stream()
                .filter(entry -> entry.getUserStressLevel() != null)
                .mapToInt(DiaryEntry::getUserStressLevel)
                .average()
                .orElse(0.0);

        // --- Build Stress History for the last 7 days ---
        Map<LocalDate, Double> dailyStressAverages = currentWeekEntries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getCreatedAt().atZone(defaultZoneId).toLocalDate(),
                        Collectors.averagingInt(DiaryEntry::getUserStressLevel)
                ));

        List<StressHistoryItem> stressHistory = Stream.iterate(LocalDate.now().minusDays(6), date -> date.plusDays(1))
                .limit(7)
                .map(date -> StressHistoryItem.builder()
                        .date(date)
                        .value(dailyStressAverages.getOrDefault(date, 0.0))
                        .build())
                .collect(Collectors.toList());

        return WeeklyStatsResponse.builder()
                .averageStress(averageStress)
                .previousWeekStress(previousWeekStress)
                .averageSleep(averageSleep)
                .mainWorry(mainWorry)
                .stressHistory(stressHistory)
                .build();
    }
}