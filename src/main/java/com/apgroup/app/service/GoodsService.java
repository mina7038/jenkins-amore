package com.apgroup.app.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apgroup.app.entity.Goods;
import com.apgroup.app.repository.GoodsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoodsService {
    private final GoodsRepository goodsRepository;

    public long count() {
        return goodsRepository.count(); // JPA 기본 제공 메서드
    }

    @Value("${upload.path}")
    private String uploadPath;

    private String saveFile(MultipartFile file) throws IOException {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs(); // 하위 디렉터리 포함 생성
        }
        if(file != null && !file.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(uploadPath, filename);
            file.transferTo(dest);
            return filename;
        }
        return null;
    }

    public Goods save(Goods goods, MultipartFile img1,
                      MultipartFile img2) {
        try {
            goods.setImg1(saveFile(img1));
            goods.setImg2(saveFile(img2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return goodsRepository.save(goods);
    }

    public Goods editGoods(Long no, Goods updateGoods, MultipartFile img1,
    MultipartFile img2) {
        Goods goods = getGoods(no);
        if(goods != null) {
            goods.setGname(updateGoods.getGname());
            goods.setComment(updateGoods.getComment());
            goods.setPrice1(updateGoods.getPrice1());
            goods.setPrice2(updateGoods.getPrice2());
            goods.setAmount(updateGoods.getAmount());
            goods.setBrand(updateGoods.getBrand());
            try {
                if(img1 != null && !img1.isEmpty()) {
                    goods.setImg1(saveFile(img1));
                }
                if(img2 != null && !img2.isEmpty()) {
                    goods.setImg2(saveFile(img2));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return goodsRepository.save(goods);
    }

    @Transactional
    public void incrementClickCount(Long no) {
        Goods goods = goodsRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        goods.setClickCount(goods.getClickCount() + 1);
    }

    public List<Goods> getTop4ByClickCount() {
        return goodsRepository.findTop4ByDeletedFalseOrderByClickCountDesc();
    }

    public List<Goods> getAll() {
        return goodsRepository.findAllByOrderByNoDesc();
    }

    public Goods getGoods(Long no) {
        return goodsRepository.findByNo(no).orElse(null);
    }

    public List<Goods> getVisibleGoods() {
        return goodsRepository.findByDeletedFalse();
    }

    @Transactional
    public void deleteGoods(Long goodsId) {
        Goods goods = goodsRepository.findById(goodsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        goods.setDeleted(true); // 논리 삭제
        goodsRepository.save(goods); // ✅ 저장해야 DB 반영됨
    }


    public List<Goods> getAllTop3GoodsByBrand() {
        List<String> brands = goodsRepository.findDistinctBrands();
        List<Goods> allTopGoods = new ArrayList<>();

        for (String brand : brands) {
            List<Goods> top3 = goodsRepository.findTop3ByBrandAndDeletedFalseOrderByNoDesc(brand);
            allTopGoods.addAll(top3);
        }

        return allTopGoods;
    }

    public Page<Goods> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "no"));
        return goodsRepository.findAll(pageable);
    }

    public Page<Goods> searchByName(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "no"));
        return goodsRepository.findByGnameContainingIgnoreCase(keyword, pageable);
    }

}
