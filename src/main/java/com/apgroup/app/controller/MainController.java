package com.apgroup.app.controller;

import java.util.List;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.apgroup.app.entity.Goods;
import com.apgroup.app.service.GoodsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final GoodsService goodsService;

    @GetMapping("/")
    public String mainPage(Model model) {
        List<Goods> topGoods = goodsService.getAllTop3GoodsByBrand();
        List<Goods> popularGoods = goodsService.getTop4ByClickCount();
        model.addAttribute("goodsList", topGoods);
        model.addAttribute("popularGoods", popularGoods);
        model.addAttribute("isMainPage", true);

        return "index";
    }
    
    @GetMapping("/intro/sul")
    public String Sul() {
    	return "introduce/sul";
    }
    @GetMapping("/intro/ay")
    public String ay() {
    	return "introduce/ay";
    }
    @GetMapping("/intro/il")
    public String il() {
    	return "introduce/il";
    }
    @GetMapping("/intro/os")
    public String os() {
    	return "introduce/os";
    }


}
