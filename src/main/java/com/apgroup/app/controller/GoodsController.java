package com.apgroup.app.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.apgroup.app.entity.Goods;
import com.apgroup.app.security.CustomUserDetails;
import com.apgroup.app.service.GoodsService;
import com.apgroup.app.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/goods")
public class GoodsController {
    private final GoodsService goodsService;
    private final ReviewService reviewService;

    @GetMapping("/list")
    public String list(
            @RequestParam(value = "brand", required = false) String brand,
            Model model) {

        // ✅ 삭제되지 않은 상품만 가져오기
        List<Goods> allGoods = goodsService.getVisibleGoods();

        // 브랜드 목록 추출
        Set<String> brandList = allGoods.stream()
                .map(Goods::getBrand)
                .collect(Collectors.toSet());

        // 선택된 브랜드 필터링
        List<Goods> filteredGoods;
        if (brand != null && !brand.isEmpty()) {
            filteredGoods = allGoods.stream()
                    .filter(g -> g.getBrand().equals(brand))
                    .collect(Collectors.toList());
        } else {
            filteredGoods = allGoods;
        }

        model.addAttribute("brandList", brandList);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("goodsList", filteredGoods);

        return "goods/list";
    }



    @GetMapping("/detail/{no}")
    public String detail(@PathVariable("no") Long no,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {

        // ✅ 상품 정보
        Goods goods = goodsService.getGoods(no);
        if (goods == null) {
            return "error/404"; // 또는 에러 페이지 또는 redirect
        }

        goodsService.incrementClickCount(no); // ✅ 클릭 수 증가 추가

        model.addAttribute("goods", goods);

        // ✅ 로그인 유저 정보
        if (userDetails != null) {
            model.addAttribute("user", userDetails.getMember());
        }

        // ✅ 리뷰 통계 정보
        double averageScore = reviewService.calculateAverageScore(no);
        int reviewCount = reviewService.countReviews(no);
        int satisfactionRate = reviewService.calculateSatisfaction(no);
        Map<Integer, Integer> scoreCountMap = reviewService.getScoreCountMap(no);
        Map<Integer, Integer> scorePercentMap = reviewService.getScorePercentMap(no);

        model.addAttribute("reviews", reviewService.getReviewsByGoodsNo(no));
        model.addAttribute("averageScore", averageScore);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("satisfactionRate", satisfactionRate);
        model.addAttribute("scoreCountMap", scoreCountMap);
        model.addAttribute("scorePercentMap", scorePercentMap);

        return "goods/detail";
    }



    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("goods", new Goods());
        return "goods/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Goods goods,
                      @RequestParam("img1file") MultipartFile img1,
                      @RequestParam("img2file") MultipartFile img2) {
        goodsService.save(goods, img1, img2);
        return "redirect:/goods/list";
    }

    @GetMapping("/edit/{no}")
    public String editForm(@PathVariable("no") Long no, Model model) {
        model.addAttribute("goods", goodsService.getGoods(no));
        return "goods/edit";
    }

    @PostMapping("/edit/{no}")
    public String edit(@PathVariable("no") Long no, @ModelAttribute Goods goods,
                       @RequestParam("img1file") MultipartFile img1,
                       @RequestParam("img2file") MultipartFile img2) {
        goodsService.save(goods, img1, img2);
        return "redirect:/goods/list";
    }

    @PostMapping("/delete/{no}")
    public String deleteGoods(@PathVariable("no") Long goodsNo) {
        goodsService.deleteGoods(goodsNo);
        return "redirect:/goods/list"; // 관리자 상품 목록으로 리디렉션
    }
}
