$(document).ready(function() {
	$('.menu-btn').on('click', function(e) {
		e.preventDefault();

		const $navsub = $('.il_navsub');
		const $menuHam = $(this).find('.menu-ham');
		const $overlay = $('.nav-overlay');

		$navsub.toggle();
		$menuHam.toggleClass('open');

		// 조건부 오버레이 처리
		if ($navsub.is(':visible')) {
			$overlay.fadeIn(300);
			$('#il_header').addClass('menu-open');
		} else {
			$overlay.fadeOut(300);
			$('#il_header').removeClass('menu-open');
		}
	});

	$('.btnClose, .nav-overlay').on('click', function() {
		$('.il_navsub').hide();
		$('.menu-ham').removeClass('open');
		$('.nav-overlay').fadeOut(300);
		$('#il_header').removeClass('menu-open');
	});
});

$(document).ready(function() {
	$('.big_item .text, .big_item .cate').on('click', function(e) {
		if (window.innerWidth <= 1024) {
			e.preventDefault();

			const $currentBox = $(this).closest('.navsub-box');
			const $currentItems = $currentBox.find('.small_item');
			const $arrowImg = $currentBox.find('.cate img');

			if ($currentItems.is(':visible')) {
				$currentItems.stop(true, true).slideUp(300);
				$arrowImg.attr('src', '/il_img/arrow-bottom-9.svg');
			} else {
				$('.small_item').stop(true, true).slideUp(300);
				$('.cate img').attr('src', '/il_img/arrow-bottom-9.svg');

				$currentItems.stop(true, true).slideDown(300);
				$arrowImg.attr('src', '/il_img/arrow-top-9.svg');
			}
		}
	});
});
$(document).ready(function() {
	if (window.innerWidth <= 1024) {
		$('.small_item').hide();
	}
});
$(window).on('resize', function() {
	if (window.innerWidth > 1024) {
		// 데스크탑으로 전환될 때 서브메뉴/화살표 초기화
		$('.small_item').hide(); // 서브메뉴 모두 닫기
		$('.cate img').attr('src', '/il_img/arrow-bottom-9.svg'); // 화살표 초기화
	}
});

$(document).ready(function() {
	$('.btnClose').on('click', function(e) {
		e.preventDefault();
		$('.il_navsub').hide(); // 네비게이션 닫기
		$('.menu-ham').removeClass('open'); // 햄버거 아이콘 원상복귀
		$('#il_header').removeClass('menu-open'); // 배경색도 초기화 (선택)
	});
});
$(document).ready(function() {
	$('.btnClose').on('click', function(e) {
		e.preventDefault();

		$('.il_navsub').hide(); // 네비게이션 닫기
		$('.menu-ham').removeClass('open'); // 햄버거 아이콘 원상복귀
		$('#il_header').removeClass('menu-open'); // 헤더 배경 초기화

		// 🔽 메뉴 닫을 때도 초기화
		$('.small_item').hide(); // 모든 서브메뉴 닫기
		$('.cate img').attr('src', '/il_img/arrow-bottom-9.svg'); // 화살표 초기화
	});
});

document.addEventListener('DOMContentLoaded', () => {
	const header = document.getElementById('il_header');
	const menuBtn = document.querySelector('.menu-btn');

	// 스크롤 시 배경색 변경
	window.addEventListener('scroll', () => {
		if (window.scrollY > 10) {
			header.classList.add('scrolled');
		} else {
			header.classList.remove('scrolled');
		}
	});

	// 메뉴 버튼 클릭 시 배경색 적용
	menuBtn.addEventListener('click', () => {
		header.classList.toggle('menu-open');
	});
});



let lastScrollTop = 0;
	let isHidden = false;

	$(window).on("scroll", function () {
		const currentScroll = $(this).scrollTop();
		const windowWidth = $(window).width();

		if (windowWidth < 859) {
			if (currentScroll > lastScrollTop && !isHidden) {
				// 아래로 스크롤 → 모두 fadeOut
				$('#il_header .head-logo, #il_header .left, #il_header .right').fadeOut(200);
				isHidden = true;
			} else if (currentScroll < lastScrollTop && isHidden) {
				// 위로 스크롤 → 모두 fadeIn + display: flex
				$('#il_header .head-logo, #il_header .left, #il_header .right')
					.fadeIn(200)
					.css('display', 'flex');
				isHidden = false;
			}
		} else {
			// PC에서는 항상 보이게
			$('#il_header .head-logo, #il_header .left, #il_header .right')
				.show()
				.css('display', 'flex');
			isHidden = false;
		}

		lastScrollTop = currentScroll;
	});
