package com.appsolve.wearther_backend.sevice;

import ch.qos.logback.core.joran.sanity.Pair;
import com.appsolve.wearther_backend.entity.Taste;
import com.appsolve.wearther_backend.entity.TasteUpperWear;
import com.appsolve.wearther_backend.entity.UpperWear;
import com.appsolve.wearther_backend.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TasteService {

    @Autowired private TasteRepository tasteRepository;

    @Autowired private TasteUpperWearRepository tasteUpperWearRepository;

    @Autowired private UpperWearRepository upperWearRepository;

    @Autowired private LowerWearRepository lowerWearRepository;

    @Autowired private OtherWearRepository otherWearRepository;

    @PostConstruct
    public void init() {
        setTaste();
        setUpperWear();
    }

    private void setTaste() {
        List<String> tasteNames = Arrays.asList("페미닌", "모던", "드뮤어", "올드머니", "힙", "캐주얼", "고프코어", "Y2K");
        List<Taste> tasteList = new ArrayList<>();

        for (String name : tasteNames) {
            Taste taste = Taste.builder()
                    .name(name)
                    .build();
            tasteList.add(taste);
        }
        tasteRepository.saveAll(tasteList);
    }

    private void setUpperWear() {
        insertUpperInnerWearData();
        matchTasteForUpperInnerWear();
        insertUpperOuterWearData();
        matchTasteForUpperOuterWear();
    }


    public void insertUpperInnerWearData() {
        List<String> upperInnerWearNames = Arrays.asList("민소매", "반소매", "맨투맨/후드티", "셔츠/블라우스", "니트",
                "오프숄더", "히트택", "기모제품");

        for (String name : upperInnerWearNames) {
            UpperWear upperWear = UpperWear.builder()
                    .name(name)
                    .type(0)
                    .build();
            upperWearRepository.save(upperWear);
        }
    }

    public void insertUpperOuterWearData() {
        List<String> upperOuterWearNames = Arrays.asList("가디건", "사파리자켓", "트위드자켓", "레더자켓", "트렌치코트",
                "숏코트", "무스탕", "경량 패딩", "롱패딩", "숏패딩", "점퍼", "후드집업", "바람막이");

        for (String name : upperOuterWearNames) {
            UpperWear upperWear = UpperWear.builder()
                    .name(name)
                    .type(1)
                    .build();
            upperWearRepository.save(upperWear);
        }
    }

    private void matchTasteForUpperInnerWear() {
        Map<Long, List<Long>> tasteToUpperInnerWearMap = new HashMap<>();
        tasteToUpperInnerWearMap.put(1L, Arrays.asList(1L, 4L, 5L, 6L, 8L));
        tasteToUpperInnerWearMap.put(2L, Arrays.asList(1L, 4L, 5L, 7L, 8L));
        tasteToUpperInnerWearMap.put(3L, Arrays.asList(5L, 6L, 8L));
        tasteToUpperInnerWearMap.put(4L, Arrays.asList(1L, 2L, 4L, 5L, 8L));
        tasteToUpperInnerWearMap.put(5L, Arrays.asList(2L, 3L, 8L));
        tasteToUpperInnerWearMap.put(6L, Arrays.asList(2L, 3L, 4L, 7L, 8L));
        tasteToUpperInnerWearMap.put(7L, Arrays.asList(1L, 2L, 3L, 7L, 8L));
        tasteToUpperInnerWearMap.put(8L, Arrays.asList(3L, 6L, 8L));

        for (Map.Entry<Long, List<Long>> entry : tasteToUpperInnerWearMap.entrySet()) {
            Long tasteId = entry.getKey();
            List<Long> upperWearIds = entry.getValue();

            Taste taste = tasteRepository.findById(tasteId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Taste ID: " + tasteId));

            for (Long upperWearId : upperWearIds) {
                UpperWear upperWear = upperWearRepository.findById(upperWearId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid UpperWear ID: " + upperWearId));

                TasteUpperWear tasteUpperWear = TasteUpperWear.builder()
                        .taste(taste)
                        .upperWear(upperWear)
                        .build();
                tasteUpperWearRepository.save(tasteUpperWear);
            }
        }
    }

    private void matchTasteForUpperOuterWear() {
        Map<Long, List<Long>> tasteToUpperOuterWearMap = new HashMap<>();
        tasteToUpperOuterWearMap.put(1L, Arrays.asList(9L, 11L, 14L, 15L));
        tasteToUpperOuterWearMap.put(2L, Arrays.asList(10L, 13L, 14L));
        tasteToUpperOuterWearMap.put(3L, Arrays.asList(9L, 11L, 13L, 17L));
        tasteToUpperOuterWearMap.put(4L, Arrays.asList(9L, 10L, 11L, 13L, 14L, 15L));
        tasteToUpperOuterWearMap.put(5L, Arrays.asList(10L, 12L, 18L, 19L,  20L, 21L));
        tasteToUpperOuterWearMap.put(6L, Arrays.asList(12L, 16L, 17L, 18L, 19L,  20L, 21L));
        tasteToUpperOuterWearMap.put(7L, Arrays.asList(10L, 16L, 17L, 19L,  20L, 21L));
        tasteToUpperOuterWearMap.put(8L, List.of(12L));

        for (Map.Entry<Long, List<Long>> entry : tasteToUpperOuterWearMap.entrySet()) {
            Long tasteId = entry.getKey();
            List<Long> upperWearIds = entry.getValue();

            Taste taste = tasteRepository.findById(tasteId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Taste ID: " + tasteId));

            for (Long upperWearId : upperWearIds) {
                UpperWear upperWear = upperWearRepository.findById(upperWearId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid UpperWear ID: " + upperWearId));

                TasteUpperWear tasteUpperWear = TasteUpperWear.builder()
                        .taste(taste)
                        .upperWear(upperWear)
                        .build();
                tasteUpperWearRepository.save(tasteUpperWear);
            }
        }
    }
}
