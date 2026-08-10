package com.minhhao.novelscout.config;

import com.minhhao.novelscout.catalog.Author;
import com.minhhao.novelscout.catalog.AuthorRepository;
import com.minhhao.novelscout.catalog.Category;
import com.minhhao.novelscout.catalog.CategoryRepository;
import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import com.minhhao.novelscout.catalog.NovelStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoCatalogSeeder implements ApplicationRunner {
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final NovelRepository novelRepository;

    public DemoCatalogSeeder(AuthorRepository authorRepository, CategoryRepository categoryRepository,
                             NovelRepository novelRepository) {
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.novelRepository = novelRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (novelRepository.count() > 0) return;

        Map<String, Category> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getSlug, Function.identity()));
        Author lamPhong = authorRepository.save(new Author("Lâm Phong", "lam-phong",
                "Tác giả chuyên viết những hành trình kỳ ảo phương Đông."));
        Author anNhien = authorRepository.save(new Author("An Nhiên", "an-nhien",
                "Những câu chuyện đô thị nhẹ nhàng và giàu cảm xúc."));
        Author minhDu = authorRepository.save(new Author("Minh Dư", "minh-du",
                "Tác giả trinh thám với nhịp kể chậm và nhiều lớp bí mật."));
        Author haVu = authorRepository.save(new Author("Hạ Vũ", "ha-vu",
                "Viết về tương lai, ký ức và những thế giới chưa từng tồn tại."));

        novelRepository.save(Novel.published("Mộc Kiếm Thiên Hà", "moc-kiem-thien-ha", lamPhong,
                "Một thiếu niên giữ thanh mộc kiếm vô danh bước vào tiên lộ, lần theo bí mật đã ngủ yên giữa chín tầng trời.",
                NovelStatus.ONGOING, 18640, new BigDecimal("4.82"), 326,
                set(categories, "tien-hiep", "kiem-hiep")));
        novelRepository.save(Novel.published("Người Gác Đèn Cuối Phố", "nguoi-gac-den-cuoi-pho", anNhien,
                "Mỗi đêm, ngọn đèn cuối con phố cũ lại soi ra một câu chuyện mà thành phố đã cố quên.",
                NovelStatus.COMPLETED, 14280, new BigDecimal("4.76"), 219,
                set(categories, "do-thi", "ngon-tinh")));
        novelRepository.save(Novel.published("Hồ Sơ Mưa Đen", "ho-so-mua-den", minhDu,
                "Một vụ án không có nạn nhân kéo nữ điều tra viên trở lại thị trấn nơi mưa chưa từng ngừng rơi.",
                NovelStatus.ONGOING, 21110, new BigDecimal("4.91"), 405,
                set(categories, "trinh-tham", "do-thi")));
        novelRepository.save(Novel.published("Trạm Sao Thứ Mười Ba", "tram-sao-thu-muoi-ba", haVu,
                "Đoàn tàu mang ký ức cuối cùng của nhân loại nhận được tín hiệu từ một nhà ga không có trên bản đồ.",
                NovelStatus.ONGOING, 17350, new BigDecimal("4.69"), 188,
                set(categories, "khoa-hoc-vien-tuong", "huyen-huyen")));
        novelRepository.save(Novel.published("Tàng Thư Trong Gió", "tang-thu-trong-gio", lamPhong,
                "Một thư sinh nghe được tiếng nói trong những trang sách trắng và vô tình mở cửa vào giang hồ đã mất.",
                NovelStatus.HIATUS, 9640, new BigDecimal("4.55"), 97,
                set(categories, "kiem-hiep", "huyen-huyen")));
        novelRepository.save(Novel.published("Ngày Thành Phố Quên Tên", "ngay-thanh-pho-quen-ten", anNhien,
                "Hai người xa lạ cùng đi tìm tên mình trong một thành phố nơi ký ức biến mất sau bình minh.",
                NovelStatus.COMPLETED, 12870, new BigDecimal("4.73"), 164,
                set(categories, "do-thi", "ngon-tinh")));
        novelRepository.save(Novel.published("Mật Mã Phòng 404", "mat-ma-phong-404", minhDu,
                "Căn phòng không tồn tại trong khách sạn lại xuất hiện trong mọi bức ảnh của một vụ mất tích kéo dài mười năm.",
                NovelStatus.COMPLETED, 15790, new BigDecimal("4.67"), 241,
                set(categories, "trinh-tham")));
        novelRepository.save(Novel.published("Kẻ Du Hành Qua Giấc Mơ", "ke-du-hanh-qua-giac-mo", haVu,
                "Khi giấc mơ trở thành nơi lưu trữ dữ liệu, một kỹ sư phát hiện ký ức của mình thuộc về người khác.",
                NovelStatus.ONGOING, 19220, new BigDecimal("4.85"), 302,
                set(categories, "khoa-hoc-vien-tuong", "huyen-huyen")));
    }

    private Set<Category> set(Map<String, Category> categories, String... slugs) {
        return java.util.Arrays.stream(slugs)
                .map(slug -> {
                    Category category = categories.get(slug);
                    if (category == null) throw new IllegalStateException("Thiếu thể loại: " + slug);
                    return category;
                })
                .collect(Collectors.toSet());
    }
}
