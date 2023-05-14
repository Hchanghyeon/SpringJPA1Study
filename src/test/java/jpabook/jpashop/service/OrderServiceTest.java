package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderSerivce orderService;

    @Autowired
    OrderRepository orderRepository;


    @Test
    public void 상품주문() {
        Member member = getMember();
        Book book = getBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        Assertions.assertThat(OrderStatus.ORDER).isEqualTo(getOrder.getStatus());
        Assertions.assertThat(1).isEqualTo(getOrder.getOrderItems().size());
        Assertions.assertThat(10000 * orderCount).isEqualTo(getOrder.getTotalPrice());
        Assertions.assertThat(8).isEqualTo(book.getStockQuantity());

    }

    @Test
    public void 상품주문_재고수량초과() {
        Member member = getMember();
        Item item = getBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        org.junit.jupiter.api.Assertions.assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(),item.getId(),orderCount);
        });
    }

    @Test
    public void 주문취소() {
    Member member = getMember();
    Book item = getBook("시골 jpa", 10000, 10);

    int orderCOunt = 2;
    Long orderId  = orderService.order(member.getId(), item.getId(), orderCOunt);

    orderService.cancelOrder(orderId);

    Order getOrder = orderRepository.findOne(orderId);

    Assertions.assertThat(OrderStatus.CANCEL).isEqualTo(getOrder.getStatus());
    Assertions.assertThat(10).isEqualTo(item.getStockQuantity());
    }


    private Book getBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member getMember() {
        // given
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        Item item = new Book();
        em.persist(member);
        return member;
    }

}
