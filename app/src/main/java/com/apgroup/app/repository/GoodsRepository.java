package com.apgroup.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apgroup.app.entity.Goods;

public interface GoodsRepository extends JpaRepository<Goods, Long> {
    List<Goods> findAllByOrderByNoDesc();
    Optional<Goods> findByNo(Long no);
    void deleteByNo(Long no);
    List<Goods> findByGnameContainingIgnoreCaseOrderByNoDesc(String keyword);
    List<Goods> findTop3ByOrderByNoDesc();
    List<Goods> findByBrandOrderByNoDesc(String brand);
    @Query("SELECT DISTINCT g.brand FROM Goods g")
    List<String> findDistinctBrands();

    List<Goods> findTop3ByBrandAndDeletedFalseOrderByNoDesc(String brand);

    // 삭제되지 않은 상품만 조회
    List<Goods> findByDeletedFalse();

    List<Goods> findTop4ByDeletedFalseOrderByClickCountDesc();

    // 상품 상세 조회 시에도 사용 가능
    Optional<Goods> findByNoAndDeletedFalse(Long no);

    Page<Goods> findAll(Pageable pageable);

    Page<Goods> findByGnameContainingIgnoreCase(String keyword, Pageable pageable);

}