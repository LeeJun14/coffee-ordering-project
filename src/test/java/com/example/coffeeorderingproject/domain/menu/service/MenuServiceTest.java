package com.example.coffeeorderingproject.domain.menu.service;

import com.example.coffeeorderingproject.domain.menu.entity.Menu;
import com.example.coffeeorderingproject.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void 전체_메뉴를_조회한다() {
        // given
        List<Menu> menus = List.of(
                Menu.create("아메리카노", 4000L),
                Menu.create("카페라떼", 5000L)
        );
        given(menuRepository.findAll()).willReturn(menus);

        // when
        List<Menu> result = menuService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("아메리카노");
        assertThat(result.get(1).getName()).isEqualTo("카페라떼");
    }

    @Test
    void 메뉴가_없으면_빈_리스트를_반환한다() {
        // given
        given(menuRepository.findAll()).willReturn(List.of());

        // when
        List<Menu> result = menuService.findAll();

        // then
        assertThat(result).isEmpty();
    }
}
