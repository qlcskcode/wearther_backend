package com.appsolve.wearther_backend.home.service;

import com.appsolve.wearther_backend.Service.MemberService;
import com.appsolve.wearther_backend.Service.MemberTasteService;
import com.appsolve.wearther_backend.Service.TasteService;
import com.appsolve.wearther_backend.apiResponse.exception.CustomException;
import com.appsolve.wearther_backend.apiResponse.exception.ErrorCode;
import com.appsolve.wearther_backend.closet.dto.ClosetResponseDto;
import com.appsolve.wearther_backend.closet.service.ClosetService;
import com.appsolve.wearther_backend.home.dto.RecommendResponseDto;
import com.appsolve.wearther_backend.home.dto.WeatherResponseDto;

import com.appsolve.wearther_backend.init_data.repository.WeatherLowerWearRepository;
import com.appsolve.wearther_backend.init_data.repository.WeatherOtherWearRepository;
import com.appsolve.wearther_backend.init_data.repository.WeatherUpperWearRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TodayRecommendService {
    private final HomeWeatherService homeWeatherService;
    private final MemberService memberService;
    private final ClosetService closetService;
    private final WeatherUpperWearRepository weatherUpperWearRepository;
    private final WeatherLowerWearRepository weatherLowerWearRepository;
    private final WeatherOtherWearRepository weatherOtherWearRepository;
    private final MemberTasteService memberTasteService;
    private final TasteService tasteService;
    private  final UvService uvService;

    private WeatherResponseDto weatherResponseDto;

    public TodayRecommendService(HomeWeatherService homeWeatherService, MemberService memberService, ClosetService closetService, WeatherUpperWearRepository weatherUpperWearRepository, WeatherLowerWearRepository weatherLowerWearRepository, WeatherOtherWearRepository weatherOtherWearRepository, MemberTasteService memberTasteService, TasteService tasteService, UvService uvService) {
        this.homeWeatherService = homeWeatherService;
        this.memberService = memberService;
        this.closetService = closetService;
        this.weatherUpperWearRepository = weatherUpperWearRepository;
        this.weatherLowerWearRepository = weatherLowerWearRepository;
        this.weatherOtherWearRepository = weatherOtherWearRepository;
        this.memberTasteService = memberTasteService;
        this.tasteService = tasteService;
        this.uvService = uvService;
    }

    // 날씨 데이터 초기화
    private void initWeatherData(double latitude, double longitude) {
        if (this.weatherResponseDto == null) {
            this.weatherResponseDto = homeWeatherService.getWeatherValue(latitude, longitude);
        }
    }

    // 기본 체감온도
    private double getFeelLikeTemp(double latitude, double longitude) {
        initWeatherData(latitude, longitude);

        try {
            String temperature = weatherResponseDto.getTemperature();
            String humidity = weatherResponseDto.getHumidity();

            double temp = Double.parseDouble(temperature.replace("°C", ""));
            double hum = Double.parseDouble(humidity.replace("%", ""));

            // 습도 50%를 기준으로 10% 증가/감소함에 따라 1°C 증가/감소
            double baseTemp = ((hum - 50) / 10) + temp;

            System.out.println("현재 기온 : " + temp + " 현재 습도 : " + hum + " 기본 체감온도: " + baseTemp);
            return baseTemp;

        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new CustomException(ErrorCode.TEMP_NOT_FOUND);
    }

    // 사용자 별 체감온도
    private Long getWeatherId(Long memberId, double latitude, double longitude) {
        double feelLikeTemp = getFeelLikeTemp(latitude, longitude);

        // 체질 정보 가져오기
        int constitution = memberService.getConstitutionByMemberId(memberId);

        // 체질에 따른 체감 온도 조절
        feelLikeTemp += (constitution == 0) ? -2 : (constitution == 1) ? 2 : 0;

        System.out.println(memberId + "의 체감온도 : " + feelLikeTemp);

        if (feelLikeTemp >= 27) return 1L;
        else if (feelLikeTemp >= 23) return 2L;
        else if (feelLikeTemp >= 20) return 3L;
        else if (feelLikeTemp >= 17) return 4L;
        else if (feelLikeTemp >= 12) return 5L;
        else if (feelLikeTemp >= 9) return 6L;
        else if (feelLikeTemp >= 5) return 7L;
        else if (feelLikeTemp >= -4) return 8L;
        else return 9L;
    }

    // 추천 리스트 생성
    private RecommendResponseDto getRecommend(Long memberId, double latitude, double longitude) {
        initWeatherData(latitude, longitude);

        Long weatherId = getWeatherId(memberId, latitude, longitude);

        // 사용자 옷장 불러오기
        ClosetResponseDto closet = closetService.getOwnedClothes(memberId);
        List<Long> uppers = closet.getUppers();
        List<Long> lowers = closet.getLowers();
        List<Long> others = closet.getOthers();

        // 사용자 취향 불러오기
        List<Long> taste = memberTasteService.getMemberTasteIds(memberId);

        List<Long> tasteUpper = new ArrayList<>();
        for (Long tasteId : taste) {
            List<Long> tempTasteUpper = tasteService.getClothesByTasteId(tasteId, "upper");
            tasteUpper.addAll(tempTasteUpper);
        }

        List<Long> tasteLower = new ArrayList<>();
        for (Long tasteId : taste) {
            List<Long> tempTasteLower = tasteService.getClothesByTasteId(tasteId, "lower");
            tasteLower.addAll(tempTasteLower);
        }

        List<Long> tasteOther = new ArrayList<>();
        for (Long tasteId : taste) {
            List<Long> tempTasteOther = tasteService.getClothesByTasteId(tasteId, "other");
            tasteOther.addAll(tempTasteOther);
        }

        System.out.println(memberId + "의 정보 - 체감 온도 ID: " + weatherId + " 보유 상의: " + uppers + " 보유 하의 " + lowers + " 보유 기타: " + others + " 취향: ");

        // 상의 추천 리스트
        List<Long> upperRecommend = weatherUpperWearRepository.findByWeatherId(weatherId).stream()
                .map(weatherUpperWear -> weatherUpperWear.getUpperWear().getId())
                .filter(uppers::contains)
                .filter(tasteUpper::contains)
                .toList();

        // 하의 추천 리스트
        List<Long> lowerRecommend = weatherLowerWearRepository.findByWeatherId(weatherId).stream()
                .map(weatherLowerWear -> weatherLowerWear.getLowerWear().getId())
                .filter(lowers::contains)
                .filter(tasteLower::contains)
                .toList();

        String rain = weatherResponseDto.getRain();
        String temperature = weatherResponseDto.getTemperature();
        int temp = Integer.parseInt(temperature.replace("°C", ""));
        int uv = uvService.getUV(latitude, longitude);
        if (!rain.equals("0") && !rain.equals("강수 없음")) {
            if (rain.equals("1mm 미만")) {
                weatherId = 10L;
            } else {
                try {
                    String numericPart = rain.replaceAll("[^0-9.]", "");
                    if (!numericPart.isEmpty()) {
                        double rainAmount = Double.parseDouble(numericPart);
                        if (rainAmount >= 5) weatherId = 11L;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if (uv >= 5){
                weatherId = 12L;
                if (temp >= 25) weatherId = 13L;
            }
        }

        System.out.println("최종 weatherID : " + weatherId);

        // 기타 추천 리스트
        List<Long> otherRecommend = weatherOtherWearRepository.findByWeatherId(weatherId).stream()
                .map(weatherOtherWear -> weatherOtherWear.getOtherWear().getId())
                .filter(others::contains)
                .toList();

        System.out.println("추천 리스트 - 상의: " + upperRecommend + " 하의: " + lowerRecommend + " 기타: " + otherRecommend);
        return new RecommendResponseDto(upperRecommend, lowerRecommend, otherRecommend);
    }

    public RecommendResponseDto getTodayRecommend(Long memberId, double latitude, double longitude) {
        this.weatherResponseDto = null;
        return getRecommend(memberId, latitude, longitude);
    }
}
